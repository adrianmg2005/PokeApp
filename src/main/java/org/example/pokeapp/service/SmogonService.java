package org.example.pokeapp.service;

import org.example.pokeapp.model.*;
import org.example.pokeapp.util.AppConfig;

import com.google.gson.*;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches competitive Smogon set data from pkmn.github.io
 * and maps it to PokeAPI names for integration.
 */
@Service
public class SmogonService {

    private static final String BASE_URL = "https://pkmn.github.io/smogon/data/sets/";
    // Generations from newest to oldest — we prioritize newer gens
    private static final String[] GENERATIONS = {"gen9", "gen8", "gen7", "gen6", "gen5", "gen4", "gen3"};
    private final HttpClient client = HttpClient.newHttpClient();
    private final Path cacheDir = AppConfig.getCacheDir();

    // pokemonKey (lowercase) -> gen -> List<SmogonSet>
    private final Map<String, Map<String, List<SmogonSet>>> setsCache = new ConcurrentHashMap<>();
    // apiName (lowercase) -> smogonName (lowercase)
    private final Map<String, String> apiToSmogonName = new ConcurrentHashMap<>();
    private boolean loaded = false;

    @PostConstruct
    public void init() {
        loadSmogonData().join();
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Load OU sets from all generations. Caches each gen to disk.
     */
    public CompletableFuture<Void> loadSmogonData() {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(cacheDir);

                for (String gen : GENERATIONS) {
                    try {
                        String cacheFileName = "smogon_" + gen + "_ou.json";
                        Path cacheFile = cacheDir.resolve(cacheFileName);

                        String json;
                        if (Files.exists(cacheFile) &&
                                System.currentTimeMillis() - Files.getLastModifiedTime(cacheFile).toMillis() < 86_400_000L) {
                            json = Files.readString(cacheFile);
                        } else {
                            String url = BASE_URL + gen + "ou.json";
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET().build();
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response.statusCode() != 200) continue;
                            json = response.body();
                            Files.writeString(cacheFile, json);
                        }

                        parseSmogonJson(json, gen);
                    } catch (Exception e) {
                        // Skip failed generations
                    }
                }

                loaded = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void parseSmogonJson(String json, String gen) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        for (String pokemonName : root.keySet()) {
            JsonObject setsObj = root.getAsJsonObject(pokemonName);
            List<SmogonSet> sets = new ArrayList<>();

            for (String setName : setsObj.keySet()) {
                JsonObject setObj = setsObj.getAsJsonObject(setName);
                SmogonSet set = new SmogonSet();
                set.pokemonName = pokemonName;
                set.setName = setName;
                set.generation = gen;
                set.level = setObj.has("level") ? setObj.get("level").getAsInt() : 100;

                // Moves: array of arrays (each slot can have alternatives)
                set.moves = new ArrayList<>();
                if (setObj.has("moves") && setObj.get("moves").isJsonArray()) {
                    for (JsonElement slotEl : setObj.getAsJsonArray("moves")) {
                        List<String> alternatives = new ArrayList<>();
                        if (slotEl.isJsonArray()) {
                            for (JsonElement moveEl : slotEl.getAsJsonArray()) {
                                alternatives.add(moveEl.getAsString());
                            }
                        } else {
                            alternatives.add(slotEl.getAsString());
                        }
                        set.moves.add(alternatives);
                    }
                }

                // Ability: can be array or string
                set.ability = parseStringOrArray(setObj, "ability");
                // Item
                set.item = parseStringOrArray(setObj, "item");
                // Nature
                set.nature = parseStringOrArray(setObj, "nature");
                // Teratypes
                set.teratypes = parseStringOrArray(setObj, "teratypes");

                // EVs
                if (setObj.has("evs") && setObj.get("evs").isJsonObject()) {
                    set.evs = new LinkedHashMap<>();
                    JsonObject evsObj = setObj.getAsJsonObject("evs");
                    for (String stat : evsObj.keySet()) {
                        set.evs.put(stat, evsObj.get(stat).getAsInt());
                    }
                }

                // IVs
                if (setObj.has("ivs") && setObj.get("ivs").isJsonObject()) {
                    set.ivs = new LinkedHashMap<>();
                    JsonObject ivsObj = setObj.getAsJsonObject("ivs");
                    for (String stat : ivsObj.keySet()) {
                        set.ivs.put(stat, ivsObj.get(stat).getAsInt());
                    }
                }

                sets.add(set);
            }

            String lowerName = pokemonName.toLowerCase().trim();
            setsCache.computeIfAbsent(lowerName, k -> new ConcurrentHashMap<>()).put(gen, sets);

            String apiSlug = pokemonName.toLowerCase().trim().replace(" ", "-");
            apiToSmogonName.put(apiSlug, lowerName);
        }
    }

