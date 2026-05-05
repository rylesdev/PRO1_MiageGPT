package com.miage.miagegpt.model;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {

    private static final DatabaseSettings DB_SETTINGS = loadDatabaseSettings();
    private static final String DB_URL = DB_SETTINGS.url;
    private static final String DB_USER = DB_SETTINGS.user;
    private static final String DB_PASSWORD = DB_SETTINGS.password;
    private static final boolean USE_POSTGRES = true;

    private static DatabaseSettings loadDatabaseSettings() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger database.properties", e);
        }

        String envUrl = System.getenv("NEON_DATABASE_URL");
        if (envUrl == null || envUrl.isEmpty()) envUrl = System.getenv("DATABASE_URL");
        String envUser = System.getenv("DATABASE_USER");
        String envPassword = System.getenv("DATABASE_PASSWORD");

        String url = firstNonEmpty(envUrl, properties.getProperty("database.url"));
        String user = firstNonEmpty(envUser, properties.getProperty("database.user"));
        String password = firstNonEmpty(envPassword, properties.getProperty("database.password"));

        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("Base NEON non configurée. Renseigne database.properties ou les variables d'environnement.");
        }

        if (url.startsWith("postgres://")) {
            String withoutProto = url.substring("postgres://".length());
            String[] parts = withoutProto.split("@", 2);
            String creds = parts[0];
            String hostpart = parts[1];
            String[] credParts = creds.split(":", 2);
            if (user == null || user.isEmpty()) user = credParts[0];
            if (password == null || password.isEmpty()) password = credParts.length > 1 ? credParts[1] : "";
            String[] hostParts = hostpart.split("/", 2);
            String hostPort = hostParts[0];
            String dbName = hostParts.length > 1 ? hostParts[1] : "";
            url = "jdbc:postgresql://" + hostPort + "/" + dbName + "?sslmode=require&channel_binding=require";
        }

        if (!url.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("DATABASE_URL doit être une URL PostgreSQL.");
        }

        System.out.println("[DB] Utilisation de la base Postgres NEON: " + url);
        return new DatabaseSettings(url, user != null ? user : "", password != null ? password : "");
    }

    private static String firstNonEmpty(String first, String second) {
        if (first != null && !first.isEmpty()) return first;
        if (second != null && !second.isEmpty()) return second;
        return null;
    }

    private static final class DatabaseSettings {
        private final String url;
        private final String user;
        private final String password;

        private DatabaseSettings(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }
    }

    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver Postgres introuvable : " + e.getMessage());
        }
        if (DB_USER != null && !DB_USER.isEmpty()) {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return DriverManager.getConnection(DB_URL);
    }

    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            System.out.println("[DB] Connexion NEON initialisée.");

            System.out.println("[DB] Base de données initialisée avec succès !");

        } catch (SQLException e) {
            System.err.println("[DB] Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getAssociationInfo() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM association_info LIMIT 1")) {

            if (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Association : ").append(rs.getString("nom")).append("\n");
                sb.append("Description : ").append(rs.getString("description")).append("\n");
                sb.append("Adresse : ").append(rs.getString("adresse")).append("\n");
                try {
                    String universite = rs.getString("universite");
                    if (universite != null && !universite.isEmpty()) sb.append("Université/Campus : ").append(universite).append("\n");
                    String typeAsso = rs.getString("type_asso");
                    if (typeAsso != null && !typeAsso.isEmpty()) sb.append("Type : ").append(typeAsso).append("\n");
                } catch (SQLException ignored) {}
                return sb.toString().trim();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String searchMemberByRole(String role) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM membres WHERE LOWER(role) LIKE ?")) {

            ps.setString(1, "%" + role.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(String.format(
                    "Membre : %s %s\nRôle : %s\nEmail : %s\nDescription : %s\n---\n",
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("description")
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String searchMemberByName(String name) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM membres WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ?")) {

            ps.setString(1, "%" + name.toLowerCase() + "%");
            ps.setString(2, "%" + name.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(String.format(
                    "Membre : %s %s\nRôle : %s\nEmail : %s\nDescription : %s\n---\n",
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("description")
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAllMembers() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM membres ORDER BY role")) {

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                String desc = rs.getString("description");
                result.append(String.format(
                    "- %s %s : %s (%s)%s\n",
                    rs.getString("prenom"),
                    rs.getString("nom"),
                    rs.getString("role"),
                    rs.getString("email"),
                    (desc != null && !desc.isEmpty()) ? " — " + desc : ""
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String searchFAQ(String keyword) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM faq WHERE LOWER(question) LIKE ? OR LOWER(reponse) LIKE ? OR LOWER(categorie) LIKE ?")) {

            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            ps.setString(2, "%" + keyword.toLowerCase() + "%");
            ps.setString(3, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(String.format(
                    "Q: %s\nR: %s\n(Catégorie: %s)\n---\n",
                    rs.getString("question"),
                    rs.getString("reponse"),
                    rs.getString("categorie")
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String globalSearch(String keyword) {
        StringBuilder results = new StringBuilder();

        String assoInfo = getAssociationInfo();
        if (assoInfo != null && containsKeyword(assoInfo, keyword)) {
            results.append("[ASSOCIATION]\n").append(assoInfo).append("\n\n");
        }

        String members = searchMemberByRole(keyword);
        if (members != null) {
            results.append("[MEMBRES]\n").append(members).append("\n");
        }
        String membersByName = searchMemberByName(keyword);
        if (membersByName != null && (members == null || !members.equals(membersByName))) {
            results.append("[MEMBRES]\n").append(membersByName).append("\n");
        }

        String faq = searchFAQ(keyword);
        if (faq != null) {
            results.append("[FAQ]\n").append(faq).append("\n");
        }

        String reseaux = searchReseaux(keyword);
        if (reseaux != null) {
            results.append("[RÉSEAUX / LIENS]\n").append(reseaux).append("\n");
        }

        return results.length() > 0 ? results.toString() : null;
    }

    private boolean containsKeyword(String text, String keyword) {
        return text.toLowerCase().contains(keyword.toLowerCase());
    }

    public void updateAssociationInfo(String nom, String description,
                                       String adresse, String universite, String typeAsso) {
        if (USE_POSTGRES) {
            System.out.println("[DB] updateAssociationInfo ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("DELETE FROM association_info");
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO association_info (nom, description, adresse, " +
                "universite, type_asso) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setString(3, adresse);
            ps.setString(4, universite);
            ps.setString(5, typeAsso);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMember(String nom, String prenom, String role, String email,
                          String description) {
        if (USE_POSTGRES) {
            System.out.println("[DB] addMember ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO membres (nom, prenom, role, email, description) " +
                 "VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, role);
            ps.setString(4, email);
            ps.setString(5, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMember(int id, String nom, String prenom, String role, String email,
                             String description) {
        if (USE_POSTGRES) {
            System.out.println("[DB] updateMember ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE membres SET nom=?, prenom=?, role=?, email=?, description=? WHERE id=?")) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, role);
            ps.setString(4, email);
            ps.setString(5, description);
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMember(int id) {
        if (USE_POSTGRES) {
            System.out.println("[DB] deleteMember ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM membres WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAllMembersRaw() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM membres ORDER BY role")) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addFAQ(String question, String reponse, String categorie) {
        if (USE_POSTGRES) {
            System.out.println("[DB] addFAQ ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO faq (question, reponse, categorie) VALUES (?, ?, ?)")) {
            ps.setString(1, question);
            ps.setString(2, reponse);
            ps.setString(3, categorie);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFAQ(int id, String question, String reponse, String categorie) {
        if (USE_POSTGRES) {
            System.out.println("[DB] updateFAQ ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE faq SET question=?, reponse=?, categorie=? WHERE id=?")) {
            ps.setString(1, question);
            ps.setString(2, reponse);
            ps.setString(3, categorie);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFAQ(int id) {
        if (USE_POSTGRES) {
            System.out.println("[DB] deleteFAQ ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM faq WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAllFAQRaw() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM faq ORDER BY categorie")) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("question"),
                    rs.getString("reponse"),
                    rs.getString("categorie")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addReseau(String type, String valeur, String libelle) {
        if (USE_POSTGRES) {
            System.out.println("[DB] addReseau ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO reseaux_sociaux (type, valeur, libelle) VALUES (?, ?, ?)")) {
            ps.setString(1, type);
            ps.setString(2, valeur);
            ps.setString(3, libelle);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateReseau(int id, String type, String valeur, String libelle) {
        if (USE_POSTGRES) {
            System.out.println("[DB] updateReseau ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE reseaux_sociaux SET type=?, valeur=?, libelle=? WHERE id=?")) {
            ps.setString(1, type);
            ps.setString(2, valeur);
            ps.setString(3, libelle);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteReseau(int id) {
        if (USE_POSTGRES) {
            System.out.println("[DB] deleteReseau ignoré : base NEON en lecture seule.");
            return;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM reseaux_sociaux WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAllReseauxRaw() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reseaux_sociaux ORDER BY type")) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("type"),
                    rs.getString("valeur"),
                    rs.getString("libelle")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getAllReseaux() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reseaux_sociaux ORDER BY type")) {

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                String libelle = rs.getString("libelle");
                result.append(String.format(
                    "- %s : %s%s\n",
                    rs.getString("type"),
                    rs.getString("valeur"),
                    (libelle != null && !libelle.isEmpty()) ? " (" + libelle + ")" : ""
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAllFAQ() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM faq ORDER BY categorie")) {

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(String.format(
                    "Q: %s\nR: %s\n(Catégorie: %s)\n---\n",
                    rs.getString("question"),
                    rs.getString("reponse"),
                    rs.getString("categorie")
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAllData() {
        StringBuilder data = new StringBuilder();

        String asso = getAssociationInfo();
        if (asso != null) {
            data.append("[ASSOCIATION]\n").append(asso).append("\n\n");
        }

        String membres = getAllMembers();
        if (membres != null) {
            data.append("[ÉQUIPE / MEMBRES]\n").append(membres).append("\n");
        }

        String faq = getAllFAQ();
        if (faq != null) {
            data.append("[FAQ]\n").append(faq).append("\n");
        }

        String reseaux = getAllReseaux();
        if (reseaux != null) {
            data.append("[RÉSEAUX / LIENS]\n").append(reseaux).append("\n");
        }

        return data.length() > 0 ? data.toString().trim() : null;
    }

    public String searchReseaux(String keyword) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM reseaux_sociaux WHERE LOWER(type) LIKE ? OR LOWER(valeur) LIKE ? OR LOWER(libelle) LIKE ?")) {

            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            ps.setString(2, "%" + keyword.toLowerCase() + "%");
            ps.setString(3, "%" + keyword.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                String libelle = rs.getString("libelle");
                result.append(String.format(
                    "%s : %s%s\n",
                    rs.getString("type"),
                    rs.getString("valeur"),
                    (libelle != null && !libelle.isEmpty()) ? " (" + libelle + ")" : ""
                ));
            }
            return result.length() > 0 ? result.toString() : null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getAssociationInfoRaw() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM association_info LIMIT 1")) {

            if (rs.next()) {
                String universite = "", typeAsso = "";
                try {
                    universite = rs.getString("universite") != null ? rs.getString("universite") : "";
                    typeAsso = rs.getString("type_asso") != null ? rs.getString("type_asso") : "";
                } catch (SQLException ignored) {}
                return new String[]{
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getString("adresse"),
                    universite,
                    typeAsso
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shutdown() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            if (!USE_POSTGRES) {
                stmt.execute("SHUTDOWN");
            }
        } catch (SQLException e) {

        }
    }
}
