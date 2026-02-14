package com.magmaguy.elitemobs.combatsystem;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the breakdown of damage components for debugging and testing purposes.
 * <p>
 * The offensive damage formula uses multiplicative layers:
 * <pre>
 * formulaDamage = baseDamage × attackSpeedFactor × skillAdjustment × weaponAdjustment × cooldownOrVelocity × sweepMultiplier
 * finalDamage = max(formulaDamage, 1) × damageModifier × combatMultiplier × critMultiplier
 * </pre>
 * <p>
 * Where:
 * <ul>
 *   <li><b>Base Damage</b>: normalizedMobHP / TARGET_HITS_TO_KILL_MOB — scales with mob level</li>
 *   <li><b>Attack Speed Factor</b>: REFERENCE_ATTACK_SPEED / actualAttackSpeed — normalizes DPS across weapons</li>
 *   <li><b>Skill Adjustment</b>: 2^((skillLevel - mobLevel) / 7.5) — exponential skill scaling</li>
 *   <li><b>Weapon Adjustment</b>: two-part linear curve from weapon level vs mob level [0.5, 1.25]</li>
 *   <li><b>Cooldown/Velocity</b>: attack cooldown (melee) or arrow velocity (ranged) [0, 1]</li>
 *   <li><b>Sweep Multiplier</b>: 0.25 for sweep secondary targets, 1.0 otherwise</li>
 *   <li><b>Damage Modifier</b>: boss-specific damage reduction</li>
 *   <li><b>Combat Multiplier</b>: global config multiplier</li>
 *   <li><b>Critical Strike</b>: 1.5x on critical hits</li>
 * </ul>
 */
public class DamageBreakdown {

    // Per-player breakdown tracking
    private static final Map<UUID, DamageBreakdown> activeBreakdowns = new ConcurrentHashMap<>();

    // Formula components
    @Getter @Setter private double baseDamage = 0;
    @Getter @Setter private double attackSpeedFactor = 1.0;
    @Getter @Setter private double skillAdjustment = 1.0;
    @Getter @Setter private double weaponAdjustment = 1.0;
    @Getter @Setter private double cooldownOrVelocity = 1.0;
    @Getter @Setter private double sweepMultiplier = 1.0;
    @Getter @Setter private double thornsDamage = 0;
    @Getter @Setter private double enchantmentMultiplier = 1.0;
    @Getter @Setter private double arrowDamageMultiplier = 1.0;

    // Levels
    @Getter @Setter private int playerSkillLevel = 1;
    @Getter @Setter private int itemLevel = 0;
    @Getter @Setter private int eliteLevel = 1;

    // Multipliers
    @Getter @Setter private double damageModifier = 1.0;
    @Getter @Setter private double combatMultiplier = 1.0;
    @Getter @Setter private double skillBonusMultiplier = 1.0;
    @Getter @Setter private double critMultiplier = 1.0;

    // Computed values
    @Getter private double formulaDamage = 0;
    @Getter private double finalDamage = 0;

    // Metadata
    @Getter @Setter private boolean isCriticalHit = false;
    @Getter @Setter private boolean isRangedAttack = false;
    @Getter @Setter private boolean isSweepAttack = false;
    @Getter @Setter private boolean isThornsAttack = false;
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
     * Calculates what the expected damage SHOULD be given proper gear at matched combat.
     * Uses the new multiplicative formula.
     *
     * @param weaponLevel The level of the weapon being used
     * @param skillLevel  The player's skill level
     * @param targetLevel The elite's level
     * @param attackSpeed The weapon's attack speed
     * @return Expected damage per hit
     */
    public static double calculateExpectedDamage(int weaponLevel, int skillLevel, int targetLevel, double attackSpeed) {
        double base = LevelScaling.calculateBaseDamageToElite(targetLevel);
        double speedFactor = LevelScaling.REFERENCE_ATTACK_SPEED / attackSpeed;
        double skillAdj = LevelScaling.calculateOffensiveSkillAdjustment(skillLevel, targetLevel);
        double weaponAdj = WeaponOffenseCalculator.getWeaponAdjustment(weaponLevel, targetLevel);
        return Math.max(base * speedFactor * skillAdj * weaponAdj, 1);
    }

    /**
     * Cleans up on plugin disable.
     */
    public static void shutdown() {
        activeBreakdowns.clear();
    }

