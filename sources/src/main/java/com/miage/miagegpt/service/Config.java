package com.miage.miagegpt.service;

import java.io.*;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String API_KEY_PROPERTY = "groq.api.key";
    private static String GROQ_API_KEY = null;

    static {
        loadApiKey();
    }

    public static String getGROQ_API_KEY() {
        return GROQ_API_KEY;
    }

    public static void setGROQ_API_KEY(String apiKey) {
        GROQ_API_KEY = apiKey == null ? null : apiKey.trim();
        saveApiKey();
    }

    private static void loadApiKey() {
        String envApiKey = firstNonEmpty(System.getenv("MIAGEGPT_GROQ_API_KEY"), System.getenv("GROQ_API_KEY"));
        if (envApiKey != null) {
            GROQ_API_KEY = envApiKey.trim();
            return;
        }

        File configFile = new File(PathResolver.getDataDir(), CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            properties.load(inputStream);
            String apiKey = properties.getProperty(API_KEY_PROPERTY);
            if (apiKey != null && !apiKey.isBlank()) {
                GROQ_API_KEY = apiKey.trim();
            }
        } catch (IOException e) {
            System.err.println("[Config] Erreur lors du chargement de la clé API: " + e.getMessage());
        }
    }

    private static void saveApiKey() {
        File configFile = new File(PathResolver.getDataDir(), CONFIG_FILE_NAME);
        Properties properties = new Properties();

        if (configFile.exists()) {
            try (InputStream inputStream = new FileInputStream(configFile)) {
                properties.load(inputStream);
            } catch (IOException e) {
                System.err.println("[Config] Erreur lors de la lecture du fichier de config: " + e.getMessage());
            }
        }

        if (GROQ_API_KEY == null || GROQ_API_KEY.isBlank()) {
            properties.remove(API_KEY_PROPERTY);
        } else {
            properties.setProperty(API_KEY_PROPERTY, GROQ_API_KEY);
        }

        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(configFile)) {
            properties.store(outputStream, "MiageGPT local configuration");
            System.out.println("[Config] Clé API GROQ sauvegardée dans " + configFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[Config] Erreur lors de l'écriture de la clé API: " + e.getMessage());
        }
    }

    private static String firstNonEmpty(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    public static boolean isApiKeyConfigured() {
        return GROQ_API_KEY != null && !GROQ_API_KEY.isBlank();
    }

    public static String getConfigurationError() {
        if (GROQ_API_KEY == null || GROQ_API_KEY.isBlank()) {
            return "Clé API non configurée. Visitez https://console.groq.com/keys";
        }
        if (!GROQ_API_KEY.startsWith("gsk_")) {
            return "Clé API invalide.";
        }
        return null;
    }
}
