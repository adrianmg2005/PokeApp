package org.example.pokeapp.model;

import java.util.ArrayList;
import java.util.List;

public class EvolutionChain {
    public ChainLink chain;

    public static class ChainLink {
        public Species species;
        public List<EvolutionDetail> evolution_details;
        public List<ChainLink> evolves_to;
    }

    public static class Species {
        public String name;
        public String url;
    }

    public static class EvolutionDetail {
        public Trigger trigger;
        public Integer min_level;
        public Item item;
        public Integer min_happiness;
        public Integer min_affection;
        public String time_of_day;
        public String known_move_type;
        public Item held_item;
        public Item known_move;
        public Item location;
        public Boolean needs_overworld_rain;
        public Boolean turn_upside_down;
        public String gender;
    }

    public static class Trigger {
        public String name;
    }

    public static class Item {
        public String name;
    }

    // Clase plana para facilitar el renderizado: cada etapa de la evolución
    public static class EvolutionStage {
        public String name;
        public int id;
        public String method; // descripción del método de evolución

        public EvolutionStage(String name, int id, String method) {
            this.name = name;
            this.id = id;
            this.method = method;
        }
    }

    /**
     * Convierte el árbol recursivo en una lista lineal de etapas.
     * Para cadenas con ramificaciones (ej: Eevee) incluye todas las ramas.
     */
    public List<List<EvolutionStage>> getEvolutionPaths() {
        List<List<EvolutionStage>> paths = new ArrayList<>();
        if (chain == null) return paths;
        traverseChain(chain, new ArrayList<>(), paths);
        return paths;
    }

    private void traverseChain(ChainLink link, List<EvolutionStage> currentPath, List<List<EvolutionStage>> result) {
        int id = extractId(link.species.url);
        String method = buildMethodDescription(link.evolution_details);
        currentPath.add(new EvolutionStage(link.species.name, id, method));

        if (link.evolves_to == null || link.evolves_to.isEmpty()) {
            result.add(new ArrayList<>(currentPath));
        } else {
            for (ChainLink next : link.evolves_to) {
                traverseChain(next, new ArrayList<>(currentPath), result);
            }
        }
    }

    private int extractId(String url) {
        if (url == null) return 0;
        String clean = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = clean.split("/");
        try { return Integer.parseInt(parts[parts.length - 1]); } catch (Exception e) { return 0; }
    }

    private String buildMethodDescription(List<EvolutionDetail> details) {
        if (details == null || details.isEmpty()) return "";
        EvolutionDetail d = details.get(0);
        if (d.trigger == null) return "";

        return switch (d.trigger.name) {
            case "level-up" -> {
                if (d.min_level != null) yield "Nivel " + d.min_level;
                if (d.min_happiness != null) yield "Felicidad " + d.min_happiness;
                if (d.known_move_type != null) yield "Con mov. tipo " + d.known_move_type;
                if (d.time_of_day != null && !d.time_of_day.isEmpty()) yield "Subir nivel (" + translateTime(d.time_of_day) + ")";
                if (d.location != null) yield "En " + capitalize(d.location.name);
                if (d.needs_overworld_rain != null && d.needs_overworld_rain) yield "Subir nivel (lluvia)";
                if (d.turn_upside_down != null && d.turn_upside_down) yield "Subir nivel (boca abajo)";
                yield "Subir nivel";
            }
            case "use-item" -> d.item != null ? capitalize(d.item.name.replace("-", " ")) : "Usar objeto";
            case "trade" -> d.held_item != null ? "Intercambio con " + capitalize(d.held_item.name.replace("-", " ")) : "Intercambio";
            case "shed" -> "Espacio + Poké Ball";
            default -> capitalize(d.trigger.name.replace("-", " "));
        };
    }

    private String translateTime(String time) {
        return switch (time) {
            case "day" -> "día";
            case "night" -> "noche";
            default -> time;
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
