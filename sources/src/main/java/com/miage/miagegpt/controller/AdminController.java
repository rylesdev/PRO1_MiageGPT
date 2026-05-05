package com.miage.miagegpt.controller;

import com.miage.miagegpt.model.DatabaseManager;

import java.util.List;

public class AdminController {

    private final DatabaseManager db;

    public AdminController() {
        this.db = DatabaseManager.getInstance();
    }

    public String[] getAssociationInfo() {
        return db.getAssociationInfoRaw();
    }

    public void updateAssociationInfo(String nom, String description,
                                      String adresse, String universite, String typeAsso) {
        db.updateAssociationInfo(nom, description, adresse, universite, typeAsso);
    }

    public List<String[]> getAllMembers() {
        return db.getAllMembersRaw();
    }

    public void addMember(String nom, String prenom, String role, String email, String description) {
        db.addMember(nom, prenom, role, email, description);
    }

    public void updateMember(int id, String nom, String prenom, String role, String email, String description) {
        db.updateMember(id, nom, prenom, role, email, description);
    }

    public void deleteMember(int id) {
        db.deleteMember(id);
    }

    public List<String[]> getAllFAQ() {
        return db.getAllFAQRaw();
    }

    public void addFAQ(String question, String reponse, String categorie) {
        db.addFAQ(question, reponse, categorie);
    }

    public void updateFAQ(int id, String question, String reponse, String categorie) {
        db.updateFAQ(id, question, reponse, categorie);
    }

    public void deleteFAQ(int id) {
        db.deleteFAQ(id);
    }

    public List<String[]> getAllReseaux() {
        return db.getAllReseauxRaw();
    }

    public void addReseau(String type, String valeur, String libelle) {
        db.addReseau(type, valeur, libelle);
    }

    public void updateReseau(int id, String type, String valeur, String libelle) {
        db.updateReseau(id, type, valeur, libelle);
    }

    public void deleteReseau(int id) {
        db.deleteReseau(id);
    }

}