    private List<String> parseStringOrArray(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return List.of();
        JsonElement el = obj.get(key);
        List<String> result = new ArrayList<>();
        if (el.isJsonArray()) {
            for (JsonElement item : el.getAsJsonArray()) {
                result.add(item.getAsString());
            }
        } else if (el.isJsonPrimitive()) {
            result.add(el.getAsString());
        }
        return result;
    }

    /**
     * Result holder containing sets and which generation they come from
     */
    public static class SmogonResult {
        public final String generation;
        public final List<SmogonSet> sets;
        public SmogonResult(String generation, List<SmogonSet> sets) {
            this.generation = generation;
            this.sets = sets;
        }
    }

    /**
     * Get competitive sets for a Pokémon, searching newest gen first.
     * Returns null if not found in any gen OU.
     */
    public SmogonResult getSets(String apiName) {
        if (apiName == null || !loaded) return null;
        Map<String, List<SmogonSet>> genMap = resolveGenMap(apiName);
        if (genMap == null || genMap.isEmpty()) return null;

        // Return the newest generation that has sets
        for (String gen : GENERATIONS) {
            List<SmogonSet> sets = genMap.get(gen);
            if (sets != null && !sets.isEmpty()) {
                return new SmogonResult(gen, sets);
            }
        }
        return null;
    }

    /**
     * Get all competitive sets across ALL generations for a Pokémon.
     * Returns a list of SmogonResult, one per generation (newest first).
     */
    public List<SmogonResult> getAllGenSets(String apiName) {
        if (apiName == null || !loaded) return List.of();
        Map<String, List<SmogonSet>> genMap = resolveGenMap(apiName);
        if (genMap == null || genMap.isEmpty()) return List.of();

        List<SmogonResult> results = new ArrayList<>();
        for (String gen : GENERATIONS) {
            List<SmogonSet> sets = genMap.get(gen);
            if (sets != null && !sets.isEmpty()) {
                results.add(new SmogonResult(gen, sets));
            }
        }
        return results;
    }

    private Map<String, List<SmogonSet>> resolveGenMap(String apiName) {
        String lower = apiName.toLowerCase().trim();

        Map<String, List<SmogonSet>> genMap = setsCache.get(lower);
        if (genMap != null) return genMap;

        String smogonName = apiToSmogonName.get(lower);
        if (smogonName != null) {
            genMap = setsCache.get(smogonName);
            if (genMap != null) return genMap;
        }

        String displayLike = lower.replace("-", " ");
        genMap = setsCache.get(displayLike);
        return genMap;
    }

    /**
     * Check if a Pokémon is in any gen OU tier
     */
    public boolean isInOU(String apiName) {
        return getSets(apiName) != null;
    }

    /**
     * Get all Pokémon names that are in OU (any gen)
     */
    public Set<String> getAllOUPokemon() {
        return Collections.unmodifiableSet(setsCache.keySet());
    }

