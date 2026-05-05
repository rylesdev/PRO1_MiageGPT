package com.miage.miagegpt.model;

public class FAQ {
    private int id;
    private String question;
    private String reponse;
    private String categorie;

    public FAQ() {}

    public FAQ(int id, String question, String reponse, String categorie) {
        this.id = id;
        this.question = question;
        this.reponse = reponse;
        this.categorie = categorie;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}
