package com.miage.miagegpt.model;

public class Event {
    private int id;
    private String nom;
    private String dateEvent;
    private String heure;
    private String lieu;
    private String description;
    private String prix;

    public Event() {}

    public Event(int id, String nom, String dateEvent, String heure, String lieu, String description, String prix) {
        this.id = id;
        this.nom = nom;
        this.dateEvent = dateEvent;
        this.heure = heure;
        this.lieu = lieu;
        this.description = description;
        this.prix = prix;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDateEvent() { return dateEvent; }
    public void setDateEvent(String dateEvent) { this.dateEvent = dateEvent; }

    public String getHeure() { return heure; }
    public void setHeure(String heure) { this.heure = heure; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrix() { return prix; }
    public void setPrix(String prix) { this.prix = prix; }
}
