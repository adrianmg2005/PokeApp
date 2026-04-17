package org.example.pokeapp.service;

import org.springframework.stereotype.Component;

import java.util.*;

import org.example.pokeapp.model.*;
import static org.example.pokeapp.util.TranslationUtils.formatMoveName;
import static org.example.pokeapp.util.TypeUtils.*;

/**
 * Auto‑generates competitive sets for Pokémon that lack official Smogon data.
 * Contains all the static data maps (signature moves, ability synergies, etc.)
 * and the logic to pick moves / items / natures based on stats & types.
 */
@Component
public class SmogonSetGenerator {

    private final PokeApiService service;

    public SmogonSetGenerator(PokeApiService service) {
        this.service = service;
    }

    // ----- Signature / iconic moves per Pokémon -----
    private static final Map<String, List<String>> SIGNATURE_MOVES = new HashMap<>() {{
        put("dracovish", List.of("Fishious Rend", "Crunch", "Psychic Fangs", "Ice Fang"));
        put("dracozolt", List.of("Bolt Beak", "Dragon Claw", "Fire Punch", "Aerial Ace"));
        put("arctovish", List.of("Fishious Rend", "Freeze-Dry", "Ice Fang", "Psychic Fangs"));
        put("arctozolt", List.of("Bolt Beak", "Freeze-Dry", "Low Kick", "Ice Punch"));
        put("iron-bundle", List.of("Freeze-Dry", "Hydro Pump", "Ice Beam", "Flip Turn"));
        put("iron-valiant", List.of("Moonblast", "Close Combat", "Knock Off", "Spirit Break"));
        put("iron-hands", List.of("Drain Punch", "Ice Punch", "Thunder Punch", "Fake Out"));
        put("iron-moth", List.of("Fiery Dance", "Energy Ball", "Psychic", "Sludge Wave"));
        put("iron-jugulis", List.of("Dark Pulse", "Air Slash", "Flamethrower", "U-turn"));
        put("iron-thorns", List.of("Stone Edge", "Ice Punch", "Thunder Punch", "Earthquake"));
        put("iron-treads", List.of("Earthquake", "Iron Head", "Rapid Spin", "Knock Off"));
        put("iron-leaves", List.of("Leaf Blade", "Psyblade", "Close Combat", "Swords Dance"));
        put("iron-boulder", List.of("Mighty Cleave", "Close Combat", "Zen Headbutt", "Swords Dance"));
        put("iron-crown", List.of("Tachyon Cutter", "Focus Blast", "Psychic", "Calm Mind"));
        put("roaring-moon", List.of("Dragon Dance", "Acrobatics", "Crunch", "Earthquake"));
        put("flutter-mane", List.of("Moonblast", "Shadow Ball", "Mystical Fire", "Thunderbolt"));
        put("great-tusk", List.of("Headlong Rush", "Close Combat", "Knock Off", "Rapid Spin"));
        put("brute-bonnet", List.of("Bullet Seed", "Sucker Punch", "Crunch", "Spore"));
        put("slither-wing", List.of("Close Combat", "First Impression", "U-turn", "Flare Blitz"));
        put("sandy-shocks", List.of("Earth Power", "Thunderbolt", "Stealth Rock", "Volt Switch"));
        put("scream-tail", List.of("Wish", "Protect", "Thunder Wave", "Dazzling Gleam"));
        put("walking-wake", List.of("Hydro Steam", "Draco Meteor", "Flamethrower", "Dragon Pulse"));
        put("gouging-fire", List.of("Dragon Dance", "Flare Blitz", "Outrage", "Earthquake"));
        put("raging-bolt", List.of("Thunderclap", "Draco Meteor", "Thunderbolt", "Calm Mind"));
        put("urshifu-single-strike", List.of("Wicked Blow", "Close Combat", "Sucker Punch", "U-turn"));
        put("urshifu-rapid-strike", List.of("Surging Strikes", "Close Combat", "Aqua Jet", "U-turn"));
        put("zacian", List.of("Behemoth Blade", "Play Rough", "Close Combat", "Swords Dance"));
        put("zacian-crowned", List.of("Behemoth Blade", "Play Rough", "Close Combat", "Swords Dance"));
        put("zamazenta", List.of("Body Press", "Iron Defense", "Crunch", "Close Combat"));
        put("calyrex-shadow", List.of("Astral Barrage", "Psyshock", "Nasty Plot", "Draining Kiss"));
        put("calyrex-ice", List.of("Glacial Lance", "High Horsepower", "Close Combat", "Trick Room"));
        put("eternatus", List.of("Dynamax Cannon", "Sludge Bomb", "Flamethrower", "Recover"));
        put("regieleki", List.of("Thunderbolt", "Volt Switch", "Rapid Spin", "Thunder Wave"));
        put("regidrago", List.of("Dragon Energy", "Draco Meteor", "Dragon Pulse", "Earth Power"));
        put("spectrier", List.of("Shadow Ball", "Nasty Plot", "Dark Pulse", "Mud Shot"));
        put("glastrier", List.of("Icicle Crash", "High Horsepower", "Close Combat", "Heavy Slam"));
        put("meowscarada", List.of("Flower Trick", "Knock Off", "U-turn", "Play Rough"));
        put("skeledirge", List.of("Torch Song", "Shadow Ball", "Slack Off", "Will-O-Wisp"));
        put("quaquaval", List.of("Aqua Step", "Close Combat", "Brave Bird", "Swords Dance"));
        put("garchomp", List.of("Earthquake", "Outrage", "Stone Edge", "Swords Dance"));
        put("dragapult", List.of("Dragon Darts", "Shadow Ball", "U-turn", "Draco Meteor"));
        put("haxorus", List.of("Outrage", "Earthquake", "Close Combat", "Dragon Dance"));
        put("kingambit", List.of("Kowtow Cleave", "Sucker Punch", "Iron Head", "Swords Dance"));
        put("gholdengo", List.of("Make It Rain", "Shadow Ball", "Focus Blast", "Nasty Plot"));
        put("annihilape", List.of("Rage Fist", "Drain Punch", "Bulk Up", "Taunt"));
        put("baxcalibur", List.of("Glaive Rush", "Earthquake", "Ice Shard", "Dragon Dance"));
        put("palafin", List.of("Wave Crash", "Jet Punch", "Close Combat", "Flip Turn"));
        put("palafin-hero", List.of("Wave Crash", "Jet Punch", "Close Combat", "Flip Turn"));
        put("tinkaton", List.of("Gigaton Hammer", "Knock Off", "Stealth Rock", "Thunder Wave"));
        put("ceruledge", List.of("Bitter Blade", "Shadow Claw", "Swords Dance", "Close Combat"));
        put("armarouge", List.of("Armor Cannon", "Psychic", "Energy Ball", "Calm Mind"));
        put("chien-pao", List.of("Icicle Crash", "Crunch", "Ice Shard", "Swords Dance"));
        put("chi-yu", List.of("Overheat", "Dark Pulse", "Psychic", "Lava Plume"));
        put("ting-lu", List.of("Earthquake", "Stealth Rock", "Whirlwind", "Ruination"));
        put("wo-chien", List.of("Knock Off", "Giga Drain", "Foul Play", "Leech Seed"));
        put("ogerpon", List.of("Ivy Cudgel", "Horn Leech", "Play Rough", "Swords Dance"));
        put("ogerpon-wellspring-mask", List.of("Ivy Cudgel", "Aqua Step", "Horn Leech", "Swords Dance"));
        put("ogerpon-hearthflame-mask", List.of("Ivy Cudgel", "Flare Blitz", "Horn Leech", "Swords Dance"));
        put("ogerpon-cornerstone-mask", List.of("Ivy Cudgel", "Stone Edge", "Horn Leech", "Swords Dance"));
        put("terapagos", List.of("Tera Starstorm", "Earth Power", "Calm Mind", "Rapid Spin"));
        put("pecharunt", List.of("Malignant Chain", "Shadow Ball", "Nasty Plot", "Toxic"));
        put("shedinja", List.of("Shadow Sneak", "Shadow Claw", "X-Scissor", "Will-O-Wisp"));
        put("ditto", List.of("Transform"));
        put("smeargle", List.of("Spore", "Stealth Rock", "Sticky Web", "Nuzzle"));
        put("wobbuffet", List.of("Counter", "Mirror Coat", "Encore", "Safeguard"));
        put("shuckle", List.of("Sticky Web", "Stealth Rock", "Encore", "Toxic"));
        put("sableye", List.of("Foul Play", "Will-O-Wisp", "Recover", "Knock Off"));
        put("toxapex", List.of("Scald", "Toxic", "Recover", "Haze"));
        put("ferrothorn", List.of("Leech Seed", "Stealth Rock", "Knock Off", "Power Whip"));
        put("blissey", List.of("Soft-Boiled", "Seismic Toss", "Thunder Wave", "Stealth Rock"));
        put("chansey", List.of("Soft-Boiled", "Seismic Toss", "Thunder Wave", "Stealth Rock"));
        put("clefable", List.of("Moonblast", "Soft-Boiled", "Calm Mind", "Thunder Wave"));
        put("rotom-wash", List.of("Hydro Pump", "Volt Switch", "Will-O-Wisp", "Thunder Wave"));
        put("rotom-heat", List.of("Overheat", "Volt Switch", "Will-O-Wisp", "Thunder Wave"));
        put("aegislash", List.of("Shadow Ball", "Flash Cannon", "King's Shield", "Shadow Sneak"));
        put("mimikyu", List.of("Play Rough", "Shadow Claw", "Shadow Sneak", "Swords Dance"));
        put("zoroark", List.of("Dark Pulse", "Flamethrower", "Focus Blast", "Nasty Plot"));
        put("zoroark-hisui", List.of("Bitter Malice", "Shadow Ball", "Nasty Plot", "Hyper Voice"));
    }};