    /**
     * Computes final damage from all components using the multiplicative formula.
     * Call this after setting all components.
     */
    public void compute() {
        if (isThornsAttack) {
            formulaDamage = thornsDamage;
        } else {
            formulaDamage = baseDamage * attackSpeedFactor * skillAdjustment
                    * weaponAdjustment * cooldownOrVelocity * sweepMultiplier
                    * enchantmentMultiplier * arrowDamageMultiplier;
        }

        finalDamage = Math.max(formulaDamage, 1) * damageModifier * combatMultiplier * critMultiplier;
    }

    /**
     * Generates a formatted string breakdown for display.
     */
    public String toFormattedString() {
        compute();

        StringBuilder sb = new StringBuilder();
        sb.append("§6=== DAMAGE BREAKDOWN ===\n");
        sb.append(String.format("§7Weapon: §f%s %s\n", weaponType,
                isThornsAttack ? "§d(Thorns)" : isRangedAttack ? "§e(Ranged)" : isSweepAttack ? "§b(Sweep)" : "§a(Melee)"));
        sb.append("\n§6--- LEVELS ---\n");
        sb.append(String.format("§7Player Skill Level: §f%d\n", playerSkillLevel));
        sb.append(String.format("§7Weapon Level: §f%d\n", itemLevel));
        sb.append(String.format("§7Elite Level: §f%d\n", eliteLevel));
        sb.append(String.format("§7Skill Difference: §f%d\n", playerSkillLevel - eliteLevel));

        if (isThornsAttack) {
            sb.append("\n§6--- THORNS ---\n");
            sb.append(String.format("§7Thorns Damage: §f%.1f\n", thornsDamage));
        } else {
            sb.append("\n§6--- FORMULA COMPONENTS ---\n");
            sb.append(String.format("§7Base Damage (HP/%s): §f%.1f\n",
                    String.format("%.0f", LevelScaling.TARGET_HITS_TO_KILL_MOB), baseDamage));
            if (!isRangedAttack) {
                sb.append(String.format("§7Attack Speed Factor: §f%.2fx\n", attackSpeedFactor));
            }
            sb.append(String.format("§7Skill Adjustment: §f%.3fx %s\n", skillAdjustment,
                    skillAdjustment > 1 ? "§a(bonus)" : skillAdjustment < 1 ? "§c(penalty)" : "§7(matched)"));
            sb.append(String.format("§7Weapon Adjustment: §f%.2fx %s\n", weaponAdjustment,
                    weaponAdjustment > 1 ? "§a(over-level)" : weaponAdjustment < 1 ? "§c(under-level)" : "§7(matched)"));
            sb.append(String.format("§7%s: §f%.2f\n",
                    isRangedAttack ? "Arrow Velocity" : "Attack Cooldown", cooldownOrVelocity));
            if (isSweepAttack) {
                sb.append(String.format("§7Sweep Reduction: §c%.0f%%\n", sweepMultiplier * 100));
            }
            if (enchantmentMultiplier != 1.0) {
                sb.append(String.format("§7Enchantment Multiplier: §e%.2fx\n", enchantmentMultiplier));
            }
            if (arrowDamageMultiplier != 1.0) {
                sb.append(String.format("§7Arrow Damage Multiplier: §c%.0f%%\n", arrowDamageMultiplier * 100));
            }
        }

        sb.append("\n§6--- MULTIPLIERS ---\n");
        sb.append(String.format("§7Damage Modifier: §f%.2fx\n", damageModifier));
        sb.append(String.format("§7Combat Multiplier: §f%.2fx\n", combatMultiplier));
        if (skillBonusMultiplier != 1.0) {
            sb.append(String.format("§7Skill Bonus Multiplier: §e%.2fx\n", skillBonusMultiplier));
        }
        if (isCriticalHit) {
            sb.append(String.format("§7Critical Hit: §e%.1fx\n", critMultiplier));
        }

        sb.append("\n§6--- RESULT ---\n");
        sb.append(String.format("§7Formula Damage: §f%.1f\n", formulaDamage));
        sb.append(String.format("§7After Multipliers: §f%.1f\n", finalDamage));
        sb.append(String.format("§6§lFINAL DAMAGE: §f§l%.1f\n", finalDamage));

        return sb.toString();
    }

    /**
     * Generates a compact one-line summary.
     */
    public String toCompactString() {
        compute();
        return String.format("§7[SkLv%d WpLv%d vs Lv%d] §fBase:%.1f × Skill:%.2f × Wpn:%.2f = §e%.1f%s",
                playerSkillLevel, itemLevel, eliteLevel,
                baseDamage, skillAdjustment, weaponAdjustment,
                finalDamage,
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
