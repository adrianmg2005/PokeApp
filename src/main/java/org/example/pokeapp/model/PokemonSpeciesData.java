package org.example.pokeapp.model;

import java.util.List;

public class PokemonSpeciesData {
    public String description;       // Spanish flavor text
    public String genus;             // e.g. "Pokémon Semilla"
    public String habitat;           // e.g. "grassland"
    public String growthRate;        // e.g. "medium-slow"
    public String color;             // e.g. "green"
    public String shape;             // e.g. "quadruped"
    public int captureRate;          // 0-255
    public int baseHappiness;        // 0-255
    public int genderRate;           // -1 = genderless, 0-8 (female eighths)
    public int hatchCounter;         // egg cycles
    public boolean isLegendary;
    public boolean isMythical;
    public boolean isBaby;
    public List<String> eggGroups;   // e.g. ["Monster", "Plant"]
    public List<String> gameVersions; // e.g. ["Red", "Blue", "Sword"]
    public List<String> varieties;    // e.g. ["charizard", "charizard-mega-x", "charizard-mega-y"]
}
