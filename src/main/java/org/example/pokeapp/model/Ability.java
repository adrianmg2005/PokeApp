package org.example.pokeapp.model;

import java.util.List;

public class Ability {
    public int id;
    public String name;
    public String spanishName;
    public String spanishEffect;
    public String spanishShortEffect;
    public String spanishFlavorText;
    public GenerationInfo generation;
    public List<PokemonEntry> pokemon;

    public static class GenerationInfo {
        public String name;
    }

    public static class PokemonEntry {
        public boolean is_hidden;
        public int slot;
        public PokemonRef pokemon;
    }

    public static class PokemonRef {
        public String name;
        public String url;

        public int getId() {
            if (url == null) return 0;
            String clean = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            String[] parts = clean.split("/");
            try { return Integer.parseInt(parts[parts.length - 1]); } catch (Exception e) { return 0; }
        }
    }

    public String getGenerationLabel() {
        if (generation == null || generation.name == null) return "—";
        return generation.name.replace("generation-", "Gen ").toUpperCase();
    }
}
