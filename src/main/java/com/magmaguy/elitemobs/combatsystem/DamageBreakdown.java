package com.magmaguy.elitemobs.combatsystem;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the breakdown of damage components for debugging and testing purposes.
 * <p>
 * Damage in EliteMobs is calculated from multiple sources:
 * <ul>
 *   <li><b>Vanilla Damage</b>: Base damage from the weapon type in Minecraft</li>
 *   <li><b>Skill Damage</b>: Contribution from player's weapon skill level (50% of elite damage)</li>
 *   <li><b>Item Damage</b>: Contribution from item's elite level (50% of elite damage)</li>
 *   <li><b>Enchantment Damage</b>: Bonus from secondary enchantments (Smite, Bane, etc.)</li>
 *   <li><b>Thorns Damage</b>: Damage from thorns enchantments</li>
 *   <li><b>Damage Modifier</b>: Boss-specific damage reduction (damageModifier config)</li>
 *   <li><b>Combat Multiplier</b>: Global config multiplier (normalizedDamageToEliteMultiplier)</li>
 *   <li><b>Skill Bonus Multiplier</b>: From active skill bonuses</li>
 *   <li><b>Level Scaling</b>: Modifier based on player vs enemy level difference</li>
 *   <li><b>Critical Strike</b>: 1.5x multiplier on critical hits</li>
 * </ul>
 * <p>
 * The final damage formula is:
 * <pre>
 * eliteDamage = (skillDamage * 0.5) + (itemDamage * 0.5) + enchantmentDamage
 * preLevelDamage = (vanillaDamage + eliteDamage) * damageModifier * combatMultiplier * skillBonusMultiplier
 * postLevelDamage = preLevelDamage / levelScalingModifier
 * finalDamage = postLevelDamage * critMultiplier
 * </pre>
 */
public class DamageBreakdown {

    // Per-player breakdown tracking
    private static final Map<UUID, DamageBreakdown> activeBreakdowns = new HashMap<>();

    // Input components
    @Getter @Setter private double vanillaDamage = 0;
    @Getter @Setter private double skillDamage = 0;
    @Getter @Setter private double itemDamage = 0;
    @Getter @Setter private double enchantmentDamage = 0;
    @Getter @Setter private double thornsDamage = 0;

    // Levels
    @Getter @Setter private int playerLevel = 1;
    @Getter @Setter private int playerSkillLevel = 1;
    @Getter @Setter private int itemLevel = 0;
    @Getter @Setter private int eliteLevel = 1;

    // Multipliers
    @Getter @Setter private double attackCooldownMultiplier = 1.0;
    @Getter @Setter private double damageModifier = 1.0;
    @Getter @Setter private double combatMultiplier = 1.0;
    @Getter @Setter private double skillBonusMultiplier = 1.0;
    @Getter @Setter private double levelScalingModifier = 1.0;
    @Getter @Setter private double critMultiplier = 1.0;

    // Computed values
    @Getter private double eliteDamage = 0;
    @Getter private double preLevelDamage = 0;
    @Getter private double postLevelDamage = 0;
    @Getter private double finalDamage = 0;

    // Metadata
    @Getter @Setter private boolean isCriticalHit = false;
    @Getter @Setter private boolean isRangedAttack = false;
    @Getter @Setter private String weaponType = "UNKNOWN";

    /**
     * Starts tracking a damage breakdown for a player.
     */
    public static DamageBreakdown startTracking(Player player) {
        DamageBreakdown breakdown = new DamageBreakdown();
        activeBreakdowns.put(player.getUniqueId(), breakdown);
        return breakdown;
    }

    /**
     * Gets the active breakdown for a player.
     */
    public static DamageBreakdown getActiveBreakdown(Player player) {
        return activeBreakdowns.get(player.getUniqueId());
    }

    /**
     * Gets the active breakdown for a player by UUID.
     */
    public static DamageBreakdown getActiveBreakdown(UUID uuid) {
        return activeBreakdowns.get(uuid);
    }

    /**
     * Stops tracking and returns the breakdown.
     */
    public static DamageBreakdown stopTracking(Player player) {
        return activeBreakdowns.remove(player.getUniqueId());
    }

