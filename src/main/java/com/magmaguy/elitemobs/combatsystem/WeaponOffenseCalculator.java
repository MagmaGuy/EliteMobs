package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Centralized, read-only weapon offense calculator for the player→elite damage formula.
 * <p>
 * This class is one half of the player→elite damage formula. It handles the
 * <b>weapon adjustment</b> layer — determining how much a player's equipped weapon
 * boosts or reduces their damage output against elite mobs. The other half
 * (skill adjustment) lives in
 * {@link com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent}.
 * <p>
 * Mirrors {@link ArmorDefenseCalculator} on the offensive side — where
 * ArmorDefenseCalculator converts armor into a damage <em>reduction</em> multiplier
 * for elite→player, this class converts weapon level into a damage <em>bonus</em>
 * multiplier for player→elite.
 *
 * <h2>Design Goals</h2>
 * <ol>
 *   <li><b>Unified item handling</b> — one code path for elite and vanilla weapons.
 *       Elite weapons use their stored item level; vanilla weapons get a level
 *       derived from their material's DPS via {@link com.magmaguy.elitemobs.api.utils.EliteItemManager#getItemLevel}.</li>
 *   <li><b>No vanilla damage dependency</b> — weapon level determines the bonus,
 *       not vanilla attack damage attributes. Vanilla Minecraft weapon damage is
 *       completely replaced by the formula.</li>
 *   <li><b>No data mutation</b> — this class only reads item data through
 *       existing APIs. It never writes to items or modifies player state.</li>
 *   <li><b>Smooth progression curve</b> — two-part linear curve mirrors the
 *       defensive gear reduction in {@link ArmorDefenseCalculator#getGearReduction}.</li>
 * </ol>
 *
 * <h2>How This Fits Into the Damage Formula</h2>
 * The full player→elite damage formula is:
 * <pre>
 * finalDamage = max(formulaDamage, 1) × damageModifier × combatMultiplier × critMultiplier
 *
 * where formulaDamage =
 *   baseDamage × attackSpeedFactor × skillAdjustment × weaponAdjustment × cooldownOrVelocity × sweepMultiplier
 *
 * and:
 *   baseDamage      = normalizedMobHP / 3.0                                         [from LevelScaling]
 *   attackSpeedFactor = 1.6 / actualAttackSpeed                                     [melee only]
 *   skillAdjustment = 2^((weaponSkillLevel - mobLevel) / 7.5)                       [from LevelScaling]
 *   weaponAdjustment = 0.5 + bonus(weaponLevel, mobLevel)                           [from THIS class]
 *   cooldownOrVelocity = player.getAttackCooldown() or normalizedArrowVelocity      [0 to 1]
 *   sweepMultiplier = 0.25 for secondary sweep targets, 1.0 for primary             [from THIS class]
 * </pre>
 * <p>
 * This class computes the {@code weaponAdjustment} and {@code sweepMultiplier}.
 *
 * <h2>Weapon Adjustment Curve (Two-Part Linear)</h2>
 * <pre>
 * Adjustment
 *   1.25 |                              ___________  ← hard cap (WEAPON_MAX_BONUS = 0.75)
 *        |                            /
 *   1.00 |..........................X  ← matching weapon = 1.0 (WEAPON_BASE_BONUS = 0.50)
 *        |                        /
 *   0.75 |                      /
 *        |                    /
 *   0.50 |__________________/___________________________
 *        0        weaponLevel=mobLevel        weaponLevel
 *
 * Below matching (weaponLevel ≤ mobLevel):
 *   bonus = WEAPON_BASE_BONUS × (weaponLevel / mobLevel)   // linear 0 → 0.50
 *   Slope depends on mob level — linear from 0% bonus to 50% bonus.
 *
 * Above matching (weaponLevel > mobLevel):
 *   bonus = WEAPON_BASE_BONUS + (weaponLevel - mobLevel) × WEAPON_BONUS_PER_LEVEL
 *   bonus = min(bonus, WEAPON_MAX_BONUS)                    // cap at 0.75
 *   Flat 2.5% per weapon level above mob, up to 75% bonus.
 *   Requires +10 weapon levels above mob to hit cap.
 *
 * weaponAdjustment = 0.5 + bonus   // range: [0.50, 1.25]
 * </pre>
 *
 * <h2>Target Scenario Table</h2>
 * <table border="1">
 *   <tr><th>Scenario</th><th>Weapon Level</th><th>Bonus</th><th>Weapon Adj</th><th>Effect on Hits</th></tr>
 *   <tr><td>Bare fists (no weapon)</td><td>0</td><td>0.00</td><td>0.50</td><td>6 hits (2× slower)</td></tr>
 *   <tr><td>Half-level weapon</td><td>mobLv/2</td><td>0.25</td><td>0.75</td><td>4 hits</td></tr>
 *   <tr><td>Matching weapon</td><td>mobLv</td><td>0.50</td><td>1.00</td><td>3 hits (baseline)</td></tr>
 *   <tr><td>+4 levels above</td><td>mobLv+4</td><td>0.60</td><td>1.10</td><td>~2.7 hits</td></tr>
 *   <tr><td>+10 levels above (cap)</td><td>mobLv+10</td><td>0.75</td><td>1.25</td><td>~2.4 hits</td></tr>
 *   <tr><td>+20 levels above</td><td>mobLv+20</td><td>0.75</td><td>1.25</td><td>~2.4 hits (still capped)</td></tr>
 * </table>
 *
 * <h2>Worked Examples</h2>
 *
 * <h3>Example 1: Lv25 matched sword vs Lv25 elite (melee)</h3>
 * <pre>
 * Player: weaponSkillLevel=25, weapon=elite sword (itemLevel=25)
 * Mob:    level=25, healthMultiplier=1.0
 *
 * Step 1 — Base damage: mobHP(25) = 2.1875 * 2^5 = 70.0, baseDamage = 70.0/3 = 23.33
 * Step 2 — Attack speed factor: sword speed=1.6, factor = 1.6/1.6 = 1.0
 * Step 3 — Skill adjustment: 2^((25-25)/7.5) = 2^0 = 1.0
 * Step 4 — Weapon adjustment: weaponLevel(25) == mobLevel(25), bonus=0.50, adj = 1.0
 * Step 5 — Cooldown: full charge = 1.0
 * Step 6 — Sweep: primary target = 1.0
 *
 * formulaDamage = 23.33 × 1.0 × 1.0 × 1.0 × 1.0 × 1.0 = 23.33
 * Mob HP = 70.0 → hits to kill = 70.0 / 23.33 = 3.0 ✓
 * </pre>
 *
 * <h3>Example 2: Lv25 axe vs Lv25 elite (melee)</h3>
 * <pre>
 * Player: weaponSkillLevel=25, weapon=elite axe (itemLevel=25)
 * Mob:    level=25, healthMultiplier=1.0
 *
 * Step 1 — Base damage: 23.33 (same as above)
 * Step 2 — Attack speed factor: axe speed=1.0, factor = 1.6/1.0 = 1.6
 * Step 3 — Skill adjustment: 1.0 (matched)
 * Step 4 — Weapon adjustment: 1.0 (matched)
 * Step 5 — Cooldown: full charge = 1.0
 *
 * formulaDamage = 23.33 × 1.6 × 1.0 × 1.0 × 1.0 = 37.33
 * Hits to kill = 70.0 / 37.33 = 1.875, BUT axes swing 1.0/sec vs sword 1.6/sec
 * Time to kill = 1.875 / 1.0 = 1.875 sec  vs  3.0 / 1.6 = 1.875 sec ✓ (equal DPS)
 * </pre>
 *
 * <h3>Example 3: Lv20 bow vs Lv25 elite (ranged)</h3>
 * <pre>
 * Player: weaponSkillLevel=20, weapon=elite bow (itemLevel=20)
 * Mob:    level=25, healthMultiplier=2.0, HP = 70.0 × 2 = 140.0
 *
 * Step 1 — Base damage: 23.33 (from normalized HP, NOT actual HP)
 * Step 2 — Attack speed factor: ranged, N/A = 1.0
 * Step 3 — Skill adjustment: 2^((20-25)/7.5) = 2^(-0.667) = 0.63
 * Step 4 — Weapon adjustment: weaponLevel(20) &lt; mobLevel(25), bonus = 0.5*(20/25) = 0.4, adj = 0.9
 * Step 5 — Arrow velocity: full draw = 1.0
 *
 * formulaDamage = 23.33 × 1.0 × 0.63 × 0.9 × 1.0 = 13.23
 * Hits to kill = 140.0 / 13.23 = 10.6 hits
 * (Under-leveled skill AND weapon, fighting a healthMultiplier=2.0 mob)
 * </pre>
 *
 * <h2>Comparison to Defensive Mirror</h2>
 * <table border="1">
 *   <tr><th>Aspect</th><th>ArmorDefenseCalculator (defense)</th><th>WeaponOffenseCalculator (offense)</th></tr>
 *   <tr><td>Curve type</td><td>Two-part linear</td><td>Two-part linear (same shape)</td></tr>
 *   <tr><td>Below-matching slope</td><td>ratio-based</td><td>ratio-based (same)</td></tr>
 *   <tr><td>Above-matching slope</td><td>2.5% per level</td><td>2.5% per level (same)</td></tr>
 *   <tr><td>Matching value</td><td>50% reduction (adj=1.0)</td><td>50% bonus (adj=1.0)</td></tr>
 *   <tr><td>Cap</td><td>75% reduction (adj=0.5)</td><td>75% bonus (adj=1.25)</td></tr>
 *   <tr><td>Levels to cap</td><td>+10 above mob</td><td>+10 above mob</td></tr>
 *   <tr><td>Naked/no-weapon</td><td>adj=2.0 (double damage taken)</td><td>adj=0.5 (half damage dealt)</td></tr>
 * </table>
 */
public class WeaponOffenseCalculator {

    /**
     * Base bonus at matching weapon level (weaponLevel == mobLevel).
     * <p>
     * Combined with the 0.5 floor: {@code weaponAdjustment = 0.5 + 0.5 = 1.0}
     */
    public static final double WEAPON_BASE_BONUS = 0.50;

    /**
     * Additional bonus per weapon level above mob level.
     * <p>
     * 2.5% per level above mob. Requires +10 levels to hit cap.
     */
    public static final double WEAPON_BONUS_PER_LEVEL = 0.025;

    /**
     * Maximum bonus achievable (caps the above-matching slope).
     * <p>
     * At max bonus: {@code weaponAdjustment = 0.5 + 0.75 = 1.25} (25% above baseline).
     */
    public static final double WEAPON_MAX_BONUS = 0.75;

    /**
     * Fraction of primary damage dealt to sweep secondary targets.
     * <p>
     * When a sword sweep hits additional targets via {@code ENTITY_SWEEP_ATTACK},
     * those targets receive 25% of the primary hit damage.
     */
    public static final double SWEEP_DAMAGE_FRACTION = 0.25;

    /**
     * Percentage of base damage per thorns enchantment level.
     * <p>
     * Thorns damage is now formula-based rather than flat.
     * At level 3: {@code baseDamage * 3 * 0.02 = 6%} of base damage.
     */
    public static final double THORNS_PERCENT_PER_LEVEL = 0.02;

    private WeaponOffenseCalculator() {
        // Utility class
    }

    /**
     * Returns the effective weapon level for a given item.
     * <p>
     * Elite weapons use their stored item level. Non-weapons return 0.
     *
     * @param item The weapon item (may be null)
     * @return The effective weapon level (0 if null, air, or non-weapon)
     */
    public static double getEffectiveWeaponLevel(ItemStack item) {
        if (item == null || item.getType().isAir()) return 0;
        return EliteItemManager.getItemLevel(item);
    }

    /**
     * Calculates the weapon adjustment multiplier from weapon level vs mob level.
     * <p>
     * Uses a two-part linear curve mirroring {@link ArmorDefenseCalculator#getGearReduction}:
     * <ul>
     *   <li>Below matching: bonus scales linearly from 0 to {@link #WEAPON_BASE_BONUS}</li>
     *   <li>Above matching: bonus increases at {@link #WEAPON_BONUS_PER_LEVEL} per level, capped at {@link #WEAPON_MAX_BONUS}</li>
     * </ul>
     * Final adjustment = 0.5 + bonus, range [0.5, 1.25].
     *
     * @param weaponLevel The weapon's effective level
     * @param mobLevel    The elite mob's level (clamped to min 1)
     * @return The weapon adjustment multiplier
     */
    public static double getWeaponAdjustment(double weaponLevel, int mobLevel) {
        if (mobLevel <= 0) mobLevel = 1;

        double bonus;
        if (weaponLevel <= mobLevel) {
            // Below or at matching: linear 0 → WEAPON_BASE_BONUS
            bonus = WEAPON_BASE_BONUS * (weaponLevel / mobLevel);
        } else {
            // Above matching: base + per-level bonus, capped
            bonus = WEAPON_BASE_BONUS + (weaponLevel - mobLevel) * WEAPON_BONUS_PER_LEVEL;
            bonus = Math.min(bonus, WEAPON_MAX_BONUS);
        }

        return 0.5 + bonus;
    }

    /**
     * Returns the sweep damage multiplier for the given damage event.
     * <p>
     * Sweep attacks (secondary targets of a sword swing) deal reduced damage.
     *
     * @param event The damage event to check
     * @return {@link #SWEEP_DAMAGE_FRACTION} for sweep attacks, 1.0 otherwise
     */
    public static double getSweepMultiplier(EntityDamageByEntityEvent event) {
        if (event != null && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return SWEEP_DAMAGE_FRACTION;
        }
        return 1.0;
    }

    /**
     * Normalizes arrow velocity to a 0–1 scale for ranged damage calculation.
     * <p>
     * Full-draw bow velocity magnitude is approximately 3.0.
     * Crossbow bolts are approximately 3.15.
     * We normalize against 3.0 and clamp to [0, 1].
     *
     * @param projectile The projectile to measure
     * @return Normalized velocity in [0, 1]
     */
    public static double normalizeArrowVelocity(Projectile projectile) {
        double magnitude = projectile.getVelocity().length();
        return Math.min(magnitude / 3.0, 1.0);
    }

}
