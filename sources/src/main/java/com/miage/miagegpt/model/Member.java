package com.miage.miagegpt.model;

public class Member {
    private int id;
    private String nom;
    private String prenom;
    private String role;
    private String email;
    private int anneeDebut;
    private String description;

    public Member() {}

    public Member(int id, String nom, String prenom, String role, String email, String description) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.email = email;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAnneeDebut() { return anneeDebut; }
    public void setAnneeDebut(int anneeDebut) { this.anneeDebut = anneeDebut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
