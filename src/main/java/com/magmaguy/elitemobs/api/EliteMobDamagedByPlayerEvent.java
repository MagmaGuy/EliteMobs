package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.*;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.entitytracker.CustomProjectileData;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.HuntersMarkSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.RangersFocusSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.WindRunnerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HeavyBoltsSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.QuickReloadSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathMarkSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.AvatarOfJudgmentSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.JudgmentSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.StunningForceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ExposeWeaknessSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.PoseidonsFavorSkill;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.Round;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Event fired when an elite mob takes damage from a player.
 * <p>
 * This class contains the complete <b>player → elite</b> damage formula, which is the
 * offensive mirror of {@link PlayerDamagedByEliteMobEvent} (the <b>elite → player</b>
 * defensive formula). Together, these two events define all combat math in EliteMobs.
 *
 * <h2>Design Goals</h2>
 * <ol>
 *   <li><b>No vanilla damage dependency</b> — vanilla Minecraft weapon damage is completely
 *       replaced by the formula. The old system added vanilla damage (~7 for diamond sword)
 *       on top of exponential elite damage, which broke scaling at low levels (88% vanilla
 *       at level 5 vs 32% at level 25). Now all damage comes from the formula.</li>
 *   <li><b>Level-consistent hit counts</b> — a standard mob (healthMultiplier=1.0) takes
 *       exactly 3 sword hits to kill at ALL levels when skill and weapon match mob level.</li>
 *   <li><b>healthMultiplier scales linearly</b> — a mob with healthMultiplier=2.0 takes
 *       exactly 6 sword hits; healthMultiplier=10.0 takes 30 hits. This holds at all levels.</li>
 *   <li><b>Symmetric with defensive formula</b> — weapon adjustment curve mirrors
 *       {@link ArmorDefenseCalculator}'s gear reduction; offensive skill scaling mirrors
 *       defensive skill scaling.</li>
 *   <li><b>Weapon pacing by feel</b> — melee attack-speed scaling preserves weapon identity
 *       without fully equalizing theoretical DPS, because dungeon combat rewards burst
 *       windows more than stationary target math predicts.</li>
 * </ol>
 *
 * <h2>Full Damage Formula</h2>
 * <pre>
 * formulaDamage = baseDamage × attackSpeedFactor × skillAdjustment
 *               × weaponAdjustment × cooldownOrVelocity × sweepMultiplier
 *               × potionMultiplier
 *               × equipmentEnchantmentMultiplier
 *               × enchantmentMultiplier
 *
 * finalDamage = max(formulaDamage, 1) × damageModifier × combatMultiplier × critMultiplier
 * </pre>
 *
 * <h2>Formula Components</h2>
 * <table border="1">
 *   <tr><th>Component</th><th>Formula</th><th>Range</th><th>Source</th></tr>
 *   <tr><td>baseDamage</td><td>normalizedMobHP / 3.0</td><td>scales with level</td>
 *       <td>{@link LevelScaling#calculateBaseDamageToElite}</td></tr>
 *   <tr><td>attackSpeedFactor</td><td>tuned pacing factor from weapon family and speed</td><td>melee only; 1.0 for ranged</td>
 *       <td>{@link WeaponOffenseCalculator#getAttackSpeedFactor}</td></tr>
 *   <tr><td>skillAdjustment</td><td>2^((skillLv - mobLv) / 7.5)</td><td>(0, ∞) centered at 1.0</td>
 *       <td>{@link LevelScaling#calculateOffensiveSkillAdjustment}</td></tr>
 *   <tr><td>weaponAdjustment</td><td>two-part linear curve</td><td>[0.5, 1.25]</td>
 *       <td>{@link WeaponOffenseCalculator#getWeaponAdjustment}</td></tr>
 *   <tr><td>cooldownOrVelocity</td><td>tracked melee charge or arrowVelocity/3.0</td><td>[0, 1]</td>
 *       <td>{@link PlayerAttackCooldownTracker} / {@link WeaponOffenseCalculator#normalizeArrowVelocity}</td></tr>
 *   <tr><td>sweepMultiplier</td><td>0.25 for sweep targets, 1.0 primary</td><td>{0.25, 1.0}</td>
 *       <td>{@link WeaponOffenseCalculator#getSweepMultiplier}</td></tr>
 *   <tr><td>equipmentEnchantmentMultiplier</td><td>1.0 + Sharpness / Power levels × 0.025 from equipped items</td><td>multiplicative</td>
 *       <td>{@link ElitePlayerInventory#getEliteEnchantmentDamage}</td></tr>
 *   <tr><td>enchantmentMultiplier</td><td>1.0 + eliteEnchantLvl × 0.025</td><td>[1.0, ~1.2]</td>
 *       <td>Smite / Bane of Arthropods (elite-only levels)</td></tr>
 *   <tr><td>damageModifier</td><td>boss-specific damage reduction</td><td>[0, 1]</td>
 *       <td>CustomBossEntity config</td></tr>
 *   <tr><td>combatMultiplier</td><td>global config multiplier</td><td>configurable</td>
 *       <td>MobCombatSettingsConfig</td></tr>
 *   <tr><td>critMultiplier</td><td>1.5× on crit</td><td>{1.0, 1.5}</td>
 *       <td>ElitePlayerInventory crit chance</td></tr>
 * </table>
 *
 * <h2>Event Flow (onEliteMobAttacked)</h2>
 * <pre>
 *   EntityDamageByEntityEvent
 *     → Nullify vanilla damage modifiers (armor etc.)
 *     → Branch by cause:
 *       ├─ !validPlayer        → raw vanilla damage (NPC plugins)
 *       ├─ bypass              → raw event damage (custom powers)
 *       ├─ THORNS              → calculateThornsDamage()
 *       ├─ ENTITY_ATTACK  ─┐
 *       ├─ SWEEP_ATTACK    ├──→ playerToEliteDamageFormula()
 *       ├─ PROJECTILE      ─┘
 *       └─ other               → raw vanilla damage
 *     → Apply damageModifier × combatMultiplier
 *     → Apply critical hit (1.5×)
 *     → Fire EliteMobDamagedByPlayerEvent (listeners can modify)
 *     → Apply skill bonuses (applySkillBonuses)
 *     → Set final damage on event
 * </pre>
 *
 * <h2>Worked Examples</h2>
 *
 * <h3>Example 1: Matched melee combat (Lv25 sword vs Lv25 elite)</h3>
 * <pre>
 * Player: weaponSkillLevel=25, weapon=elite sword (itemLevel=25)
 * Mob:    level=25, healthMultiplier=1.0, HP=70.0
 *
 * baseDamage       = 70.0 / 3 = 23.33
 * attackSpeedFactor = 1.6 / 1.6 = 1.0  (sword)
 * skillAdjustment  = 2^((25-25)/7.5) = 1.0
 * weaponAdjustment = 0.5 + 0.5 = 1.0   (matching)
 * cooldown         = 1.0                (full charge)
 * sweepMultiplier  = 1.0                (primary target)
 *
 * formulaDamage = 23.33 × 1.0 × 1.0 × 1.0 × 1.0 × 1.0 = 23.33
 * Hits to kill  = 70.0 / 23.33 = 3.0 ✓
 * </pre>
 *
 * <h3>Example 2: Tuned axe pacing (Lv25 axe vs Lv25 elite)</h3>
 * <pre>
 * baseDamage       = 23.33
 * attackSpeedFactor ≈ 1.26  (axe is slower, but no longer receives full inverse-speed burst)
 * formulaDamage    = 23.33 × 1.26 = 29.5 per hit
 * Hits to kill     = 70.0 / 29.5 = 2.37
 * </pre>
 *
 * <h3>Example 3: Under-leveled ranged (Lv20 bow vs Lv25 elite, healthMultiplier=2.0)</h3>
 * <pre>
 * baseDamage       = 23.33  (from normalized HP, NOT actual HP)
 * attackSpeedFactor = 1.0   (ranged, skipped)
 * skillAdjustment  = 2^((20-25)/7.5) = 0.63
 * weaponAdjustment = 0.5 + 0.5*(20/25) = 0.9
 * arrowVelocity    = 1.0   (full draw)
 *
 * formulaDamage = 23.33 × 0.63 × 0.9 × 1.0 = 13.23
 * Actual mob HP = 70.0 × 2.0 = 140.0
 * Hits to kill  = 140.0 / 13.23 = 10.6 hits
 * </pre>
 *
 * <h3>Example 4: Level consistency check</h3>
 * <pre>
 * Level 5:   mobHP = 2.1875 * 2^1 = 4.375,   baseDmg = 4.375/3 = 1.46  → 3 hits ✓
 * Level 25:  mobHP = 2.1875 * 2^5 = 70.0,     baseDmg = 70.0/3 = 23.33  → 3 hits ✓
 * Level 50:  mobHP = 2.1875 * 2^10 = 2240.0,  baseDmg = 2240/3 = 746.7  → 3 hits ✓
 * Level 100: mobHP = 2.1875 * 2^20 = 2.29M,   baseDmg = 764K            → 3 hits ✓
 * </pre>
 *
 * <h2>Comparison to Defensive Formula</h2>
 * <table border="1">
 *   <tr><th>Aspect</th><th>Defensive (elite→player)</th><th>Offensive (player→elite)</th></tr>
 *   <tr><td>Base damage</td><td>playerMaxHP / 5</td><td>normalizedMobHP / 3</td></tr>
 *   <tr><td>Skill scaling</td><td>2^((mobLv - armorLv) / 7.5)</td><td>2^((weaponLv - mobLv) / 7.5)</td></tr>
 *   <tr><td>Gear/weapon curve</td><td>ArmorDefenseCalculator</td><td>WeaponOffenseCalculator</td></tr>
 *   <tr><td>Matched gear adj</td><td>1.0 (50% reduction)</td><td>1.0 (50% bonus)</td></tr>
 *   <tr><td>No gear adj</td><td>2.0 (double damage taken)</td><td>0.5 (half damage dealt)</td></tr>
 *   <tr><td>Target at matched</td><td>~5 hits to kill player</td><td>~3 hits to kill mob</td></tr>
 *   <tr><td>1-shot protection</td><td>Yes (cap at maxHP-1)</td><td>No (floor at 1 damage)</td></tr>
 * </table>
 *
 * <h2>Special Cases</h2>
 * <ul>
 *   <li><b>Thorns</b>: Formula-based ({@code baseDamage × thornsLevel × 0.02}), not flat damage.</li>
 *   <li><b>Sweep</b>: Secondary targets take 25% of primary damage.</li>
 *   <li><b>NPC plugins</b>: Non-valid players use raw vanilla event damage (bypass formula).</li>
 *   <li><b>Custom/bypass damage</b>: Boss powers that set bypass=true use raw event damage.</li>
 *   <li><b>Skill bonuses</b>: Applied as multiplicative modifiers AFTER the formula via
 *       {@link #applySkillBonuses()}.</li>
 * </ul>
 *
 * @see PlayerDamagedByEliteMobEvent The defensive (elite → player) mirror of this event
 * @see WeaponOffenseCalculator Weapon adjustment curve and sweep/thorns constants
 * @see ArmorDefenseCalculator The defensive gear curve that this weapon curve mirrors
 * @see LevelScaling#calculateBaseDamageToElite Base damage calculation
 * @see LevelScaling#calculateOffensiveSkillAdjustment Skill adjustment calculation
 */
public class EliteMobDamagedByPlayerEvent extends EliteDamageEvent {

    private static final HandlerList handlers = new HandlerList();
    // Cross-skill target debuff bonuses, processed in the same fixed order the previous
    // hand-rolled blocks ran in: HuntersMark, DeathMark, Judgment, ExposeWeakness.
    private static final List<String> TARGET_DEBUFF_SKILL_IDS = List.of(
            HuntersMarkSkill.SKILL_ID,
            DeathMarkSkill.SKILL_ID,
            JudgmentSkill.SKILL_ID,
            ExposeWeaknessSkill.SKILL_ID);
    @Getter
    private final Entity entity;
    @Getter
    private final EliteEntity eliteMobEntity;
    @Getter
    private final Player player;
    @Getter
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    @Getter
    private final boolean criticalStrike;
    @Getter
    private boolean criticalStrikeDamageApplied;
    @Getter
    private final boolean isCustomDamage;
    @Getter
    private final double damageModifier;
    @Getter
    public boolean rangedAttack;

    /**
     * The weapon skill type from launch time (for ranged attacks).
     * When set, {@link #applySkillBonuses()} uses this instead of reading the player's mainhand.
     * Null for melee attacks.
     */
    @Getter
    @Setter
    private SkillType rangedSkillType = null;

    /**
     * The weapon skill level from launch time (for ranged attacks).
     * When {@link #rangedSkillType} is set, this provides the skill level from launch time.
     * 0 for melee attacks.
     */
    @Getter
    @Setter
    private int rangedSkillLevel = 0;

    /**
     * Event fired when an elite is damaged by a player.
     *
     * @param eliteEntity    Elite damaged.
     * @param player         Player acting as the damged.
     * @param event          Original Minecraft damage event.
     * @param damage         Damage. Can be modifed!
     * @param criticalStrike Whether the strike is a critical strike.
     * @param isCustomDamage Whether the amount of damage is custom, meaning it should apply with no damage reduction of any kind, including armor!
     * @param damageModifier Damage modifiers that the boss may have to reduce incoming damage.
     */
    public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, double damage, boolean criticalStrike, boolean isCustomDamage, double damageModifier) {
        super(damage, event);
        this.entity = eliteEntity.getLivingEntity();
        this.eliteMobEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.rangedAttack = event != null && event.getDamager() instanceof Projectile;
        this.criticalStrike = criticalStrike;
        this.criticalStrikeDamageApplied = criticalStrike;
        this.isCustomDamage = isCustomDamage;
        this.damageModifier = damageModifier;
    }

    /**
     * Constructor for testing purposes that allows explicit ranged attack flag.
     * Use this when simulating attacks without an actual EntityDamageByEntityEvent.
     *
     * @param eliteEntity    Elite damaged.
     * @param player         Player acting as the damager.
     * @param damage         Base damage amount.
     * @param isRangedAttack Whether this is a ranged attack.
     */
    public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, double damage, boolean isRangedAttack) {
        super(damage, null);
        this.entity = eliteEntity.getLivingEntity();
        this.eliteMobEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = null;
        this.rangedAttack = isRangedAttack;
        this.criticalStrike = false;
        this.criticalStrikeDamageApplied = false;
        this.isCustomDamage = false;
        this.damageModifier = 1.0;
    }

    /**
     * Constructor for testing purposes with explicit ranged attack and critical strike flags.
     * Used by CombatSimulator.simulateCriticalAttack() to test skills that require critical hits.
     *
     * @param eliteEntity    Elite damaged.
     * @param player         Player acting as the damager.
     * @param damage         Base damage amount.
     * @param isRangedAttack Whether this is a ranged attack.
     * @param criticalStrike Whether this is a critical strike.
     */
    public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, double damage, boolean isRangedAttack, boolean criticalStrike) {
        super(damage, null);
        this.entity = eliteEntity.getLivingEntity();
        this.eliteMobEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = null;
        this.rangedAttack = isRangedAttack;
        this.criticalStrike = criticalStrike;
        // Test/simulation callers pass raw damage, so no critical multiplier is present yet.
        this.criticalStrikeDamageApplied = false;
        this.isCustomDamage = false;
        this.damageModifier = 1.0;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public double getDamageWithoutCriticalStrike() {
        if (!criticalStrikeDamageApplied) return getDamage();
        return getDamage() / 1.5D;
    }

    /**
     * Combines one skill's multiplier into the running total. Additive for every weapon type:
     * each skill contributes its bonus fraction, so three skills at 1.2x total 1.6x rather than
     * compounding to 1.73x.
     * <p>
     * Axes previously used max() here, which meant only the single largest bonus applied and, worse,
     * that critical strikes were silently discarded on any axe hit where a skill beat 1.5x. Running
     * one weapon type on a different stacking rule also made axe damage incomparable to everything
     * else.
     */
    static double mergeOffensiveMultiplier(double accumulatedMultiplier,
                                             double candidateMultiplier) {
        return accumulatedMultiplier + (candidateMultiplier - 1.0);
    }

    /**
     * Unified method to apply all active skill bonuses to this damage event.
     * This is the single entry point for the skill bonus system to modify offensive damage.
     * <p>
     * Processes all active weapon skills for the player's current weapon type,
     * applying damage multipliers from PASSIVE, CONDITIONAL, STACKING, and PROC skills.
     * <p>
     * For ranged attacks, uses {@link #rangedSkillType} and {@link #rangedSkillLevel}
     * (stored at projectile launch time) instead of reading the player's current mainhand.
     * This prevents weapon-switch bugs where the player switches weapons between firing and impact.
     */
    public void applySkillBonuses() {
        if (player == null || player.hasMetadata("NPC")) return;
        if (SkillsConfig.isWorldExcludedFromSkills(player)) return;
        if (!ElitePlayerInventory.playerInventories.containsKey(player.getUniqueId())) return;

        final boolean debug = DebugMessage.isDebugEnabled(player);

        // For ranged attacks, use skill type/level from launch time (stored in PDC).
        // For melee, read from current mainhand.
        SkillType weaponSkillType;
        int skillLevel;
        if (rangedSkillType != null) {
            weaponSkillType = rangedSkillType;
            skillLevel = rangedSkillLevel > 0 ? rangedSkillLevel : SkillBonusRegistry.getPlayerSkillLevel(player, weaponSkillType);
        } else {
            weaponSkillType = getWeaponSkillType(player);
            if (weaponSkillType == null) return;
            skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, weaponSkillType);
        }
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), weaponSkillType);

        // Skill bonuses stack additively on top of the already-crit damage, for every weapon type.
        // The critical strike stays a separate multiplicative step applied before this event, so it
        // is never folded into (and therefore never swallowed by) the skill multiplier.
        double damageMultiplier = 1.0;
        StringBuilder debugLog = null;
        if (debug) debugLog = new StringBuilder("[SkillBonuses] ");

        // First pass: process damage-modifying skills
        for (String skillId : activeSkillIds) {
            SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
            if (skill == null || !skill.isEnabled()) continue;
            if (!skill.meetsLevelRequirement(skillLevel)) continue;

            // Skip skills that don't affect damage - they'll be processed in second pass
            // Exception: Avatar of Judgment applies damage boost via buff check even though
            // it triggers as a side effect (affectsDamage=false)
            if (!skill.affectsDamage()) {
                // Check for Avatar buff - apply damage boost if buff is active
                if (skill instanceof AvatarOfJudgmentSkill avatar && AvatarOfJudgmentSkill.hasAvatarBuff(player)) {
                    double avatarBoost = avatar.getDamageBoost(skillLevel);
                    if (debug)
                        debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", avatarBoost)).append(" ");
                    damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, avatarBoost);
                }
                continue;
            }

            double skillMultiplier = processOffensiveSkill(skill, skillLevel);
            if (debug && skillMultiplier != 1.0) {
                debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", skillMultiplier)).append(" ");
            }
            damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, skillMultiplier);
        }

        // Check cross-skill debuff bonuses on the target (applied by any player's previous hits)
        if (eliteMobEntity != null && eliteMobEntity.getLivingEntity() != null) {
            LivingEntity target = eliteMobEntity.getLivingEntity();

            for (String debuffSkillId : TARGET_DEBUFF_SKILL_IDS) {
                if (!(SkillBonusRegistry.getSkillById(debuffSkillId) instanceof TargetDebuffBonus targetDebuff))
                    continue;
                if (!targetDebuff.appliesTo(target, player)) continue;
                int debuffLevel = SkillBonusRegistry.getPlayerSkillLevel(player, targetDebuff.levelSource());
                double debuffBonus = targetDebuff.bonusFor(player, target, debuffLevel);
                damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, 1.0 + debuffBonus);
                if (debug)
                    debugLog.append(targetDebuff.debugLabel()).append(String.format("+%.2f", debuffBonus)).append(" ");
            }

            // Riposte: bonus damage if riposte is ready (player blocked recently)
            if (weaponSkillType == SkillType.SWORDS && RiposteSkill.hasRiposteReady(player.getUniqueId())) {
                SkillBonus riposteSkill = SkillBonusRegistry.getSkillById(RiposteSkill.SKILL_ID);
                if (riposteSkill != null && riposteSkill.isActive(player) && riposteSkill.meetsLevelRequirement(skillLevel)) {
                    // Delegate to riposte's onProc to apply bonus and consume the riposte
                    if (riposteSkill instanceof RiposteSkill riposte) {
                        riposte.onProc(player, this);
                    }
                }
            }
        }

        // Apply the combined damage multiplier
        if (damageMultiplier != 1.0) {
            double oldDamage = getDamage();
            setDamage(oldDamage * damageMultiplier);
            if (debug) {
                debugLog.append("| Total=").append(String.format("%.2fx", damageMultiplier))
                        .append(" | Damage: ").append(String.format("%.1f", oldDamage))
                        .append(" -> ").append(String.format("%.1f", getDamage()));
                DebugMessage.log(player, debugLog.toString());
            }
        }

        // Second pass: process non-damage skills (side effects only)
        for (String skillId : activeSkillIds) {
            SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
            if (skill == null || !skill.isEnabled()) continue;
            if (!skill.meetsLevelRequirement(skillLevel)) continue;
            if (skill.affectsDamage()) continue; // Already processed above

            processSideEffectSkill(skill, skillLevel);
        }
    }

    /**
     * Processes a non-damage skill for its side effects (bleed, debuffs, extra projectiles, etc.).
     * These skills have affectsDamage() = false but still need their effects triggered on hit.
     */
    private void processSideEffectSkill(SkillBonus skill, int skillLevel) {
        switch (skill.getBonusType()) {
            case PROC -> {
                if (skill instanceof ProcSkill procSkill) {
                    double procChance = procSkill.getProcChance(skillLevel);
                    if (ThreadLocalRandom.current().nextDouble() < procChance
                            && com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcCooldownTracker
                                    .tryConsume(player, skill.getSkillId(), procSkill.getInternalCooldownMillis())) {
                        procSkill.onProc(player, this);
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                    }
                }
            }
            case STACKING -> {
                if (skill instanceof StackingSkill stackingSkill) {
                    // Special handling for target-tracking stacking skills
                    if (skill instanceof RangersFocusSkill rf && getEliteMobEntity() != null
                            && getEliteMobEntity().getLivingEntity() != null) {
                        rf.setTargetedEnemy(player, getEliteMobEntity().getLivingEntity().getUniqueId());
                    }
                    // Skills that only stack on particular attack shapes veto the stack themselves
                    if (!stackingSkill.stacksOnHit(this)) break;
                    if (stackingSkill.banksStacksExternally()) {
                        // Stacks are added elsewhere (e.g. a kill listener); only read and display them
                        skill.incrementProcCount(player);
                        SkillBonus.sendStackingSkillActionBar(player, skill,
                                stackingSkill.getCurrentStacks(player), stackingSkill.getMaxStacks());
                    } else {
                        stackingSkill.addStack(player);
                        skill.incrementProcCount(player);
                        int stacks = stackingSkill.getCurrentStacks(player);
                        SkillBonus.sendStackingSkillActionBar(player, skill, stacks, stackingSkill.getMaxStacks());
                    }
                }
            }
            case CONDITIONAL -> {
                if (skill instanceof ConditionalSkill conditionalSkill) {
                    if (conditionalSkill.conditionMet(player, this)) {
                        conditionalSkill.onConditionMet(player, this);
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                    }
                }
            }
            case COOLDOWN -> {
                if (skill instanceof CooldownSkill cooldownSkill) {
                    if (!cooldownSkill.triggersOnOffensiveHit()) break;
                    if (!cooldownSkill.isOnCooldown(player)) {
                        if (!cooldownSkill.tryActivate(player, this)) break;
                        if (!cooldownSkill.isOnCooldown(player)) break;
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                        // Note: skills that conditionally activate (e.g. VorpalStrike on crits)
                        // gate themselves in tryActivate and start their own cooldown in
                        // onActivate; the isOnCooldown re-check above detects that they fired
                    }
                }
            }
            case PASSIVE -> {
                // Passive side-effect skills (knockback, stun, speed, haste, etc.)
                if (skill instanceof HeavyBoltsSkill hb && getEliteMobEntity() != null
                        && getEliteMobEntity().getLivingEntity() != null) {
                    hb.applyKnockback(player, getEliteMobEntity().getLivingEntity());
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                if (skill instanceof StunningForceSkill sf) {
                    sf.onHit(player, this);
                    skill.incrementProcCount(player);
                }
                if (skill instanceof WindRunnerSkill wr) {
                    wr.applySpeedBoost(player);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                if (skill instanceof QuickReloadSkill qr) {
                    qr.applyHaste(player);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
            }
        }
    }

    /**
     * Processes a single offensive skill and returns its damage multiplier contribution.
     * Tracks proc counts for testing purposes.
     */
    private double processOffensiveSkill(SkillBonus skill, int skillLevel) {
        return switch (skill.getBonusType()) {
            case PASSIVE -> {
                // Trigger on-hit effects for passive skills that need them
                if (skill instanceof PoseidonsFavorSkill pf) {
                    pf.onHit(player, EliteMobDamagedByPlayerEvent.this);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                // Heavy Bolts: apply knockback alongside the damage bonus
                if (skill instanceof HeavyBoltsSkill hb && eliteMobEntity != null && eliteMobEntity.getLivingEntity() != null) {
                    hb.applyKnockback(player, eliteMobEntity.getLivingEntity());
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                skill.incrementProcCount(player); // Track activation
                yield 1.0 + skill.getBonusValue(skillLevel);
            }
            case CONDITIONAL -> {
                if (skill instanceof ConditionalSkill conditionalSkill) {
                    if (conditionalSkill.conditionMet(player, this)) {
                        conditionalSkill.onConditionMet(player, this);
                        skill.incrementProcCount(player); // Track activation
                        SkillBonus.sendSkillActionBar(player, skill);
                        yield 1.0 + conditionalSkill.getConditionalBonus(player, skillLevel);
                    }
                }
                yield 1.0;
            }
            case STACKING -> {
                if (skill instanceof StackingSkill stackingSkill) {
                    // Skills that only stack on particular attack shapes veto the stack themselves
                    if (!stackingSkill.stacksOnHit(this)) yield 1.0;
                    // RangersFocus: track target before calculating stacks (switch resets stacks)
                    if (skill instanceof RangersFocusSkill rf && eliteMobEntity != null
                            && eliteMobEntity.getLivingEntity() != null) {
                        rf.setTargetedEnemy(player, eliteMobEntity.getLivingEntity().getUniqueId());
                    }
                    int stacks = stackingSkill.getCurrentStacks(player);
                    if (stackingSkill.banksStacksExternally()) {
                        // Stacks are added elsewhere (e.g. a kill listener); only read and display them
                        skill.incrementProcCount(player); // Track activation
                        SkillBonus.sendStackingSkillActionBar(player, skill, stacks, stackingSkill.getMaxStacks());
                    } else {
                        stackingSkill.addStack(player); // Add stack for this hit
                        skill.incrementProcCount(player); // Track activation
                        SkillBonus.sendStackingSkillActionBar(player, skill, stacks + 1, stackingSkill.getMaxStacks());
                    }
                    yield 1.0 + (stacks * stackingSkill.getBonusPerStack(skillLevel));
                }
                yield 1.0;
            }
            case PROC -> {
                if (skill instanceof ProcSkill procSkill) {
                    double procChance = procSkill.getProcChance(skillLevel);
                    if (ThreadLocalRandom.current().nextDouble() < procChance
                            && com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcCooldownTracker
                                    .tryConsume(player, skill.getSkillId(), procSkill.getInternalCooldownMillis())) {
                        procSkill.onProc(player, this);
                        skill.incrementProcCount(player); // Track proc
                        SkillBonus.sendSkillActionBar(player, skill);
                        yield 1.0 + skill.getBonusValue(skillLevel);
                    }
                }
                yield 1.0;
            }
            case COOLDOWN -> {
                if (skill instanceof CooldownSkill cooldownSkill) {
                    if (!cooldownSkill.triggersOnOffensiveHit()) yield 1.0;
                    if (!cooldownSkill.isOnCooldown(player)) {
                        // Ask the skill whether it actually fired. Skills with their own gating
                        // condition report false, in which case neither the cooldown nor the
                        // damage bonus is consumed and the condition keeps its meaning.
                        if (!cooldownSkill.tryActivate(player, this)) yield 1.0;
                        cooldownSkill.startCooldown(player, skillLevel);
                        skill.incrementProcCount(player); // Track activation
                        SkillBonus.sendSkillActionBar(player, skill);
                        yield 1.0 + skill.getBonusValue(skillLevel);
                    }
                }
                yield 1.0;
            }
        };
    }

    /**
     * Determines the weapon skill type based on the player's main hand item.
     */
    static SkillType getWeaponSkillType(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR) return null;

        Material type = mainHand.getType();
        String typeName = type.name();

        if (typeName.endsWith("_SWORD")) return SkillType.SWORDS;
        if (typeName.endsWith("_AXE")) return SkillType.AXES;
        if (type == Material.BOW) return SkillType.BOWS;
        if (type == Material.CROSSBOW) return SkillType.CROSSBOWS;
        if (type == Material.TRIDENT) return SkillType.TRIDENTS;
        if (typeName.endsWith("_HOE")) return SkillType.HOES;

        // Check for maces (1.21+)
        try {
            if (type == Material.MACE) return SkillType.MACES;
        } catch (NoSuchFieldError e) {
            // MACE doesn't exist pre-1.21
        }

        // Check for spears (1.21.11+)
        if (typeName.endsWith("_SPEAR")) return SkillType.SPEARS;

        return null;
    }

}