    // ----- Ability → preferred moves (synergy) -----
    private static final Map<String, List<String>> ABILITY_SYNERGY_MOVES = new HashMap<>() {{
        put("strong-jaw", List.of("Fishious Rend", "Crunch", "Psychic Fangs", "Ice Fang", "Fire Fang", "Thunder Fang", "Poison Fang", "Jaw Lock", "Hyper Fang"));
        put("iron-fist", List.of("Drain Punch", "Ice Punch", "Thunder Punch", "Fire Punch", "Mach Punch", "Close Combat", "Bullet Punch", "Meteor Mash", "Shadow Punch", "Hammer Arm", "Focus Punch", "Mega Punch", "Power-Up Punch", "Plasma Fists", "Surging Strikes", "Wicked Blow"));
        put("technician", List.of("Bullet Punch", "Mach Punch", "Fake Out", "Quick Attack", "Aerial Ace", "Bug Bite", "Fury Cutter", "Icicle Spear", "Rock Blast", "Tail Slap", "Bullet Seed", "Water Shuriken", "Triple Axel", "U-turn"));
        put("sheer-force", List.of("Iron Head", "Rock Slide", "Flamethrower", "Ice Beam", "Thunderbolt", "Earth Power", "Crunch", "Psychic", "Flash Cannon", "Shadow Ball", "Focus Blast", "Fire Blast", "Sludge Bomb"));
        put("adaptability", List.of());
        put("reckless", List.of("Brave Bird", "Flare Blitz", "Head Smash", "Wild Charge", "Wood Hammer", "Double-Edge", "High Jump Kick", "Wave Crash", "Head Charge", "Take Down"));
        put("mega-launcher", List.of("Aura Sphere", "Dragon Pulse", "Dark Pulse", "Water Pulse", "Terrain Pulse", "Heal Pulse", "Origin Pulse"));
        put("serene-grace", List.of("Iron Head", "Air Slash", "Thunder", "Fire Blast", "Body Slam", "Zen Headbutt", "Rock Slide", "Ancient Power", "Shadow Ball"));
        put("tough-claws", List.of("Outrage", "Dragon Claw", "Crunch", "Close Combat", "Iron Head", "Earthquake", "Play Rough", "Waterfall", "Liquidation", "Flare Blitz", "Brave Bird"));
        put("hustle", List.of("Bolt Beak", "Outrage", "Close Combat", "Earthquake", "Iron Head", "Dragon Rush", "Rock Slide"));
        put("skill-link", List.of("Icicle Spear", "Rock Blast", "Bullet Seed", "Tail Slap", "Pin Missile", "Bone Rush", "Scale Shot"));
        put("poison-touch", List.of("Drain Punch", "Ice Punch", "Thunder Punch", "Knock Off", "Earthquake", "Gunk Shot", "Poison Jab"));
        put("protean", List.of());
        put("libero", List.of());
        put("no-guard", List.of("Dynamic Punch", "Inferno", "Zap Cannon", "Stone Edge", "Focus Blast", "Thunder", "Blizzard", "Hurricane"));
        put("sand-force", List.of("Earthquake", "Stone Edge", "Rock Slide", "Iron Head", "Flash Cannon", "Earth Power"));
        put("swift-swim", List.of("Waterfall", "Flip Turn", "Liquidation", "Hydro Pump", "Surf"));
        put("chlorophyll", List.of("Solar Beam", "Solar Blade", "Growth"));
        put("sand-rush", List.of("Earthquake", "Iron Head", "Rock Slide", "Swords Dance"));
        put("slush-rush", List.of("Icicle Crash", "Ice Shard", "Earthquake", "Iron Head"));
        put("refrigerate", List.of("Return", "Body Slam", "Double-Edge", "Hyper Voice", "Fake Out", "Extreme Speed"));
        put("galvanize", List.of("Return", "Body Slam", "Double-Edge", "Hyper Voice", "Fake Out", "Extreme Speed"));
        put("pixilate", List.of("Hyper Voice", "Return", "Body Slam", "Quick Attack", "Extreme Speed"));
        put("aerilate", List.of("Return", "Double-Edge", "Body Slam", "Hyper Voice", "Extreme Speed"));
        put("guts", List.of("Facade", "Close Combat", "Knock Off", "Earthquake"));
        put("tinted-lens", List.of());
        put("speed-boost", List.of("Protect", "Close Combat", "Flare Blitz", "Swords Dance"));
        put("contrary", List.of("Leaf Storm", "Overheat", "Draco Meteor", "Superpower", "Close Combat", "V-Create"));
        put("moxie", List.of("Earthquake", "Close Combat", "Knock Off", "Dragon Dance"));
        put("beast-boost", List.of());
        put("quark-drive", List.of());
        put("protosynthesis", List.of());
    }};

