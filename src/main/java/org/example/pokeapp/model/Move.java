package org.example.pokeapp.model;

import java.util.List;

public class Move {
    public int id;
    public String name;
    public int accuracy;
    public int power;
    public int pp;
    public int priority;
    public MoveType type;
    public DamageClass damage_class;
    public Generation generation;
    public List<EffectEntry> effect_entries;
    public List<FlavorTextEntry> flavor_text_entries;
    public MoveMeta meta;
    public List<LearnedByPokemon> learned_by_pokemon;
    public List<MoveName> names;

    public static class MoveType {
        public String name;
    }

    public static class DamageClass {
        public String name;
    }

    public static class Generation {
        public String name;
    }

    public static class LearnedByPokemon {
        public String name;
        public String url;

        public int getId() {
            if (url == null) return 0;
            String[] parts = url.replace("https://pokeapi.co/api/v2/pokemon/", "").split("/");
            try { return Integer.parseInt(parts[0]); } catch (Exception e) { return 0; }
        }
    }

    public static class MoveName {
        public String name;
        public Language language;
    }

    public static class EffectEntry {
        public String effect;
        public String short_effect;
        public Language language;
    }

    public static class FlavorTextEntry {
        public String flavor_text;
        public Language language;
        public VersionGroup version_group;
    }

    public static class Language {
        public String name;
    }

    public static class VersionGroup {
        public String name;
    }

    public static class MoveMeta {
        public MoveCategory category;
        public int min_hits;
        public int max_hits;
        public int drain;
        public int healing;
        public int crit_rate;
        public int flinch_chance;
        public int stat_chance;
    }

    public static class MoveCategory {
        public String name;
    }

    public String getSpanishName() {
        if (names != null) {
            for (MoveName mn : names) {
                if (mn.language != null && "es".equals(mn.language.name)) {
                    return mn.name;
                }
            }
        }
        return name != null ? name.substring(0, 1).toUpperCase() + name.substring(1).replace("-", " ") : "";
    }

    public String getDescriptionEs() {
        if (flavor_text_entries != null) {
            for (FlavorTextEntry entry : flavor_text_entries) {
                if (entry.language != null && "es".equals(entry.language.name)) {
                    return entry.flavor_text;
                }
            }
        }
        if (effect_entries != null) {
            for (EffectEntry entry : effect_entries) {
                if (entry.language != null && "en".equals(entry.language.name)) {
                    return entry.short_effect;
                }
            }
        }
        return "";
    }

    public String getDamageClassName() {
        return damage_class != null ? damage_class.name : "unknown";
    }

    public String getTypeName() {
        return type != null ? type.name : "normal";
    }

    public String getGenerationLabel() {
        if (generation == null || generation.name == null) return "—";
        return generation.name.replace("generation-", "Gen ").toUpperCase();
    }
}