    /**
     * Clears all active breakdowns.
     */
    public static void clearAll() {
        activeBreakdowns.clear();
    }

    /**
     * Checks if breakdown tracking is active for a player.
     */
    public static boolean isTracking(Player player) {
        return activeBreakdowns.containsKey(player.getUniqueId());
    }

    /**
     * Checks if breakdown tracking is active for a player UUID.
     */
    public static boolean isTracking(UUID uuid) {
        return activeBreakdowns.containsKey(uuid);
    }

    /**
     * Calculates what the expected damage SHOULD be given proper gear.
     * Useful for comparing expected vs actual damage.
     *
     * @param weaponLevel   The level of the weapon being used
     * @param skillLevel    The player's skill level
     * @param targetLevel   The elite's level
     * @param attackSpeed   The weapon's attack speed
     * @return Expected damage per hit
     */
    public static double calculateExpectedDamage(int weaponLevel, int skillLevel, int targetLevel, double attackSpeed) {
        // Skill damage contribution
        double skillDamage = skillLevel * CombatSystem.DPS_PER_LEVEL / attackSpeed;

        // Item damage contribution (same formula, using weapon level)
        double itemDamage = weaponLevel * CombatSystem.DPS_PER_LEVEL / attackSpeed;

        // Effective elite damage with 50/50 split
        double eliteDamage = (skillDamage * CombatSystem.SKILL_CONTRIBUTION_RATIO)
                + (itemDamage * CombatSystem.ITEM_CONTRIBUTION_RATIO);

        // Assume vanilla iron sword damage (~6)
        double vanillaDamage = 6.0;

        // Total pre-level damage
        double totalDamage = vanillaDamage + eliteDamage;

        // Apply level scaling
        double levelModifier = LevelScaling.getLevelModifier(skillLevel, targetLevel);
        totalDamage = totalDamage / levelModifier;

        return totalDamage;
    }

    /**
     * Cleans up on plugin disable.
     */
    public static void shutdown() {
        activeBreakdowns.clear();
    }

    /**
     * Computes intermediate and final damage values from the components.
     * Call this after setting all components.
     */
    public void compute() {
        // Calculate effective elite damage (50/50 skill/item split + enchantments)
        eliteDamage = (skillDamage * CombatSystem.SKILL_CONTRIBUTION_RATIO)
                + (itemDamage * CombatSystem.ITEM_CONTRIBUTION_RATIO)
                + enchantmentDamage
                + thornsDamage;

        // Apply attack cooldown to elite damage (melee only)
        double effectiveEliteDamage = eliteDamage * attackCooldownMultiplier;

        // Combine vanilla and elite damage, apply multipliers
        preLevelDamage = (vanillaDamage + effectiveEliteDamage) * damageModifier * combatMultiplier * skillBonusMultiplier;

        // Apply level scaling (divides by modifier when attacking higher level)
        postLevelDamage = preLevelDamage / levelScalingModifier;

        // Apply critical hit multiplier
        finalDamage = postLevelDamage * critMultiplier;
    }