    // ----- Ability → preferred items -----
    private static final Map<String, List<String>> ABILITY_ITEMS = new HashMap<>() {{
        put("guts", List.of("Flame Orb", "Toxic Orb"));
        put("magic-guard", List.of("Life Orb", "Flame Orb"));
        put("speed-boost", List.of("Life Orb", "Swords Dance"));
        put("swift-swim", List.of("Life Orb", "Choice Band", "Choice Specs"));
        put("chlorophyll", List.of("Life Orb", "Choice Specs"));
        put("sand-rush", List.of("Choice Band", "Life Orb"));
        put("slush-rush", List.of("Choice Band", "Life Orb"));
        put("contrary", List.of("Life Orb", "Choice Specs"));
        put("unburden", List.of("Sitrus Berry", "White Herb", "Electric Seed"));
        put("moxie", List.of("Choice Scarf", "Lum Berry"));
        put("hustle", List.of("Choice Scarf", "Choice Band"));
        put("skill-link", List.of("King's Rock", "Choice Band", "Life Orb"));
        put("quark-drive", List.of("Booster Energy", "Choice Specs", "Choice Band"));
        put("protosynthesis", List.of("Booster Energy", "Choice Band", "Choice Specs"));
    }};

    private static final Map<String, List<String>> PHYS_MOVES_BY_TYPE = Map.ofEntries(
            Map.entry("normal", List.of("Return", "Body Slam", "Facade", "Double-Edge", "Extreme Speed", "Quick Attack")),
            Map.entry("fire", List.of("Flare Blitz", "Fire Punch", "Blaze Kick", "Flame Charge", "Sacred Fire")),
            Map.entry("water", List.of("Waterfall", "Aqua Tail", "Liquidation", "Aqua Jet", "Wave Crash", "Fishious Rend")),
            Map.entry("grass", List.of("Wood Hammer", "Seed Bomb", "Leaf Blade", "Power Whip", "Horn Leech")),
            Map.entry("electric", List.of("Wild Charge", "Thunder Punch", "Volt Tackle", "Zing Zap", "Bolt Beak", "Bolt Strike")),
            Map.entry("ice", List.of("Ice Punch", "Icicle Crash", "Ice Shard", "Triple Axel", "Avalanche", "Glacial Lance")),
            Map.entry("fighting", List.of("Close Combat", "Drain Punch", "Brick Break", "Mach Punch", "Cross Chop", "Superpower")),
            Map.entry("poison", List.of("Poison Jab", "Gunk Shot", "Cross Poison")),
            Map.entry("ground", List.of("Earthquake", "High Horsepower", "Drill Run", "Stomping Tantrum", "Headlong Rush")),
            Map.entry("flying", List.of("Brave Bird", "Aerial Ace", "Acrobatics", "Drill Peck", "Fly")),
            Map.entry("psychic", List.of("Zen Headbutt", "Psycho Cut", "Psychic Fangs")),
            Map.entry("bug", List.of("X-Scissor", "Megahorn", "U-turn", "Leech Life", "Lunge", "First Impression")),
            Map.entry("rock", List.of("Stone Edge", "Rock Slide", "Head Smash", "Rock Blast")),
            Map.entry("ghost", List.of("Shadow Claw", "Phantom Force", "Shadow Sneak", "Poltergeist", "Spirit Shackle")),
            Map.entry("dragon", List.of("Outrage", "Dragon Claw", "Dragon Rush", "Scale Shot")),
            Map.entry("dark", List.of("Knock Off", "Crunch", "Sucker Punch", "Throat Chop", "Jaw Lock", "Wicked Blow")),
            Map.entry("steel", List.of("Iron Head", "Iron Tail", "Metal Claw", "Bullet Punch", "Smart Strike", "Behemoth Blade")),
            Map.entry("fairy", List.of("Play Rough", "Dazzling Gleam", "Spirit Break"))
    );

