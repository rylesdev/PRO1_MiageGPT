package com.miage.miagegpt.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    private static String GROQ_API_KEY = null;

    public static String getGROQ_API_KEY() {
        return GROQ_API_KEY;
    }

    public static void setGROQ_API_KEY(String apiKey) {
        GROQ_API_KEY = apiKey;
        updateConfigFile(apiKey);
    }

    private static void updateConfigFile(String apiKey) {
        try {
            String configFilePath = "src/main/java/com/miage/miagegpt/service/Config.java";
            String content = new String(Files.readAllBytes(Paths.get(configFilePath)));

            String newLine = "    private static String GROQ_API_KEY = \"" + apiKey + "\";";

            String[] lines = content.split("\n");
            StringBuilder updated = new StringBuilder();

            for (String line : lines) {
                if (line.trim().startsWith("private static String GROQ_API_KEY")) {
                    updated.append(newLine);
                } else {
                    updated.append(line);
                }
                updated.append("\n");
            }

            Files.write(Paths.get(configFilePath), updated.toString().getBytes());
            System.out.println("[Config] Clé API GROQ mise à jour");
        } catch (IOException e) {
            System.err.println("[Config] Erreur lors de l'écriture: " + e.getMessage());
        }
    }

    public static boolean isApiKeyConfigured() {
        return GROQ_API_KEY != null && !GROQ_API_KEY.isEmpty();
    }

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