    /**
     * Generates a formatted string breakdown for display.
     */
    public String toFormattedString() {
        compute();

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== DAMAGE BREAKDOWN ===\n");
        sb.append(String.format("§7Weapon: §f%s %s\n", weaponType, isRangedAttack ? "§e(Ranged)" : "§a(Melee)"));
        sb.append("\n§6--- LEVELS ---\n");
        sb.append(String.format("§7Player Skill Level: §f%d\n", playerSkillLevel));
        sb.append(String.format("§7Item Level: §f%d\n", itemLevel));
        sb.append(String.format("§7Effective Player Level: §f%d\n", playerLevel));
        sb.append(String.format("§7Elite Level: §f%d\n", eliteLevel));
        sb.append(String.format("§7Level Difference: §f%d\n", playerLevel - eliteLevel));

        sb.append("\n§6--- DAMAGE COMPONENTS ---\n");
        sb.append(String.format("§7Vanilla Damage: §f%.1f\n", vanillaDamage));
        sb.append(String.format("§7Skill Damage (raw): §f%.1f\n", skillDamage));
        sb.append(String.format("§7Item Damage (raw): §f%.1f\n", itemDamage));
        sb.append(String.format("§7  -> Skill Contribution (%.0f%%): §f%.1f\n",
                CombatSystem.SKILL_CONTRIBUTION_RATIO * 100, skillDamage * CombatSystem.SKILL_CONTRIBUTION_RATIO));
        sb.append(String.format("§7  -> Item Contribution (%.0f%%): §f%.1f\n",
                CombatSystem.ITEM_CONTRIBUTION_RATIO * 100, itemDamage * CombatSystem.ITEM_CONTRIBUTION_RATIO));
        if (enchantmentDamage > 0) {
            sb.append(String.format("§7Enchantment Damage: §f%.1f\n", enchantmentDamage));
        }
        if (thornsDamage > 0) {
            sb.append(String.format("§7Thorns Damage: §f%.1f\n", thornsDamage));
        }
        sb.append(String.format("§7§lTotal Elite Damage: §f%.1f\n", eliteDamage));

        sb.append("\n§6--- MULTIPLIERS ---\n");
        if (attackCooldownMultiplier < 1.0) {
            sb.append(String.format("§7Attack Cooldown: §c%.1f%%\n", attackCooldownMultiplier * 100));
        }
        sb.append(String.format("§7Damage Modifier: §f%.2fx\n", damageModifier));
        sb.append(String.format("§7Combat Multiplier: §f%.2fx\n", combatMultiplier));
        if (skillBonusMultiplier != 1.0) {
            sb.append(String.format("§7Skill Bonus Multiplier: §e%.2fx\n", skillBonusMultiplier));
        }
        sb.append(String.format("§7Level Scaling: §f%.3fx %s\n",
                levelScalingModifier,
                levelScalingModifier > 1 ? "§c(penalty)" : levelScalingModifier < 1 ? "§a(bonus)" : "§7(neutral)"));
        if (isCriticalHit) {
            sb.append(String.format("§7Critical Hit: §e%.1fx\n", critMultiplier));
        }

        sb.append("\n§6--- DAMAGE CALCULATION ---\n");
        sb.append(String.format("§7Vanilla + Elite: §f%.1f + %.1f = %.1f\n",
                vanillaDamage, eliteDamage * attackCooldownMultiplier, vanillaDamage + eliteDamage * attackCooldownMultiplier));
        sb.append(String.format("§7After Multipliers: §f%.1f\n", preLevelDamage));
        sb.append(String.format("§7After Level Scaling: §f%.1f\n", postLevelDamage));
        if (isCriticalHit) {
            sb.append(String.format("§7After Crit: §e%.1f\n", finalDamage));
        }
        sb.append(String.format("§6§lFINAL DAMAGE: §f§l%.1f\n", finalDamage));

        return sb.toString();
    }

    /**
     * Generates a compact one-line summary.
     */
    public String toCompactString() {
        compute();
        return String.format("§7[Lv%d vs Lv%d] §fVanilla:%.1f + Elite:%.1f = §e%.1f §7(scale:%.2fx%s)",
                playerLevel, eliteLevel, vanillaDamage, eliteDamage,
                finalDamage, levelScalingModifier,
                isCriticalHit ? " §cCRIT" : "");
    }

    /**
     * Generates a comparison of expected vs actual damage.
     */
    public String toComparisonString(double actualDamage) {
        compute();

        double expected = finalDamage;
        double difference = actualDamage - expected;
        double percentDiff = expected > 0 ? (difference / expected) * 100 : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== EXPECTED VS ACTUAL ===\n");
        sb.append(String.format("§7Expected Damage: §f%.1f\n", expected));
        sb.append(String.format("§7Actual Damage: §f%.1f\n", actualDamage));
        sb.append(String.format("§7Difference: §%s%.1f (%.1f%%)\n",
                Math.abs(percentDiff) < 5 ? "a" : "c",
                difference, percentDiff));

        if (Math.abs(percentDiff) > 10) {
            sb.append("\n§c⚠ Large discrepancy detected! Check:\n");
            sb.append("§7- Skill bonuses active?\n");
            sb.append("§7- Enchantment effects?\n");
            sb.append("§7- Boss damage modifiers?\n");
            sb.append("§7- Plugin conflicts?\n");
        }

        return sb.toString();
    }
}
