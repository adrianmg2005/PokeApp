package org.example.pokeapp.controller;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.example.pokeapp.model.*;
import org.example.pokeapp.service.*;
import org.example.pokeapp.util.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final PokeApiService pokeApi;
    private final SmogonService smogon;
    private final SmogonSetGenerator setGenerator;
    private final ChampionsTeamService championsTeamService;
    private final Gson gson = new Gson();

    public ApiController(PokeApiService pokeApi, SmogonService smogon, SmogonSetGenerator setGenerator, ChampionsTeamService championsTeamService) {
        this.pokeApi = pokeApi;
        this.smogon = smogon;
        this.setGenerator = setGenerator;
        this.championsTeamService = championsTeamService;
    }

    // ── Status ──
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "loaded", pokeApi.isLoaded(),
                "message", pokeApi.getLoadingMessage(),
                "percent", pokeApi.getLoadingPercent()
        );
    }

    // ── Pokemon ──
    @GetMapping("/pokemon")
    public List<Map<String, Object>> allPokemon() {
        return pokeApi.getAllPokemon().stream()
                .filter(p -> p.id > 0 && p.id <= 1025)
                .sorted(Comparator.comparingInt(p -> p.id))
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.id);
                    m.put("name", p.name);
                    m.put("spanishName", pokeApi.getSpanishPokemonName(p.name));
                    m.put("types", p.types != null ? p.types.stream().map(t -> t.type.name).toList() : List.of());
                    // Include base stats for advanced filtering
                    Map<String, Integer> stats = new LinkedHashMap<>();
                    stats.put("hp", p.getStat("hp"));
                    stats.put("attack", p.getStat("attack"));
                    stats.put("defense", p.getStat("defense"));
                    stats.put("special-attack", p.getStat("special-attack"));
                    stats.put("special-defense", p.getStat("special-defense"));
                    stats.put("speed", p.getStat("speed"));
                    m.put("stats", stats);
                    m.put("totalStats", stats.values().stream().mapToInt(Integer::intValue).sum());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/pokemon/{name}")
    public Map<String, Object> getPokemon(@PathVariable String name) {
        String lower = name.toLowerCase().trim();
        // Try as apiName first, then as Spanish display name
        Pokemon p = pokeApi.getPokemon(lower);
        if (p == null) {
            String apiName = pokeApi.getApiNameFromSpanish(lower);
            if (apiName != null) p = pokeApi.getPokemon(apiName);
        }
        if (p == null) return Map.of("error", "Pokémon no encontrado");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", p.id);
        result.put("name", p.name);
        result.put("spanishName", pokeApi.getSpanishPokemonName(p.name));
        result.put("height", p.height);
        result.put("weight", p.weight);

        // Types
        List<String> types = p.types != null ? p.types.stream().map(t -> t.type.name).toList() : List.of();
        result.put("types", types);
        result.put("typesSpanish", types.stream().map(pokeApi::getSpanishTypeName).toList());

        // Stats
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("hp", p.getStat("hp"));
        stats.put("attack", p.getStat("attack"));
        stats.put("defense", p.getStat("defense"));
        stats.put("special-attack", p.getStat("special-attack"));
        stats.put("special-defense", p.getStat("special-defense"));
        stats.put("speed", p.getStat("speed"));
        result.put("stats", stats);
        result.put("totalStats", stats.values().stream().mapToInt(Integer::intValue).sum());

        // Abilities
        if (p.abilities != null) {
            result.put("abilities", p.abilities.stream().map(a -> {
                Map<String, Object> am = new LinkedHashMap<>();
                am.put("name", a.ability.name);
                am.put("spanishName", pokeApi.getAbilitySpanishName(a.ability.name));
                am.put("isHidden", a.is_hidden);
                am.put("description", pokeApi.getAbilityFlavorText(a.ability.name));
                return am;
            }).toList());
        }

        // Type effectiveness
        result.put("typeMatchup", calculateTypeMatchup(types));

        // Sprites
        String spriteBase = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";
        result.put("sprite", spriteBase + p.id + ".png");
        result.put("spriteShiny", spriteBase + "shiny/" + p.id + ".png");
        result.put("spriteArtwork", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + p.id + ".png");
        result.put("spriteArtworkShiny", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/" + p.id + ".png");

        // Animated sprites (Pokémon Showdown)
        String showdownName = p.name.toLowerCase().replaceAll("[^a-z0-9]+", "");
        result.put("spriteAnim", "https://play.pokemonshowdown.com/sprites/ani/" + showdownName + ".gif");
        result.put("spriteAnimShiny", "https://play.pokemonshowdown.com/sprites/ani-shiny/" + showdownName + ".gif");

        // Held items
        if (p.held_items != null && !p.held_items.isEmpty()) {
            result.put("heldItems", p.held_items.stream().map(h -> h.item.name).toList());
        }

        // Game versions
        if (p.game_indices != null) {
            result.put("games", p.game_indices.stream()
                    .map(g -> PokeApiService.translateGameVersion(g.version.name))
                    .distinct().toList());
        }

        return result;
    }

    @GetMapping("/pokemon/{name}/species")
    public Object getSpecies(@PathVariable String name) {
        Pokemon p = pokeApi.getPokemon(name.toLowerCase().trim());
        if (p == null) return Map.of("error", "No encontrado");
        try {
            PokemonSpeciesData data = pokeApi.getSpeciesData(p.name).join();
            if (data == null) return Map.of("error", "Sin datos de especie");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("description", data.description);
            result.put("genus", data.genus);
            if (data.habitat != null) result.put("habitat", TranslationUtils.translateHabitat(data.habitat));
            if (data.growthRate != null) result.put("growthRate", TranslationUtils.translateGrowthRate(data.growthRate));
            if (data.color != null) result.put("color", TranslationUtils.translateColor(data.color));
            if (data.shape != null) result.put("shape", TranslationUtils.translateShape(data.shape));
            result.put("captureRate", data.captureRate);
            result.put("baseHappiness", data.baseHappiness);
            result.put("genderRate", data.genderRate);
            result.put("hatchCounter", data.hatchCounter);
            result.put("isLegendary", data.isLegendary);
            result.put("isMythical", data.isMythical);
            result.put("isBaby", data.isBaby);
            result.put("eggGroups", data.eggGroups);
            if (data.varieties != null && data.varieties.size() > 1) {
                result.put("varieties", data.varieties.stream().map(v -> {
                    Map<String, Object> vm = new LinkedHashMap<>();
                    vm.put("name", v);
                    vm.put("spanishName", pokeApi.getSpanishPokemonName(v));
                    Pokemon vp = pokeApi.getPokemon(v);
                    vm.put("id", vp != null ? vp.id : 0);
                    return vm;
                }).toList());
            }
            return result;
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    @GetMapping("/pokemon/{name}/evolution")
    public Object getEvolution(@PathVariable String name) {
        Pokemon p = pokeApi.getPokemon(name.toLowerCase().trim());
        if (p == null) return Map.of("error", "No encontrado");
        try {
            EvolutionChain chain = pokeApi.getEvolutionChain(p.name).join();
            if (chain == null) return Map.of("error", "Sin cadena evolutiva");
            List<List<EvolutionChain.EvolutionStage>> paths = chain.getEvolutionPaths();
            return Map.of("paths", paths.stream().map(path ->
                    path.stream().map(stage -> {
                        Map<String, Object> sm = new LinkedHashMap<>();
                        sm.put("name", stage.name);
                        sm.put("id", stage.id);
                        sm.put("spanishName", pokeApi.getSpanishPokemonName(stage.name));
                        sm.put("method", stage.method);
                        return sm;
                    }).toList()
            ).toList());
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    // ── Smogon Sets ──
    @GetMapping("/pokemon/{name}/smogon")
    public Object getSmogonSets(@PathVariable String name) {
        Pokemon p = pokeApi.getPokemon(name.toLowerCase().trim());
        if (p == null) return Map.of("error", "No encontrado");

        Map<String, Object> result = new LinkedHashMap<>();
        List<SmogonService.SmogonResult> allGens = smogon.getAllGenSets(p.name);

        if (!allGens.isEmpty()) {
            result.put("source", "smogon");
            result.put("generations", allGens.stream().map(gr -> {
                Map<String, Object> gm = new LinkedHashMap<>();
                gm.put("gen", gr.generation);
                gm.put("genLabel", SmogonService.translateGen(gr.generation));
                gm.put("sets", gr.sets.stream().map(s -> formatSmogonSet(s)).toList());
                return gm;
            }).toList());
        } else {
            // Generate suggested sets
            List<SmogonSet> suggested = setGenerator.generateSuggestedSets(p);
            if (!suggested.isEmpty()) {
                result.put("source", "generated");
                result.put("sets", suggested.stream().map(this::formatSmogonSet).toList());
            } else {
                result.put("source", "none");
            }
        }
        return result;
    }

    private Map<String, Object> formatSmogonSet(SmogonSet set) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", set.setName);
        m.put("generation", set.generation);
        if (set.ability != null) m.put("ability", set.ability.stream()
                .map(a -> pokeApi.getAbilitySpanishName(a.toLowerCase().replace(" ", "-"))).toList());
        if (set.item != null) m.put("item", set.item.stream()
                .map(SmogonService::translateItem).toList());
        if (set.nature != null) m.put("nature", set.nature.stream()
                .map(SmogonService::translateNature).toList());
        if (set.moves != null) {
            m.put("moves", set.moves.stream().map(slot ->
                    slot.stream().map(mv -> {
                        Map<String, String> mvMap = new LinkedHashMap<>();
                        String slug = mv.toLowerCase().replace(" ", "-");
                        mvMap.put("slug", slug);
                        mvMap.put("name", pokeApi.getMoveSpanishName(slug));
                        Move move = pokeApi.getMoveCached(slug);
                        mvMap.put("type", move != null && move.type != null ? move.type.name : "normal");
                        return mvMap;
                    }).toList()
            ).toList());
        }
        if (set.evs != null && !set.evs.isEmpty()) {
            m.put("evs", set.evs.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .map(e -> e.getValue() + " " + SmogonService.translateStat(e.getKey()))
                    .collect(Collectors.joining(" / ")));
        }
        if (set.ivs != null && !set.ivs.isEmpty()) {
            m.put("ivs", set.ivs.entrySet().stream()
                    .filter(e -> e.getValue() < 31)
                    .map(e -> e.getValue() + " " + SmogonService.translateStat(e.getKey()))
                    .collect(Collectors.joining(" / ")));
        }
        if (set.teratypes != null && !set.teratypes.isEmpty()) {
            m.put("teratypes", set.teratypes.stream()
                    .map(t -> pokeApi.getSpanishTypeName(t.toLowerCase())).toList());
        }
        return m;
    }

    // ── Moves ──
    @GetMapping("/moves")
    public List<Map<String, Object>> allMoves() {
        return pokeApi.getAllMoveNamesCached().stream().sorted().map(name -> {
            String slug = pokeApi.getMoveSlug(name);
            Move m = pokeApi.getMoveCached(slug);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("slug", slug);
            map.put("spanishName", name);
            if (m != null) {
                map.put("type", m.type != null ? m.type.name : "normal");
                map.put("power", m.power);
                map.put("pp", m.pp);
                map.put("accuracy", m.accuracy);
                map.put("damageClass", m.damage_class != null ? m.damage_class.name : "status");
            }
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/moves/{slug}")
    public Object getMove(@PathVariable String slug) {
        String lower = slug.toLowerCase().trim();
        // Try as slug or as Spanish name
        Move m = pokeApi.getMoveCached(lower);
        if (m == null) {
            String resolvedSlug = pokeApi.getMoveSlug(lower);
            m = pokeApi.getMoveCached(resolvedSlug);
        }
        // If cached move lacks full data (descriptions/names), fetch from API
        if (m != null && m.flavor_text_entries == null) {
            try { m = pokeApi.searchMove(m.name).join(); } catch (Exception ignored) {}
        }
        if (m == null) {
            try { m = pokeApi.searchMove(lower).join(); } catch (Exception e) { /* ignore */ }
        }
        if (m == null) return Map.of("error", "Movimiento no encontrado");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", m.id);
        result.put("slug", m.name);
        result.put("spanishName", m.getSpanishName());
        result.put("type", m.type != null ? m.type.name : "normal");
        result.put("typeSpanish", pokeApi.getSpanishTypeName(m.type != null ? m.type.name : "normal"));
        result.put("power", m.power);
        result.put("pp", m.pp);
        result.put("accuracy", m.accuracy);
        result.put("priority", m.priority);
        result.put("damageClass", m.damage_class != null ? m.damage_class.name : "status");
        result.put("damageClassSpanish", TranslationUtils.translateDamageClass(m.damage_class != null ? m.damage_class.name : "status"));
        result.put("description", m.getDescriptionEs());
        if (m.generation != null) result.put("generation", m.generation.name);
        if (m.learned_by_pokemon != null) {
            result.put("learnedBy", m.learned_by_pokemon.stream()
                    .filter(lbp -> lbp.getId() > 0 && lbp.getId() <= 1025)
                    .map(lbp -> {
                        Map<String, Object> pm = new LinkedHashMap<>();
                        pm.put("name", lbp.name);
                        pm.put("id", lbp.getId());
                        pm.put("spanishName", pokeApi.getSpanishPokemonName(lbp.name));
                        return pm;
                    }).toList());
        }
        return result;
    }

    // ── Abilities ──
    @GetMapping("/abilities")
    public List<Map<String, String>> allAbilities() {
        return pokeApi.getAllAbilityNamesCached().stream().sorted().map(name -> {
            String slug = pokeApi.getAbilitySlug(name);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("slug", slug);
            m.put("spanishName", name);
            m.put("description", pokeApi.getAbilityDescription(slug));
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/abilities/{slug}")
    public Object getAbility(@PathVariable String slug) {
        String lower = slug.toLowerCase().trim();
        Ability a = pokeApi.getAbilityCached(lower);
        if (a == null) {
            try { a = pokeApi.searchAbility(lower).join(); } catch (Exception e) { /* ignore */ }
        }
        if (a == null) return Map.of("error", "Habilidad no encontrada");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", a.id);
        result.put("slug", a.name);
        result.put("spanishName", a.spanishName != null ? a.spanishName : pokeApi.getAbilitySpanishName(a.name));
        result.put("description", a.spanishEffect);
        result.put("flavorText", a.spanishFlavorText);
        result.put("generation", a.getGenerationLabel());
        if (a.pokemon != null) {
            result.put("pokemon", a.pokemon.stream()
                    .filter(pe -> pe.pokemon != null && pe.pokemon.getId() > 0 && pe.pokemon.getId() <= 1025)
                    .map(pe -> {
                        Map<String, Object> pm = new LinkedHashMap<>();
                        pm.put("name", pe.pokemon.name);
                        pm.put("id", pe.pokemon.getId());
                        pm.put("spanishName", pokeApi.getSpanishPokemonName(pe.pokemon.name));
                        pm.put("isHidden", pe.is_hidden);
                        return pm;
                    }).toList());
        }
        return result;
    }

    // ── Items ──
    @GetMapping("/items")
    public List<Map<String, String>> allItems() {
        return pokeApi.getAllItemNamesCached().stream().sorted().map(name -> {
            String slug = pokeApi.getItemSlugFromName(name);
            Map<String, String> m = new LinkedHashMap<>();
            m.put("slug", slug);
            m.put("spanishName", name);
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/items/{slug}")
    public Object getItem(@PathVariable String slug) {
        String lower = slug.toLowerCase().trim();
        Item item = pokeApi.getItemCached(lower);
        if (item == null) {
            try { item = pokeApi.searchItem(lower).join(); } catch (Exception e) { /* ignore */ }
        }
        if (item == null) return Map.of("error", "Objeto no encontrado");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", item.id);
        result.put("slug", item.name);
        result.put("spanishName", item.spanishName);
        result.put("category", item.category);
        result.put("cost", item.cost);
        result.put("spriteUrl", item.spriteUrl);
        result.put("effect", item.spanishEffect);
        result.put("flavorText", item.spanishFlavorText);
        if (item.heldByPokemon != null) {
            result.put("heldBy", item.heldByPokemon.stream()
                    .filter(h -> h.pokemon != null && h.pokemon.getId() > 0 && h.pokemon.getId() <= 1025)
                    .map(h -> {
                        Map<String, Object> pm = new LinkedHashMap<>();
                        pm.put("name", h.pokemon.name);
                        pm.put("id", h.pokemon.getId());
                        pm.put("spanishName", pokeApi.getSpanishPokemonName(h.pokemon.name));
                        return pm;
                    }).toList());
        }
        return result;
    }

    // ── Search ──
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String q, @RequestParam(defaultValue = "pokemon") String type) {
        String query = q.toLowerCase().trim();
        if (query.length() < 2) return List.of();

        return switch (type) {
            case "pokemon" -> pokeApi.getAllPokemon().stream()
                    .filter(p -> p.id > 0 && p.id <= 10325)
                    .filter(p -> {
                        String spanish = pokeApi.getSpanishPokemonName(p.name).toLowerCase();
                        return spanish.contains(query) || p.name.contains(query) || String.valueOf(p.id).equals(query);
                    })
                    .sorted(Comparator.comparingInt(p -> p.id))
                    .limit(20)
                    .map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", p.id);
                        m.put("name", p.name);
                        m.put("spanishName", pokeApi.getSpanishPokemonName(p.name));
                        m.put("types", p.types != null ? p.types.stream().map(t -> t.type.name).toList() : List.of());
                        return m;
                    }).collect(Collectors.toList());
            case "moves" -> pokeApi.getAllMoveNamesCached().stream()
                    .filter(name -> name.toLowerCase().contains(query))
                    .limit(20)
                    .map(name -> {
                        String slug = pokeApi.getMoveSlug(name);
                        Move mv = pokeApi.getMoveCached(slug);
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("slug", slug);
                        m.put("spanishName", name);
                        if (mv != null) m.put("type", mv.type != null ? mv.type.name : "normal");
                        return m;
                    }).collect(Collectors.toList());
            case "abilities" -> pokeApi.getAllAbilityNamesCached().stream()
                    .filter(name -> name.toLowerCase().contains(query))
                    .limit(20)
                    .map(name -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("slug", pokeApi.getAbilitySlug(name));
                        m.put("spanishName", name);
                        return m;
                    }).collect(Collectors.toList());
            default -> List.of();
        };
    }

    // ── Champions Teams ──
    @GetMapping("/champions/teams")
    public List<ChampionsTeam> getChampionsTeams(
            @RequestParam(required = false) String archetype,
            @RequestParam(required = false) String format) {
        if (archetype != null && !archetype.isBlank()) {
            return championsTeamService.getTeamsByArchetype(archetype);
        }
        if (format != null && !format.isBlank()) {
            return championsTeamService.getTeamsByFormat(format);
        }
        return championsTeamService.getAllTeams();
    }

    // ── Types ──
    @GetMapping("/types")
    public Map<String, Object> getTypes() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String type : TypeUtils.ALL_TYPES) {
            Map<String, Object> td = new LinkedHashMap<>();
            td.put("spanishName", pokeApi.getSpanishTypeName(type));
            td.put("color", TypeUtils.getTypeColor(type));
            td.put("colorDark", TypeUtils.getTypeColorDark(type));
            td.put("abbrev", TypeUtils.TYPE_ABBREV.get(type));
            String json = pokeApi.getTypeJson(type);
            if (json != null) {
                try {
                    var obj = JsonParser.parseString(json).getAsJsonObject();
                    if (obj.has("damage_relations")) {
                        TypeData typeData = gson.fromJson(obj, TypeData.class);
                        if (typeData.damage_relations != null) {
                            td.put("doubleDamageFrom", typeData.damage_relations.double_damage_from.stream().map(t -> t.name).toList());
                            td.put("halfDamageFrom", typeData.damage_relations.half_damage_from.stream().map(t -> t.name).toList());
                            td.put("noDamageFrom", typeData.damage_relations.no_damage_from.stream().map(t -> t.name).toList());
                            td.put("doubleDamageTo", typeData.damage_relations.double_damage_to.stream().map(t -> t.name).toList());
                            td.put("halfDamageTo", typeData.damage_relations.half_damage_to.stream().map(t -> t.name).toList());
                            td.put("noDamageTo", typeData.damage_relations.no_damage_to.stream().map(t -> t.name).toList());
                        }
                    }
                } catch (Exception ignored) {}
            }
            result.put(type, td);
        }
        return result;
    }

    // ── Type matchup calculation ──
    private Map<String, Object> calculateTypeMatchup(List<String> types) {
        Map<String, Double> effectiveness = new LinkedHashMap<>();
        for (String defType : TypeUtils.ALL_TYPES) {
            effectiveness.put(defType, 1.0);
        }

        for (String pokemonType : types) {
            String json = pokeApi.getTypeJson(pokemonType);
            if (json == null) continue;
            try {
                TypeData typeData = gson.fromJson(json, TypeData.class);
                if (typeData.damage_relations == null) continue;
                for (var t : typeData.damage_relations.double_damage_from)
                    effectiveness.merge(t.name, 2.0, (a, b) -> a * b);
                for (var t : typeData.damage_relations.half_damage_from)
                    effectiveness.merge(t.name, 0.5, (a, b) -> a * b);
                for (var t : typeData.damage_relations.no_damage_from)
                    effectiveness.put(t.name, 0.0);
            } catch (Exception ignored) {}
        }

        List<Map<String, Object>> weak = new ArrayList<>();
        List<Map<String, Object>> resist = new ArrayList<>();
        List<Map<String, Object>> immune = new ArrayList<>();

        for (var entry : effectiveness.entrySet()) {
            double val = entry.getValue();
            Map<String, Object> tm = Map.of(
                    "type", entry.getKey(),
                    "spanishName", pokeApi.getSpanishTypeName(entry.getKey()),
                    "color", TypeUtils.getTypeColor(entry.getKey()),
                    "multiplier", val
            );
            if (val == 0.0) immune.add(tm);
            else if (val > 1.0) weak.add(tm);
            else if (val < 1.0) resist.add(tm);
        }

        return Map.of("weak", weak, "resist", resist, "immune", immune);
    }
}