    public static String translateGen(String gen) {
        if (gen == null) return "";
        return switch (gen) {
            case "gen9" -> "Gen 9 (Scarlet/Violet)";
            case "gen8" -> "Gen 8 (Sword/Shield)";
            case "gen7" -> "Gen 7 (Sun/Moon)";
            case "gen6" -> "Gen 6 (X/Y)";
            case "gen5" -> "Gen 5 (Black/White)";
            case "gen4" -> "Gen 4 (Diamond/Pearl)";
            case "gen3" -> "Gen 3 (Ruby/Sapphire)";
            default -> gen.toUpperCase();
        };
    }

    // --- Translation helpers for Smogon English names ---

    public static String translateNature(String nature) {
        if (nature == null) return "";
        return switch (nature.toLowerCase()) {
            case "adamant" -> "Firme";
            case "bold" -> "Osada";
            case "brave" -> "Audaz";
            case "calm" -> "Serena";
            case "careful" -> "Cauta";
            case "gentle" -> "Amable";
            case "hasty" -> "Activa";
            case "impish" -> "Agitada";
            case "jolly" -> "Alegre";
            case "lax" -> "Floja";
            case "lonely" -> "Huraña";
            case "mild" -> "Afable";
            case "modest" -> "Modesta";
            case "naive" -> "Ingenua";
            case "naughty" -> "Pícara";
            case "quiet" -> "Mansa";
            case "rash" -> "Alocada";
            case "relaxed" -> "Plácida";
            case "sassy" -> "Grosera";
            case "serious" -> "Seria";
            case "timid" -> "Miedosa";
            case "bashful" -> "Tímida";
            case "docile" -> "Dócil";
            case "hardy" -> "Fuerte";
            case "quirky" -> "Rara";
            default -> nature;
        };
    }

    public static String translateStat(String stat) {
        if (stat == null) return "";
        return switch (stat.toLowerCase()) {
            case "hp" -> "PS";
            case "atk" -> "Atq";
            case "def" -> "Def";
            case "spa" -> "At.Esp";
            case "spd" -> "Def.Esp";
            case "spe" -> "Vel";
            default -> stat;
        };
    }

    public static String translateItem(String item) {
        if (item == null) return "";
        return switch (item) {
            case "Leftovers" -> "Restos";
            case "Choice Band" -> "Cinta Elegida";
            case "Choice Specs" -> "Gafas Elegidas";
            case "Choice Scarf" -> "Pañuelo Elegido";
            case "Life Orb" -> "Vidasfera";
            case "Heavy-Duty Boots" -> "Botas Gruesas";
            case "Rocky Helmet" -> "Casco Dentado";
            case "Assault Vest" -> "Chaleco Asalto";
            case "Focus Sash" -> "Bandana";
            case "Eviolite" -> "Piedra Evolutiva";
            case "Black Sludge" -> "Lodo Negro";
            case "Toxic Orb" -> "Orbe Tóxico";
            case "Flame Orb" -> "Orbe Llama";
            case "Lum Berry" -> "Baya Lum";
            case "Sitrus Berry" -> "Baya Zidra";
            case "Booster Energy" -> "Energía Potenciadora";
            case "Clear Amulet" -> "Amuleto Nítido";
            case "Covert Cloak" -> "Capa Furtiva";
            case "Loaded Dice" -> "Dado Trucado";
            case "Red Card" -> "Tarjeta Roja";
            case "Shed Shell" -> "Muda Concha";
            case "Air Balloon" -> "Globo Helio";
            case "Expert Belt" -> "Cinto Maestro";
            case "Weakness Policy" -> "Seguro Debilidad";
            case "Light Clay" -> "Refleluz";
            case "Mental Herb" -> "Hierba Mental";
            case "Power Herb" -> "Hierba Ímpetu";
            case "White Herb" -> "Hierba Blanca";
            case "Mirror Herb" -> "Hierba Espejo";
            case "Throat Spray" -> "Spray Bucal";
            case "Terrain Extender" -> "Extensión Campo";
            case "Protective Pads" -> "Parche Blindado";
            case "Safety Goggles" -> "Gafas Protectoras";
            default -> item; // Keep English if no translation
        };
    }
}
