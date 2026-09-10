package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.MagicEnchantmentGeneration;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Immutable, resolved loot policy. Configuration parsing and death-event delivery live elsewhere. */
public record ClassLootProfile(String primary, List<Rule> enchantments, List<Rule> rareEnchantments,
                               List<String> potionEffects) {
    public ClassLootProfile {
        enchantments = List.copyOf(enchantments);
        rareEnchantments = List.copyOf(rareEnchantments);
        potionEffects = List.copyOf(potionEffects);
    }

    public ClassLootProfile(String primary, List<Rule> enchantments, List<Rule> rareEnchantments) {
        this(primary, enchantments, rareEnchantments, List.of());
    }

    public record Rule(String key, int level, double chance) {}
    public record Roll(HashMap<Enchantment, Integer> nativeEnchantments, HashMap<String, Integer> customEnchantments) {}

    public Roll roll(double fraction, int minimumPrimaryLevel) {
        HashMap<Enchantment, Integer> nativeLevels = new HashMap<>();
        HashMap<String, Integer> customLevels = new HashMap<>();
        for (Rule rule : enchantments) add(rule, nativeLevels, customLevels);
        for (Rule rule : rareEnchantments) add(rule, nativeLevels, customLevels);
        Enchantment primaryEnchantment = nativeEnchantment(primary);
        int primaryFloor = Math.min(minimumPrimaryLevel, nativeLevels.getOrDefault(primaryEnchantment, 0));
        // Reserve the primary floor inside the same budget, rather than adding power after the roll.
        return new Roll(ScalableItemConstructor.rollEnchantments(nativeLevels, fraction,
                primaryEnchantment, primaryFloor), customLevels);
    }

    private static void add(Rule rule, HashMap<Enchantment, Integer> nativeLevels, HashMap<String, Integer> customLevels) {
        var config = EnchantmentsConfig.getEnchantment(rule.key);
        if (config == null || !config.isEnabled() || !config.isEnabledForProcedurallyGeneratedItems()) return;
        if (ThreadLocalRandom.current().nextDouble() >= rule.chance) return;
        // maxLevelV2 is the normal procedural/value reference, not the authored-content ceiling.
        int level = Math.min(rule.level, config.getMaxEnchantmentLevel());
        if (MagicEnchantmentGeneration.KEYS.contains(rule.key)) level = Math.min(level, 3);
        if (level <= 0) return;
        Enchantment nativeEnchantment = nativeEnchantment(rule.key);
        if (nativeEnchantment != null) nativeLevels.merge(nativeEnchantment, level, Math::max);
        else customLevels.merge(MagicEnchantmentGeneration.KEYS.contains(rule.key)
                ? "freeminecraftmodels:" + rule.key : rule.key, level, Math::max);
    }

    public static Enchantment nativeEnchantment(String key) {
        return Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase(Locale.ROOT)));
    }

    public static boolean supports(ClassLootFamily family, String key) {
        if (family.isWeapon()) return supports(family.skill(), key);
        // EM counts damage enchantments from worn armor and the offhand, too.
        if (Set.of("sharpness", "smite", "bane_of_arthropods").contains(key)) return true;
        if (Set.of("unbreaking", "mending", "vanishing_curse", "protection").contains(key)) return true;
        if (family == ClassLootFamily.SHIELDS) return false;
        if (Set.of("fire_protection", "blast_protection", "projectile_protection", "thorns", "binding_curse").contains(key))
            return true;
        return switch (family.slot()) {
            case "HELMET" -> Set.of("aqua_affinity", "respiration").contains(key);
            case "LEGGINGS" -> key.equals("swift_sneak");
            case "BOOTS" -> Set.of("feather_falling", "depth_strider", "frost_walker", "soul_speed").contains(key);
            default -> false;
        };
    }

    /** Deliberate EM affinities, including Power on crossbows and Sharpness on melee families. */
    public static boolean supports(SkillType skill, String key) {
        if (Set.of("unbreaking", "mending", "vanishing_curse").contains(key)) return true;
        if (skill == SkillType.STAVES || skill == SkillType.WANDS)
            return key.equals("power") || MagicEnchantmentGeneration.supports(skill, key);
        if (key.equals("critical_strikes")) return true;
        return switch (skill) {
            case BOWS -> Set.of("power", "flame", "punch", "infinity").contains(key);
            case CROSSBOWS -> Set.of("power", "quick_charge", "multishot", "piercing").contains(key);
            case TRIDENTS -> Set.of("sharpness", "impaling", "loyalty", "channeling").contains(key);
            case SWORDS -> Set.of("sharpness", "fire_aspect", "knockback", "looting", "sweeping_edge").contains(key);
            case AXES -> Set.of("sharpness", "fire_aspect", "looting").contains(key);
            case HOES, MACES, SPEARS -> Set.of("sharpness", "fire_aspect").contains(key);
            default -> false;
        };
    }
}
