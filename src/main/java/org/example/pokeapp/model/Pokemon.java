package org.example.pokeapp.model;

import java.util.List;

public class Pokemon {

    public int id;
    public String name;
    public String spanishName;
    public int height;  // in decimetres
    public int weight;  // in hectograms
    public Species species;

    public List<Stat> stats;
    public List<TypeSlot> types;
    public List<AbilitySlot> abilities;
    public List<GameIndex> game_indices;
    public List<HeldItem> held_items;

    public int getStat(String statName) {
        for (Stat s : stats) {
            if (s.stat.name.equals(statName)) {
                return s.base_stat;
            }
        }
        return 0;
    }

    public static class Stat {
        public int base_stat;
        public StatInfo stat;
    }

    public static class StatInfo {
        public String name;
    }

    public static class TypeSlot {
        public int slot;
        public Type type;
    }

    public static class Type {
        public String name;
    }

    public static class AbilitySlot {
        public Ability ability;
        public boolean is_hidden;
        public int slot;
    }

    public static class Ability {
        public String name;
    }

    public static class GameIndex {
        public int game_index;
        public Version version;
    }

    public static class Version {
        public String name;
    }

    public static class HeldItem {
        public ItemRef item;
    }

    public static class ItemRef {
        public String name;
    }

    public static class Species {
        public String name;
    }
}