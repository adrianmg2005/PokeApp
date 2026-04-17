package org.example.pokeapp.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Centralized app configuration: data paths, version info.
 * User data (favorites, history) is stored separately from cache
 * so that app updates never touch personal data.
 */
public final class AppConfig {

    private static final String APP_NAME = "PokeApp";
    private static final String VERSION;

    static {
        String v = "1.0.0";
        try (InputStream is = AppConfig.class.getResourceAsStream("/version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                v = props.getProperty("app.version", v);
            }
        } catch (Exception ignored) { }
        VERSION = v;
    }

    private AppConfig() { }

    /** Application version string, loaded from version.properties */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Directory for user data that must survive app updates
     * (favorites, history, settings).
     * Windows: %APPDATA%/PokeApp/
     * Others:  ~/.pokeapp/
     */
    public static Path getUserDataDir() {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path dir;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            dir = (appData != null)
                    ? Paths.get(appData, APP_NAME)
                    : Paths.get(System.getProperty("user.home"), "." + APP_NAME.toLowerCase());
        } else {
            dir = Paths.get(System.getProperty("user.home"), "." + APP_NAME.toLowerCase());
        }
        try { Files.createDirectories(dir); } catch (Exception ignored) { }
        return dir;
    }

    /**
     * Directory for API cache data (can be rebuilt from network).
     * Stored inside user data dir so it also survives updates.
     */
    public static Path getCacheDir() {
        Path dir = getUserDataDir().resolve("cache");
        try { Files.createDirectories(dir); } catch (Exception ignored) { }
        return dir;
    }

    /**
     * Migrates data from the old ~/.pokeapp_cache/ directory to the new locations.
     * User data (favorites.json, history.json) → getUserDataDir()
     * Cache files → getCacheDir()
     * Only copies files that don't already exist in the new location.
     */
    public static void migrateOldData() {
        Path oldDir = Paths.get(System.getProperty("user.home"), ".pokeapp_cache");
        if (!Files.isDirectory(oldDir)) return;

        Path userDir = getUserDataDir();
        Path cacheDir = getCacheDir();

        // User data files → user data dir
        String[] userFiles = {"favorites.json", "history.json"};
        for (String f : userFiles) {
            Path src = oldDir.resolve(f);
            Path dst = userDir.resolve(f);
            if (Files.exists(src) && !Files.exists(dst)) {
                try { Files.copy(src, dst); } catch (Exception ignored) { }
            }
        }

        // Cache files → cache dir
        try (var entries = Files.list(oldDir)) {
            entries
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".json"))
                .filter(p -> !p.getFileName().toString().equals("favorites.json")
                          && !p.getFileName().toString().equals("history.json"))
                .forEach(src -> {
                    Path dst = cacheDir.resolve(src.getFileName());
                    if (!Files.exists(dst)) {
                        try { Files.copy(src, dst); } catch (Exception ignored) { }
                    }
                });
        } catch (Exception ignored) { }
    }
}
