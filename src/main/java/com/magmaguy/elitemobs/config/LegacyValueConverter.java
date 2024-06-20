package com.magmaguy.elitemobs.config;

import java.util.Locale;

public class LegacyValueConverter {
    private LegacyValueConverter() {
    }

    public static String parseEnchantment(String materialName) {
        switch (materialName.toUpperCase(Locale.ROOT)) {
            case "POWER":
                return "POWER";
            case "ARROW_FIRE":
                return "FLAME";
            case "ARROW_INFINITE":
                return "INFINITY";
            case "ARROW_KNOCKBACK":
                return "PUNCH";
            case "BANE_OF_ARTHROPODS":
                return "BANE_OF_ARTHROPODS";
            case "SMITE":
                return "SMITE";
            case "EFFICIENCY":
                return "EFFICIENCY";
            case "DURABILITY":
                return "UNBREAKING";
            case "FORTUNE":
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
            case "FEATHER_FALLING":
                return "FEATHER_FALLING";
            case "FIRE_PROTECTION":
                return "FIRE_PROTECTION";
            case "PROJECTILE_PROTECTION":
                return "PROJECTILE_PROTECTION";
            case "AQUA_AFFINITY":
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

    public static String parseDeserializedBlocks(String originalDeserializedBlock) {
        if (originalDeserializedBlock.endsWith("grass"))
            return originalDeserializedBlock.replace("grass", "grass_block[snowy=false]");
        return originalDeserializedBlock;
    }
}
