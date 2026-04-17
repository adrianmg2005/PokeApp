package org.example.pokeapp.model;

import java.util.List;

public class TypeData {
    public DamageRelations damage_relations;

    public static class DamageRelations {
        public List<TypeName> double_damage_from;
        public List<TypeName> half_damage_from;
        public List<TypeName> no_damage_from;
        public List<TypeName> double_damage_to;
        public List<TypeName> half_damage_to;
        public List<TypeName> no_damage_to;
    }

    public static class TypeName {
        public String name;
    }
}