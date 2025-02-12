package com.magmaguy.elitemobs.config;

import org.bukkit.Particle;

import java.util.Locale;

public class LegacyValueConverter {
    private LegacyValueConverter() {
    }

    public static String parseEnchantment(String materialName) {
        switch (materialName.toUpperCase(Locale.ROOT)) {
            case "DAMAGE_ALL":
                return "SHARPNESS";
            case "ARROW_DAMAGE":
                return "POWER";
            case "ARROW_FIRE":
                return "FLAME";
            case "ARROW_INFINITE":
                return "INFINITY";
            case "ARROW_KNOCKBACK":
                return "PUNCH";
            case "DAMAGE_ARTHROPODS":
                return "BANE_OF_ARTHROPODS";
            case "DAMAGE_UNDEAD":
                return "SMITE";
            case "DIG_SPEED":
                return "EFFICIENCY";
            case "DURABILITY":
                return "UNBREAKING";
            case "LOOT_BONUS_BLOCKS":
                return "FORTUNE";
            case "LOOT_BONUS_MOBS":
                return "LOOTING";
            case "LUCK":
                return "LUCK_OF_THE_SEA";
            case "RESPIRATION":
                return "RESPIRATION";
            case "PROTECTION_ENVIRONMENTAL":
                return "PROTECTION";
            case "BLAST_PROTECTION":
                return "BLAST_PROTECTION";
            case "PROTECTION_FALL":
                return "FEATHER_FALLING";
            case "PROTECTION_FIRE":
                return "FIRE_PROTECTION";
            case "PROTECTION_PROJECTILE":
                return "PROJECTILE_PROTECTION";
            case "WATER_WORKER":
                return "AQUA_AFFINITY";
            default:
                return materialName;
        }
    }

    public static String parsePotionEffect(String potionEffectName) {
        switch (potionEffectName.toUpperCase(Locale.ROOT)) {
            case "CONFUSION":
                return "NAUSEA";
            case "DAMAGE_RESISTANCE":
                return "RESISTANCE";
            case "FAST_DIGGING":
                return "HASTE";
            case "HARM":
                return "INSTANT_DAMAGE";
            case "HEAL":
                return "INSTANT_HEALTH";
            case "INCREASE_DAMAGE":
                return "STRENGTH";
            case "JUMP":
                return "JUMP_BOOST";
            case "SLOW":
                return "SLOWNESS";
            case "SLOW_DIGGING":
                return "MINING_FATIGUE";
            default:
                return potionEffectName;
        }
    }

    public static String parseParticle(String potionEffectName) {
        switch (potionEffectName.toUpperCase(Locale.ROOT)) {
            case "EXPLOSION_NORMAL", "EXPLOSION_LARGE":
                return Particle.EXPLOSION.toString();
            case "SMOKE_NORMAL":
                return Particle.SMOKE.toString();
            case "SMOKE_LARGE":
                return Particle.LARGE_SMOKE.toString();
            case "REDSTONE":
                return Particle.DUST.toString();
            case "SLIME":
                return Particle.ITEM_SLIME.toString();
            case "DRIP_LAVA":
                return Particle.DRIPPING_WATER.toString();
            case "EXPLOSION_HUGE":
                return Particle.EXPLOSION.toString();
            case "SNOWBALL":
                return Particle.SNOWFLAKE.toString();
            case "SPELL":
                return Particle.WITCH.toString();
            case "DRIP_WATER":
                return Particle.DRIPPING_WATER.toString();
            case "SPELL_MOB":
                return Particle.WITCH.toString();
            case "VILLAGER_ANGRY":
                return Particle.ANGRY_VILLAGER.toString();
            case "WATER_BUBBLE":
                return Particle.UNDERWATER.toString();
            case "VILLAGER_HAPPY":
                return Particle.HAPPY_VILLAGER.toString();
            case "WATER_SPLASH":
                return Particle.SPLASH.toString();
            default:
                return potionEffectName;
        }
    }

    public static String parseDeserializedBlocks(String originalDeserializedBlock) {
        if (originalDeserializedBlock.endsWith("grass"))
            return originalDeserializedBlock.replace("grass", "grass_block[snowy=false]");
        return originalDeserializedBlock;
    }
}
