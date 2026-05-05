package com.miage.miagegpt.model;

import com.miage.miagegpt.service.PathResolver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = initDbUrl();

    private static String initDbUrl() {
        java.io.File dataDir = PathResolver.getDataDir();
        String dbPath = new java.io.File(dataDir, "miagegpt_data").getAbsolutePath();
        System.out.println("[DB] Emplacement de la base de données : " + dbPath + ".mv.db");
        return "jdbc:h2:" + dbPath;
    }
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

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
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS association_info (" +
                "   id INT PRIMARY KEY AUTO_INCREMENT," +
                "   nom VARCHAR(200) NOT NULL," +
                "   description TEXT," +
                "   adresse VARCHAR(300)" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS membres (" +
                "   id INT PRIMARY KEY AUTO_INCREMENT," +
                "   nom VARCHAR(100) NOT NULL," +
                "   prenom VARCHAR(100)," +
                "   role VARCHAR(100) NOT NULL," +
                "   email VARCHAR(150)," +
                "   annee_debut INT," +
                "   description TEXT" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS faq (" +
                "   id INT PRIMARY KEY AUTO_INCREMENT," +
                "   question VARCHAR(500) NOT NULL," +
                "   reponse TEXT NOT NULL," +
                "   categorie VARCHAR(100)" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS reseaux_sociaux (" +
                "   id INT PRIMARY KEY AUTO_INCREMENT," +
                "   type VARCHAR(100) NOT NULL," +
                "   valeur VARCHAR(300) NOT NULL," +
                "   libelle VARCHAR(200)" +
                ")"
            );

            try { stmt.execute("ALTER TABLE association_info ADD COLUMN IF NOT EXISTS universite VARCHAR(200)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE association_info ADD COLUMN IF NOT EXISTS type_asso VARCHAR(100)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE association_info ADD COLUMN IF NOT EXISTS slogan VARCHAR(300)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE association_info DROP COLUMN IF EXISTS annee_fondation"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE association_info DROP COLUMN IF EXISTS nb_membres"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE association_info DROP COLUMN IF EXISTS email"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE association_info DROP COLUMN IF EXISTS site_web"); } catch (SQLException ignored) {}
            try { stmt.execute("DROP TABLE IF EXISTS evenements"); } catch (SQLException ignored) {}

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
            stmt.execute("SHUTDOWN");
        } catch (SQLException e) {

        }
    }
}
