package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyLocomotion;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.WaterMob;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Physical and behavioral defaults shared by configured and newly introduced creature types. */
final class MobTypeDefaults {
    private MobTypeDefaults() {
    }

    static EliteMindBodyLocomotion locomotion(EntityType type) {
        // Names keep the common plugin compatible with types added after older supported servers.
        return switch (type.name()) {
            case "ALLAY", "BAT", "BEE", "BLAZE", "ENDER_DRAGON", "GHAST", "HAPPY_GHAST",
                 "PARROT", "PHANTOM", "VEX", "WITHER" -> EliteMindBodyLocomotion.FLYING;
            case "COD", "DOLPHIN", "ELDER_GUARDIAN", "GLOW_SQUID", "GUARDIAN", "NAUTILUS",
                 "PUFFERFISH", "SALMON", "SQUID", "TADPOLE", "TROPICAL_FISH", "ZOMBIE_NAUTILUS"
                    -> EliteMindBodyLocomotion.AQUATIC;
            case "AXOLOTL", "DROWNED", "FROG", "TURTLE" -> EliteMindBodyLocomotion.AMPHIBIOUS;
            case "SHULKER" -> EliteMindBodyLocomotion.STATIONARY;
            default -> inheritedLocomotion(type);
        };
    }

    private static EliteMindBodyLocomotion inheritedLocomotion(EntityType type) {
        Class<?> entityClass = type.getEntityClass();
        if (entityClass != null && Flying.class.isAssignableFrom(entityClass)) return EliteMindBodyLocomotion.FLYING;
        if (entityClass != null && WaterMob.class.isAssignableFrom(entityClass)) return EliteMindBodyLocomotion.AQUATIC;
        return EliteMindBodyLocomotion.GROUNDED;
    }

    static String behavior(EntityType type) {
        boolean neutral = switch (type.name()) {
            case "BEE", "ENDERMAN", "GOAT", "IRON_GOLEM", "LLAMA", "PANDA", "PIGLIN",
                 "POLAR_BEAR", "TRADER_LLAMA", "WOLF", "ZOMBIFIED_PIGLIN" -> true;
            default -> false;
        };
        if (!neutral && type.getEntityClass() != null && Enemy.class.isAssignableFrom(type.getEntityClass())) {
            return null;
        }
        return "basic_melee.lua";
    }

    static String nameTemplate(EntityType type) {
        String readableType = Arrays.stream(type.name().toLowerCase(Locale.ROOT).split("_"))
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
        return "&fLvl &2$level &fElite &2" + readableType;
    }
}
