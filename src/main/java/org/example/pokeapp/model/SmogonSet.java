package org.example.pokeapp.model;

import java.util.List;
import java.util.Map;

public class SmogonSet {
    public String pokemonName;
    public String setName;
    public String generation; // e.g. "gen9", "gen8"
    public List<List<String>> moves; // Each slot can have alternatives
    public List<String> ability;
    public List<String> item;
    public List<String> nature;
    public Map<String, Integer> evs;
    public Map<String, Integer> ivs;
    public List<String> teratypes;
    public int level;
}