    private static final Map<String, List<String>> SPEC_MOVES_BY_TYPE = Map.ofEntries(
            Map.entry("normal", List.of("Hyper Voice", "Boomburst", "Tri Attack", "Swift")),
            Map.entry("fire", List.of("Flamethrower", "Fire Blast", "Heat Wave", "Lava Plume", "Overheat", "Magma Storm")),
            Map.entry("water", List.of("Surf", "Hydro Pump", "Scald", "Muddy Water", "Water Pulse", "Steam Eruption")),
            Map.entry("grass", List.of("Energy Ball", "Leaf Storm", "Giga Drain", "Solar Beam", "Grass Knot")),
            Map.entry("electric", List.of("Thunderbolt", "Thunder", "Volt Switch", "Discharge", "Charge Beam")),
            Map.entry("ice", List.of("Ice Beam", "Blizzard", "Freeze-Dry", "Frost Breath")),
            Map.entry("fighting", List.of("Focus Blast", "Aura Sphere", "Vacuum Wave")),
            Map.entry("poison", List.of("Sludge Bomb", "Sludge Wave", "Venoshock")),
            Map.entry("ground", List.of("Earth Power", "Mud Shot", "Scorching Sands")),
            Map.entry("flying", List.of("Air Slash", "Hurricane", "Aeroblast")),
            Map.entry("psychic", List.of("Psychic", "Psyshock", "Psybeam", "Future Sight", "Expanding Force")),
            Map.entry("bug", List.of("Bug Buzz", "Signal Beam", "Struggle Bug")),
            Map.entry("rock", List.of("Power Gem", "Ancient Power", "Meteor Beam")),
            Map.entry("ghost", List.of("Shadow Ball", "Hex", "Night Shade", "Astral Barrage")),
            Map.entry("dragon", List.of("Dragon Pulse", "Draco Meteor", "Dragon Breath")),
            Map.entry("dark", List.of("Dark Pulse", "Night Daze", "Snarl", "Foul Play")),
            Map.entry("steel", List.of("Flash Cannon", "Steel Beam", "Mirror Shot")),
            Map.entry("fairy", List.of("Moonblast", "Dazzling Gleam", "Draining Kiss", "Fleur Cannon"))
    );

    private static final List<String> SUPPORT_MOVES = List.of(
            "Stealth Rock", "Spikes", "Toxic Spikes", "Sticky Web",
            "Toxic", "Will-O-Wisp", "Thunder Wave", "Glare", "Nuzzle",
            "Recover", "Roost", "Slack Off", "Soft-Boiled", "Wish", "Synthesis", "Moonlight",
            "Defog", "Rapid Spin", "Haze", "Whirlwind",
            "Light Screen", "Reflect", "Aurora Veil",
            "Taunt", "Encore", "Trick", "Switcheroo",
            "Heal Bell", "Aromatherapy",
            "Leech Seed", "Protect", "Substitute",
            "Parting Shot", "Teleport", "U-turn", "Volt Switch",
            "Pain Split", "Strength Sap", "Calm Mind", "Nasty Plot"
    );

