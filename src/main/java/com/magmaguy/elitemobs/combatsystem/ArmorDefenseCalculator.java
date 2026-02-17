package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.items.ItemTagger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Centralized, read-only defense reader for any ItemStack (elite or vanilla).
 * <p>
 * This class is one half of the elite→player damage formula. It handles the
 * <b>gear adjustment</b> layer — determining how much a player's equipped armor
 * and protection enchantments reduce incoming damage from elite mobs. The other
 * half (skill adjustment) lives in
 * {@link com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent}.
 *
 * <h2>Design Goals</h2>
 * <ol>
 *   <li><b>Unified item handling</b> — one code path for both elite and vanilla
 *       items, eliminating the old split where vanilla armor was ignored entirely.</li>
 *   <li><b>Damage-type awareness</b> — Projectile, Blast, and Fire Protection
 *       enchantments now actually contribute to defense against their respective
 *       damage types, rather than being flat subtraction or ignored.</li>
 *   <li><b>No data mutation</b> — this class only reads item data through
 *       existing APIs ({@link EliteItemManager}, {@link ItemTagger}). It never
 *       writes to items or modifies player state.</li>
 *   <li><b>Smooth progression curve</b> — the two-part gear reduction ensures
 *       gear always matters (no dead zones) while preventing over-geared players
 *       from becoming completely invulnerable.</li>
 * </ol>
 *
 * <h2>How This Fits Into the Damage Formula</h2>
 * The full elite→player damage formula is:
 * <pre>
 * finalDamage = baseDamage × skillAdjustment × gearAdjustment × otherMultipliers
 *
 * where:
 *   baseDamage      = playerMaxHP / 5.0
 *   skillAdjustment = 2^((mobLevel - armorSkillLevel) / 7.5)   [from LevelScaling]
 *   gearAdjustment  = 2.0 × (1 - gearReduction)               [from THIS class]
 * </pre>
 * <p>
 * This class computes the {@code gearAdjustment} multiplier. It converts the
 * player's equipped armor and enchantments into a single "gear score", compares
 * that to the attacking mob's level, and outputs a multiplier:
 * <ul>
 *   <li><b>2.0</b> — naked player, gear provides zero protection (0% reduction)</li>
 *   <li><b>1.0</b> — gear matches mob level, baseline combat (50% reduction)</li>
 *   <li><b>0.5</b> — peak gear, maximum protection (75% reduction, hard cap)</li>
 * </ul>
 *
 * <h2>Gear Score Composition</h2>
 * <pre>
 * gearScore = (sum of armorLevel across 4 armor slots) / 4
 *           + (sum of enchantBonus across ALL 6 slots) / 4
 * </pre>
 * <ul>
 *   <li>Armor level: elite items use their stored level, vanilla items use
 *       a material-based lookup (leather=1 through netherite=7).</li>
 *   <li>Enchant bonus: Protection (all types) + damage-type-specific protection,
 *       scaled by {@link #ENCHANT_SCALE} (1/3) to convert enchant levels into
 *       armor-level-equivalent units.</li>
 *   <li>Mainhand + offhand contribute enchant bonus only (not armor level),
 *       allowing tank builds with Protection on shields/weapons.</li>
 *   <li>The /4 on enchant sum normalizes to per-armor-slot units, while counting
 *       6 slots means full enchants can provide up to 50% more bonus than 4 alone.</li>
 * </ul>
 *
 * <h2>Gear Reduction Curve (Two-Part Linear)</h2>
 * <pre>
 * Reduction
 *   75% |                              ___________  ← hard cap
 *       |                            /
 *   50% |..........................X  ← matching gear = 50%
 *       |                        /
 *   25% |                      /
 *       |                    /
 *    0% |__________________/___________________________
 *       0        gearScore=mobLevel        gearScore
 *
 * Below matching (gearScore ≤ mobLevel):
 *   reduction = 0.50 × (gearScore / mobLevel)
 *   Slope depends on mob level — linear from 0% to 50%.
 *
 * Above matching (gearScore > mobLevel):
 *   reduction = 0.50 + (gearScore - mobLevel) × 0.025
 *   Flat 2.5% per gear level above mob, up to 75% cap.
 *   Requires +10 gear levels above mob to hit cap.
 * </pre>
 *
 * <h2>Target Scenario Table</h2>
 * <p>
 * These are the combat outcomes this system is designed to produce.
 * "Hits" = {@code playerMaxHP / finalDamage}, the number of hits to kill the player.
 * <p>
 * <table border="1">
 *   <tr><th>Scenario</th><th>Skill Adj</th><th>Gear Red</th><th>Gear Adj</th><th>Hits</th></tr>
 *   <tr><td>Lv1 naked vs Lv1</td>        <td>1.0</td>  <td>0%</td>   <td>2.0</td>  <td>2.5</td></tr>
 *   <tr><td>Lv1 naked vs Lv5</td>        <td>1.45</td> <td>0%</td>   <td>2.0</td>  <td>~1.7</td></tr>
 *   <tr><td>Lv1 vanilla diamond vs Lv5</td><td>1.45</td><td>50%</td>  <td>1.0</td>  <td>~3.5</td></tr>
 *   <tr><td>Lv25 matched vs Lv25</td>    <td>1.0</td>  <td>50%</td>  <td>1.0</td>  <td>5.0</td></tr>
 *   <tr><td>Lv25 matched+ProtIV vs Lv25</td><td>1.0</td><td>53.3%</td><td>0.93</td> <td>5.4</td></tr>
 *   <tr><td>Lv25 matched vs Lv30</td>    <td>1.59</td> <td>41.7%</td><td>1.17</td> <td>2.7</td></tr>
 *   <tr><td>Lv25 peak gear vs Lv25</td>  <td>1.0</td>  <td>75%</td>  <td>0.50</td> <td>10.0</td></tr>
 *   <tr><td>Lv50 matched vs Lv50</td>    <td>1.0</td>  <td>50%</td>  <td>1.0</td>  <td>5.0</td></tr>
 * </table>
 * <p>
 * Key design targets verified by the table:
 * <ul>
 *   <li><b>5 hits at matched level</b> — the core balance target. A properly geared
 *       player survives exactly 5 normal hits from a same-level elite mob, at ALL levels.</li>
 *   <li><b>2.5 hits naked</b> — no gear = double damage penalty via 2.0 multiplier.
 *       Immediate incentive to equip any armor.</li>
 *   <li><b>Vanilla armor matters</b> — a level 1 player in diamond armor survives ~3.5
 *       hits from a level 5 mob instead of ~1.7 naked. This fixes the old system where
 *       vanilla armor was completely ignored.</li>
 *   <li><b>Enchants are meaningful but not dominant</b> — Protection IV on all 4 armor
 *       pieces adds ~1.33 to gear score (via ENCHANT_SCALE=1/3), pushing from 50% to
 *       53.3% reduction. Noticeable (~8% more survivability) but not game-breaking.</li>
 *   <li><b>Level-consistent</b> — matched combat at level 25 and level 50 both produce
 *       exactly 5 hits. The formula scales perfectly across all levels.</li>
 *   <li><b>Peak gear cap at 10 hits</b> — even with the best possible gear (+10 levels
 *       above mob), players still take meaningful damage. Prevents invulnerability.</li>
 * </ul>
 *
 * <h2>Worked Examples</h2>
 *
 * <h3>Example 1: Lv25 matched gear + Prot IV vs Lv25 (melee)</h3>
 * <pre>
 * Player: armorSkillLevel=25, maxHP = 20 + 24*2 = 68
 *         4 elite armor pieces each at armorLevel=25, each with Protection IV
 * Mob:    level=25, melee attack
 *
 * Step 1 — Armor level per piece: 25 (elite item, stored value)
 * Step 2 — Enchant bonus per piece: Protection IV = 4 * (1/3) = 1.333
 *          (melee → only general Protection, no type-specific)
 * Step 3 — Gear score:
 *          armorLevelSum = 25*4 = 100, /4 = 25.0
 *          enchantBonusSum = 1.333*4 = 5.333 (no mainhand/offhand enchants), /4 = 1.333
 *          gearScore = 25.0 + 1.333 = 26.333
 * Step 4 — Gear reduction:
 *          gearScore(26.333) > mobLevel(25) → above-matching formula
 *          reduction = 0.50 + (26.333 - 25) * 0.025 = 0.50 + 0.033 = 0.533 (53.3%)
 * Step 5 — Gear adjustment: 2.0 * (1 - 0.533) = 0.933
 * Step 6 — Final damage: baseDamage(13.6) * skillAdj(1.0) * gearAdj(0.933) = 12.69
 *          Hits to kill: 68 / 12.69 = 5.36 ≈ 5.4 hits
 * </pre>
 *
 * <h3>Example 2: Lv1 vanilla diamond vs Lv5 skeleton (projectile)</h3>
 * <pre>
 * Player: armorSkillLevel=1, maxHP = 20
 *         Full vanilla diamond armor (no enchants), no mainhand/offhand enchants
 * Mob:    level=5, skeleton arrow (PROJECTILE damage type)
 *
 * Step 1 — Armor level per piece: 5 (vanilla diamond → VANILLA_ARMOR_LEVELS lookup)
 * Step 2 — Enchant bonus: 0 (no enchants on any piece)
 * Step 3 — Gear score: (5+5+5+5)/4 + 0/4 = 5.0
 * Step 4 — Gear reduction: gearScore(5) == mobLevel(5)
 *          reduction = 0.50 * (5/5) = 0.50 (50%)
 * Step 5 — Gear adjustment: 2.0 * (1 - 0.50) = 1.0
 * Step 6 — Final damage: baseDamage(4) * skillAdj(1.447) * gearAdj(1.0) = 5.79
 *          Hits to kill: 20 / 5.79 = 3.45 ≈ 3.5 hits
 *
 * Note: If the diamond armor had Projectile Protection IV, the enchant bonus
 * would be (4+4) * (1/3) = 2.667 per piece (Protection IV would be 0 since it's
 * vanilla unenchanted, so just Proj Prot IV = 4 * 1/3 = 1.333 per piece).
 * This would raise gear score to 5 + (1.333*4)/4 = 6.333, increasing reduction
 * to 53.3% and improving survival noticeably against arrows specifically.
 * </pre>
 */
public class ArmorDefenseCalculator {

    /**
     * Converts enchantment levels into armor-level-equivalent units.
     * <p>
     * Value: 1/3 — meaning 3 enchantment levels = 1 armor level of defense.
     * <p>
     * This ratio is chosen so that Protection IV on all 4 armor pieces contributes
     * roughly +1.33 to gear score (16 enchant levels * 1/3 / 4 slots). That's enough
     * to bump reduction from 50% to ~53.3% at matching level — noticeable and worth
     * pursuing, but not so large that enchantments overshadow armor level itself.
     * <p>
     * For damage-type-specific enchantments (Projectile/Blast/Fire Protection), they
     * stack additively with general Protection before scaling. So Prot IV + Proj Prot IV
     * on one piece = (4+4) * 1/3 = 2.67 armor-level-equivalent against projectiles.
     */
    public static final double ENCHANT_SCALE = 1.0 / 3.0;

    /**
     * The maximum gear reduction achievable at exactly matching gear score.
     * <p>
     * Value: 0.50 (50%) — when {@code gearScore == mobLevel}, the player's gear
     * provides exactly 50% damage reduction. This maps to a gear adjustment of 1.0,
     * meaning the base damage formula ({@code maxHP / 5.0}) hits for exactly its
     * face value, giving the target 5 hits to kill.
     * <p>
     * Below this point (under-geared), reduction scales linearly down to 0%, causing
     * the gear adjustment to rise toward 2.0 (double damage for naked players).
     */
    public static final double GEAR_BASE_REDUCTION = 0.50;

    /**
     * Additional damage reduction per gear-score level above the mob's level.
     * <p>
     * Value: 0.025 (2.5% per level) — once gear score exceeds mob level, each
     * additional gear level provides 2.5% more reduction. This means:
     * <ul>
     *   <li>+4 levels above mob: 50% + 10% = 60% reduction</li>
     *   <li>+8 levels above mob: 50% + 20% = 70% reduction</li>
     *   <li>+10 levels above mob: 50% + 25% = 75% reduction (hits the cap)</li>
     * </ul>
     * <p>
     * The slope is deliberately shallow — over-gearing provides diminishing
     * returns in terms of survivability (each 2.5% reduction is worth less
     * when you're already at 60%+ reduction).
     */
    public static final double GEAR_BONUS_PER_LEVEL = 0.025;

    /**
     * Hard cap on gear reduction, preventing invulnerability.
     * <p>
     * Value: 0.75 (75%) — maps to a gear adjustment of 0.50, meaning even with
     * the absolute best gear, the player still takes half the base damage. This
     * produces a 10-hit survival at matched skill level.
     * <p>
     * Reaching this cap requires gear score 10 levels above the mob:
     * {@code 0.50 + 10 * 0.025 = 0.75}.
     */
    public static final double GEAR_MAX_REDUCTION = 0.75;

    /**
     * Vanilla material armor levels for non-elite items.
     * <p>
     * These values determine how much defense vanilla (non-EliteMobs) armor provides
     * in the gear reduction formula. They are intentionally separate from
     * {@link CombatSystem#getMaterialTier}, which is used for elite item generation
     * and has a different scale/purpose.
     * <p>
     * The tier progression (1 → 7) is designed so that:
     * <ul>
     *   <li>Leather (1) gives meaningful but small protection against low-level mobs</li>
     *   <li>Diamond (5) matches a level 5 mob exactly — full diamond vs level 5 = 50% reduction</li>
     *   <li>Netherite (7) outperforms diamond but still falls off against high-level content</li>
     *   <li>A new player in full vanilla diamond has a fighting chance against early elite mobs,
     *       which was impossible in the old system where vanilla armor was completely ignored</li>
     * </ul>
     */
    private static final Map<Material, Integer> VANILLA_ARMOR_LEVELS = Map.ofEntries(
            // Leather = 1
            Map.entry(Material.LEATHER_HELMET, 1), Map.entry(Material.LEATHER_CHESTPLATE, 1),
            Map.entry(Material.LEATHER_LEGGINGS, 1), Map.entry(Material.LEATHER_BOOTS, 1),
            // Gold = 2
            Map.entry(Material.GOLDEN_HELMET, 2), Map.entry(Material.GOLDEN_CHESTPLATE, 2),
            Map.entry(Material.GOLDEN_LEGGINGS, 2), Map.entry(Material.GOLDEN_BOOTS, 2),
            // Chainmail = 3
            Map.entry(Material.CHAINMAIL_HELMET, 3), Map.entry(Material.CHAINMAIL_CHESTPLATE, 3),
            Map.entry(Material.CHAINMAIL_LEGGINGS, 3), Map.entry(Material.CHAINMAIL_BOOTS, 3),
            // Iron = 4
            Map.entry(Material.IRON_HELMET, 4), Map.entry(Material.IRON_CHESTPLATE, 4),
            Map.entry(Material.IRON_LEGGINGS, 4), Map.entry(Material.IRON_BOOTS, 4),
            // Turtle = 4 (same as iron)
            Map.entry(Material.TURTLE_HELMET, 4),
            // Diamond = 5
            Map.entry(Material.DIAMOND_HELMET, 5), Map.entry(Material.DIAMOND_CHESTPLATE, 5),
            Map.entry(Material.DIAMOND_LEGGINGS, 5), Map.entry(Material.DIAMOND_BOOTS, 5),
            // Netherite = 7
            Map.entry(Material.NETHERITE_HELMET, 7), Map.entry(Material.NETHERITE_CHESTPLATE, 7),
            Map.entry(Material.NETHERITE_LEGGINGS, 7), Map.entry(Material.NETHERITE_BOOTS, 7)
    );

    private ArmorDefenseCalculator() {
    }

    /**
     * Returns the effective armor level for a single item.
     * <p>
     * <b>Elite items:</b> delegates to {@link EliteItemManager#getArmorLevel(ItemStack)},
     * which returns {@code getEliteDefense(item) * 4}. The elite defense includes both
     * the base material tier and the item's stored defense attribute, so an elite item's
     * armor level directly corresponds to the mob level it was designed to match.
     * <p>
     * <b>Vanilla items:</b> looks up the material in {@link #VANILLA_ARMOR_LEVELS}.
     * Non-armor items (swords, tools, blocks, etc.) return 0 — they contribute no
     * armor level to the gear score.
     * <p>
     * <b>Null/air items:</b> return 0 — handles empty armor slots gracefully since
     * {@code getArmorContents()} can contain null entries.
     *
     * @param item The item to evaluate (may be null)
     * @return The effective armor level (0 if null, air, or non-armor vanilla item)
     */
    public static double getEffectiveArmorLevel(ItemStack item) {
        if (item == null || item.getType().isAir()) return 0;
        if (EliteItemManager.isEliteMobsItem(item))
            return EliteItemManager.getArmorLevel(item);
        return VANILLA_ARMOR_LEVELS.getOrDefault(item.getType(), 0);
    }

    /**
     * Calculates the enchantment bonus for a single item, converted to armor-level units.
     * <p>
     * Always includes general Protection. Additionally includes the type-specific
     * protection enchantment if the damage type matches:
     * <ul>
     *   <li>PROJECTILE → + Projectile Protection</li>
     *   <li>EXPLOSION → + Blast Protection</li>
     *   <li>FIRE → + Fire Protection</li>
     *   <li>MELEE/OTHER → general Protection only</li>
     * </ul>
     * <p>
     * Uses {@link ItemTagger#getEnchantment(org.bukkit.inventory.meta.ItemMeta,
     * org.bukkit.NamespacedKey)} which handles both elite items (reads from
     * PersistentDataContainer) and vanilla items (falls back to
     * {@code getEnchantLevel()}).
     * <p>
     * The raw enchantment level sum is multiplied by {@link #ENCHANT_SCALE} (1/3)
     * to convert to armor-level-equivalent units.
     * <p>
     * <b>Example:</b> An item with Protection IV + Projectile Protection III,
     * evaluated against PROJECTILE damage: {@code (4 + 3) * (1/3) = 2.33} armor-level units.
     *
     * @param item The item to evaluate (may be null)
     * @param type The damage type being defended against
     * @return The enchant bonus in armor-level-equivalent units (0 if null or no enchants)
     */
    public static double getEnchantBonus(ItemStack item, DamageType type) {
        if (item == null || item.getItemMeta() == null) return 0;

        int total = ItemTagger.getEnchantment(item.getItemMeta(), Enchantment.PROTECTION.getKey());

        switch (type) {
            case PROJECTILE ->
                    total += ItemTagger.getEnchantment(item.getItemMeta(), Enchantment.PROJECTILE_PROTECTION.getKey());
            case EXPLOSION ->
                    total += ItemTagger.getEnchantment(item.getItemMeta(), Enchantment.BLAST_PROTECTION.getKey());
            case FIRE ->
                    total += ItemTagger.getEnchantment(item.getItemMeta(), Enchantment.FIRE_PROTECTION.getKey());
            default -> {
                // MELEE and OTHER: only general Protection applies
            }
        }

        return total * ENCHANT_SCALE;
    }

    /**
     * Combined defensive value for one armor piece: armor level + enchant bonus.
     * <p>
     * This is a convenience method used primarily for debugging and display.
     * The gear score formula calls {@link #getEffectiveArmorLevel} and
     * {@link #getEnchantBonus} separately because they're aggregated differently
     * (armor level from 4 slots only, enchant bonus from 6 slots).
     *
     * @param item The item to evaluate
     * @param type The damage type being defended against
     * @return The total defensive value in armor-level units
     */
    public static double getDefensiveValue(ItemStack item, DamageType type) {
        return getEffectiveArmorLevel(item) + getEnchantBonus(item, type);
    }

    /**
     * Computes the player's total gear score for the gear reduction formula.
     * <p>
     * The gear score combines two components with different slot sources:
     * <pre>
     * gearScore = (sum of armorLevel for 4 armor slots) / 4
     *           + (sum of enchantBonus for ALL 6 slots) / 4
     * </pre>
     * <p>
     * <b>Why armor level uses only 4 slots:</b> Only actual armor pieces (helmet,
     * chestplate, leggings, boots) have meaningful armor levels. Weapons and
     * shields don't provide armor defense.
     * <p>
     * <b>Why enchant bonus uses 6 slots:</b> Protection enchantments can appear on
     * weapons and shields (especially elite items). Including mainhand + offhand
     * rewards tank builds that enchant shields with Protection, providing up to 50%
     * more enchant contribution than armor-only builds ({@code 6/4 = 1.5×}).
     * <p>
     * <b>Why both are divided by 4:</b> The /4 normalizes the gear score to a single
     * "average armor level" unit, so it can be directly compared to mob level. Without
     * normalization, a level-25 player with four level-25 armor pieces would have a
     * gear score of 100 instead of 25.
     * <p>
     * <b>Example — Lv25 full matched gear, Prot IV all, melee:</b>
     * <pre>
     * armorLevelSum = 25 + 25 + 25 + 25 = 100 → /4 = 25.0
     * enchantBonus per armor piece = 4 * (1/3) = 1.333  (Protection IV, melee → no type-specific)
     * enchantBonusSum = 1.333 * 4 + 0 + 0 = 5.333 → /4 = 1.333
     * gearScore = 25.0 + 1.333 = 26.333
     * </pre>
     * <p>
     * <b>Edge case — partial armor:</b> If a player wears only 2 armor pieces,
     * armorLevelSum is halved but still divided by 4, yielding half the gear score.
     * This is intentional — missing armor slots are a real defensive gap.
     *
     * @param player The player whose equipment to evaluate
     * @param type   The damage type being defended against
     * @return The gear score in armor-level-equivalent units
     */
    public static double getGearScore(Player player, DamageType type) {
        ItemStack[] armor = player.getInventory().getArmorContents();

        double armorLevelSum = 0;
        double enchantBonusSum = 0;

        // 4 armor slots: contribute both armor level and enchant bonus
        for (ItemStack piece : armor) {
            armorLevelSum += getEffectiveArmorLevel(piece);
            enchantBonusSum += getEnchantBonus(piece, type);
        }

        // Mainhand + offhand: contribute ONLY enchant bonus (not armor level)
        enchantBonusSum += getEnchantBonus(player.getInventory().getItemInMainHand(), type);
        enchantBonusSum += getEnchantBonus(player.getInventory().getItemInOffHand(), type);

        return armorLevelSum / 4.0 + enchantBonusSum / 4.0;
    }

    /**
     * Calculates the gear reduction percentage from gear score vs mob level.
     * <p>
     * Uses a two-part linear curve:
     * <p>
     * <b>Below matching</b> ({@code gearScore ≤ mobLevel}):
     * <pre>
     * reduction = 0.50 × (gearScore / mobLevel)
     * </pre>
     * Scales linearly from 0% (naked) to 50% (matching). The slope is ratio-based,
     * meaning the rate depends on mob level: against a level 5 mob, each gear level
     * gives 10% reduction; against level 50, each gives 1%. This naturally makes
     * low-level vanilla armor effective against low-level mobs but irrelevant
     * against high-level ones.
     * <p>
     * <b>Above matching</b> ({@code gearScore > mobLevel}):
     * <pre>
     * reduction = 0.50 + (gearScore - mobLevel) × 0.025
     * </pre>
     * Flat 2.5% per gear level above mob. Reaches the 75% hard cap at +10 levels.
     * <p>
     * <b>Key reduction values:</b>
     * <table border="1">
     *   <tr><th>Gear Score</th><th>vs Mob Level</th><th>Reduction</th></tr>
     *   <tr><td>0</td><td>any</td><td>0%</td></tr>
     *   <tr><td>mobLevel/2</td><td>any</td><td>25%</td></tr>
     *   <tr><td>mobLevel</td><td>any</td><td>50%</td></tr>
     *   <tr><td>mobLevel + 4</td><td>any</td><td>60%</td></tr>
     *   <tr><td>mobLevel + 10</td><td>any</td><td>75% (cap)</td></tr>
     *   <tr><td>mobLevel + 20</td><td>any</td><td>75% (still capped)</td></tr>
     * </table>
     *
     * @param gearScore The player's computed gear score
     * @param mobLevel  The attacking mob's level (clamped to min 1 internally)
     * @return Gear reduction as a fraction in [0.0, 0.75]
     */
    public static double getGearReduction(double gearScore, int mobLevel) {
        if (mobLevel <= 0) mobLevel = 1;
        double reduction;
        if (gearScore <= mobLevel)
            reduction = GEAR_BASE_REDUCTION * (gearScore / mobLevel);
        else
            reduction = GEAR_BASE_REDUCTION + (gearScore - mobLevel) * GEAR_BONUS_PER_LEVEL;
        return Math.max(0, Math.min(GEAR_MAX_REDUCTION, reduction));
    }

    /**
     * Converts gear reduction into the final damage multiplier used in the formula.
     * <p>
     * {@code gearAdjustment = 2.0 × (1 - gearReduction)}
     * <p>
     * The 2.0 factor is the key to the "no pre-compensation" design. Instead of
     * inflating base damage by 4× and then reducing it, we set base damage to
     * {@code maxHP / 5} (what a matched player should receive) and let the gear
     * adjustment modulate around it:
     * <ul>
     *   <li><b>0% reduction (naked)</b>: {@code 2.0 × 1.0 = 2.0} — takes 2× base damage,
     *       dies in 2.5 hits. Strong incentive to get ANY armor.</li>
     *   <li><b>50% reduction (matched)</b>: {@code 2.0 × 0.5 = 1.0} — takes exactly
     *       base damage, dies in 5 hits. The design target.</li>
     *   <li><b>75% reduction (peak)</b>: {@code 2.0 × 0.25 = 0.5} — takes half base
     *       damage, dies in 10 hits. Maximum achievable survivability from gear.</li>
     * </ul>
     *
     * @param gearScore The player's computed gear score
     * @param mobLevel  The attacking mob's level
     * @return The gear adjustment multiplier (range: [0.5, 2.0])
     */
    public static double getGearAdjustment(double gearScore, int mobLevel) {
        return 2.0 * (1.0 - getGearReduction(gearScore, mobLevel));
    }

    /**
     * Maps an {@link EntityDamageByEntityEvent}'s cause to a {@link DamageType}.
     * <p>
     * This determines which protection enchantments are relevant for the attack:
     * <ul>
     *   <li>{@code ENTITY_ATTACK, ENTITY_SWEEP_ATTACK} → {@link DamageType#MELEE}</li>
     *   <li>{@code PROJECTILE} → {@link DamageType#PROJECTILE} (arrows, fireballs aimed at player)</li>
     *   <li>{@code ENTITY_EXPLOSION, BLOCK_EXPLOSION} → {@link DamageType#EXPLOSION} (creepers, ghasts)</li>
     *   <li>{@code FIRE, FIRE_TICK} → {@link DamageType#FIRE}</li>
     *   <li>Everything else → {@link DamageType#OTHER} (treated as MELEE for enchant purposes)</li>
     * </ul>
     *
     * @param event The damage event to classify
     * @return The classified damage type
     */
    public static DamageType fromEvent(EntityDamageByEntityEvent event) {
        return switch (event.getCause()) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> DamageType.MELEE;
            case PROJECTILE -> DamageType.PROJECTILE;
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> DamageType.EXPLOSION;
            case FIRE, FIRE_TICK -> DamageType.FIRE;
            default -> DamageType.OTHER;
        };
    }

    /**
     * Damage type categories for damage-type-aware defense calculation.
     * <p>
     * Each type determines which protection enchantments contribute to the
     * enchant bonus on top of general Protection:
     * <ul>
     *   <li>{@link #MELEE} — general Protection only</li>
     *   <li>{@link #PROJECTILE} — Protection + Projectile Protection</li>
     *   <li>{@link #EXPLOSION} — Protection + Blast Protection</li>
     *   <li>{@link #FIRE} — Protection + Fire Protection</li>
     *   <li>{@link #OTHER} — general Protection only (same as MELEE)</li>
     * </ul>
     */
    public enum DamageType {MELEE, PROJECTILE, EXPLOSION, FIRE, OTHER}
}
