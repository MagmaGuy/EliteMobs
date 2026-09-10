package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.BlastRadiusConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.IgnitionConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.MulticastConfig;
import com.magmaguy.elitemobs.skills.SkillType;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** EM owns generation probabilities; FMM Lua definitions own the resulting effects. */
public final class MagicEnchantmentGeneration {
    public static final String MULTICAST = "multicast";
    public static final String BLAST_RADIUS = "blast_radius";
    public static final String IGNITION = "ignition";
    public static final List<String> KEYS = List.of(MULTICAST, BLAST_RADIUS, IGNITION);

    private MagicEnchantmentGeneration() { }

    public static boolean supports(SkillType skill, String key) {
        if (MULTICAST.equals(key)) return skill == SkillType.WANDS;
        if (BLAST_RADIUS.equals(key) || IGNITION.equals(key)) return skill == SkillType.STAVES;
        return false;
    }

    public static HashMap<String, Integer> generate(double itemLevel, SkillType skill) {
        HashMap<String, Integer> result = new HashMap<>();
        if (itemLevel < 10) return result;
        if (ThreadLocalRandom.current().nextDouble()
                >= com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig.getCustomEnchantmentChance()) return result;
        for (String key : KEYS) {
            if (!supports(skill, key)) continue;
            var config = EnchantmentsConfig.getEnchantment(key);
            if (config == null || !config.isEnabled() || !config.isEnabledForProcedurallyGeneratedItems()) continue;
            double chance = switch (key) {
                case MULTICAST -> MulticastConfig.generationChance();
                case BLAST_RADIUS -> BlastRadiusConfig.generationChance();
                default -> IgnitionConfig.generationChance();
            };
            if (ThreadLocalRandom.current().nextDouble() >= chance) continue;
            int maximum = Math.min(3, Math.min(config.getMaxLevel(), config.getMaxEnchantmentLevel()));
            if (maximum <= 0) continue;
            int level;
            if (key.equals(MULTICAST)) {
                level = maximum >= 3 && itemLevel >= MulticastConfig.rareMinimumLevel()
                        && ThreadLocalRandom.current().nextDouble() < MulticastConfig.rareLevelChance() ? 3
                        : itemLevel >= 35 && maximum >= 2 && ThreadLocalRandom.current().nextDouble() < .25 ? 2 : 1;
            } else level = ThreadLocalRandom.current().nextInt(1,
                    Math.min(maximum, itemLevel >= 65 ? 3 : itemLevel >= 30 ? 2 : 1) + 1);
            result.put("freeminecraftmodels:" + key, level);
        }
        return result;
    }

}
