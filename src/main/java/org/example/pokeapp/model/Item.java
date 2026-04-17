package org.example.pokeapp.model;

import java.util.List;

public class Item {
    public int id;
    public String name;
    public String spanishName;
    public String spanishEffect;
    public String spanishFlavorText;
    public String category;
    public String spriteUrl;
    public int cost;
    public List<HeldByPokemon> heldByPokemon;

    public static class HeldByPokemon {
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
}
