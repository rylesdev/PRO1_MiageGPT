package com.miage.miagegpt.service;

import java.io.File;

public class PathResolver {

    private static File dataDir = null;

    public static synchronized File getDataDir() {
        if (dataDir != null)
            return dataDir;

        String explicit = System.getProperty("miagegpt.data.dir");
        if (explicit != null && !explicit.isBlank()) {
            File f = new File(explicit.trim());
            f.mkdirs();
            dataDir = f;
            System.out.println("[PathResolver] data/ via propriété système : " + dataDir.getAbsolutePath());
            return dataDir;
        }

        File userDir = new File(System.getProperty("user.dir"));
        File candidate = new File(userDir, "data");
        if (!candidate.exists()) {
            File parent = userDir.getParentFile();
            if (parent != null) {
                File parentCandidate = new File(parent, "data");
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
