package org.example.pokeapp.service;

import org.example.pokeapp.model.*;
import org.example.pokeapp.util.AppConfig;

import com.google.gson.*;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PokeApiService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final Path cacheDir = AppConfig.getCacheDir();
    private static final String CACHE_VERSION = "4";

    private Map<String, Pokemon> pokemonCache = new ConcurrentHashMap<>();
    private Map<String, String> typeCache = new ConcurrentHashMap<>();
    private boolean isLoaded = false;
    private String loadingMessage = "Iniciando...";
    private int loadingPercent = 0;

    @PostConstruct
    public void init() {
        AppConfig.migrateOldData();
        loadAllData((message, percent) -> {
            this.loadingMessage = message;
            this.loadingPercent = percent;
            System.out.println("[" + percent + "%] " + message);
        }).join();
    }

    public String getLoadingMessage() { return loadingMessage; }
    public int getLoadingPercent() { return loadingPercent; }

    // Mapas de nombres en español
    private Map<String, String> apiNameToDisplayName = new ConcurrentHashMap<>();
    private Map<String, String> displayNameLowerToApiName = new ConcurrentHashMap<>();
    private Map<String, String> spanishTypeNames = new ConcurrentHashMap<>();
    private Map<String, String> moveSlugToSpanish = new ConcurrentHashMap<>();
    private Map<String, String> moveSpanishLowerToSlug = new ConcurrentHashMap<>();

    // Movimientos
    private final Map<String, Move> moveCache = new ConcurrentHashMap<>();
    private List<String> allMoveNames = null;

    // Habilidades
    private Map<String, String> abilitySlugToSpanish = new ConcurrentHashMap<>();
    private Map<String, String> abilitySpanishLowerToSlug = new ConcurrentHashMap<>();
    private Map<String, String> abilitySlugToDesc = new ConcurrentHashMap<>();
    private Map<String, String> abilitySlugToFlavorText = new ConcurrentHashMap<>();
    private List<String> allAbilityNames = null;
    private final Map<String, Ability> abilityCache = new ConcurrentHashMap<>();

    // Objetos (items)
    private Map<String, String> itemSlugToSpanish = new ConcurrentHashMap<>();
    private Map<String, String> itemSpanishLowerToSlug = new ConcurrentHashMap<>();
    private List<String> allItemNames = null;
    private final Map<String, Item> itemCache = new ConcurrentHashMap<>();

    // Aliases for common competitive names that don't match PokeAPI exactly
    private static final Map<String, String> NAME_ALIASES = Map.of(
            "urshifu", "urshifu-single-strike",
            "ogerpon-wellspring", "ogerpon-wellspring-mask",
            "ogerpon-hearthflame", "ogerpon-hearthflame-mask",
            "ogerpon-cornerstone", "ogerpon-cornerstone-mask"
    );

    public Pokemon getPokemon(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        Pokemon p = pokemonCache.get(lower);
        if (p != null) return p;
        String apiName = displayNameLowerToApiName.get(lower);
        if (apiName != null) return pokemonCache.get(apiName);
        String alias = NAME_ALIASES.get(lower);
        if (alias != null) return pokemonCache.get(alias);
        return null;
    }

    public List<String> getAllPokemonNames() {
        if (!apiNameToDisplayName.isEmpty()) {
            return new ArrayList<>(apiNameToDisplayName.values());
        }
        return pokemonCache.keySet().stream()
                .map(n -> n.substring(0, 1).toUpperCase() + n.substring(1))
                .collect(Collectors.toList());
    }

    public List<Pokemon> getAllPokemon() {
        return new ArrayList<>(pokemonCache.values());
    }

    public String getSpanishPokemonName(String apiName) {
        if (apiName == null) return "";
        String display = apiNameToDisplayName.get(apiName.toLowerCase().trim());
        if (display != null) return display;
        return apiName.substring(0, 1).toUpperCase() + apiName.substring(1);
    }

    public String getApiNameFromSpanish(String spanishName) {
        if (spanishName == null) return null;
        String lower = spanishName.toLowerCase().trim();
        for (Map.Entry<String, String> entry : apiNameToDisplayName.entrySet()) {
            if (entry.getValue().toLowerCase().equals(lower)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getSpanishTypeName(String englishType) {
        if (englishType == null) return "";
        String spanish = spanishTypeNames.get(englishType.toLowerCase());
        if (spanish != null) return spanish.toUpperCase();
        return englishType.toUpperCase();
    }

    public String getMoveSpanishName(String slug) {
        if (slug == null) return "";
        String lower = slug.toLowerCase().trim();
        String spanish = moveSlugToSpanish.get(lower);
        if (spanish != null) return spanish;
        Move m = moveCache.get(lower);
        if (m != null && m.names != null) {
            for (Move.MoveName mn : m.names) {
                if (mn.language != null && "es".equals(mn.language.name)) {
                    return mn.name;
                }
            }
        }
        return slug.substring(0, 1).toUpperCase() + slug.substring(1).replace("-", " ");
    }

    public String getMoveSlug(String spanishName) {
        if (spanishName == null) return "";
        String slug = moveSpanishLowerToSlug.get(spanishName.toLowerCase().trim());
        if (slug != null) return slug;
        return spanishName.toLowerCase().trim().replace(" ", "-");
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public String getTypeJson(String type) {
        return typeCache.get(type);
    }

    // --- HABILIDADES ---
    public String getAbilitySpanishName(String slug) {
        if (slug == null) return "";
        String spanish = abilitySlugToSpanish.get(slug.toLowerCase().trim());
        if (spanish != null) return spanish;
        return slug.substring(0, 1).toUpperCase() + slug.substring(1).replace("-", " ");
    }

    public String getAbilitySlug(String spanishName) {
        if (spanishName == null) return "";
        String slug = abilitySpanishLowerToSlug.get(spanishName.toLowerCase().trim());
        if (slug != null) return slug;
        return spanishName.toLowerCase().trim().replace(" ", "-");
    }

    public String getAbilityDescription(String slug) {
        if (slug == null) return "";
        return abilitySlugToDesc.getOrDefault(slug.toLowerCase().trim(), "");
    }

    public String getAbilityFlavorText(String slug) {
        if (slug == null) return "";
        return abilitySlugToFlavorText.getOrDefault(slug.toLowerCase().trim(), "");
    }

    public List<String> getAllAbilityNamesCached() {
        return allAbilityNames != null ? allAbilityNames : List.of();
    }

    public Ability getAbilityCached(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        Ability a = abilityCache.get(lower.replace(" ", "-"));
        if (a != null) return a;
        String slug = abilitySpanishLowerToSlug.get(lower);
        if (slug != null) return abilityCache.get(slug);
        return null;
    }

    public CompletableFuture<Ability> searchAbility(String name) {
        String cleanName = name.toLowerCase().trim().replace(" ", "-");
        String slug = abilitySpanishLowerToSlug.get(name.toLowerCase().trim());
        if (slug != null) cleanName = slug;

        final String lookupName = cleanName;
        Ability cached = abilityCache.get(lookupName);
        if (cached != null && cached.pokemon != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://pokeapi.co/api/v2/ability/" + lookupName))
                        .GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                    Ability ability = new Ability();
                    ability.id = obj.get("id").getAsInt();
                    ability.name = obj.get("name").getAsString();
                    ability.spanishName = getAbilitySpanishName(lookupName);
                    ability.spanishEffect = getAbilityDescription(lookupName);
                    ability.spanishFlavorText = getAbilityFlavorText(lookupName);

                    if (obj.has("generation") && !obj.get("generation").isJsonNull()) {
                        ability.generation = new Ability.GenerationInfo();
                        ability.generation.name = obj.getAsJsonObject("generation").get("name").getAsString();
                    }
                    if (obj.has("pokemon")) {
                        ability.pokemon = new java.util.ArrayList<>();
                        for (JsonElement el : obj.getAsJsonArray("pokemon")) {
                            JsonObject pe = el.getAsJsonObject();
                            Ability.PokemonEntry entry = new Ability.PokemonEntry();
                            entry.is_hidden = pe.get("is_hidden").getAsBoolean();
                            entry.slot = pe.get("slot").getAsInt();
                            entry.pokemon = new Ability.PokemonRef();
                            entry.pokemon.name = pe.getAsJsonObject("pokemon").get("name").getAsString();
                            entry.pokemon.url = pe.getAsJsonObject("pokemon").get("url").getAsString();
                            ability.pokemon.add(entry);
                        }
                    }
                    // Update Spanish fields from API if missing
                    if ((ability.spanishEffect == null || ability.spanishEffect.isEmpty()) && obj.has("effect_entries")) {
                        for (JsonElement el : obj.getAsJsonArray("effect_entries")) {
                            JsonObject ee = el.getAsJsonObject();
                            if ("es".equals(ee.getAsJsonObject("language").get("name").getAsString())) {
                                ability.spanishEffect = ee.get("short_effect").getAsString();
                                break;
                            }
                        }
                    }
                    if ((ability.spanishFlavorText == null || ability.spanishFlavorText.isEmpty()) && obj.has("flavor_text_entries")) {
                        for (JsonElement el : obj.getAsJsonArray("flavor_text_entries")) {
                            JsonObject ft = el.getAsJsonObject();
                            if ("es".equals(ft.getAsJsonObject("language").get("name").getAsString())) {
                                ability.spanishFlavorText = ft.get("flavor_text").getAsString();
                                break;
                            }
                        }
                    }
                    abilityCache.put(lookupName, ability);
                    return ability;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    // --- OBJETOS (ITEMS) ---

    private static final Map<String, String> ITEM_FALLBACK_DESC = Map.ofEntries(
        Map.entry("booster-energy", "Si lo lleva un Pokémon con Carga Cuark o Paleosíntesis, potencia su mejor estadística sin necesidad de clima o campo."),
        Map.entry("clear-amulet", "Protege al portador de que sus estadísticas sean reducidas por movimientos o habilidades rivales."),
        Map.entry("covert-cloak", "Protege al portador de los efectos secundarios de los movimientos del rival."),
        Map.entry("loaded-dice", "Hace que los movimientos multiimpacto golpeen más veces (mínimo 4)."),
        Map.entry("mirror-herb", "Copa una vez las subidas de estadísticas del rival y se consume."),
        Map.entry("punching-glove", "Potencia los movimientos de puñetazo un 10% y evita el contacto."),
        Map.entry("ability-shield", "Protege la habilidad del portador de ser cambiada o anulada.")
    );

    private void initItemNames() {
        // Competitive items from SmogonService.translateItem()
        Map<String, String> competitive = new LinkedHashMap<>();
        competitive.put("leftovers", "Restos");
        competitive.put("choice-band", "Cinta Elegida");
        competitive.put("choice-specs", "Gafas Elegidas");
        competitive.put("choice-scarf", "Pañuelo Elegido");
        competitive.put("life-orb", "Vidasfera");
        competitive.put("heavy-duty-boots", "Botas Gruesas");
        competitive.put("rocky-helmet", "Casco Dentado");
        competitive.put("assault-vest", "Chaleco Asalto");
        competitive.put("focus-sash", "Bandana");
        competitive.put("eviolite", "Piedra Evolutiva");
        competitive.put("black-sludge", "Lodo Negro");
        competitive.put("toxic-orb", "Orbe Tóxico");
        competitive.put("flame-orb", "Orbe Llama");
        competitive.put("lum-berry", "Baya Lum");
        competitive.put("sitrus-berry", "Baya Zidra");
        competitive.put("booster-energy", "Energía Potenciadora");
        competitive.put("clear-amulet", "Amuleto Nítido");
        competitive.put("covert-cloak", "Capa Furtiva");
        competitive.put("loaded-dice", "Dado Trucado");
        competitive.put("red-card", "Tarjeta Roja");
        competitive.put("shed-shell", "Muda Concha");
        competitive.put("air-balloon", "Globo Helio");
        competitive.put("expert-belt", "Cinto Maestro");
        competitive.put("weakness-policy", "Seguro Debilidad");
        competitive.put("light-clay", "Refleluz");
        competitive.put("mental-herb", "Hierba Mental");
        competitive.put("power-herb", "Hierba Ímpetu");
        competitive.put("white-herb", "Hierba Blanca");
        competitive.put("mirror-herb", "Hierba Espejo");
        competitive.put("throat-spray", "Spray Bucal");
        competitive.put("terrain-extender", "Extensión Campo");
        competitive.put("protective-pads", "Parche Blindado");
        competitive.put("safety-goggles", "Gafas Protectoras");
        for (var e : competitive.entrySet()) {
            itemSlugToSpanish.putIfAbsent(e.getKey(), e.getValue());
            itemSpanishLowerToSlug.putIfAbsent(e.getValue().toLowerCase(), e.getKey());
        }
        allItemNames = new ArrayList<>(itemSlugToSpanish.values());
    }

    public String getItemSpanishName(String slug) {
        if (slug == null) return "";
        String spanish = itemSlugToSpanish.get(slug.toLowerCase().trim());
        if (spanish != null) return spanish;
        return slug.substring(0, 1).toUpperCase() + slug.substring(1).replace("-", " ");
    }

    public List<String> getAllItemNamesCached() {
        return allItemNames != null ? allItemNames : List.of();
    }

    public Item getItemCached(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        Item i = itemCache.get(lower.replace(" ", "-"));
        if (i != null) return i;
        String slug = itemSpanishLowerToSlug.get(lower);
        if (slug != null) return itemCache.get(slug);
        return null;
    }

    public String getItemSlugFromName(String name) {
        if (name == null) return name;
        String lower = name.toLowerCase().trim();
        String slug = itemSpanishLowerToSlug.get(lower);
        if (slug != null) return slug;
        return lower.replace(" ", "-");
    }

    public CompletableFuture<Item> searchItem(String name) {
        String cleanName = name.toLowerCase().trim().replace(" ", "-");
        String slug = itemSpanishLowerToSlug.get(name.toLowerCase().trim());
        if (slug != null) cleanName = slug;

        final String lookupName = cleanName;
        Item cached = itemCache.get(lookupName);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://pokeapi.co/api/v2/item/" + lookupName))
                        .GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                    Item item = new Item();
                    item.id = obj.get("id").getAsInt();
                    item.name = obj.get("name").getAsString();
                    item.cost = obj.has("cost") ? obj.get("cost").getAsInt() : 0;

                    // Spanish name
                    String esName = getItemSpanishName(lookupName);
                    if (obj.has("names")) {
                        for (JsonElement el : obj.getAsJsonArray("names")) {
                            JsonObject nameObj = el.getAsJsonObject();
                            if ("es".equals(nameObj.getAsJsonObject("language").get("name").getAsString())) {
                                esName = nameObj.get("name").getAsString();
                                break;
                            }
                        }
                    }
                    item.spanishName = esName;

                    // Category
                    if (obj.has("category") && !obj.get("category").isJsonNull()) {
                        item.category = obj.getAsJsonObject("category").get("name").getAsString();
                    }

                    // Sprite
                    if (obj.has("sprites") && !obj.get("sprites").isJsonNull()) {
                        JsonObject sprites = obj.getAsJsonObject("sprites");
                        if (sprites.has("default") && !sprites.get("default").isJsonNull()) {
                            item.spriteUrl = sprites.get("default").getAsString();
                        }
                    }

                    // Spanish effect
                    if (obj.has("effect_entries")) {
                        for (JsonElement el : obj.getAsJsonArray("effect_entries")) {
                            JsonObject ee = el.getAsJsonObject();
                            if ("es".equals(ee.getAsJsonObject("language").get("name").getAsString())) {
                                item.spanishEffect = ee.has("short_effect") ? ee.get("short_effect").getAsString() : "";
                                break;
                            }
                        }
                        if (item.spanishEffect == null || item.spanishEffect.isEmpty()) {
                            for (JsonElement el : obj.getAsJsonArray("effect_entries")) {
                                JsonObject ee = el.getAsJsonObject();
                                if ("en".equals(ee.getAsJsonObject("language").get("name").getAsString())) {
                                    item.spanishEffect = ee.has("short_effect") ? ee.get("short_effect").getAsString() : "";
                                    break;
                                }
                            }
                        }
                    }

                    // Spanish flavor text (take LAST entry = most recent game version)
                    if (obj.has("flavor_text_entries")) {
                        String esFlavor = null;
                        String enFlavor = null;
                        for (JsonElement el : obj.getAsJsonArray("flavor_text_entries")) {
                            JsonObject ft = el.getAsJsonObject();
                            String lang = ft.getAsJsonObject("language").get("name").getAsString();
                            if ("es".equals(lang)) {
                                esFlavor = ft.get("text").getAsString();
                            } else if ("en".equals(lang)) {
                                enFlavor = ft.get("text").getAsString();
                            }
                        }
                        item.spanishFlavorText = (esFlavor != null && !esFlavor.isEmpty()) ? esFlavor : enFlavor;
                    }

                    // Held by pokemon
                    if (obj.has("held_by_pokemon") && obj.get("held_by_pokemon").isJsonArray()) {
                        item.heldByPokemon = new ArrayList<>();
                        for (JsonElement el : obj.getAsJsonArray("held_by_pokemon")) {
                            JsonObject hbp = el.getAsJsonObject();
                            if (hbp.has("pokemon") && hbp.get("pokemon").isJsonObject()) {
                                Item.HeldByPokemon entry = new Item.HeldByPokemon();
                                entry.pokemon = new Item.PokemonRef();
                                entry.pokemon.name = hbp.getAsJsonObject("pokemon").get("name").getAsString();
                                entry.pokemon.url = hbp.getAsJsonObject("pokemon").get("url").getAsString();
                                item.heldByPokemon.add(entry);
                            }
                        }
                    }

                    // Fallback descriptions for items PokeAPI doesn't have data for
                    if ((item.spanishFlavorText == null || item.spanishFlavorText.isEmpty())
                            && (item.spanishEffect == null || item.spanishEffect.isEmpty())) {
                        String fallback = ITEM_FALLBACK_DESC.get(lookupName);
                        if (fallback != null) item.spanishFlavorText = fallback;
                    }

                    // Update maps
                    itemSlugToSpanish.put(lookupName, item.spanishName);
                    itemSpanishLowerToSlug.put(item.spanishName.toLowerCase(), lookupName);
                    allItemNames = new ArrayList<>(itemSlugToSpanish.values());
                    itemCache.put(lookupName, item);
                    return item;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    // CARGAR TODO: cache o descarga
    public CompletableFuture<Void> loadAllData(ProgressCallback callback) {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(cacheDir);

                Path versionFile = cacheDir.resolve("cache_version.txt");
                Path pokemonCacheFile = cacheDir.resolve("pokemon_cache.json");
                Path typeCacheFile = cacheDir.resolve("type_cache.json");
                Path speciesCacheFile = cacheDir.resolve("species_names_cache.json");
                Path moveCacheFile = cacheDir.resolve("move_cache.json");
                Path abilityCacheFile = cacheDir.resolve("ability_cache.json");

                boolean cacheValid = Files.exists(versionFile)
                        && Files.readString(versionFile).trim().equals(CACHE_VERSION)
                        && Files.exists(pokemonCacheFile)
                        && Files.exists(typeCacheFile)
                        && Files.exists(speciesCacheFile)
                        && Files.exists(moveCacheFile)
                        && Files.exists(abilityCacheFile);

                if (cacheValid) {
                    callback.onProgress("Cargando desde cache...", 0);
                    loadFromCache(pokemonCacheFile, typeCacheFile, speciesCacheFile, moveCacheFile, abilityCacheFile, callback);
                } else {
                    callback.onProgress("Descargando datos por primera vez...", 0);
                    downloadAllData(pokemonCacheFile, typeCacheFile, speciesCacheFile, moveCacheFile, abilityCacheFile, versionFile, callback);
                }

                isLoaded = true;
                callback.onProgress("¡Listo!", 100);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onProgress("Error: " + e.getMessage(), 0);
            }
        });
    }

    private void loadFromCache(Path pokemonFile, Path typeFile, Path speciesFile, Path moveFile, Path abilityFile, ProgressCallback callback) throws IOException {
        callback.onProgress("Cargando Pokémon...", 15);
        String pokemonJson = Files.readString(pokemonFile);
        JsonObject pokemonObj = JsonParser.parseString(pokemonJson).getAsJsonObject();
        for (String name : pokemonObj.keySet()) {
            Pokemon p = gson.fromJson(pokemonObj.get(name), Pokemon.class);
            pokemonCache.put(name, p);
        }

        callback.onProgress("Cargando tipos...", 35);
        String typeJson = Files.readString(typeFile);
        JsonObject typeObj = JsonParser.parseString(typeJson).getAsJsonObject();
        for (String type : typeObj.keySet()) {
            typeCache.put(type, typeObj.get(type).getAsString());
        }
        parseSpanishTypeNames();

        callback.onProgress("Cargando nombres en español...", 55);
        String speciesJson = Files.readString(speciesFile);
        JsonObject speciesObj = JsonParser.parseString(speciesJson).getAsJsonObject();
        for (String apiName : speciesObj.keySet()) {
            String spanishName = speciesObj.get(apiName).getAsString();
            apiNameToDisplayName.put(apiName, spanishName);
            displayNameLowerToApiName.put(spanishName.toLowerCase(), apiName);
        }
        for (String apiName : pokemonCache.keySet()) {
            if (!apiNameToDisplayName.containsKey(apiName)) {
                String display = apiName.substring(0, 1).toUpperCase() + apiName.substring(1);
                apiNameToDisplayName.put(apiName, display);
                displayNameLowerToApiName.putIfAbsent(display.toLowerCase(), apiName);
            }
        }

        callback.onProgress("Cargando movimientos...", 75);
        String moveJson = Files.readString(moveFile);
        JsonObject moveObj = JsonParser.parseString(moveJson).getAsJsonObject();
        for (String slug : moveObj.keySet()) {
            JsonObject md = moveObj.getAsJsonObject(slug);
            String spanishName = md.has("es") ? md.get("es").getAsString() : slug;
            moveSlugToSpanish.put(slug, spanishName);
            moveSpanishLowerToSlug.put(spanishName.toLowerCase(), slug);

            Move move = new Move();
            move.name = slug;
            move.id = md.has("id") ? md.get("id").getAsInt() : 0;
            move.power = md.has("power") ? md.get("power").getAsInt() : 0;
            move.pp = md.has("pp") ? md.get("pp").getAsInt() : 0;
            move.accuracy = md.has("accuracy") ? md.get("accuracy").getAsInt() : 0;
            move.priority = md.has("priority") ? md.get("priority").getAsInt() : 0;
            if (md.has("type")) {
                move.type = new Move.MoveType();
                move.type.name = md.get("type").getAsString();
            }
            if (md.has("damageClass")) {
                move.damage_class = new Move.DamageClass();
                move.damage_class.name = md.get("damageClass").getAsString();
            }
            if (md.has("generation")) {
                move.generation = new Move.Generation();
                move.generation.name = md.get("generation").getAsString();
            }
            moveCache.put(slug, move);
        }
        allMoveNames = new ArrayList<>(moveSlugToSpanish.values());

        // Cargar habilidades
        callback.onProgress("Cargando habilidades...", 85);
        String abilityJson = Files.readString(abilityFile);
        JsonObject abilityObj = JsonParser.parseString(abilityJson).getAsJsonObject();
        for (String slug : abilityObj.keySet()) {
            JsonObject ad = abilityObj.getAsJsonObject(slug);
            String spanishName = ad.has("es") ? ad.get("es").getAsString() : slug;
            abilitySlugToSpanish.put(slug, spanishName);
            abilitySpanishLowerToSlug.put(spanishName.toLowerCase(), slug);
            if (ad.has("desc")) abilitySlugToDesc.put(slug, ad.get("desc").getAsString());
            if (ad.has("flavor")) abilitySlugToFlavorText.put(slug, ad.get("flavor").getAsString());

            Ability ability = new Ability();
            ability.name = slug;
            ability.id = ad.has("id") ? ad.get("id").getAsInt() : 0;
            ability.spanishName = spanishName;
            ability.spanishEffect = ad.has("desc") ? ad.get("desc").getAsString() : "";
            ability.spanishFlavorText = ad.has("flavor") ? ad.get("flavor").getAsString() : "";
            if (ad.has("generation")) {
                ability.generation = new Ability.GenerationInfo();
                ability.generation.name = ad.get("generation").getAsString();
            }
            if (ad.has("pokemon") && ad.get("pokemon").isJsonArray()) {
                ability.pokemon = new ArrayList<>();
                for (JsonElement pe : ad.getAsJsonArray("pokemon")) {
                    JsonObject peObj = pe.getAsJsonObject();
                    Ability.PokemonEntry entry = new Ability.PokemonEntry();
                    entry.is_hidden = peObj.has("is_hidden") && peObj.get("is_hidden").getAsBoolean();
                    entry.slot = peObj.has("slot") ? peObj.get("slot").getAsInt() : 0;
                    if (peObj.has("pokemon") && peObj.get("pokemon").isJsonObject()) {
                        entry.pokemon = new Ability.PokemonRef();
                        entry.pokemon.name = peObj.getAsJsonObject("pokemon").get("name").getAsString();
                        entry.pokemon.url = peObj.getAsJsonObject("pokemon").has("url") ? peObj.getAsJsonObject("pokemon").get("url").getAsString() : "";
                    }
                    ability.pokemon.add(entry);
                }
            }
            abilityCache.put(slug, ability);
        }
        allAbilityNames = new ArrayList<>(abilitySlugToSpanish.values());

        // Inicializar nombres de objetos competitivos
        initItemNames();
    }

    private void downloadAllData(Path pokemonFile, Path typeFile, Path speciesFile, Path moveFile, Path abilityFile, Path versionFile, ProgressCallback callback) throws Exception {
        // Paso 1: Descargar todos los Pokémon (1-1025 + formas 10001-10325)
        callback.onProgress("Obteniendo lista de Pokémon...", 2);
        List<Integer> allIds = new ArrayList<>();
        for (int i = 1; i <= 1025; i++) allIds.add(i);
        for (int i = 10001; i <= 10325; i++) allIds.add(i);
        int totalPokemon = allIds.size();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        final int[] downloaded = {0};

        for (int id : allIds) {
            final int currentId = id;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://pokeapi.co/api/v2/pokemon/" + currentId))
                            .GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        Pokemon p = gson.fromJson(response.body(), Pokemon.class);
                        pokemonCache.put(p.name, p);
                    }
                    synchronized (downloaded) {
                        downloaded[0]++;
                        int progress = 2 + (int) (23.0 * downloaded[0] / totalPokemon);
                        callback.onProgress("Descargando Pokémon " + downloaded[0] + "/" + totalPokemon, progress);
                    }
                } catch (Exception e) { /* ignorar */ }
            }));
            if (futures.size() >= 50) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                futures.clear();
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.clear();

        // Paso 2: Descargar nombres en español (pokemon-species, solo 1-1025)
        callback.onProgress("Descargando nombres en español...", 25);
        downloaded[0] = 0;
        int totalSpecies = 1025;
        JsonObject speciesObj = new JsonObject();

        for (int id = 1; id <= 1025; id++) {
            final int currentId = id;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://pokeapi.co/api/v2/pokemon-species/" + currentId))
                            .GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
                        String apiName = obj.get("name").getAsString();
                        JsonArray names = obj.getAsJsonArray("names");
                        String spanishName = apiName;
                        for (JsonElement el : names) {
                            JsonObject nameObj = el.getAsJsonObject();
                            if ("es".equals(nameObj.getAsJsonObject("language").get("name").getAsString())) {
                                spanishName = nameObj.get("name").getAsString();
                                break;
                            }
                        }
                        synchronized (speciesObj) {
                            speciesObj.addProperty(apiName, spanishName);
                        }
                        apiNameToDisplayName.put(apiName, spanishName);
                        displayNameLowerToApiName.put(spanishName.toLowerCase(), apiName);
                    }
                    synchronized (downloaded) {
                        downloaded[0]++;
                        int progress = 25 + (int) (12.0 * downloaded[0] / totalSpecies);
                        callback.onProgress("Nombres en español " + downloaded[0] + "/" + totalSpecies, progress);
                    }
                } catch (Exception e) { /* ignorar */ }
            }));
            if (futures.size() >= 50) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                futures.clear();
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.clear();

        // Pokémon sin nombre español (formas alternas)
        for (String apiName : pokemonCache.keySet()) {
            if (!apiNameToDisplayName.containsKey(apiName)) {
                String display = apiName.substring(0, 1).toUpperCase() + apiName.substring(1);
                apiNameToDisplayName.put(apiName, display);
                displayNameLowerToApiName.putIfAbsent(display.toLowerCase(), apiName);
            }
        }

        // Paso 3: Descargar tipos
        callback.onProgress("Descargando tipos...", 37);
        String[] types = {"normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison",
                "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"};
        for (String type : types) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://pokeapi.co/api/v2/type/" + type))
                            .GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    typeCache.put(type, response.body());
                } catch (Exception e) { /* ignorar */ }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.clear();
        parseSpanishTypeNames();

        // Paso 4: Descargar todos los movimientos
        callback.onProgress("Obteniendo lista de movimientos...", 40);
        HttpRequest moveListReq = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/move?limit=1000"))
                .GET().build();
        HttpResponse<String> moveListResp = client.send(moveListReq, HttpResponse.BodyHandlers.ofString());
        List<String> moveSlugs = new ArrayList<>();
        if (moveListResp.statusCode() == 200) {
            JsonObject listObj = JsonParser.parseString(moveListResp.body()).getAsJsonObject();
            for (JsonElement entry : listObj.getAsJsonArray("results")) {
                moveSlugs.add(entry.getAsJsonObject().get("name").getAsString());
            }
        }

        callback.onProgress("Descargando movimientos...", 42);
        downloaded[0] = 0;
        int totalMoves = moveSlugs.size();
        JsonObject moveDataObj = new JsonObject();

        for (String slug : moveSlugs) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://pokeapi.co/api/v2/move/" + slug))
                            .GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonObject fullMove = JsonParser.parseString(response.body()).getAsJsonObject();
                        String spanishName = slug;
                        if (fullMove.has("names")) {
                            for (JsonElement el : fullMove.getAsJsonArray("names")) {
                                JsonObject nameObj = el.getAsJsonObject();
                                if ("es".equals(nameObj.getAsJsonObject("language").get("name").getAsString())) {
                                    spanishName = nameObj.get("name").getAsString();
                                    break;
                                }
                            }
                        }
                        JsonObject md = new JsonObject();
                        md.addProperty("es", spanishName);
                        md.addProperty("id", fullMove.has("id") ? fullMove.get("id").getAsInt() : 0);
                        md.addProperty("power", fullMove.has("power") && !fullMove.get("power").isJsonNull() ? fullMove.get("power").getAsInt() : 0);
                        md.addProperty("pp", fullMove.has("pp") && !fullMove.get("pp").isJsonNull() ? fullMove.get("pp").getAsInt() : 0);
                        md.addProperty("accuracy", fullMove.has("accuracy") && !fullMove.get("accuracy").isJsonNull() ? fullMove.get("accuracy").getAsInt() : 0);
                        md.addProperty("priority", fullMove.has("priority") && !fullMove.get("priority").isJsonNull() ? fullMove.get("priority").getAsInt() : 0);
                        if (fullMove.has("type") && !fullMove.get("type").isJsonNull()) {
                            md.addProperty("type", fullMove.getAsJsonObject("type").get("name").getAsString());
                        }
                        if (fullMove.has("damage_class") && !fullMove.get("damage_class").isJsonNull()) {
                            md.addProperty("damageClass", fullMove.getAsJsonObject("damage_class").get("name").getAsString());
                        }
                        if (fullMove.has("generation") && !fullMove.get("generation").isJsonNull()) {
                            md.addProperty("generation", fullMove.getAsJsonObject("generation").get("name").getAsString());
                        }
                        synchronized (moveDataObj) {
                            moveDataObj.add(slug, md);
                        }
                        moveSlugToSpanish.put(slug, spanishName);
                        moveSpanishLowerToSlug.put(spanishName.toLowerCase(), slug);
                    }
                    synchronized (downloaded) {
                        downloaded[0]++;
                        int progress = 42 + (int) (33.0 * downloaded[0] / totalMoves);
                        callback.onProgress("Descargando movimientos " + downloaded[0] + "/" + totalMoves, progress);
                    }
                } catch (Exception e) { /* ignorar */ }
            }));
            if (futures.size() >= 50) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                futures.clear();
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.clear();

        allMoveNames = new ArrayList<>(moveSlugToSpanish.values());

        // Paso 5: Descargar todas las habilidades
        callback.onProgress("Obteniendo lista de habilidades...", 75);
        HttpRequest abilityListReq = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/ability?limit=400"))
                .GET().build();
        HttpResponse<String> abilityListResp = client.send(abilityListReq, HttpResponse.BodyHandlers.ofString());
        List<String> abilitySlugs = new ArrayList<>();
        if (abilityListResp.statusCode() == 200) {
            JsonObject listObj = JsonParser.parseString(abilityListResp.body()).getAsJsonObject();
            for (JsonElement entry : listObj.getAsJsonArray("results")) {
                abilitySlugs.add(entry.getAsJsonObject().get("name").getAsString());
            }
        }

        callback.onProgress("Descargando habilidades...", 76);
        downloaded[0] = 0;
        int totalAbilities = abilitySlugs.size();
        JsonObject abilityDataObj = new JsonObject();

        for (String slug : abilitySlugs) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://pokeapi.co/api/v2/ability/" + slug))
                            .GET().build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonObject fullAbility = JsonParser.parseString(response.body()).getAsJsonObject();
                        String spanishName = slug;
                        if (fullAbility.has("names")) {
                            for (JsonElement el : fullAbility.getAsJsonArray("names")) {
                                JsonObject nameObj = el.getAsJsonObject();
                                if ("es".equals(nameObj.getAsJsonObject("language").get("name").getAsString())) {
                                    spanishName = nameObj.get("name").getAsString();
                                    break;
                                }
                            }
                        }
                        String desc = "";
                        if (fullAbility.has("flavor_text_entries")) {
                            for (JsonElement el : fullAbility.getAsJsonArray("flavor_text_entries")) {
                                JsonObject ft = el.getAsJsonObject();
                                if ("es".equals(ft.getAsJsonObject("language").get("name").getAsString())) {
                                    desc = ft.get("flavor_text").getAsString();
                                    break;
                                }
                            }
                        }
                        String effect = "";
                        if (fullAbility.has("effect_entries")) {
                            for (JsonElement el : fullAbility.getAsJsonArray("effect_entries")) {
                                JsonObject ee = el.getAsJsonObject();
                                if ("es".equals(ee.getAsJsonObject("language").get("name").getAsString())) {
                                    effect = ee.has("short_effect") ? ee.get("short_effect").getAsString() : "";
                                    break;
                                }
                            }
                        }
                        if (effect.isEmpty() && fullAbility.has("effect_entries")) {
                            for (JsonElement el : fullAbility.getAsJsonArray("effect_entries")) {
                                JsonObject ee = el.getAsJsonObject();
                                if ("en".equals(ee.getAsJsonObject("language").get("name").getAsString())) {
                                    effect = ee.has("short_effect") ? ee.get("short_effect").getAsString() : "";
                                    break;
                                }
                            }
                        }
                        String flavor = desc.isEmpty() ? effect : desc;
                        JsonObject ad = new JsonObject();
                        ad.addProperty("es", spanishName);
                        ad.addProperty("id", fullAbility.has("id") ? fullAbility.get("id").getAsInt() : 0);
                        ad.addProperty("desc", effect);
                        ad.addProperty("flavor", flavor);
                        if (fullAbility.has("generation") && !fullAbility.get("generation").isJsonNull()) {
                            ad.addProperty("generation", fullAbility.getAsJsonObject("generation").get("name").getAsString());
                        }
                        if (fullAbility.has("pokemon")) {
                            ad.add("pokemon", fullAbility.getAsJsonArray("pokemon"));
                        }
                        synchronized (abilityDataObj) {
                            abilityDataObj.add(slug, ad);
                        }
                        abilitySlugToSpanish.put(slug, spanishName);
                        abilitySpanishLowerToSlug.put(spanishName.toLowerCase(), slug);
                        if (!effect.isEmpty()) abilitySlugToDesc.put(slug, effect);
                        if (!flavor.isEmpty()) abilitySlugToFlavorText.put(slug, flavor);
                    }
                    synchronized (downloaded) {
                        downloaded[0]++;
                        int progress = 76 + (int) (14.0 * downloaded[0] / totalAbilities);
                        callback.onProgress("Descargando habilidades " + downloaded[0] + "/" + totalAbilities, progress);
                    }
                } catch (Exception e) { /* ignorar */ }
            }));
            if (futures.size() >= 50) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                futures.clear();
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.clear();

        allAbilityNames = new ArrayList<>(abilitySlugToSpanish.values());

        // Inicializar nombres de objetos competitivos
        initItemNames();

        // Paso 6: Guardar en cache
        callback.onProgress("Guardando en cache...", 92);
        saveToCache(pokemonFile, typeFile);
        Files.writeString(speciesFile, gson.toJson(speciesObj));
        Files.writeString(moveFile, gson.toJson(moveDataObj));
        Files.writeString(abilityFile, gson.toJson(abilityDataObj));
        Files.writeString(versionFile, CACHE_VERSION);

        // Construir moveCache con datos básicos
        for (String slug : moveDataObj.keySet()) {
            JsonObject md = moveDataObj.getAsJsonObject(slug);
            Move move = new Move();
            move.name = slug;
            move.id = md.has("id") ? md.get("id").getAsInt() : 0;
            move.power = md.has("power") ? md.get("power").getAsInt() : 0;
            move.pp = md.has("pp") ? md.get("pp").getAsInt() : 0;
            move.accuracy = md.has("accuracy") ? md.get("accuracy").getAsInt() : 0;
            move.priority = md.has("priority") ? md.get("priority").getAsInt() : 0;
            if (md.has("type")) {
                move.type = new Move.MoveType();
                move.type.name = md.get("type").getAsString();
            }
            if (md.has("damageClass")) {
                move.damage_class = new Move.DamageClass();
                move.damage_class.name = md.get("damageClass").getAsString();
            }
            if (md.has("generation")) {
                move.generation = new Move.Generation();
                move.generation.name = md.get("generation").getAsString();
            }
            moveCache.put(slug, move);
        }

        // Construir abilityCache con datos básicos
        for (String slug : abilityDataObj.keySet()) {
            JsonObject ad = abilityDataObj.getAsJsonObject(slug);
            Ability ability = new Ability();
            ability.name = slug;
            ability.id = ad.has("id") ? ad.get("id").getAsInt() : 0;
            ability.spanishName = ad.has("es") ? ad.get("es").getAsString() : slug;
            ability.spanishShortEffect = ad.has("desc") ? ad.get("desc").getAsString() : "";
            ability.spanishFlavorText = ad.has("flavor") ? ad.get("flavor").getAsString() : "";
            if (ad.has("generation")) {
                ability.generation = new Ability.GenerationInfo();
                ability.generation.name = ad.get("generation").getAsString();
            }
            if (ad.has("pokemon") && ad.get("pokemon").isJsonArray()) {
                ability.pokemon = new ArrayList<>();
                for (JsonElement pe : ad.getAsJsonArray("pokemon")) {
                    JsonObject peObj = pe.getAsJsonObject();
                    Ability.PokemonEntry entry = new Ability.PokemonEntry();
                    entry.is_hidden = peObj.has("is_hidden") && peObj.get("is_hidden").getAsBoolean();
                    entry.slot = peObj.has("slot") ? peObj.get("slot").getAsInt() : 0;
                    if (peObj.has("pokemon") && peObj.get("pokemon").isJsonObject()) {
                        entry.pokemon = new Ability.PokemonRef();
                        entry.pokemon.name = peObj.getAsJsonObject("pokemon").get("name").getAsString();
                        entry.pokemon.url = peObj.getAsJsonObject("pokemon").has("url") ? peObj.getAsJsonObject("pokemon").get("url").getAsString() : "";
                    }
                    ability.pokemon.add(entry);
                }
            }
            abilityCache.put(slug, ability);
        }
    }

    private void parseSpanishTypeNames() {
        for (String type : typeCache.keySet()) {
            try {
                String json = typeCache.get(type);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("names")) {
                    for (JsonElement el : obj.getAsJsonArray("names")) {
                        JsonObject nameObj = el.getAsJsonObject();
                        if ("es".equals(nameObj.getAsJsonObject("language").get("name").getAsString())) {
                            spanishTypeNames.put(type, nameObj.get("name").getAsString());
                            break;
                        }
                    }
                }
            } catch (Exception e) { /* ignorar */ }
        }
    }

    private void saveToCache(Path pokemonFile, Path typeFile) throws IOException {
        JsonObject pokemonObj = new JsonObject();
        for (Map.Entry<String, Pokemon> entry : pokemonCache.entrySet()) {
            pokemonObj.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
        }
        Files.writeString(pokemonFile, gson.toJson(pokemonObj));

        JsonObject typeObj = new JsonObject();
        for (Map.Entry<String, String> entry : typeCache.entrySet()) {
            typeObj.addProperty(entry.getKey(), entry.getValue());
        }
        Files.writeString(typeFile, gson.toJson(typeObj));
    }

    public interface ProgressCallback {
        void onProgress(String message, int percent);
    }

    // Buscar movimiento (acepta nombre en español o slug en inglés)
    public CompletableFuture<Move> searchMove(String name) {
        String cleanName = name.toLowerCase().trim().replace(" ", "-");
        // Resolver nombre español si aplica
        String slug = moveSpanishLowerToSlug.get(name.toLowerCase().trim());
        if (slug != null) cleanName = slug;

        final String lookupName = cleanName;
        Move cached = moveCache.get(lookupName);
        if (cached != null && cached.flavor_text_entries != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://pokeapi.co/api/v2/move/" + lookupName))
                        .GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Move move = gson.fromJson(response.body(), Move.class);
                    if (move != null) moveCache.put(lookupName, move);
                    return move;
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public Move getMoveCached(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        Move m = moveCache.get(lower.replace(" ", "-"));
        if (m != null) return m;
        String slug = moveSpanishLowerToSlug.get(lower);
        if (slug != null) return moveCache.get(slug);
        return null;
    }

    public CompletableFuture<List<String>> loadAllMoveNames() {
        if (allMoveNames != null) return CompletableFuture.completedFuture(allMoveNames);
        return CompletableFuture.completedFuture(List.of());
    }

    public List<String> getAllMoveNamesCached() {
        return allMoveNames != null ? allMoveNames : List.of();
    }

    // Cadena evolutiva
    private final Map<String, EvolutionChain> evolutionCache = new ConcurrentHashMap<>();
    // Species extra data cache
    private final Map<String, PokemonSpeciesData> speciesDataCache = new ConcurrentHashMap<>();
    private final Map<String, String> speciesJsonCache = new ConcurrentHashMap<>();

    /**
     * Resolves the base species name for a pokemon (handles forms like urshifu-single-strike-gmax -> urshifu).
     */
    public String getSpeciesName(String pokemonName) {
        if (pokemonName == null) return pokemonName;
        String lower = pokemonName.toLowerCase().trim();
        Pokemon p = pokemonCache.get(lower);
        if (p == null) {
            String apiName = displayNameLowerToApiName.get(lower);
            if (apiName != null) p = pokemonCache.get(apiName);
        }
        if (p != null && p.species != null && p.species.name != null) {
            return p.species.name;
        }
        return lower;
    }

    private String fetchSpeciesJson(String key) {
        String cached = speciesJsonCache.get(key);
        if (cached != null) return cached;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://pokeapi.co/api/v2/pokemon-species/" + key))
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                speciesJsonCache.put(key, resp.body());
                return resp.body();
            }
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    public CompletableFuture<EvolutionChain> getEvolutionChain(String pokemonName) {
        String key = getSpeciesName(pokemonName);
        EvolutionChain cached = evolutionCache.get(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String speciesJson = fetchSpeciesJson(key);
                if (speciesJson == null) return null;

                JsonObject speciesObj = JsonParser.parseString(speciesJson).getAsJsonObject();
                if (!speciesObj.has("evolution_chain") || speciesObj.get("evolution_chain").isJsonNull()) return null;
                String evoUrl = speciesObj.getAsJsonObject("evolution_chain").get("url").getAsString();

                HttpRequest evoReq = HttpRequest.newBuilder()
                        .uri(URI.create(evoUrl))
                        .GET().build();
                HttpResponse<String> evoResp = client.send(evoReq, HttpResponse.BodyHandlers.ofString());
                if (evoResp.statusCode() != 200) return null;

                EvolutionChain chain = gson.fromJson(evoResp.body(), EvolutionChain.class);
                if (chain != null) evolutionCache.put(key, chain);
                return chain;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public CompletableFuture<PokemonSpeciesData> getSpeciesData(String pokemonName) {
        String key = getSpeciesName(pokemonName);
        PokemonSpeciesData cached = speciesDataCache.get(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = fetchSpeciesJson(key);
                if (json == null) return null;

                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                PokemonSpeciesData data = new PokemonSpeciesData();

                // Flavor text (Spanish, fallback to English)
                if (obj.has("flavor_text_entries") && obj.get("flavor_text_entries").isJsonArray()) {
                    String esText = null, enText = null;
                    for (JsonElement el : obj.getAsJsonArray("flavor_text_entries")) {
                        JsonObject entry = el.getAsJsonObject();
                        String lang = entry.getAsJsonObject("language").get("name").getAsString();
                        if ("es".equals(lang) && esText == null) {
                            esText = entry.get("flavor_text").getAsString().replace("\n", " ").replace("\f", " ");
                        }
                        if ("en".equals(lang) && enText == null) {
                            enText = entry.get("flavor_text").getAsString().replace("\n", " ").replace("\f", " ");
                        }
                    }
                    data.description = esText != null ? esText : enText;
                }

                // Genus (category) - "Pokémon Semilla"
                if (obj.has("genera") && obj.get("genera").isJsonArray()) {
                    for (JsonElement el : obj.getAsJsonArray("genera")) {
                        JsonObject g = el.getAsJsonObject();
                        if ("es".equals(g.getAsJsonObject("language").get("name").getAsString())) {
                            data.genus = g.get("genus").getAsString();
                            break;
                        }
                    }
                    if (data.genus == null) {
                        for (JsonElement el : obj.getAsJsonArray("genera")) {
                            JsonObject g = el.getAsJsonObject();
                            if ("en".equals(g.getAsJsonObject("language").get("name").getAsString())) {
                                data.genus = g.get("genus").getAsString();
                                break;
                            }
                        }
                    }
                }

                // Simple fields
                data.captureRate = obj.has("capture_rate") ? obj.get("capture_rate").getAsInt() : -1;
                data.baseHappiness = obj.has("base_happiness") && !obj.get("base_happiness").isJsonNull()
                        ? obj.get("base_happiness").getAsInt() : -1;
                data.genderRate = obj.has("gender_rate") ? obj.get("gender_rate").getAsInt() : -1;
                data.hatchCounter = obj.has("hatch_counter") && !obj.get("hatch_counter").isJsonNull()
                        ? obj.get("hatch_counter").getAsInt() : -1;
                data.isLegendary = obj.has("is_legendary") && obj.get("is_legendary").getAsBoolean();
                data.isMythical = obj.has("is_mythical") && obj.get("is_mythical").getAsBoolean();
                data.isBaby = obj.has("is_baby") && obj.get("is_baby").getAsBoolean();

                // Habitat
                if (obj.has("habitat") && !obj.get("habitat").isJsonNull()) {
                    data.habitat = obj.getAsJsonObject("habitat").get("name").getAsString();
                }

                // Growth rate
                if (obj.has("growth_rate") && !obj.get("growth_rate").isJsonNull()) {
                    data.growthRate = obj.getAsJsonObject("growth_rate").get("name").getAsString();
                }

                // Color
                if (obj.has("color") && !obj.get("color").isJsonNull()) {
                    data.color = obj.getAsJsonObject("color").get("name").getAsString();
                }

                // Shape
                if (obj.has("shape") && !obj.get("shape").isJsonNull()) {
                    data.shape = obj.getAsJsonObject("shape").get("name").getAsString();
                }

                // Egg groups
                data.eggGroups = new java.util.ArrayList<>();
                if (obj.has("egg_groups") && obj.get("egg_groups").isJsonArray()) {
                    for (JsonElement el : obj.getAsJsonArray("egg_groups")) {
                        data.eggGroups.add(el.getAsJsonObject().get("name").getAsString());
                    }
                }

                // Game versions from pokedex_numbers -> version (more reliable from varieties -> game_indices on Pokemon obj)
                data.gameVersions = new java.util.ArrayList<>();
                // We get game appearances from the Pokemon object's game_indices instead
                // This is handled in the controller

                // Varieties (forms)
                data.varieties = new java.util.ArrayList<>();
                if (obj.has("varieties") && obj.get("varieties").isJsonArray()) {
                    for (JsonElement el : obj.getAsJsonArray("varieties")) {
                        JsonObject v = el.getAsJsonObject();
                        String vName = v.getAsJsonObject("pokemon").get("name").getAsString();
                        data.varieties.add(vName);
                    }
                }

                speciesDataCache.put(key, data);
                return data;
            } catch (Exception e) {
                return null;
            }
        });
    }

    // Game version translation
    public static String translateGameVersion(String version) {
        return switch (version.toLowerCase()) {
            case "red" -> "Rojo";
            case "blue" -> "Azul";
            case "yellow" -> "Amarillo";
            case "gold" -> "Oro";
            case "silver" -> "Plata";
            case "crystal" -> "Cristal";
            case "ruby" -> "Rubí";
            case "sapphire" -> "Zafiro";
            case "emerald" -> "Esmeralda";
            case "firered" -> "Rojo Fuego";
            case "leafgreen" -> "Verde Hoja";
            case "diamond" -> "Diamante";
            case "pearl" -> "Perla";
            case "platinum" -> "Platino";
            case "heartgold" -> "Oro HeartGold";
            case "soulsilver" -> "Plata SoulSilver";
            case "black" -> "Negro";
            case "white" -> "Blanco";
            case "black-2" -> "Negro 2";
            case "white-2" -> "Blanco 2";
            case "x" -> "X";
            case "y" -> "Y";
            case "omega-ruby" -> "Rubí Omega";
            case "alpha-sapphire" -> "Zafiro Alfa";
            case "sun" -> "Sol";
            case "moon" -> "Luna";
            case "ultra-sun" -> "Ultra Sol";
            case "ultra-moon" -> "Ultra Luna";
            case "lets-go-pikachu" -> "Let's Go Pikachu";
            case "lets-go-eevee" -> "Let's Go Eevee";
            case "sword" -> "Espada";
            case "shield" -> "Escudo";
            case "brilliant-diamond" -> "Diamante Brillante";
            case "shining-pearl" -> "Perla Reluciente";
            case "legends-arceus" -> "Leyendas Arceus";
            case "scarlet" -> "Escarlata";
            case "violet" -> "Púrpura";
            default -> capitalize(version.replace("-", " "));
        };
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
