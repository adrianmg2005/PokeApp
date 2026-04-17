package org.example.pokeapp.service;

import org.example.pokeapp.model.ChampionsTeam;
import org.example.pokeapp.model.ChampionsTeam.TeamMember;
import org.example.pokeapp.model.ChampionsTeam.Evs;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChampionsTeamService {

    private final List<ChampionsTeam> teams = new ArrayList<>();

    public ChampionsTeamService() {
        loadTeams();
    }

    public List<ChampionsTeam> getAllTeams() {
        return teams;
    }

    public List<ChampionsTeam> getTeamsByArchetype(String archetype) {
        return teams.stream()
                .filter(t -> t.archetype.equalsIgnoreCase(archetype))
                .toList();
    }

    public List<ChampionsTeam> getTeamsByFormat(String format) {
        return teams.stream()
                .filter(t -> t.format.equalsIgnoreCase(format))
                .toList();
    }

    private void loadTeams() {
        // ═══════════════════════════════════════════
        //  DOUBLES TEAMS
        // ═══════════════════════════════════════════

        // 1. Best Rain Team
        teams.add(new ChampionsTeam(
                "Rain Hyper Offense", "3UBW99MLVL", "Doubles", "Rain", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Pelipper", "Damp Rock", "Drizzle", "Modest", "Water",
                                List.of("Weather Ball", "Hurricane", "U-turn", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0)),
                        new TeamMember("Basculegion", "Life Orb", "Swift Swim", "Adamant", "Water",
                                List.of("Wave Crash", "Aqua Jet", "Last Respects", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Sneasler", "Focus Sash", "Poison Touch", "Jolly", "Poison",
                                List.of("Close Combat", "Dire Claw", "Fake Out", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Incineroar", "Safety Goggles", "Intimidate", "Careful", "Fire",
                                List.of("Fake Out", "Knock Off", "Flare Blitz", "Parting Shot"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Rillaboom", "Miracle Seed", "Grassy Surge", "Adamant", "Grass",
                                List.of("Grassy Glide", "Wood Hammer", "Fake Out", "U-turn"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));

        // 2. Aaron Zheng's Sun Team
        teams.add(new ChampionsTeam(
                "Sun Hyper Offense", "NFVS4SYCW2", "Doubles", "Sun", "Aaron Zheng", "Reg M-A",
                List.of(
                        new TeamMember("Mega Charizard Y", "Charizardite Y", "Drought", "Timid", "Fire",
                                List.of("Heat Wave", "Solar Beam", "Weather Ball", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Venusaur", "Focus Sash", "Chlorophyll", "Modest", "Grass",
                                List.of("Sleep Powder", "Leaf Storm", "Sludge Bomb", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Garchomp", "Choice Scarf", "Rough Skin", "Adamant", "Dragon",
                                List.of("Rock Slide", "Dragon Claw", "Earthquake", "Bulldoze"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Wash Rotom", "Leftovers", "Levitate", "Bold", "Water",
                                List.of("Hydro Pump", "Thunderbolt", "Will-O-Wisp", "Protect"),
                                new Evs(252, 0, 128, 128, 0, 0)),
                        new TeamMember("Sneasler", "White Herb", "Unburden", "Jolly", "Fighting",
                                List.of("Dire Claw", "Close Combat", "Rock Tomb", "Fake Out"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Adamant", "Fire",
                                List.of("Darkest Lariat", "Flare Blitz", "Fake Out", "Parting Shot"),
                                new Evs(252, 252, 0, 0, 0, 4))
                )
        ));

        // 3. Mega Meganium Rain
        teams.add(new ChampionsTeam(
                "Mega Meganium Rain", "DM966DN4Y5", "Doubles", "Rain", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Meganium", "Meganiumite", "Overgrow", "Bold", "Grass",
                                List.of("Giga Drain", "Leech Seed", "Protect", "Synthesis"),
                                new Evs(252, 0, 128, 0, 128, 0)),
                        new TeamMember("Pelipper", "Damp Rock", "Drizzle", "Modest", "Water",
                                List.of("Scald", "Hurricane", "U-turn", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0)),
                        new TeamMember("Mega Floette", "Floettite", "Flower Veil", "Calm", "Fairy",
                                List.of("Moonblast", "Calm Mind", "Wish", "Protect"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Sinistcha", "Covert Cloak", "Hospitality", "Bold", "Grass",
                                List.of("Matcha Gotcha", "Shadow Ball", "Strength Sap", "Protect"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Incineroar", "Safety Goggles", "Intimidate", "Careful", "Fire",
                                List.of("Fake Out", "Knock Off", "Flare Blitz", "Parting Shot"),
                                new Evs(252, 0, 4, 0, 252, 0))
                )
        ));

        // 4. アルマ's Balance
        teams.add(new ChampionsTeam(
                "Doubles Balance", "9W9WNY91PT", "Doubles", "Balance", "アルマ", "Reg M-A",
                List.of(
                        new TeamMember("Mega Venusaur", "Venusaurite", "Thick Fat", "Bold", "Grass",
                                List.of("Giga Drain", "Sludge Bomb", "Leech Seed", "Protect"),
                                new Evs(252, 0, 128, 0, 128, 0)),
                        new TeamMember("Mega Lopunny", "Lopunnite", "Scrappy", "Jolly", "Normal",
                                List.of("Fake Out", "Close Combat", "Return", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Hippowdon", "Leftovers", "Sand Stream", "Impish", "Ground",
                                List.of("Earthquake", "Slack Off", "Stealth Rock", "Yawn"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Hydreigon", "Choice Specs", "Levitate", "Modest", "Dark",
                                List.of("Dark Pulse", "Draco Meteor", "Flash Cannon", "U-turn"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Aegislash", "Weakness Policy", "Stance Change", "Quiet", "Steel",
                                List.of("Shadow Ball", "Flash Cannon", "King's Shield", "Shadow Sneak"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Primarina", "Assault Vest", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Ice Beam", "Aqua Jet"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 5. Sand Team
        teams.add(new ChampionsTeam(
                "Sand Rush Offense", "WUG3MSUWST", "Doubles", "Sand", "Key", "Reg M-A",
                List.of(
                        new TeamMember("Tyranitar", "Assault Vest", "Sand Stream", "Adamant", "Rock",
                                List.of("Rock Slide", "Crunch", "Low Kick", "Ice Punch"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Mega Garchomp", "Garchompite", "Sand Force", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Rock Slide", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Rotom-Wash", "Sitrus Berry", "Levitate", "Calm", "Water",
                                List.of("Hydro Pump", "Thunderbolt", "Will-O-Wisp", "Protect"),
                                new Evs(252, 0, 0, 4, 252, 0)),
                        new TeamMember("Volcarona", "Lum Berry", "Flame Body", "Timid", "Fire",
                                List.of("Heat Wave", "Bug Buzz", "Quiver Dance", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mimikyu", "Mental Herb", "Disguise", "Jolly", "Ghost",
                                List.of("Play Rough", "Shadow Claw", "Trick Room", "Shadow Sneak"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Corviknight", "Leftovers", "Mirror Armor", "Impish", "Flying",
                                List.of("Brave Bird", "Body Press", "Iron Defense", "Roost"),
                                new Evs(252, 0, 252, 0, 4, 0))
                )
        ));

        // 6. Offensive Snow (Game8 Guide)
        teams.add(new ChampionsTeam(
                "Offensive Snow", "SNWH09FRL2", "Doubles", "Snow", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Froslass", "Froslassite", "Snow Warning", "Timid", "Ice",
                                List.of("Blizzard", "Shadow Ball", "Aurora Veil", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Wash Rotom", "Choice Scarf", "Levitate", "Modest", "Electric",
                                List.of("Hydro Pump", "Thunderbolt", "Volt Switch", "Trick"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Aerodactyl", "Focus Sash", "Unnerve", "Jolly", "Rock",
                                List.of("Rock Slide", "Dual Wingbeat", "Tailwind", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Garchomp", "White Herb", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Rock Slide", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Kingambit", "Chople Berry", "Defiant", "Adamant", "Dark",
                                List.of("Kowtow Cleave", "Iron Head", "Sucker Punch", "Protect"),
                                new Evs(252, 252, 4, 0, 0, 0)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Adamant", "Fire",
                                List.of("Flare Blitz", "Darkest Lariat", "Fake Out", "Parting Shot"),
                                new Evs(252, 252, 4, 0, 0, 0))
                )
        ));

        // 7. Aurora Veil Snow
        teams.add(new ChampionsTeam(
                "Aurora Veil Snow", "AVLM5NWT8K", "Doubles", "Snow", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Alolan Ninetales", "Light Clay", "Snow Warning", "Timid", "Ice",
                                List.of("Blizzard", "Freeze-Dry", "Aurora Veil", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mega Floette", "Floettite", "Flower Veil", "Calm", "Fairy",
                                List.of("Moonblast", "Calm Mind", "Wish", "Protect"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Maushold", "Safety Goggles", "Friend Guard", "Jolly", "Normal",
                                List.of("Follow Me", "Super Fang", "Helping Hand", "Protect"),
                                new Evs(252, 4, 0, 0, 0, 252)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Careful", "Fire",
                                List.of("Fake Out", "Knock Off", "Flare Blitz", "Parting Shot"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Kingambit", "Assault Vest", "Defiant", "Adamant", "Dark",
                                List.of("Kowtow Cleave", "Iron Head", "Sucker Punch", "Low Kick"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Basculegion", "Choice Scarf", "Adaptability", "Adamant", "Water",
                                List.of("Wave Crash", "Aqua Jet", "Last Respects", "Psychic Fangs"),
                                new Evs(4, 252, 0, 0, 0, 252))
                )
        ));

        // 8. Sand Dragon Dance
        teams.add(new ChampionsTeam(
                "Sand Dragon Dance", "SNDH0M3T4G", "Doubles", "Sand", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Tyranitar", "Tyranitarite", "Sand Stream", "Adamant", "Rock",
                                List.of("Dragon Dance", "Rock Slide", "Crunch", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Excadrill", "Focus Sash", "Sand Rush", "Jolly", "Ground",
                                List.of("Earthquake", "Iron Head", "Rock Slide", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Aerodactyl", "Life Orb", "Unnerve", "Jolly", "Rock",
                                List.of("Rock Slide", "Dual Wingbeat", "Tailwind", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Adamant", "Fire",
                                List.of("Flare Blitz", "Darkest Lariat", "Fake Out", "Parting Shot"),
                                new Evs(252, 252, 4, 0, 0, 0)),
                        new TeamMember("Sinistcha", "Covert Cloak", "Hospitality", "Bold", "Grass",
                                List.of("Matcha Gotcha", "Shadow Ball", "Strength Sap", "Rage Powder"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Garchomp", "Choice Scarf", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Rock Slide", "Poison Jab"),
                                new Evs(4, 252, 0, 0, 0, 252))
                )
        ));

        // 9. Tailwind HO (Blastoise)
        teams.add(new ChampionsTeam(
                "Blastoise Tailwind HO", "TWHO8BLS2Q", "Doubles", "Hyper Offense", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Blastoise", "Blastoisinite", "Mega Launcher", "Modest", "Water",
                                List.of("Water Pulse", "Dark Pulse", "Aura Sphere", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Talonflame", "Sharp Beak", "Gale Wings", "Jolly", "Fire",
                                List.of("Brave Bird", "Flare Blitz", "Tailwind", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Sinistcha", "Covert Cloak", "Hospitality", "Bold", "Grass",
                                List.of("Matcha Gotcha", "Shadow Ball", "Strength Sap", "Rage Powder"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Kingambit", "Assault Vest", "Defiant", "Adamant", "Dark",
                                List.of("Kowtow Cleave", "Iron Head", "Sucker Punch", "Low Kick"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Sneasler", "Focus Sash", "Unburden", "Jolly", "Fighting",
                                List.of("Close Combat", "Dire Claw", "Fake Out", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Garchomp", "Life Orb", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Rock Slide", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252))
                )
        ));

        // 10. Mega Gardevoir Balance
        teams.add(new ChampionsTeam(
                "Gardevoir Aurora Balance", "GRDV4R5TUP", "Doubles", "Balance", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Gardevoir", "Gardevoirite", "Pixilate", "Timid", "Fairy",
                                List.of("Hyper Voice", "Psyshock", "Calm Mind", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Alolan Ninetales", "Light Clay", "Snow Warning", "Timid", "Ice",
                                List.of("Blizzard", "Aurora Veil", "Icy Wind", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Careful", "Fire",
                                List.of("Fake Out", "Flare Blitz", "Knock Off", "Parting Shot"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Maushold", "Safety Goggles", "Friend Guard", "Jolly", "Normal",
                                List.of("Follow Me", "Super Fang", "Helping Hand", "Protect"),
                                new Evs(252, 4, 0, 0, 0, 252)),
                        new TeamMember("Kingambit", "Chople Berry", "Defiant", "Adamant", "Dark",
                                List.of("Kowtow Cleave", "Iron Head", "Sucker Punch", "Protect"),
                                new Evs(252, 252, 4, 0, 0, 0)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 11. Mega Feraligatr Rain
        teams.add(new ChampionsTeam(
                "Feraligatr Rain Setup", "FRLG6RN8PW", "Doubles", "Rain", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Feraligatr", "Feraligatrite", "Sheer Force", "Adamant", "Water",
                                List.of("Waterfall", "Ice Punch", "Dragon Dance", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Pelipper", "Damp Rock", "Drizzle", "Bold", "Water",
                                List.of("Scald", "Hurricane", "U-turn", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Sneasler", "Focus Sash", "Unburden", "Jolly", "Fighting",
                                List.of("Close Combat", "Dire Claw", "Fake Out", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Careful", "Fire",
                                List.of("Fake Out", "Flare Blitz", "Darkest Lariat", "Parting Shot"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Sinistcha", "Covert Cloak", "Hospitality", "Bold", "Grass",
                                List.of("Matcha Gotcha", "Shadow Ball", "Strength Sap", "Rage Powder"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Archaludon", "Choice Specs", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Dragon Pulse", "Thunderbolt"),
                                new Evs(4, 0, 0, 252, 0, 252))
                )
        ));

        // 12. Sun Trick Room
        teams.add(new ChampionsTeam(
                "Sun Trick Room", "SNTR1CK7QR", "Doubles", "Trick Room", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Torkoal", "Charcoal", "Drought", "Quiet", "Fire",
                                List.of("Heat Wave", "Earth Power", "Solar Beam", "Protect"),
                                new Evs(252, 0, 0, 252, 4, 0)),
                        new TeamMember("Mimikyu", "Mental Herb", "Disguise", "Brave", "Fairy",
                                List.of("Trick Room", "Play Rough", "Shadow Claw", "Shadow Sneak"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Mega Venusaur", "Venusaurite", "Thick Fat", "Quiet", "Grass",
                                List.of("Giga Drain", "Sludge Bomb", "Sleep Powder", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Brave", "Fire",
                                List.of("Fake Out", "Flare Blitz", "Darkest Lariat", "Parting Shot"),
                                new Evs(252, 252, 4, 0, 0, 0)),
                        new TeamMember("Sinistcha", "Covert Cloak", "Hospitality", "Quiet", "Grass",
                                List.of("Matcha Gotcha", "Shadow Ball", "Strength Sap", "Rage Powder"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Aegislash", "Weakness Policy", "Stance Change", "Quiet", "Ghost",
                                List.of("Shadow Ball", "Flash Cannon", "King's Shield", "Shadow Sneak"),
                                new Evs(252, 0, 4, 252, 0, 0))
                )
        ));

        // 13. Whimsicott Tailwind HO
        teams.add(new ChampionsTeam(
                "Greninja Tailwind HO", "WHMS5TWL9K", "Doubles", "Hyper Offense", "Game8 Meta", "Reg M-A",
                List.of(
                        new TeamMember("Mega Greninja", "Greninjaite", "Protean", "Timid", "Water",
                                List.of("Hydro Pump", "Dark Pulse", "Ice Beam", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Whimsicott", "Focus Sash", "Prankster", "Timid", "Fairy",
                                List.of("Tailwind", "Moonblast", "Encore", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Garchomp", "Life Orb", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Rock Slide", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Incineroar", "Sitrus Berry", "Intimidate", "Adamant", "Fire",
                                List.of("Fake Out", "Flare Blitz", "Darkest Lariat", "Parting Shot"),
                                new Evs(252, 252, 4, 0, 0, 0)),
                        new TeamMember("Kingambit", "Assault Vest", "Defiant", "Adamant", "Dark",
                                List.of("Kowtow Cleave", "Iron Head", "Sucker Punch", "Low Kick"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Archaludon", "Choice Specs", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Dragon Pulse", "Thunderbolt"),
                                new Evs(4, 0, 0, 252, 0, 252))
                )
        ));

        // ═══════════════════════════════════════════
        //  SINGLES TEAMS
        // ═══════════════════════════════════════════

        // 6. ほかぜ's Balance
        teams.add(new ChampionsTeam(
                "Singles Balance", "BAUUAUHL23", "Singles", "Balance", "ほかぜ", "Reg M-A",
                List.of(
                        new TeamMember("Mega Gengar", "Gengarite", "Shadow Tag", "Timid", "Ghost",
                                List.of("Shadow Ball", "Sludge Bomb", "Focus Blast", "Destiny Bond"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mega Lopunny", "Lopunnite", "Scrappy", "Jolly", "Normal",
                                List.of("Return", "Close Combat", "Fake Out", "Ice Punch"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Hippowdon", "Leftovers", "Sand Stream", "Impish", "Ground",
                                List.of("Earthquake", "Slack Off", "Stealth Rock", "Yawn"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Greninja", "Choice Specs", "Protean", "Timid", "Water",
                                List.of("Hydro Pump", "Dark Pulse", "Ice Beam", "U-turn"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mimikyu", "Life Orb", "Disguise", "Jolly", "Ghost",
                                List.of("Play Rough", "Shadow Claw", "Swords Dance", "Shadow Sneak"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Corviknight", "Leftovers", "Mirror Armor", "Impish", "Flying",
                                List.of("Brave Bird", "Body Press", "Iron Defense", "Roost"),
                                new Evs(252, 0, 252, 0, 4, 0))
                )
        ));

        // 7. Hyper Offense — Mega Charizard Y
        teams.add(new ChampionsTeam(
                "Charizard Y HO", "0AJX50JWXH", "Singles", "Hyper Offense", "ともです", "Reg M-A",
                List.of(
                        new TeamMember("Mega Charizard Y", "Charizardite Y", "Drought", "Timid", "Fire",
                                List.of("Fire Blast", "Solar Beam", "Focus Blast", "Roost"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Gyarados", "Leftovers", "Intimidate", "Adamant", "Water",
                                List.of("Waterfall", "Dragon Dance", "Earthquake", "Bounce"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Mega Floette", "Floettite", "Flower Veil", "Calm", "Fairy",
                                List.of("Moonblast", "Wish", "Protect", "Aromatherapy"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Glimmora", "Focus Sash", "Toxic Debris", "Timid", "Rock",
                                List.of("Power Gem", "Sludge Wave", "Stealth Rock", "Mortal Spin"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Kingambit", "Leftovers", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Swords Dance"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));

        // 8. Starmie Balance
        teams.add(new ChampionsTeam(
                "Mega Starmie Balance", "50K7V5WK6M", "Singles", "Balance", "wing", "Reg M-A",
                List.of(
                        new TeamMember("Mega Starmie", "Starmiite", "Natural Cure", "Timid", "Water",
                                List.of("Hydro Pump", "Psychic", "Ice Beam", "Rapid Spin"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mega Meganium", "Meganiumite", "Overgrow", "Bold", "Grass",
                                List.of("Giga Drain", "Leech Seed", "Synthesis", "Protect"),
                                new Evs(252, 0, 128, 0, 128, 0)),
                        new TeamMember("Glaceon", "Choice Specs", "Snow Cloak", "Modest", "Ice",
                                List.of("Blizzard", "Ice Beam", "Shadow Ball", "Water Pulse"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Aqua Jet"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Ceruledge", "Focus Sash", "Flash Fire", "Adamant", "Fire",
                                List.of("Bitter Blade", "Shadow Claw", "Swords Dance", "Protect"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Kingambit", "Assault Vest", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Low Kick"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));

        // 9. シン's Balance
        teams.add(new ChampionsTeam(
                "Meganium Balance", "JUA4JPPDU4", "Singles", "Balance", "シン", "Reg M-A",
                List.of(
                        new TeamMember("Mega Meganium", "Meganiumite", "Overgrow", "Bold", "Grass",
                                List.of("Giga Drain", "Leech Seed", "Synthesis", "Protect"),
                                new Evs(252, 0, 128, 0, 128, 0)),
                        new TeamMember("Hippowdon", "Leftovers", "Sand Stream", "Impish", "Ground",
                                List.of("Earthquake", "Slack Off", "Stealth Rock", "Yawn"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Sneasler", "Focus Sash", "Unburden", "Jolly", "Fighting",
                                List.of("Dire Claw", "Close Combat", "Swords Dance", "Fake Out"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Ceruledge", "Life Orb", "Flash Fire", "Adamant", "Fire",
                                List.of("Bitter Blade", "Shadow Claw", "Swords Dance", "Close Combat"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Bellibolt", "Leftovers", "Electromorphosis", "Bold", "Electric",
                                List.of("Thunderbolt", "Volt Switch", "Slack Off", "Toxic"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Kingambit", "Leftovers", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Swords Dance"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));

        // 10. くるり's Balance
        teams.add(new ChampionsTeam(
                "Gyarados Balance", "N4MUGMKU0R", "Singles", "Balance", "くるり", "Reg M-A",
                List.of(
                        new TeamMember("Mega Gyarados", "Gyaradosite", "Mold Breaker", "Jolly", "Water",
                                List.of("Waterfall", "Earthquake", "Dragon Dance", "Crunch"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Mega Lucario", "Lucarionite", "Adaptability", "Jolly", "Fighting",
                                List.of("Close Combat", "Bullet Punch", "Swords Dance", "Extreme Speed"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Hippowdon", "Leftovers", "Sand Stream", "Impish", "Ground",
                                List.of("Earthquake", "Slack Off", "Whirlwind", "Yawn"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Rotom", "Choice Scarf", "Levitate", "Timid", "Electric",
                                List.of("Thunderbolt", "Volt Switch", "Shadow Ball", "Trick"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0))
                )
        ));

        // 11. しおいぬ's Hyper Offense
        teams.add(new ChampionsTeam(
                "Gyarados HO", "M3JUXW8QHA", "Singles", "Hyper Offense", "しおいぬ", "Reg M-A",
                List.of(
                        new TeamMember("Mega Gyarados", "Gyaradosite", "Mold Breaker", "Jolly", "Water",
                                List.of("Waterfall", "Earthquake", "Dragon Dance", "Crunch"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Dragonite", "Choice Band", "Multiscale", "Adamant", "Dragon",
                                List.of("Outrage", "Extreme Speed", "Earthquake", "Fire Punch"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Azumarill", "Sitrus Berry", "Huge Power", "Adamant", "Water",
                                List.of("Play Rough", "Aqua Jet", "Belly Drum", "Knock Off"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Infernape", "Focus Sash", "Iron Fist", "Jolly", "Fire",
                                List.of("Close Combat", "Flare Blitz", "Mach Punch", "U-turn"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Espathra", "Leftovers", "Speed Boost", "Timid", "Psychic",
                                List.of("Stored Power", "Calm Mind", "Protect", "Dazzling Gleam"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 12. dポイント's Hyper Offense
        teams.add(new ChampionsTeam(
                "Kangaskhan HO", "TG8AXCBM83", "Singles", "Hyper Offense", "dポイント", "Reg M-A",
                List.of(
                        new TeamMember("Mega Kangaskhan", "Kangaskhanite", "Parental Bond", "Jolly", "Normal",
                                List.of("Return", "Fake Out", "Sucker Punch", "Power-Up Punch"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Mega Greninja", "Greninjaite", "Protean", "Timid", "Water",
                                List.of("Hydro Pump", "Dark Pulse", "Ice Beam", "U-turn"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Corviknight", "Leftovers", "Mirror Armor", "Impish", "Flying",
                                List.of("Brave Bird", "Body Press", "Iron Defense", "Roost"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Ceruledge", "Focus Sash", "Flash Fire", "Adamant", "Fire",
                                List.of("Bitter Blade", "Shadow Claw", "Swords Dance", "Close Combat"),
                                new Evs(4, 252, 0, 0, 0, 252))
                )
        ));

        // 13. ころ's Balance
        teams.add(new ChampionsTeam(
                "Ditto Balance", "AMWBLWC4C5", "Singles", "Balance", "ころ", "Reg M-A",
                List.of(
                        new TeamMember("Ditto", "Choice Scarf", "Imposter", "Relaxed", "Normal",
                                List.of("Transform"),
                                new Evs(252, 0, 0, 0, 0, 0)),
                        new TeamMember("Dragonite", "Choice Band", "Multiscale", "Adamant", "Dragon",
                                List.of("Outrage", "Extreme Speed", "Earthquake", "Fire Punch"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Aegislash", "Weakness Policy", "Stance Change", "Quiet", "Steel",
                                List.of("Shadow Ball", "Flash Cannon", "King's Shield", "Shadow Sneak"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Garganacl", "Leftovers", "Purifying Salt", "Careful", "Rock",
                                List.of("Salt Cure", "Recover", "Stealth Rock", "Body Press"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Mega Scovillain", "Scovillainite", "Chlorophyll", "Modest", "Fire",
                                List.of("Flamethrower", "Energy Ball", "Overheat", "Protect"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 14. はいどら's Balance
        teams.add(new ChampionsTeam(
                "Venusaur Balance", "TH0XEJL6S6", "Singles", "Balance", "はいどら", "Reg M-A",
                List.of(
                        new TeamMember("Mega Venusaur", "Venusaurite", "Thick Fat", "Bold", "Grass",
                                List.of("Giga Drain", "Sludge Bomb", "Leech Seed", "Synthesis"),
                                new Evs(252, 0, 128, 0, 128, 0)),
                        new TeamMember("Mega Scizor", "Scizorite", "Technician", "Adamant", "Steel",
                                List.of("Bullet Punch", "U-turn", "Swords Dance", "Roost"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Tyranitar", "Leftovers", "Sand Stream", "Adamant", "Rock",
                                List.of("Stone Edge", "Crunch", "Stealth Rock", "Earthquake"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Hydreigon", "Choice Specs", "Levitate", "Modest", "Dark",
                                List.of("Dark Pulse", "Draco Meteor", "Flash Cannon", "U-turn"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Skeledirge", "Leftovers", "Unaware", "Bold", "Fire",
                                List.of("Torch Song", "Shadow Ball", "Will-O-Wisp", "Slack Off"),
                                new Evs(252, 0, 252, 0, 4, 0))
                )
        ));

        // 15. 半田ユウ's Balance
        teams.add(new ChampionsTeam(
                "Gengar Balance", "YX2GVSBY8T", "Singles", "Balance", "半田ユウ", "Reg M-A",
                List.of(
                        new TeamMember("Mega Gengar", "Gengarite", "Shadow Tag", "Timid", "Ghost",
                                List.of("Shadow Ball", "Sludge Bomb", "Focus Blast", "Destiny Bond"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Heat Rotom", "Leftovers", "Levitate", "Bold", "Fire",
                                List.of("Overheat", "Volt Switch", "Will-O-Wisp", "Pain Split"),
                                new Evs(252, 0, 128, 0, 128, 0)),
                        new TeamMember("Mega Chesnaught", "Chesnaughtite", "Bulletproof", "Impish", "Grass",
                                List.of("Drain Punch", "Wood Hammer", "Spiky Shield", "Leech Seed"),
                                new Evs(252, 4, 252, 0, 0, 0)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Meowscarada", "Choice Band", "Protean", "Jolly", "Grass",
                                List.of("Flower Trick", "Knock Off", "U-turn", "Play Rough"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 16. かんた's Hyper Offense
        teams.add(new ChampionsTeam(
                "Clefable HO", "AMXAG6G20C", "Singles", "Hyper Offense", "かんた", "Reg M-A",
                List.of(
                        new TeamMember("Mega Clefable", "Clefablite", "Magic Guard", "Bold", "Fairy",
                                List.of("Moonblast", "Soft-Boiled", "Stealth Rock", "Thunder Wave"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Mega Victreebel", "Victrebelite", "Chlorophyll", "Modest", "Grass",
                                List.of("Leaf Storm", "Sludge Bomb", "Weather Ball", "Sleep Powder"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Basculegion", "Choice Scarf", "Adaptability", "Adamant", "Water",
                                List.of("Wave Crash", "Last Respects", "Aqua Jet", "Flip Turn"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Espathra", "Leftovers", "Speed Boost", "Timid", "Psychic",
                                List.of("Stored Power", "Calm Mind", "Protect", "Dazzling Gleam"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Glimmora", "Focus Sash", "Toxic Debris", "Timid", "Rock",
                                List.of("Power Gem", "Sludge Wave", "Stealth Rock", "Mortal Spin"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Kingambit", "Leftovers", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Swords Dance"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));

        // 17. Lig's Balance
        teams.add(new ChampionsTeam(
                "Clefable Scizor Balance", "U8H4T20BVT", "Singles", "Balance", "Lig", "Reg M-A",
                List.of(
                        new TeamMember("Mega Clefable", "Clefablite", "Magic Guard", "Bold", "Fairy",
                                List.of("Moonblast", "Soft-Boiled", "Stealth Rock", "Thunder Wave"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Mega Scizor", "Scizorite", "Technician", "Adamant", "Steel",
                                List.of("Bullet Punch", "U-turn", "Swords Dance", "Roost"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Hydreigon", "Choice Specs", "Levitate", "Modest", "Dark",
                                List.of("Dark Pulse", "Draco Meteor", "Flash Cannon", "U-turn"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Incineroar", "Leftovers", "Intimidate", "Careful", "Fire",
                                List.of("Knock Off", "Flare Blitz", "U-turn", "Will-O-Wisp"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Espathra", "Sitrus Berry", "Speed Boost", "Timid", "Psychic",
                                List.of("Stored Power", "Calm Mind", "Protect", "Dazzling Gleam"),
                                new Evs(4, 0, 0, 252, 0, 252))
                )
        ));

        // 18. hoosedeer's Balance
        teams.add(new ChampionsTeam(
                "Typhlosion Balance", "UL3G4VXJC4", "Singles", "Balance", "hoosedeer", "Reg M-A",
                List.of(
                        new TeamMember("Hisuian Typhlosion", "Choice Specs", "Flash Fire", "Modest", "Fire",
                                List.of("Shadow Ball", "Flamethrower", "Focus Blast", "Eruption"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Hydreigon", "Life Orb", "Levitate", "Timid", "Dark",
                                List.of("Dark Pulse", "Draco Meteor", "Flash Cannon", "Flamethrower"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mega Floette", "Floettite", "Flower Veil", "Calm", "Fairy",
                                List.of("Moonblast", "Wish", "Protect", "Aromatherapy"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Sneasler", "Focus Sash", "Unburden", "Jolly", "Fighting",
                                List.of("Dire Claw", "Close Combat", "Swords Dance", "Fake Out"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Kingambit", "Leftovers", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Swords Dance"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));

        // 19. PBA|モーガン's Balance
        teams.add(new ChampionsTeam(
                "Dragonite Balance", "6PYDNVTVT9", "Singles", "Balance", "PBA|モーガン", "Reg M-A",
                List.of(
                        new TeamMember("Mega Dragonite", "Dragonitite", "Multiscale", "Adamant", "Dragon",
                                List.of("Outrage", "Extreme Speed", "Earthquake", "Dragon Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Mega Scizor", "Scizorite", "Technician", "Adamant", "Steel",
                                List.of("Bullet Punch", "U-turn", "Swords Dance", "Roost"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Hippowdon", "Leftovers", "Sand Stream", "Impish", "Ground",
                                List.of("Earthquake", "Slack Off", "Stealth Rock", "Yawn"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Hydreigon", "Choice Specs", "Levitate", "Modest", "Dark",
                                List.of("Dark Pulse", "Draco Meteor", "Flash Cannon", "U-turn"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Diggersby", "Choice Band", "Huge Power", "Adamant", "Normal",
                                List.of("Earthquake", "Return", "Fire Punch", "Quick Attack"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0))
                )
        ));

        // 20. ねこ's Balance
        teams.add(new ChampionsTeam(
                "Delphox Balance", "83J4SUD0VG", "Singles", "Balance", "ねこ", "Reg M-A",
                List.of(
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Mega Delphox", "Delphoxite", "Blaze", "Timid", "Fire",
                                List.of("Fire Blast", "Psychic", "Grass Knot", "Calm Mind"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mega Floette", "Floettite", "Flower Veil", "Calm", "Fairy",
                                List.of("Moonblast", "Wish", "Protect", "Aromatherapy"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Corviknight", "Leftovers", "Mirror Armor", "Impish", "Flying",
                                List.of("Brave Bird", "Body Press", "Iron Defense", "Roost"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Kingambit", "Leftovers", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Swords Dance"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 21. rikku伊藤's Sun
        teams.add(new ChampionsTeam(
                "Charizard Y Sun", "8GFPDD7VEC", "Singles", "Sun", "rikku伊藤", "Reg M-A",
                List.of(
                        new TeamMember("Mega Charizard Y", "Charizardite Y", "Drought", "Timid", "Fire",
                                List.of("Fire Blast", "Solar Beam", "Focus Blast", "Roost"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Mega Scizor", "Scizorite", "Technician", "Adamant", "Steel",
                                List.of("Bullet Punch", "U-turn", "Swords Dance", "Roost"),
                                new Evs(252, 252, 0, 0, 4, 0)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Primarina", "Leftovers", "Torrent", "Modest", "Water",
                                List.of("Sparkling Aria", "Moonblast", "Calm Mind", "Protect"),
                                new Evs(252, 0, 4, 252, 0, 0)),
                        new TeamMember("Meowscarada", "Choice Band", "Protean", "Jolly", "Grass",
                                List.of("Flower Trick", "Knock Off", "U-turn", "Play Rough"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Archaludon", "Assault Vest", "Stamina", "Modest", "Steel",
                                List.of("Flash Cannon", "Electro Shot", "Body Press", "Dragon Pulse"),
                                new Evs(252, 0, 0, 252, 4, 0))
                )
        ));

        // 22. そら's Balance
        teams.add(new ChampionsTeam(
                "Clefable Starmie Balance", "BELY0DHBVW", "Singles", "Balance", "そら", "Reg M-A",
                List.of(
                        new TeamMember("Mega Clefable", "Clefablite", "Magic Guard", "Bold", "Fairy",
                                List.of("Moonblast", "Soft-Boiled", "Stealth Rock", "Thunder Wave"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Mega Starmie", "Starmiite", "Natural Cure", "Timid", "Water",
                                List.of("Hydro Pump", "Psychic", "Ice Beam", "Rapid Spin"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Corviknight", "Leftovers", "Mirror Armor", "Impish", "Flying",
                                List.of("Brave Bird", "Body Press", "Iron Defense", "Roost"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Sneasler", "Focus Sash", "Unburden", "Jolly", "Fighting",
                                List.of("Dire Claw", "Close Combat", "Swords Dance", "Fake Out"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Bellibolt", "Leftovers", "Electromorphosis", "Bold", "Electric",
                                List.of("Thunderbolt", "Volt Switch", "Slack Off", "Toxic"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Espathra", "Sitrus Berry", "Speed Boost", "Timid", "Psychic",
                                List.of("Stored Power", "Calm Mind", "Protect", "Dazzling Gleam"),
                                new Evs(4, 0, 0, 252, 0, 252))
                )
        ));

        // 23. リック's Sun
        teams.add(new ChampionsTeam(
                "Charizard Y Sun", "A877DSBGTJ", "Singles", "Sun", "リック", "Reg M-A",
                List.of(
                        new TeamMember("Mega Charizard Y", "Charizardite Y", "Drought", "Timid", "Fire",
                                List.of("Fire Blast", "Solar Beam", "Focus Blast", "Roost"),
                                new Evs(4, 0, 0, 252, 0, 252)),
                        new TeamMember("Hisuian Arcanine", "Choice Band", "Rock Head", "Adamant", "Rock",
                                List.of("Head Smash", "Flare Blitz", "Close Combat", "Extreme Speed"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Garchomp", "Rocky Helmet", "Rough Skin", "Jolly", "Dragon",
                                List.of("Earthquake", "Dragon Claw", "Stealth Rock", "Swords Dance"),
                                new Evs(4, 252, 0, 0, 0, 252)),
                        new TeamMember("Hippowdon", "Leftovers", "Sand Stream", "Impish", "Ground",
                                List.of("Earthquake", "Slack Off", "Whirlwind", "Yawn"),
                                new Evs(252, 0, 252, 0, 4, 0)),
                        new TeamMember("Mega Floette", "Floettite", "Flower Veil", "Calm", "Fairy",
                                List.of("Moonblast", "Wish", "Protect", "Aromatherapy"),
                                new Evs(252, 0, 4, 0, 252, 0)),
                        new TeamMember("Kingambit", "Leftovers", "Defiant", "Adamant", "Dark",
                                List.of("Sucker Punch", "Iron Head", "Kowtow Cleave", "Swords Dance"),
                                new Evs(252, 252, 0, 0, 4, 0))
                )
        ));
    }
}
