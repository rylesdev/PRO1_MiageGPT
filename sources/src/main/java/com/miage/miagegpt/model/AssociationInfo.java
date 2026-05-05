package com.miage.miagegpt.model;

public class AssociationInfo {
    private int id;
    private String nom;
    private String description;
    private int anneeFondation;
    private String adresse;
    private String email;
    private String siteWeb;
    private String universite;
    private String typeAsso;
    private int nbMembres;
    private String slogan;

    public AssociationInfo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAnneeFondation() { return anneeFondation; }
    public void setAnneeFondation(int anneeFondation) { this.anneeFondation = anneeFondation; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public String getUniversite() { return universite; }
    public void setUniversite(String universite) { this.universite = universite; }

    public String getTypeAsso() { return typeAsso; }
    public void setTypeAsso(String typeAsso) { this.typeAsso = typeAsso; }

    public int getNbMembres() { return nbMembres; }
    public void setNbMembres(int nbMembres) { this.nbMembres = nbMembres; }

    public String getSlogan() { return slogan; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
}