    private static final List<String> SETUP_MOVES = List.of(
            "Swords Dance", "Dragon Dance", "Bulk Up", "Belly Drum",
            "Nasty Plot", "Calm Mind", "Quiver Dance", "Shell Smash",
            "Work Up", "Coil", "Shift Gear", "Agility", "Rock Polish",
            "Iron Defense", "Cotton Guard", "Cosmic Power", "Curse",
            "Tail Glow", "Growth", "Victory Dance"
    );

    // =========================================================================
    //  PUBLIC API
    // =========================================================================

    public List<SmogonSet> generateSuggestedSets(Pokemon p) {
        List<SmogonSet> sets = new ArrayList<>();
        if (p.stats == null || p.types == null) return sets;

        int atk = p.getStat("attack");
        int def = p.getStat("defense");
        int spa = p.getStat("special-attack");
        int spd = p.getStat("special-defense");
        int spe = p.getStat("speed");
        int hp  = p.getStat("hp");

        List<String> types = p.types.stream().map(t -> t.type.name).toList();
        String type1 = types.get(0);
        String type2 = types.size() > 1 ? types.get(1) : null;

        boolean physicallyOriented = atk >= spa;
        int bestOffense  = Math.max(atk, spa);
        int bestDefense  = Math.max(def, spd);

        List<String> allAbilitySlugs = new ArrayList<>();
        String bestAbility   = null;
        String hiddenAbility = null;
        if (p.abilities != null && !p.abilities.isEmpty()) {
            for (Pokemon.AbilitySlot slot : p.abilities) {
                allAbilitySlugs.add(slot.ability.name);
                if (slot.is_hidden) hiddenAbility = slot.ability.name;
                else if (bestAbility == null) bestAbility = slot.ability.name;
            }
            if (bestAbility == null) bestAbility = p.abilities.get(0).ability.name;
        }

        List<String> signatureMoves = SIGNATURE_MOVES.getOrDefault(p.name, List.of());
        boolean hasSignature = !signatureMoves.isEmpty();

        List<String> synergyMoves = new ArrayList<>();
        String synergyAbility = null;
        for (String abilSlug : allAbilitySlugs) {
            List<String> abilMoves = ABILITY_SYNERGY_MOVES.getOrDefault(abilSlug, List.of());
            if (!abilMoves.isEmpty() && synergyMoves.isEmpty()) {
                synergyMoves.addAll(abilMoves);
                synergyAbility = abilSlug;
            }
        }

        List<String> abilityItems = null;
        for (String abilSlug : allAbilitySlugs) {
            List<String> items = ABILITY_ITEMS.get(abilSlug);
            if (items != null && !items.isEmpty()) {
                abilityItems = items;
                if (synergyAbility == null) synergyAbility = abilSlug;
                break;
            }
        }

        // SET 0: SIGNATURE SET
        if (hasSignature) {
            SmogonSet sigSet = new SmogonSet();
            sigSet.pokemonName = p.name;
            sigSet.setName = "⭐ Set Óptimo";
            sigSet.generation = "sugerido";
            String sigAbility = synergyAbility != null ? synergyAbility : bestAbility;
            sigSet.ability = sigAbility != null ? List.of(formatMoveName(sigAbility)) : List.of();
            int physCount = 0, specCount = 0;
            for (String mv : signatureMoves) {
                Move m = service.getMoveCached(mv.toLowerCase().replace(" ", "-"));
                if (m != null && m.damage_class != null) {
                    if ("physical".equals(m.damage_class.name)) physCount++;
                    else if ("special".equals(m.damage_class.name)) specCount++;
                }
            }
            boolean sigPhysical = physCount >= specCount;
            sigSet.nature = List.of(sigPhysical ? (spe >= 90 ? "Jolly" : "Adamant") : (spe >= 90 ? "Timid" : "Modest"));
            if (abilityItems != null) sigSet.item = abilityItems;
            else sigSet.item = sigPhysical ?
                    List.of(spe >= 100 ? "Choice Scarf" : "Choice Band", "Life Orb") :
                    List.of(spe >= 100 ? "Choice Specs" : "Life Orb", "Choice Specs");
            sigSet.evs = new LinkedHashMap<>();
            sigSet.evs.put(sigPhysical ? "atk" : "spa", 252);
            sigSet.evs.put("spe", 252);
            sigSet.evs.put("hp", 4);
            sigSet.moves = new ArrayList<>();
            for (String mv : signatureMoves) {
                if (sigSet.moves.size() >= 4) break;
                if (service.getMoveCached(mv.toLowerCase().replace(" ", "-")) != null)
                    sigSet.moves.add(List.of(mv));
            }
            if (sigSet.moves.size() >= 2) sets.add(sigSet);
        }

        // SET 1: ABILITY SYNERGY SET
        if (synergyAbility != null && !synergyMoves.isEmpty()) {
            SmogonSet synSet = new SmogonSet();
            synSet.pokemonName = p.name;
            synSet.setName = "🔗 Sinergia: " + formatMoveName(synergyAbility);
            synSet.generation = "sugerido";
            synSet.ability = List.of(formatMoveName(synergyAbility));
            int physC = 0, specC = 0;
            for (String mv : synergyMoves) {
                Move m = service.getMoveCached(mv.toLowerCase().replace(" ", "-"));
                if (m != null && m.damage_class != null) {
                    if ("physical".equals(m.damage_class.name)) physC++;
                    else if ("special".equals(m.damage_class.name)) specC++;
                }
            }
            boolean synPhys = physC >= specC;
            synSet.nature = List.of(synPhys ? (spe >= 90 ? "Jolly" : "Adamant") : (spe >= 90 ? "Timid" : "Modest"));
            synSet.item = abilityItems != null ? abilityItems : List.of("Life Orb", synPhys ? "Choice Band" : "Choice Specs");
            synSet.evs = new LinkedHashMap<>();
            synSet.evs.put(synPhys ? "atk" : "spa", 252);
            synSet.evs.put("spe", 252);
            synSet.evs.put("hp", 4);
            synSet.moves = new ArrayList<>();
            List<String> usedSlugs = new ArrayList<>();
            for (String mv : synergyMoves) {
                if (synSet.moves.size() >= 4) break;
                String slug = mv.toLowerCase().replace(" ", "-");
                if (service.getMoveCached(slug) != null && !usedSlugs.contains(slug)) {
                    synSet.moves.add(List.of(mv));
                    usedSlugs.add(slug);
                }
            }
            if (synSet.moves.size() < 4) {
                if (synPhys) addBestPhysMovesExcluding(synSet, types, usedSlugs);
                else addBestSpecMovesExcluding(synSet, types, usedSlugs);
            }
            if (synSet.moves.size() >= 2 && !isDuplicateSet(sets, synSet)) sets.add(synSet);
        }

        // SET 2: Physical Sweeper
        if (atk >= 80 && spe >= 70 && atk >= spa - 10) {
            SmogonSet physSet = new SmogonSet();
            physSet.pokemonName = p.name;
            physSet.setName = spe >= 100 ? "Sweeper Físico" : "Atacante Físico";
            physSet.generation = "sugerido";
            physSet.ability = bestAbility != null ? List.of(formatMoveName(bestAbility)) : List.of();
            physSet.nature = List.of(spe >= 90 ? "Jolly" : "Adamant");
            physSet.item = abilityItems != null ? abilityItems : List.of(spe >= 100 ? "Choice Scarf" : "Choice Band", "Life Orb");
            physSet.evs = new LinkedHashMap<>();
            physSet.evs.put("atk", 252); physSet.evs.put("spe", 252); physSet.evs.put("hp", 4);
            physSet.moves = new ArrayList<>();
            addBestPhysMoves(physSet, types);
            if (physSet.moves.size() >= 2 && !isDuplicateSet(sets, physSet)) sets.add(physSet);
        }

        // SET 3: Special Sweeper
        if (spa >= 80 && spe >= 70 && spa >= atk - 10) {
            SmogonSet specSet = new SmogonSet();
            specSet.pokemonName = p.name;
            specSet.setName = spe >= 100 ? "Sweeper Especial" : "Atacante Especial";
            specSet.generation = "sugerido";
            specSet.ability = bestAbility != null ? List.of(formatMoveName(bestAbility)) : List.of();
            specSet.nature = List.of(spe >= 90 ? "Timid" : "Modest");
            specSet.item = abilityItems != null ? abilityItems : List.of(spe >= 100 ? "Choice Specs" : "Life Orb", "Choice Specs");
            specSet.evs = new LinkedHashMap<>();
            specSet.evs.put("spa", 252); specSet.evs.put("spe", 252); specSet.evs.put("hp", 4);
            specSet.moves = new ArrayList<>();
            addBestSpecMoves(specSet, types);
            if (specSet.moves.size() >= 2 && !isDuplicateSet(sets, specSet)) sets.add(specSet);
        }

        // SET 4: Setup Sweeper
        if (bestOffense >= 85 && spe >= 60) {
            String setupMove = findFirstAvailableMove(SETUP_MOVES);
            if (setupMove != null) {
                SmogonSet setupSet = new SmogonSet();
                setupSet.pokemonName = p.name;
                setupSet.generation = "sugerido";
                boolean physSetup = physicallyOriented &&
                        findFirstAvailableMove(List.of("Swords Dance", "Dragon Dance", "Bulk Up", "Belly Drum", "Coil", "Shift Gear")) != null;
                if (physSetup) {
                    setupSet.setName = "Setup Sweeper Físico";
                    setupSet.nature = List.of(spe >= 90 ? "Jolly" : "Adamant");
                    setupSet.item = List.of("Life Orb", "Lum Berry");
                    setupSet.evs = new LinkedHashMap<>();
                    setupSet.evs.put("atk", 252); setupSet.evs.put("spe", 252); setupSet.evs.put("hp", 4);
                    String physSetupMove = findFirstAvailableMove(List.of("Swords Dance", "Dragon Dance", "Bulk Up", "Belly Drum", "Coil", "Shift Gear"));
                    setupSet.moves = new ArrayList<>();
                    if (physSetupMove != null) setupSet.moves.add(List.of(physSetupMove));
                    addBestPhysMoves(setupSet, types);
                } else {
                    setupSet.setName = "Setup Sweeper Especial";
                    setupSet.nature = List.of(spe >= 90 ? "Timid" : "Modest");
                    setupSet.item = List.of("Life Orb", "Leftovers");
                    setupSet.evs = new LinkedHashMap<>();
                    setupSet.evs.put("spa", 252); setupSet.evs.put("spe", 252); setupSet.evs.put("hp", 4);
                    String specSetupMove = findFirstAvailableMove(List.of("Nasty Plot", "Calm Mind", "Quiver Dance", "Shell Smash", "Tail Glow"));
                    setupSet.moves = new ArrayList<>();
                    if (specSetupMove != null) setupSet.moves.add(List.of(specSetupMove));
                    addBestSpecMoves(setupSet, types);
                }
                setupSet.ability = bestAbility != null ? List.of(formatMoveName(bestAbility)) : List.of();
                if (setupSet.moves.size() >= 3 && !isDuplicateSet(sets, setupSet)) sets.add(setupSet);
            }
        }

        // SET 5: Defensive / Wall
        if ((def >= 85 || spd >= 85) && hp >= 70) {
            SmogonSet wallSet = new SmogonSet();
            wallSet.pokemonName = p.name;
            wallSet.generation = "sugerido";
            boolean physWall = def >= spd;
            wallSet.setName = physWall ? "Wall Físico" : "Wall Especial";
            wallSet.ability = bestAbility != null ? List.of(formatMoveName(bestAbility)) : List.of();
            wallSet.nature = List.of(physWall ? "Impish" : "Calm");
            wallSet.item = List.of("Leftovers", "Heavy-Duty Boots");
            wallSet.evs = new LinkedHashMap<>();
            wallSet.evs.put("hp", 252); wallSet.evs.put(physWall ? "def" : "spd", 252); wallSet.evs.put(physWall ? "spd" : "def", 4);
            wallSet.moves = new ArrayList<>();
            String recovery = findFirstAvailableMove(List.of("Recover", "Roost", "Slack Off", "Soft-Boiled", "Synthesis", "Moonlight", "Wish"));
            if (recovery != null) wallSet.moves.add(List.of(recovery));
            String status = findFirstAvailableMove(List.of("Toxic", "Will-O-Wisp", "Thunder Wave", "Scald"));
            if (status != null) wallSet.moves.add(List.of(status));
            String util = findFirstAvailableMove(List.of("Stealth Rock", "Spikes", "Defog", "Haze", "Whirlwind", "Protect"));
            if (util != null) wallSet.moves.add(List.of(util));
            if (physWall && atk > spa) addSingleBestPhysMove(wallSet, type1);
            else addSingleBestSpecMove(wallSet, type1);
            if (wallSet.moves.size() >= 3 && !isDuplicateSet(sets, wallSet)) sets.add(wallSet);
        }

        // SET 6: Bulky Attacker
        if (hp >= 80 && bestOffense >= 80 && bestDefense >= 75 && spe < 100) {
            SmogonSet bulkySet = new SmogonSet();
            bulkySet.pokemonName = p.name;
            bulkySet.setName = "Atacante Bulky";
            bulkySet.generation = "sugerido";
            bulkySet.ability = bestAbility != null ? List.of(formatMoveName(bestAbility)) : List.of();
            bulkySet.item = List.of("Assault Vest", "Leftovers");
            bulkySet.evs = new LinkedHashMap<>();
            bulkySet.evs.put("hp", 252);
            if (physicallyOriented) {
                bulkySet.nature = List.of("Adamant");
                bulkySet.evs.put("atk", 252); bulkySet.evs.put("def", 4);
                bulkySet.moves = new ArrayList<>();
                addBestPhysMoves(bulkySet, types);
            } else {
                bulkySet.nature = List.of("Modest");
                bulkySet.evs.put("spa", 252); bulkySet.evs.put("spd", 4);
                bulkySet.moves = new ArrayList<>();
                addBestSpecMoves(bulkySet, types);
            }
            if (bulkySet.moves.size() >= 2 && !isDuplicateSet(sets, bulkySet)) sets.add(bulkySet);
        }

        // SET 7: Support / Utility
        if (bestOffense < 80 && (hp + def + spd) / 3.0 >= 75) {
            SmogonSet suppSet = new SmogonSet();
            suppSet.pokemonName = p.name;
            suppSet.setName = "Soporte / Utility";
            suppSet.generation = "sugerido";
            suppSet.ability = bestAbility != null ? List.of(formatMoveName(bestAbility)) : List.of();
            suppSet.nature = List.of(def >= spd ? "Bold" : "Calm");
            suppSet.item = List.of("Leftovers", "Light Clay", "Eject Button");
            suppSet.evs = new LinkedHashMap<>();
            suppSet.evs.put("hp", 252); suppSet.evs.put(def >= spd ? "def" : "spd", 252); suppSet.evs.put("spe", 4);
            suppSet.moves = new ArrayList<>();
            List<String> usedMoves = new ArrayList<>();
            for (String m : SUPPORT_MOVES) {
                if (suppSet.moves.size() >= 4) break;
                String slug = m.toLowerCase().replace(" ", "-");
                if (service.getMoveCached(slug) != null && !usedMoves.contains(slug)) {
                    suppSet.moves.add(List.of(m));
                    usedMoves.add(slug);
                }
            }
            if (suppSet.moves.size() >= 3 && !isDuplicateSet(sets, suppSet)) sets.add(suppSet);
        }

        if (sets.size() > 4) sets = new ArrayList<>(sets.subList(0, 4));
        return sets;
    }

