package com.rvy.scanner.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a local {@code .env} file into system properties before Spring Boot starts.
 * Spring does not read {@code .env} on its own.
 */
public final class DotenvLoader {

    private static final List<Path> CANDIDATES = List.of(
            Path.of(".env"),
            Path.of("src/.env"));

    private DotenvLoader() {
    }

    public static void load() {
        for (Path path : CANDIDATES) {
            if (Files.isRegularFile(path)) {
                loadFile(path);
                return;
            }
        }
    }

    private static void loadFile(Path path) {
        try {
            for (String raw : Files.readAllLines(path)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("export ")) {
                    line = line.substring("export ".length()).trim();
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = unquote(line.substring(eq + 1).trim());
                if (System.getenv(key) == null || System.getenv(key).isBlank()) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not read " + path + ": " + ex.getMessage());
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
