package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.BlastRadiusConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.IgnitionConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.MulticastConfig;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Registration, compatibility and generation for the three implemented magic mechanics. */
public final class MagicWeaponEnchantment extends CustomEnchantment {
    public static final String MULTICAST = "multicast";
    public static final String BLAST_RADIUS = "blast_radius";
    public static final String IGNITION = "ignition";
    public static final List<String> KEYS = List.of(MULTICAST, BLAST_RADIUS, IGNITION);

    public MagicWeaponEnchantment(String key) { super(key, false); }

    public static boolean supports(SkillType skill, String key) {
        if (MULTICAST.equals(key)) return skill == SkillType.WANDS;
        if (BLAST_RADIUS.equals(key) || IGNITION.equals(key)) return skill == SkillType.STAVES;
        return false;
    }

    public static boolean compatible(ItemStack item, NamespacedKey enchantment) {
        SkillType skill = WeaponIdentityResolver.progressionSkill(item);
        if (enchantment.getNamespace().equals("elitemobs") && KEYS.contains(enchantment.getKey()))
            return supports(skill, enchantment.getKey());
        if (skill != SkillType.STAVES && skill != SkillType.WANDS) return true;
        if (enchantment.getNamespace().equals("minecraft"))
            return EnchantmentGenerator.supportedMagicEnchantments().stream().anyMatch(e -> e.getKey().equals(enchantment));
        return false;
    }

    public static int level(ItemStack item, String key) {
        if (!supports(WeaponIdentityResolver.progressionSkill(item), key)) return 0;
        var config = EnchantmentsConfig.getEnchantment(key);
        if (config == null || !config.isEnabled()) return 0;
        return Math.max(0, Math.min(Math.min(3, config.getMaxEnchantmentLevel()),
                CustomEnchantment.getCustomEnchantmentLevel(item, key)));
    }

    public static void removeLegacyPunch(ItemStack item) {
        if (!WeaponIdentityResolver.isMagicWeapon(item)) return;
        var meta = item.getItemMeta();
        var punch = org.bukkit.enchantments.Enchantment.PUNCH;
        if (!meta.hasEnchant(punch) && !meta.getPersistentDataContainer().has(punch.getKey())) return;
        meta.removeEnchant(punch);
        meta.getPersistentDataContainer().remove(punch.getKey());
        item.setItemMeta(meta);
        new com.magmaguy.elitemobs.items.EliteItemLore(item, false);
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
            result.put(key, level);
        }
        return result;
    }

    public static String effectDescription(String key, int level) {
        return switch (key) {
            case MULTICAST -> (level >= 3 ? 3 : 2) + " bolts, "
                    + Math.round(MulticastConfig.damageMultiplier(level) * 100) + "% damage each";
            case BLAST_RADIUS -> "+" + Math.round((BlastRadiusConfig.radiusMultiplier(level) - 1) * 100) + "% blast radius";
            case IGNITION -> IgnitionConfig.fireTicks(level) / 20D + "s fire on impact";
            default -> "";
        };
    }
}
