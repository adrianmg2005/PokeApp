package org.example.pokeapp.util;

import org.example.pokeapp.service.PokeApiService;

import java.util.Arrays;

/**
 * Static translation helpers (Spanish labels for colours, shapes, habitats, etc.).
 */
public final class TranslationUtils {

    private TranslationUtils() {}

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Convert a slug like "strong-jaw" into "Strong Jaw". */
    public static String formatMoveName(String slug) {
        if (slug == null) return "";
        return Arrays.stream(slug.split("-"))
                .map(w -> w.isEmpty() ? "" : Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .reduce((a, b) -> a + " " + b).orElse(slug);
    }

    public static String translateDamageClass(String dc) {
        return switch (dc) {
            case "physical" -> "FÍSICO";
            case "special"  -> "ESPECIAL";
            case "status"   -> "ESTADO";
            default         -> dc.toUpperCase();
        };
    }

    public static String translateColor(String color) {
        return switch (color.toLowerCase()) {
            case "black"  -> "Negro";
            case "blue"   -> "Azul";
            case "brown"  -> "Marrón";
            case "gray"   -> "Gris";
            case "green"  -> "Verde";
            case "pink"   -> "Rosa";
            case "purple" -> "Púrpura";
            case "red"    -> "Rojo";
            case "white"  -> "Blanco";
            case "yellow" -> "Amarillo";
            default       -> capitalize(color);
        };
    }

    public static String translateShape(String shape) {
        return switch (shape.toLowerCase()) {
            case "ball"      -> "Esfera";
            case "squiggle"  -> "Ondulado";
            case "fish"      -> "Pez";
            case "arms"      -> "Con brazos";
            case "blob"      -> "Amorfo";
            case "upright"   -> "Bípedo";
            case "legs"      -> "Con patas";
            case "quadruped" -> "Cuadrúpedo";
            case "wings"     -> "Con alas";
            case "tentacles" -> "Tentáculos";
            case "heads"     -> "Múltiples cabezas";
            case "humanoid"  -> "Humanoide";
            case "bug-wings" -> "Insecto alado";
            case "armor"     -> "Acorazado";
            default          -> capitalize(shape);
        };
    }

    public static String translateHabitat(String habitat) {
        return switch (habitat.toLowerCase()) {
            case "cave"          -> "Cueva";
            case "forest"        -> "Bosque";
            case "grassland"     -> "Pradera";
            case "mountain"      -> "Montaña";
            case "rare"          -> "Raro";
            case "rough-terrain" -> "Terreno accidentado";
            case "sea"           -> "Mar";
            case "urban"         -> "Urbano";
            case "waters-edge"   -> "Orilla del agua";
            default              -> capitalize(habitat);
        };
    }

    public static String translateGrowthRate(String rate) {
        return switch (rate.toLowerCase()) {
            case "slow"                 -> "Lento";
            case "medium"               -> "Medio";
            case "fast"                 -> "Rápido";
            case "medium-slow"          -> "Medio-Lento";
            case "slow-then-very-fast"  -> "Lento → Muy Rápido";
            case "fast-then-very-slow"  -> "Rápido → Muy Lento";
            case "erratic"              -> "Errático";
            case "fluctuating"          -> "Fluctuante";
            default                     -> capitalize(rate.replace("-", " "));
        };
    }

    public static String translateEggGroup(String group) {
        return switch (group.toLowerCase()) {
            case "monster"                    -> "Monstruo";
            case "water1"                     -> "Agua 1";
            case "water2"                     -> "Agua 2";
            case "water3"                     -> "Agua 3";
            case "bug"                        -> "Bicho";
            case "flying"                     -> "Volador";
            case "ground"                     -> "Campo";
            case "fairy"                      -> "Hada";
            case "plant"                      -> "Planta";
            case "humanshape", "human-like"   -> "Humanoide";
            case "mineral"                    -> "Mineral";
            case "amorphous"                  -> "Amorfo";
            case "ditto"                      -> "Ditto";
            case "dragon"                     -> "Dragón";
            case "no-eggs"                    -> "Sin huevos";
            case "indeterminate"              -> "Indeterminado";
            default                           -> capitalize(group.replace("-", " "));
        };
    }

    /** Prettify a Pokémon‑form API name into a Spanish label.  e.g. "charizard-mega-x" → "Charizard (Mega X)". */
    public static String formatFormName(String formApiName, String baseName, PokeApiService service) {
        String base = service.getSpanishPokemonName(baseName);
        if (formApiName.equalsIgnoreCase(baseName)) return base;

        String suffix = formApiName.toLowerCase().replace(baseName.toLowerCase(), "").replaceFirst("^-", "");
        if (suffix.isEmpty()) return base;

        String label = switch (suffix) {
            case "mega"          -> "Mega";
            case "mega-x"        -> "Mega X";
            case "mega-y"        -> "Mega Y";
            case "gmax"          -> "Gigamax";
            case "alola"         -> "Alola";
            case "galar"         -> "Galar";
            case "hisui"         -> "Hisui";
            case "paldea"        -> "Paldea";
            case "origin"        -> "Origen";
            case "sky"           -> "Cielo";
            case "primal"        -> "Primigenio";
            case "therian"       -> "Tótem";
            case "incarnate"     -> "Avatar";
            case "black"         -> "Negro";
            case "white"         -> "Blanco";
            case "resolute"      -> "Resoluto";
            case "pirouette"     -> "Danza";
            case "ash"           -> "Ash";
            case "10"            -> "10%";
            case "complete"      -> "Completa";
            case "school"        -> "Banco";
            case "dusk-mane"     -> "Melena Crepuscular";
            case "dawn-wings"    -> "Alas del Alba";
            case "ultra"         -> "Ultra";
            case "crowned"       -> "Coronado";
            case "eternamax"     -> "Eternamax";
            case "rapid-strike"  -> "Flujo Rápido";
            case "single-strike" -> "Golpe Único";
            case "ice"           -> "Hielo";
            case "shadow"        -> "Sombra";
            case "heat"          -> "Calor";
            case "wash"          -> "Lavado";
            case "frost"         -> "Frío";
            case "fan"           -> "Ventilador";
            case "mow"           -> "Corte";
            case "attack"        -> "Ataque";
            case "defense"       -> "Defensa";
            case "speed"         -> "Velocidad";
            case "sandy"         -> "Arena";
            case "trash"         -> "Basura";
            case "plant"         -> "Planta";
            case "sunshine"      -> "Sol";
            case "overcast"      -> "Nublado";
            case "snowy"         -> "Nieve";
            case "rainy"         -> "Lluvia";
            case "zen"           -> "Zen";
            case "blade"         -> "Espada";
            case "shield"        -> "Escudo";
            case "small"         -> "Pequeño";
            case "large"         -> "Grande";
            case "super"         -> "Super";
            case "unbound"       -> "Desatado";
            case "midnight"      -> "Nocturno";
            case "dusk"          -> "Crepúsculo";
            case "midday"        -> "Diurno";
            case "low-key"       -> "Grave";
            case "amped"         -> "Agudo";
            case "noice"         -> "Hielo Puro";
            case "hangry"        -> "Furioso";
            case "gulping"       -> "Tragón";
            case "gorging"       -> "Voraz";
            case "bloodmoon"     -> "Luna Roja";
            case "wellspring"    -> "Fuente";
            case "hearthflame"   -> "Llama";
            case "cornerstone"   -> "Cimiento";
            default              -> capitalize(suffix.replace("-", " "));
        };
        return base + " (" + label + ")";
    }

    public static String getGameColor(String version) {
        return switch (version.toLowerCase()) {
            case "red", "ruby", "omega-ruby", "scarlet"      -> "rgba(220, 50, 50, 0.35)";
            case "blue", "sapphire", "alpha-sapphire"         -> "rgba(50, 100, 220, 0.35)";
            case "yellow", "lets-go-pikachu"                  -> "rgba(220, 200, 50, 0.35)";
            case "gold", "heartgold"                          -> "rgba(200, 170, 50, 0.35)";
            case "silver", "soulsilver"                       -> "rgba(180, 180, 200, 0.35)";
            case "crystal"                                    -> "rgba(100, 200, 220, 0.35)";
            case "emerald"                                    -> "rgba(50, 180, 80, 0.35)";
            case "firered"                                    -> "rgba(230, 80, 50, 0.35)";
            case "leafgreen"                                  -> "rgba(80, 200, 80, 0.35)";
            case "diamond", "brilliant-diamond"               -> "rgba(120, 140, 220, 0.35)";
            case "pearl", "shining-pearl"                     -> "rgba(200, 150, 180, 0.35)";
            case "platinum"                                   -> "rgba(160, 160, 180, 0.35)";
            case "black", "black-2"                           -> "rgba(60, 60, 60, 0.5)";
            case "white", "white-2"                           -> "rgba(200, 200, 200, 0.25)";
            case "x"                                          -> "rgba(50, 100, 180, 0.35)";
            case "y"                                          -> "rgba(200, 50, 50, 0.35)";
            case "sun", "ultra-sun"                           -> "rgba(230, 160, 50, 0.35)";
            case "moon", "ultra-moon"                         -> "rgba(80, 80, 180, 0.35)";
            case "lets-go-eevee"                              -> "rgba(180, 140, 80, 0.35)";
            case "sword"                                      -> "rgba(50, 130, 200, 0.35)";
            case "shield"                                     -> "rgba(200, 50, 80, 0.35)";
            case "legends-arceus"                             -> "rgba(50, 100, 120, 0.35)";
            case "violet"                                     -> "rgba(140, 50, 200, 0.35)";
            default                                           -> "rgba(100, 100, 100, 0.35)";
        };
    }
}
