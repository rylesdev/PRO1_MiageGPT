package com.miage.miagegpt.model;

public class Reseau {
    private int id;
    private String type;
    private String valeur;
    private String libelle;

    public Reseau() {}

    public Reseau(int id, String type, String valeur, String libelle) {
        this.id = id;
        this.type = type;
        this.valeur = valeur;
        this.libelle = libelle;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
