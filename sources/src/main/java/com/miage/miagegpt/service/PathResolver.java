package com.miage.miagegpt.service;

import java.io.File;
import java.net.URISyntaxException;

public class PathResolver {

    private static File dataDir = null;

    public static synchronized File getDataDir() {
        if (dataDir != null)
            return dataDir;

        // 1. Priorité : propriété système explicite
        String explicit = System.getProperty("miagegpt.data.dir");
        if (explicit != null && !explicit.isBlank()) {
            File f = new File(explicit.trim());
            f.mkdirs();
            dataDir = f;
            System.out.println("[PathResolver] data/ via propriété système : " + dataDir.getAbsolutePath());
            return dataDir;
        }

        // 2. Chercher à côté du jar / exécutable MiageGPT-Data
        try {
            File jarLocation = new File(PathResolver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File jarDir = jarLocation.getParentFile();
            File miageDataDir = new File(jarDir, "MiageGPT-Data");
            miageDataDir.mkdirs();
            dataDir = miageDataDir;
            System.out.println("[PathResolver] data/ à côté du jar : " + dataDir.getAbsolutePath());
            return dataDir;
        } catch (URISyntaxException | NullPointerException e) {
            System.out.println("[PathResolver] Impossible de trouver le chemin du jar, fallback sur user.dir");
        }

        // 3. Fallback : user.dir/data
        File userDir = new File(System.getProperty("user.dir"));
        File candidate = new File(userDir, "MiageGPT-Data");
        if (!candidate.exists()) {
            File parent = userDir.getParentFile();
            if (parent != null) {
                File parentCandidate = new File(parent, "MiageGPT-Data");
                if (parentCandidate.exists()) {
                    candidate = parentCandidate;
                }
            }
        }
        candidate.mkdirs();
        dataDir = candidate;
        System.out.println("[PathResolver] data/ via user.dir : " + dataDir.getAbsolutePath());
        return dataDir;
    }
}