    // =========================================================================
    //  INTERNAL HELPERS
    // =========================================================================

    private boolean isDuplicateSet(List<SmogonSet> existing, SmogonSet candidate) {
        if (candidate.moves == null || candidate.moves.isEmpty()) return false;
        List<String> candidateSlugs = candidate.moves.stream()
                .flatMap(slot -> slot.stream())
                .map(m -> m.toLowerCase().replace(" ", "-"))
                .sorted().toList();
        for (SmogonSet s : existing) {
            if (s.moves == null) continue;
            List<String> sSlugs = s.moves.stream()
                    .flatMap(slot -> slot.stream())
                    .map(m -> m.toLowerCase().replace(" ", "-"))
                    .sorted().toList();
            long common = candidateSlugs.stream().filter(sSlugs::contains).count();
            if (common >= 3) return true;
        }
        return false;
    }

    private void addBestPhysMoves(SmogonSet set, List<String> types) {
        List<String> usedTypes = new ArrayList<>();
        for (String type : types) {
            if (set.moves.size() >= 4) break;
            String move = findFirstAvailableMoveFromList(PHYS_MOVES_BY_TYPE.getOrDefault(type, List.of()));
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(type); }
        }
        for (String covType : List.of("ground", "ice", "rock", "fighting", "fire", "dark", "ghost", "steel", "electric", "flying")) {
            if (set.moves.size() >= 4 || usedTypes.contains(covType)) continue;
            String move = findFirstAvailableMoveFromList(PHYS_MOVES_BY_TYPE.getOrDefault(covType, List.of()));
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(covType); }
        }
    }

    private void addBestSpecMoves(SmogonSet set, List<String> types) {
        List<String> usedTypes = new ArrayList<>();
        for (String type : types) {
            if (set.moves.size() >= 4) break;
            String move = findFirstAvailableMoveFromList(SPEC_MOVES_BY_TYPE.getOrDefault(type, List.of()));
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(type); }
        }
        for (String covType : List.of("ice", "fire", "ground", "psychic", "ghost", "dark", "electric", "fighting", "grass", "fairy")) {
            if (set.moves.size() >= 4 || usedTypes.contains(covType)) continue;
            String move = findFirstAvailableMoveFromList(SPEC_MOVES_BY_TYPE.getOrDefault(covType, List.of()));
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(covType); }
        }
    }

    private void addBestPhysMovesExcluding(SmogonSet set, List<String> types, List<String> excludeSlugs) {
        List<String> usedTypes = new ArrayList<>();
        for (String type : types) {
            if (set.moves.size() >= 4) break;
            String move = findFirstAvailableMoveExcluding(PHYS_MOVES_BY_TYPE.getOrDefault(type, List.of()), excludeSlugs);
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(type); excludeSlugs.add(move.toLowerCase().replace(" ", "-")); }
        }
        for (String covType : List.of("ground", "ice", "rock", "fighting", "fire", "dark", "ghost", "steel", "electric", "flying")) {
            if (set.moves.size() >= 4 || usedTypes.contains(covType)) continue;
            String move = findFirstAvailableMoveExcluding(PHYS_MOVES_BY_TYPE.getOrDefault(covType, List.of()), excludeSlugs);
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(covType); excludeSlugs.add(move.toLowerCase().replace(" ", "-")); }
        }
    }

    private void addBestSpecMovesExcluding(SmogonSet set, List<String> types, List<String> excludeSlugs) {
        List<String> usedTypes = new ArrayList<>();
        for (String type : types) {
            if (set.moves.size() >= 4) break;
            String move = findFirstAvailableMoveExcluding(SPEC_MOVES_BY_TYPE.getOrDefault(type, List.of()), excludeSlugs);
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(type); excludeSlugs.add(move.toLowerCase().replace(" ", "-")); }
        }
        for (String covType : List.of("ice", "fire", "ground", "psychic", "ghost", "dark", "electric", "fighting", "grass", "fairy")) {
            if (set.moves.size() >= 4 || usedTypes.contains(covType)) continue;
            String move = findFirstAvailableMoveExcluding(SPEC_MOVES_BY_TYPE.getOrDefault(covType, List.of()), excludeSlugs);
            if (move != null) { set.moves.add(List.of(move)); usedTypes.add(covType); excludeSlugs.add(move.toLowerCase().replace(" ", "-")); }
        }
    }

    private void addSingleBestPhysMove(SmogonSet set, String type) {
        if (set.moves.size() >= 4) return;
        String move = findFirstAvailableMoveFromList(PHYS_MOVES_BY_TYPE.getOrDefault(type, List.of()));
        if (move != null) set.moves.add(List.of(move));
    }

    private void addSingleBestSpecMove(SmogonSet set, String type) {
        if (set.moves.size() >= 4) return;
        String move = findFirstAvailableMoveFromList(SPEC_MOVES_BY_TYPE.getOrDefault(type, List.of()));
        if (move != null) set.moves.add(List.of(move));
    }

    private String findFirstAvailableMove(List<String> moves) {
        for (String m : moves) {
            if (service.getMoveCached(m.toLowerCase().replace(" ", "-")) != null) return m;
        }
        return null;
    }

    private String findFirstAvailableMoveFromList(List<String> moves) {
        return findFirstAvailableMove(moves);
    }

    private String findFirstAvailableMoveExcluding(List<String> moves, List<String> excludeSlugs) {
        for (String m : moves) {
            String slug = m.toLowerCase().replace(" ", "-");
            if (excludeSlugs.contains(slug)) continue;
            if (service.getMoveCached(slug) != null) return m;
        }
        return null;
    }
}
