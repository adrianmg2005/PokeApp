package org.example.pokeapp.model;

import java.util.List;

public class ChampionsTeam {
    public String name;
    public String teamCode;
    public String format;      // "Doubles" or "Singles"
    public String archetype;   // "Rain", "Sun", "Sand", "Snow", "Trick Room", "Hyper Offense", "Balance", etc.
    public String author;
    public String regulation;
    public List<TeamMember> pokemon;

    public ChampionsTeam() {}

    public ChampionsTeam(String name, String teamCode, String format, String archetype, String author, String regulation, List<TeamMember> pokemon) {
        this.name = name;
        this.teamCode = teamCode;
        this.format = format;
        this.archetype = archetype;
        this.author = author;
        this.regulation = regulation;
        this.pokemon = pokemon;
    }

    public static class TeamMember {
        public String name;
        public String item;
        public String ability;
        public String nature;
        public String teraType;
        public List<String> moves;
        public Evs evs;

        public TeamMember() {}

        public TeamMember(String name, String item, String ability, String nature, String teraType, List<String> moves, Evs evs) {
            this.name = name;
            this.item = item;
            this.ability = ability;
            this.nature = nature;
            this.teraType = teraType;
            this.moves = moves;
            this.evs = evs;
        }
    }

    public static class Evs {
        public int hp, atk, def, spa, spd, spe;

        public Evs() {}

        public Evs(int hp, int atk, int def, int spa, int spd, int spe) {
            this.hp = hp;
            this.atk = atk;
            this.def = def;
            this.spa = spa;
            this.spd = spd;
            this.spe = spe;
        }
    }
}
