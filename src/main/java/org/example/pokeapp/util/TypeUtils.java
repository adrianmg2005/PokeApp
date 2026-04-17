package org.example.pokeapp.util;

import java.util.Map;

/**
 * Centralised type‑colour / style helpers used across every tab.
 * All methods are static – no instance needed.
 */
public final class TypeUtils {

    private TypeUtils() {}

    public static final String[] ALL_TYPES = {
            "normal", "fire", "water", "grass", "electric", "ice",
            "fighting", "poison", "ground", "flying", "psychic",
            "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
    };

    public static final Map<String, String> TYPE_ABBREV = Map.ofEntries(
            Map.entry("normal", "NOR"), Map.entry("fire", "FUE"), Map.entry("water", "AGU"),
            Map.entry("grass", "PLA"), Map.entry("electric", "ELE"), Map.entry("ice", "HIE"),
            Map.entry("fighting", "LUC"), Map.entry("poison", "VEN"), Map.entry("ground", "TIE"),
            Map.entry("flying", "VOL"), Map.entry("psychic", "PSI"), Map.entry("bug", "BIC"),
            Map.entry("rock", "ROC"), Map.entry("ghost", "FAN"), Map.entry("dragon", "DRA"),
            Map.entry("dark", "SIN"), Map.entry("steel", "ACE"), Map.entry("fairy", "HAD")
    );

    /* ---- colour look-ups ---- */

    public static String getTypeColor(String type) {
        return switch (type) {
            case "fire"     -> "#F08030";
            case "water"    -> "#6890F0";
            case "grass"    -> "#78C850";
            case "electric" -> "#F8D030";
            case "ice"      -> "#98D8D8";
            case "fighting" -> "#C03028";
            case "poison"   -> "#A040A0";
            case "ground"   -> "#E0C068";
            case "flying"   -> "#A890F0";
            case "psychic"  -> "#F85888";
            case "bug"      -> "#A8B820";
            case "rock"     -> "#B8A038";
            case "ghost"    -> "#705898";
            case "dragon"   -> "#7038F8";
            case "dark"     -> "#705848";
            case "steel"    -> "#B8B8D0";
            case "fairy"    -> "#EE99AC";
            case "normal"   -> "#A8A878";
            default         -> "#777";
        };
    }

    /** Darker / richer palette used in the type‑defence grid header. */
    public static String getTypeColorDark(String type) {
        return switch (type) {
            case "fire"     -> "#E62829";
            case "water"    -> "#2980EF";
            case "grass"    -> "#3FA129";
            case "electric" -> "#FAC000";
            case "ice"      -> "#3DCEF3";
            case "fighting" -> "#FF8000";
            case "poison"   -> "#9141CB";
            case "ground"   -> "#915121";
            case "flying"   -> "#81B9EF";
            case "psychic"  -> "#EF4179";
            case "bug"      -> "#91A119";
            case "rock"     -> "#AFA981";
            case "ghost"    -> "#704170";
            case "dragon"   -> "#5060E1";
            case "dark"     -> "#624D4E";
            case "steel"    -> "#60A1B8";
            case "fairy"    -> "#EF70EF";
            default         -> "#A0A29F";
        };
    }

    /* ---- style strings ---- */

    /** Badge used in the Pokédex for the main type labels. */
    public static String getTypeStyle(String type) {
        String color = getTypeColor(type);
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 6 14;" +
                "-fx-background-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 4, 0.5, 0, 2);";
    }

    /** Smaller badge used in autocomplete lists, comparator panels, etc. */
    public static String getTypeStyleCompact(String type) {
        String color = getTypeColor(type);
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 4 10;" +
                "-fx-background-radius: 10;";
    }

    /** Tiny badge used for compact move previews in team builder. */
    public static String getCompactMoveStyle(String type) {
        String color = getTypeColor(type);
        return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 8px; " +
                "-fx-font-weight: bold; -fx-padding: 1 5; -fx-background-radius: 6;";
    }

    /** Badge used for Smogon‑set move cards. */
    public static String getMoveSmogonStyle(String type) {
        String color = getTypeColor(type);
        return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-padding: 6 14; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0.3, 0, 1);";
    }
}
