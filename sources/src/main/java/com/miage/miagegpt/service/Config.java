package com.miage.miagegpt.service;

public class Config {
    public static final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");
    
    public static String getConfigurationError() {
        if (GROQ_API_KEY == null || GROQ_API_KEY.isEmpty()) {
            return "Clé API non configurée. Visitez https://console.groq.com/keys";
        }
        if (!GROQ_API_KEY.startsWith("gsk_")) {
            return "Clé API invalide.";
        }
        return null;
    }
}