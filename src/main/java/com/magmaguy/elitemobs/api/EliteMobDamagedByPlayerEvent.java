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
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.HuntersMarkSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.RangersFocusSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.WindRunnerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HeavyBoltsSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.QuickReloadSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.AvatarOfJudgmentSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.StunningForceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ExposeWeaknessSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.PoseidonsFavorSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.ReturningHasteSkill;
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
import java.util.UUID;
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
        if (!criticalStrike) return getDamage();
        return getDamage() / 1.5D;
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

        double damageMultiplier = 1.0;
        StringBuilder debugLog = new StringBuilder();
        debugLog.append("[SkillBonuses] ");

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
                    debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", avatarBoost)).append(" ");
                    damageMultiplier += (avatarBoost - 1.0);
                }
                continue;
            }

            double skillMultiplier = processOffensiveSkill(skill, skillLevel);
            if (skillMultiplier != 1.0) {
                debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", skillMultiplier)).append(" ");
            }
            damageMultiplier += (skillMultiplier - 1.0); // Additive stacking for linear scaling
        }

        // Check cross-skill debuff bonuses on the target (applied by any player's previous hits)
        if (eliteMobEntity != null && eliteMobEntity.getLivingEntity() != null) {
            UUID targetUUID = eliteMobEntity.getLivingEntity().getUniqueId();

            // HuntersMark: bonus damage against marked targets
            if (HuntersMarkSkill.isMarkedBy(eliteMobEntity.getLivingEntity(), player)) {
                int bowLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.BOWS);
                SkillBonus markSkill = SkillBonusRegistry.getSkillById(HuntersMarkSkill.SKILL_ID);
                if (markSkill instanceof HuntersMarkSkill hm) {
                    double markBonus = hm.getMarkBonus(bowLevel);
                    damageMultiplier += markBonus;
                    debugLog.append("HuntersMark=").append(String.format("+%.2f", markBonus)).append(" ");
                }
            }

            // ExposeWeakness: bonus damage against debuffed targets
            if (ExposeWeaknessSkill.isDebuffed(targetUUID)) {
                double exposeBonus = ExposeWeaknessSkill.getDamageMultiplier(targetUUID) - 1.0;
                damageMultiplier += exposeBonus;
                debugLog.append("ExposeWeakness=").append(String.format("+%.2f", exposeBonus)).append(" ");
            }

            // Riposte: bonus damage if riposte is ready (player blocked recently)
            if (RiposteSkill.hasRiposteReady(player.getUniqueId())) {
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
            debugLog.append("| Total=").append(String.format("%.2fx", damageMultiplier))
                    .append(" | Damage: ").append(String.format("%.1f", oldDamage))
                    .append(" -> ").append(String.format("%.1f", getDamage()));
            DebugMessage.log(player, debugLog.toString());
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
                    // Special handling for ReturningHaste - only stack on projectile attacks
                    if (skill instanceof ReturningHasteSkill) {
                        if (entityDamageByEntityEvent == null) {
                            // In test mode (rangedAttack flag set without real event), allow stacking
                            if (!rangedAttack) break;
                        } else if (entityDamageByEntityEvent.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                            break; // Skip stacking for melee trident
                        }
                    }
                    stackingSkill.addStack(player);
                    skill.incrementProcCount(player);
                    int stacks = stackingSkill.getCurrentStacks(player);
                    SkillBonus.sendStackingSkillActionBar(player, skill, stacks, stackingSkill.getMaxStacks());
                }
            }
            case CONDITIONAL -> {
                if (skill instanceof ConditionalSkill conditionalSkill) {
                    if (conditionalSkill.conditionMet(player, this)) {
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                    }
                }
            }
            case COOLDOWN -> {
                if (skill instanceof CooldownSkill cooldownSkill) {
                    if (!cooldownSkill.triggersOnOffensiveHit()) break;
                    if (!cooldownSkill.isOnCooldown(player)) {
                        cooldownSkill.onActivate(player, this);
                        if (!cooldownSkill.isOnCooldown(player)) break;
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                        // Note: skills that conditionally activate (e.g. VorpalStrike on crits)
                        // handle their own cooldown start in onActivate
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
                        skill.incrementProcCount(player); // Track activation
                        SkillBonus.sendSkillActionBar(player, skill);
                        yield 1.0 + conditionalSkill.getConditionalBonus(skillLevel);
                    }
                }
                yield 1.0;
            }
            case STACKING -> {
                if (skill instanceof StackingSkill stackingSkill) {
                    // ReturningHaste: only stack on projectile attacks (thrown trident, not melee)
                    if (skill instanceof ReturningHasteSkill) {
                        if (entityDamageByEntityEvent == null) {
                            // In test mode (rangedAttack flag set without real event), allow stacking
                            if (!rangedAttack) yield 1.0;
                        } else if (entityDamageByEntityEvent.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                            yield 1.0; // Skip stacking for melee trident
                        }
                    }
                    // RangersFocus: track target before calculating stacks (switch resets stacks)
                    if (skill instanceof RangersFocusSkill rf && eliteMobEntity != null
                            && eliteMobEntity.getLivingEntity() != null) {
                        rf.setTargetedEnemy(player, eliteMobEntity.getLivingEntity().getUniqueId());
                    }
                    int stacks = stackingSkill.getCurrentStacks(player);
                    stackingSkill.addStack(player); // Add stack for this hit
                    skill.incrementProcCount(player); // Track activation
                    SkillBonus.sendStackingSkillActionBar(player, skill, stacks + 1, stackingSkill.getMaxStacks());
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
                        cooldownSkill.onActivate(player, this);
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
    private static SkillType getWeaponSkillType(Player player) {
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

    //The thing that calls the event
    public static class EliteMobDamagedByPlayerEventFilter implements Listener {
        public static boolean bypass = false;

        /**
         * Applies a projectile hit that was caught by a custom model hitbox rather
         * than by Minecraft's native entity hitbox.
         * <p>
         * Some modeled-hit callbacks can call {@link LivingEntity#damage(double, Entity)}
         * and still land raw damage without producing the {@link EntityDamageByEntityEvent}
         * shape EliteMobs handles. This method explicitly fires the same event shape a
         * normal arrow hit would produce, lets EliteMobs and other listeners modify it,
         * then applies the final event damage to the mob's health.
         */
        public static void applyModeledProjectileHit(Player player, EliteEntity eliteEntity, Projectile projectile, double rawDamage) {
            if (player == null || eliteEntity == null || projectile == null) return;
            LivingEntity target = eliteEntity.getLivingEntity();
            if (target == null || !target.isValid()) return;

            EntityDamageByEntityEvent syntheticEvent = new EntityDamageByEntityEvent(
                    projectile,
                    target,
                    EntityDamageEvent.DamageCause.PROJECTILE,
                    DamageSource.builder(DamageType.MOB_ATTACK).build(),
                    rawDamage);
            new EventCaller(syntheticEvent);
            if (syntheticEvent.isCancelled()) return;

            double finalDamage = Math.max(0D, syntheticEvent.getDamage());
            if (finalDamage <= 0D || !eliteEntity.isValid() || target.isDead()) return;

            double newHealth = target.getHealth() - finalDamage;
            target.setLastDamageCause(syntheticEvent);
            if (newHealth <= 0D) {
                eliteEntity.syncPluginHealth(0D);
                target.setHealth(0D);
            } else {
                eliteEntity.setHealth(newHealth);
            }
        }

        /**
         * Gets the total elite thorns enchantment level across all armor pieces.
         * Only counts levels above vanilla max (elite-only portion).
         */
        private static int getEliteThornsLevel(Player player) {
            if (!ItemSettingsConfig.isUseEliteEnchantments()) return 0;
            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            int thornsLevel = 0;
            if (elitePlayerInventory.helmet.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.helmet.thornsLevel - Enchantment.THORNS.getMaxLevel();
            if (elitePlayerInventory.chestplate.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.chestplate.thornsLevel - Enchantment.THORNS.getMaxLevel();
            if (elitePlayerInventory.leggings.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.leggings.thornsLevel - Enchantment.THORNS.getMaxLevel();
            if (elitePlayerInventory.boots.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.boots.thornsLevel - Enchantment.THORNS.getMaxLevel();
            return thornsLevel;
        }

        /**
         * Gets the secondary enchantment multiplier (Smite/Bane of Arthropods).
         * Returns a multiplier (e.g. 1.05 for 5% bonus) rather than flat damage.
         */
        private static double getSecondaryEnchantmentMultiplier(Player player, LivingEntity livingEntity) {
            if (ItemSettingsConfig.isUseEliteEnchantments()) return 1.0;
            if (livingEntity instanceof Spider || livingEntity instanceof Silverfish) {
                int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageArthropodsLevel(player.getInventory().getItemInMainHand(), false);
                level -= Enchantment.BANE_OF_ARTHROPODS.getMaxLevel();
                if (level < 1) return 1.0;
                return 1.0 + level * 0.025; // 2.5% per level
            }
            if (livingEntity instanceof Zombie || livingEntity instanceof Skeleton || livingEntity instanceof Wither || livingEntity instanceof SkeletonHorse || livingEntity instanceof ZombieHorse || livingEntity.getType().equals(EntityType.ZOMBIFIED_PIGLIN)) {
                int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageUndeadLevel(player.getInventory().getItemInMainHand(), false);
                level -= Enchantment.SMITE.getMaxLevel();
                if (level < 1) return 1.0;
                return 1.0 + level * 0.025; // 2.5% per level
            }
            return 1.0;
        }

        /**
         * Resolves the attack speed used for cooldown timing.
         * <p>
         * The item speed is still used for per-hit damage pacing. This method
         * is only for the recharge window, where vanilla uses the player's live
         * ATTACK_SPEED attribute after equipment and modifier updates. If Bukkit ever
         * reports the unmodified player base speed (4.0) while the held weapon is
         * slower, fall back to the item-derived speed to avoid making slow weapons
         * recharge as fast as fists.
         */
        private static double getCooldownAttackSpeed(Player player, double itemAttackSpeed, boolean isRangedWeaponMelee) {
            if (isRangedWeaponMelee) return 4.0;

            AttributeInstance attackSpeedAttribute = player.getAttribute(Attribute.ATTACK_SPEED);
            if (attackSpeedAttribute == null) return itemAttackSpeed;

            double attributeAttackSpeed = attackSpeedAttribute.getValue();
            if (!Double.isFinite(attributeAttackSpeed) || attributeAttackSpeed <= 0D) return itemAttackSpeed;

            if (itemAttackSpeed < 4.0D && attributeAttackSpeed == 4.0D) return itemAttackSpeed;
            return attributeAttackSpeed;
        }

        private static double getCustomDamageModifier(EliteEntity eliteEntity, Material itemStackType) {
            if (!(eliteEntity instanceof CustomBossEntity)) return 1;
            return ((CustomBossEntity) eliteEntity).getDamageModifier(itemStackType);
        }

        private static boolean isCriticalHit(Player player) {
            double criticalStrike = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getCritChance(false);
            return ThreadLocalRandom.current().nextDouble() < criticalStrike;
        }

        /**
         * Gets the player's weapon skill level for the weapon they are currently using.
         *
         * @param player The player to get the skill level for
         * @return The player's weapon skill level (minimum 1)
         */
        private static int getPlayerWeaponSkillLevel(Player player) {
            if (SkillsConfig.isWorldExcludedFromSkills(player)) return 1;
            SkillType weaponSkillType = getWeaponSkillType(player);
            if (weaponSkillType == null) return 1;
            long skillXP = PlayerData.getSkillXP(player.getUniqueId(), weaponSkillType);
            return Math.max(1, SkillXPCalculator.levelFromTotalXP(skillXP));
        }

        /**
         * Calculates player-to-elite damage using the pure formula approach.
         * <p>
         * This is the offensive counterpart to
         * {@link PlayerDamagedByEliteMobEvent.PlayerDamagedByEliteMobEventFilter#eliteToPlayerDamageFormula
         * eliteToPlayerDamageFormula}. No pre-compensation. Seven multiplicative layers:
         * <ol>
         *   <li><b>Base damage</b> = normalizedMobHP / {@link LevelScaling#TARGET_HITS_TO_KILL_MOB}
         *       — ensures constant hit count at all levels</li>
         *   <li><b>Attack speed factor</b> = {@link WeaponOffenseCalculator#getAttackSpeedFactor}
         *       — tuned melee pacing factor from weapon family and speed (melee only; 1.0 for ranged)</li>
         *   <li><b>Skill adjustment</b> = 2^((skillLv - mobLv) / {@link LevelScaling#OFFENSIVE_SKILL_SCALING_RATE})
         *       — exponential scaling from player skill vs mob level</li>
         *   <li><b>Weapon adjustment</b> = {@link WeaponOffenseCalculator#getWeaponAdjustment}
         *       — two-part linear curve [0.5, 1.25] from weapon level vs mob level</li>
         *   <li><b>Cooldown / velocity</b> = tracked melee charge from
         *       {@link PlayerAttackCooldownTracker} (melee) or
         *       {@link WeaponOffenseCalculator#normalizeArrowVelocity} (ranged) — [0, 1]</li>
         *   <li><b>Sweep multiplier</b> = {@link WeaponOffenseCalculator#SWEEP_DAMAGE_FRACTION}
         *       for sweep secondary targets, 1.0 for primary — handles sword sweep</li>
         *   <li><b>Equipment enchantment multiplier</b> = 1.0 + Sharpness / Power levels × 0.025
         *       summed from all equipped slots, preserving legacy global-slot behavior</li>
         *   <li><b>Enchantment multiplier</b> = 1.0 + eliteEnchantLevel × 0.025
         *       — Smite / Bane of Arthropods (elite-only levels above vanilla max)</li>
         * </ol>
         * <p>
         * Expected outcomes at matched combat (weaponSkillLv == mobLv, weaponItemLv == mobLv):
         * <table border="1">
         *   <tr><th>Weapon</th><th>Speed</th><th>Dmg/Hit</th><th>Hits</th><th>TTK (sec)</th></tr>
         *   <tr><td>Sword</td><td>1.6</td><td>23.33</td><td>3.0</td><td>1.875</td></tr>
         *   <tr><td>Axe</td><td>1.0</td><td>29.5</td><td>2.37</td><td>2.37</td></tr>
         *   <tr><td>Hoe</td><td>4.0</td><td>9.33</td><td>7.5</td><td>1.875</td></tr>
         *   <tr><td>Bow</td><td>N/A</td><td>23.33</td><td>3.0</td><td>N/A</td></tr>
         * </table>
         * <p>
         * For ranged attacks, weapon level, skill type, and skill level are read from the
         * projectile's {@link org.bukkit.persistence.PersistentDataContainer} (stored at
         * launch time), so weapon switching between firing and impact does not corrupt
         * the damage calculation.
         *
         * @param player      The attacking player
         * @param eliteEntity The elite mob being attacked
         * @param event       The original damage event
         * @return The formula-computed damage (before damageModifier/combatMultiplier/crit)
         */
        private static double playerToEliteDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
            int mobLevel = eliteEntity.getLevel();
            ItemStack weapon = player.getInventory().getItemInMainHand();
            boolean isRanged = event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE;
            boolean isSweep = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
            boolean skillsExcluded = SkillsConfig.isWorldExcludedFromSkills(player);

            // Ranged-only weapons (bows, crossbows) used in melee count as unarmed strikes
            boolean isRangedWeaponMelee = false;
            if (!isRanged && weapon != null) {
                Material weaponType = weapon.getType();
                if (weaponType == Material.BOW || weaponType == Material.CROSSBOW) {
                    isRangedWeaponMelee = true;
                }
            }

            // 1. Base damage — fraction of normalized mob HP
            double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(eliteEntity, mobLevel);

            // 2. Attack speed pacing (melee only).
            // The damage factor uses the item's weapon family and speed so slow weapons
            // still hit harder per swing without full inverse-speed burst. The cooldown
            // window below separately uses the player's live
            // ATTACK_SPEED attribute when available, because that is what vanilla uses
            // for actual recharge timing after equipment and skill modifiers.
            double attackSpeed = isRangedWeaponMelee ? 4.0 : EliteItemManager.getAttackSpeed(weapon);
            double cooldownAttackSpeed = getCooldownAttackSpeed(player, attackSpeed, isRangedWeaponMelee);
            double attackSpeedFactor = 1.0;
            if (!isRanged) {
                attackSpeedFactor = WeaponOffenseCalculator.getAttackSpeedFactor(weapon, attackSpeed);
            }

            // 3 & 4. Resolve weapon level and skill level.
            // For ranged: read from projectile PDC (stored at launch time) to avoid weapon-switch bugs.
            // For melee: read from current mainhand as before.
            double weaponLevel;
            int weaponSkillLevel;
            if (isRanged) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile instanceof Trident trident) {
                    // Trident IS the weapon — read item level directly from the thrown trident
                    weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(trident.getItem());
                } else {
                    // Arrow/bolt: read weapon level from PDC (stored at launch time)
                    double storedLevel = ItemTagger.getArrowWeaponLevel(projectile);
                    weaponLevel = storedLevel >= 0 ? storedLevel : WeaponOffenseCalculator.getEffectiveWeaponLevel(weapon);
                }
                // Skill level: read from PDC (stored at launch time)
                Projectile proj = (Projectile) event.getDamager();
                int storedSkillLevel = ItemTagger.getArrowSkillLevel(proj);
                weaponSkillLevel = storedSkillLevel >= 0 ? storedSkillLevel : getPlayerWeaponSkillLevel(player);
            } else if (isRangedWeaponMelee) {
                // Ranged weapons used in melee = unarmed (level 0, skill level 1)
                weaponLevel = 0;
                weaponSkillLevel = 1;
            } else {
                weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(weapon);
                weaponSkillLevel = getPlayerWeaponSkillLevel(player);
            }
            if (skillsExcluded) weaponSkillLevel = 1;

            double skillAdjustment = LevelScaling.calculateOffensiveSkillAdjustment(weaponSkillLevel, mobLevel);
            double weaponAdjustment = WeaponOffenseCalculator.getWeaponAdjustment(weaponLevel, mobLevel);

            // 5. Attack cooldown (melee) or arrow velocity (ranged)
            double cooldownOrVelocity;
            long ticksSinceLastHit = PlayerAttackCooldownTracker.NO_PREVIOUS_HIT;
            if (isRanged) {
                // Prefer the launch-time velocity stored in the projectile PDC. Reading
                // velocity at impact under-reports because arrows decelerate from gravity
                // and drag — long-range shots drop well below the 3.0 full-draw value
                // and silently halve damage. Fall back to current velocity for arrows
                // tagged before this field existed.
                Projectile rangedProjectile = (Projectile) event.getDamager();
                double launchVelocity = ItemTagger.getArrowLaunchVelocity(rangedProjectile);
                if (launchVelocity >= 0) {
                    cooldownOrVelocity = WeaponOffenseCalculator.normalizeArrowVelocity(launchVelocity);
                } else {
                    cooldownOrVelocity = WeaponOffenseCalculator.normalizeArrowVelocity(rangedProjectile);
                }
            } else if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                // Sweep secondaries only fire as part of a full-strength primary swing
                // (vanilla requires >0.9 attack charge to sweep at all), so treat them as
                // fully charged here. The 0.25 sweep reduction is applied separately via
                // sweepMultiplier — don't double-penalize the secondary targets.
                cooldownOrVelocity = 1.0;
            } else {
                // Primary melee swing: reconstruct the attack charge ourselves instead of
                // trusting Bukkit's Player#getAttackCooldown(). Inside an
                // EntityDamageByEntityEvent the vanilla attack-strength ticker has already
                // been consumed/reset for this swing, so getAttackCooldown() reports a
                // near-zero charge even for a fully recharged swing — which silently gutted
                // melee damage (a matched sword landed ~10% of its intended hit). We instead
                // measure the real-time gap since this player's previous melee hit
                // (GameClock-backed PlayerAttackCooldownTracker) and divide by the weapon's
                // full-recharge window (20 / attackSpeed ticks), clamped to [0, 1] — the same
                // shape as the vanilla attack-strength curve. Spam-clicking still yields a low
                // charge (short gap); a properly paced full swing yields 1.0.
                ticksSinceLastHit = PlayerAttackCooldownTracker.recordHit(player);
                double rechargeTicks = cooldownAttackSpeed > 0 ? 20.0 / cooldownAttackSpeed : 20.0;
                cooldownOrVelocity = ticksSinceLastHit == PlayerAttackCooldownTracker.NO_PREVIOUS_HIT
                        ? 1.0 // first tracked swing (or first after a 5-min idle eviction): assume fully charged
                        : Math.min(ticksSinceLastHit / rechargeTicks, 1.0);
            }

            // 6. Sweep reduction (secondary targets)
            double sweepMultiplier = WeaponOffenseCalculator.getSweepMultiplier(event);

            // 7. Strength/Weakness potion scaling
            double potionMultiplier = PotionCombatModifierCalculator.getOutgoingDamageMultiplier(player);

            // 8. Sharpness/Power percentage bonus from all equipped slots.
            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            double equipmentEnchantmentBonus = elitePlayerInventory != null
                    ? elitePlayerInventory.getEliteEnchantmentDamage(true)
                    : 0D;
            double equipmentEnchantmentMultiplier = 1.0 + equipmentEnchantmentBonus;

            // 9. Secondary enchantment multiplier (Smite/Bane)
            LivingEntity target = eliteEntity.getLivingEntity();
            double enchantmentMultiplier = (target != null) ?
                    getSecondaryEnchantmentMultiplier(player, target) : 1.0;

            // 10. Skill-spawned arrow damage multiplier (Multishot, Arrow Rain, etc.)
            // Skills that spawn extra arrows store a damage multiplier in the arrow's PDC
            // to reduce their damage relative to the formula output.
            double arrowDamageMultiplier = 1.0;
            if (isRanged) {
                double storedMultiplier = ItemTagger.getArrowDamageMultiplier((Projectile) event.getDamager());
                if (storedMultiplier > 0) {
                    arrowDamageMultiplier = storedMultiplier;
                }
            }

            // Compute formula damage
            double formulaDamage = baseDamage * attackSpeedFactor * skillAdjustment
                    * weaponAdjustment * cooldownOrVelocity * sweepMultiplier
                    * potionMultiplier
                    * equipmentEnchantmentMultiplier
                    * enchantmentMultiplier * arrowDamageMultiplier;

            // Populate breakdown if tracking is active
            DamageBreakdown breakdown = DamageBreakdown.getActiveBreakdown(player);
            if (breakdown != null) {
                breakdown.setBaseDamage(baseDamage);
                breakdown.setAttackSpeedFactor(attackSpeedFactor);
                breakdown.setSkillAdjustment(skillAdjustment);
                breakdown.setWeaponAdjustment(weaponAdjustment);
                breakdown.setCooldownOrVelocity(cooldownOrVelocity);
                breakdown.setSweepMultiplier(sweepMultiplier);
                breakdown.setPotionMultiplier(potionMultiplier);
                breakdown.setEquipmentEnchantmentMultiplier(equipmentEnchantmentMultiplier);
                breakdown.setEnchantmentMultiplier(enchantmentMultiplier);
                breakdown.setArrowDamageMultiplier(arrowDamageMultiplier);
                breakdown.setPlayerSkillLevel(weaponSkillLevel);
                breakdown.setItemLevel((int) weaponLevel);
                breakdown.setEliteLevel(mobLevel);
                breakdown.setWeaponType(weapon.getType().name());
                breakdown.setRangedAttack(isRanged);
                breakdown.setSweepAttack(isSweep);
            }

            // Debug logging
            DebugMessage.log(player, "[Formula] Base=" + String.format("%.1f", baseDamage) +
                    " Speed=" + String.format("%.2f", attackSpeedFactor) +
                    " Skill=" + String.format("%.3f", skillAdjustment) +
                    " (Lv" + weaponSkillLevel + " vs " + mobLevel + ")" +
                    " Wpn=" + String.format("%.2f", weaponAdjustment) +
                    " (Lv" + (int) weaponLevel + ")" +
                    " CD=" + String.format("%.2f", cooldownOrVelocity) +
                    (ticksSinceLastHit != PlayerAttackCooldownTracker.NO_PREVIOUS_HIT
                            ? " Δticks=" + ticksSinceLastHit
                            : "") +
                    (potionMultiplier != 1.0 ? " Pot=" + String.format("%.2f", potionMultiplier) : "") +
                    (isSweep ? " Sweep=" + String.format("%.2f", sweepMultiplier) : "") +
                    (equipmentEnchantmentMultiplier != 1.0 ? " EquipEnchant=" + String.format("%.2f", equipmentEnchantmentMultiplier) + "x" : "") +
                    (arrowDamageMultiplier != 1.0 ? " ArrowMult=" + String.format("%.2f", arrowDamageMultiplier) : "") +
                    " = " + String.format("%.1f", formulaDamage));

            // Per-player diagnostic breakdown (toggle with /em debug)
            if (DebugMessage.isDebugEnabled(player)) {
                DebugMessage.send(player, "§e── Formula (playerToEliteDamageFormula) ──");
                DebugMessage.send(player, "§7Inputs: weaponSkillLv=§f" + weaponSkillLevel
                        + " §7weaponLv=§f" + ((int) weaponLevel)
                        + " §7mobLv-in-formula=§f" + mobLevel
                        + " §8(mob level is overridden by player skill level when scaled combat is on)");
                DebugMessage.send(player, "§7Base = mobHP_at_this_level / TARGET_HITS_TO_KILL_MOB ("
                        + String.format("%.1f", LevelScaling.TARGET_HITS_TO_KILL_MOB) + ")");
                DebugMessage.send(player, "§7   = §f" + String.format("%.2f", baseDamage)
                        + " §8(damage needed per hit to kill in TARGET_HITS hits; uses legacy or recommended HP curve)");
                DebugMessage.send(player, "§7× Attack speed factor = tuned melee pacing curve = §f"
                        + String.format("%.3f", attackSpeedFactor) + " §8(1.0 for ranged; family+speed adjusted)");
                if (!isRanged && cooldownAttackSpeed != attackSpeed) {
                    DebugMessage.send(player, "§7  ↳ cooldown speed = §f" + String.format("%.3f", cooldownAttackSpeed)
                            + " §8(player ATTACK_SPEED attribute used for recharge timing)");
                }
                DebugMessage.send(player, "§7× Skill adjustment = 2^((skillLv − mobLv)/"
                        + String.format("%.1f", LevelScaling.OFFENSIVE_SKILL_SCALING_RATE) + ") = §f"
                        + String.format("%.3f", skillAdjustment) + " §8(>1 if you out-skill the mob)");
                DebugMessage.send(player, "§7× Weapon adjustment = §f" + String.format("%.3f", weaponAdjustment)
                        + " §8(piecewise linear from weapon vs mob level; 0.5 under, 1.0 matched, 1.25 over)");
                DebugMessage.send(player, "§7× " + (isRanged ? "Arrow velocity" : "Attack cooldown") + " = §f"
                        + String.format("%.3f", cooldownOrVelocity)
                        + " §8(0..1; partial swings/slow arrows scale damage down)");
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    String gapDisplay = ticksSinceLastHit == PlayerAttackCooldownTracker.NO_PREVIOUS_HIT
                            ? "first hit (no prior swing tracked)"
                            : ticksSinceLastHit + " ticks ("
                                    + String.format("%.2f", ticksSinceLastHit / 20.0) + "s)";
                    DebugMessage.send(player, "§7  ↳ ticks since last melee hit by this player = §f"
                            + gapDisplay
                            + " §8(the attack charge above is now derived from this real-time gap"
                            + " ÷ the weapon's recharge window, NOT Bukkit getAttackCooldown())");
                }
                DebugMessage.send(player, "§7× Sweep multiplier = §f" + String.format("%.3f", sweepMultiplier)
                        + " §8(SWEEP_DAMAGE_FRACTION for sweep secondaries, 1.0 otherwise)");
                DebugMessage.send(player, "§7× Potion multiplier (outgoing) = §f"
                        + String.format("%.3f", potionMultiplier) + " §8(strength/weakness on you)");
                DebugMessage.send(player, "§7× Equipment enchantment multiplier = §f"
                        + String.format("%.3f", equipmentEnchantmentMultiplier)
                        + " §8(Sharpness/Power from all equipped slots; +"
                        + String.format("%.1f", equipmentEnchantmentBonus * 100)
                        + "% damage)");
                DebugMessage.send(player, "§7× Enchantment multiplier = §f"
                        + String.format("%.3f", enchantmentMultiplier)
                        + " §8(Smite/Bane elite-only levels above vanilla max)");
                DebugMessage.send(player, "§7× Arrow damage multiplier = §f"
                        + String.format("%.3f", arrowDamageMultiplier)
                        + " §8(set by Multishot/Arrow Rain skills on extra arrows; 1.0 for normal shots)");
                DebugMessage.send(player, "§7= formulaDamage (BEFORE outer combat/config multiplier and crit) = §f"
                        + String.format("%.2f", formulaDamage));
            }

            return formulaDamage;
        }

        /**
         * Wraps playerToEliteDamageFormula for scaled combat.
         * Simulates the boss at the player's weapon skill level, then rescales
         * to the boss's actual HP pool. This makes level irrelevant while
         * keeping gear meaningful.
         */
        private static double scaledPlayerToEliteDamage(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
            // 1. Get the player's weapon skill level (the "simulated" mob level)
            // Ranged weapons used in melee = unarmed, so simulated level is 1
            boolean isRangedMelee = event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE;
            if (isRangedMelee) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand != null) {
                    Material type = mainHand.getType();
                    isRangedMelee = type == Material.BOW || type == Material.CROSSBOW;
                } else {
                    isRangedMelee = false;
                }
            }
            int simulatedMobLevel = isRangedMelee ? 1 : getPlayerWeaponSkillLevel(player);
            if (simulatedMobLevel <= 0) simulatedMobLevel = 1;

            // For ranged attacks, read skill level from projectile PDC (stored at launch time)
            if (!SkillsConfig.isWorldExcludedFromSkills(player) &&
                    event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE &&
                    event.getDamager() instanceof Projectile proj) {
                int storedSkillLevel = ItemTagger.getArrowSkillLevel(proj);
                if (storedSkillLevel > 0) simulatedMobLevel = storedSkillLevel;
            }

            // 2. Temporarily override mob level to simulate matched combat
            int realMobLevel = eliteEntity.getLevel();
            eliteEntity.setLevel(simulatedMobLevel);

            // 3. Run the standard formula (now sees mobLevel = player's level)
            double formulaDamage = playerToEliteDamageFormula(player, eliteEntity, event);

            // 4. Restore real level immediately
            eliteEntity.setLevel(realMobLevel);

            // 5. Rescale: convert from "damage to simulated mob" to "equivalent % of actual boss HP"
            double simulatedMobHP = NaturalEliteCombatTweak.getTweakedMobHealthForLevel(
                    eliteEntity, simulatedMobLevel, eliteEntity.getHealthMultiplier());
            double actualBossMaxHP = eliteEntity.getMaxHealth();
            double damagePercentage = formulaDamage / simulatedMobHP;
            double rescaledDamage = damagePercentage * actualBossMaxHP;

            if (DebugMessage.isDebugEnabled(player)) {
                DebugMessage.send(player, "§e── Scaled-combat rescale ──");
                DebugMessage.send(player, "§7Real mob level (restored): §f" + realMobLevel
                        + " §7  Simulated mob level used in formula: §f" + simulatedMobLevel
                        + " §8(player's weapon skill level; 1 for ranged-in-melee)");
                DebugMessage.send(player, "§7formulaDamage @ simulated level = §f"
                        + String.format("%.2f", formulaDamage));
                DebugMessage.send(player, "§7Simulated mob HP @ simulated level = §f"
                        + String.format("%.2f", simulatedMobHP)
                        + " §8(NaturalEliteCombatTweak.getTweakedMobHealthForLevel × healthMultiplier=§7"
                        + String.format("%.2f", eliteEntity.getHealthMultiplier()) + "§8)");
                DebugMessage.send(player, "§7Actual boss max HP = §f" + String.format("%.2f", actualBossMaxHP));
                DebugMessage.send(player, "§7damagePercentage = formulaDamage / simulatedMobHP = §f"
                        + String.format("%.4f", damagePercentage)
                        + " §8(% of simulated mob HP this hit took)");
                DebugMessage.send(player, "§7rescaledDamage = damagePercentage × actualBossMaxHP = §f"
                        + String.format("%.2f", rescaledDamage)
                        + " §8(equivalent % of REAL mob HP; combat feels level-agnostic)");
            }

            return rescaledDamage;
        }

        /**
         * Calculates thorns damage using the formula approach.
         * <p>
         * Thorns damage is a percentage of base damage per elite thorns level
         * (only levels above vanilla max count). This replaces the old flat
         * {@code level * 2.5D} approach, which didn't scale with mob level.
         * <p>
         * Formula: {@code baseDamage × thornsLevel × THORNS_PERCENT_PER_LEVEL}
         * <p>
         * At level 3 elite thorns: {@code baseDamage × 3 × 0.02 = 6%} of base damage.
         *
         * @param player      The player whose armor thorns are being evaluated
         * @param eliteEntity The elite mob taking thorns damage
         * @return The thorns damage amount, or 0 if no elite thorns
         */
        private static double calculateThornsDamage(Player player, EliteEntity eliteEntity) {
            int thornsLevel = getEliteThornsLevel(player);
            if (thornsLevel <= 0) return 0;

            double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(eliteEntity, eliteEntity.getLevel());
            return baseDamage * thornsLevel * WeaponOffenseCalculator.THORNS_PERCENT_PER_LEVEL;
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerShootArrow(ProjectileLaunchEvent event) {
            if (!(event.getEntity().getShooter() instanceof Player player)) return;
            EliteItemManager.tagArrow(event.getEntity());

            // Store combat data at launch time for accurate ranged damage calculation.
            // Without this, switching weapons between firing and impact would use the
            // wrong weapon level, skill type, and skill level.
            Projectile projectile = event.getEntity();
            ItemStack weapon;
            if (projectile instanceof Trident trident) {
                weapon = trident.getItem();
            } else {
                weapon = player.getInventory().getItemInMainHand();
            }

            double weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(weapon);
            ItemTagger.setArrowWeaponLevel(projectile, weaponLevel);

            // Capture the launch-time velocity magnitude. Arrows decelerate from gravity
            // and drag in flight; reading projectile.getVelocity() at impact gives values
            // well below 3.0 even for full-draw shots, which underflows the ranged damage
            // multiplier and was the source of "bows do very little damage" reports.
            ItemTagger.setArrowLaunchVelocity(projectile, projectile.getVelocity().length());

            SkillType skillType = getWeaponSkillType(player);
            if (skillType != null) {
                ItemTagger.setArrowSkillType(projectile, skillType.name());
                long skillXP = SkillsConfig.isWorldExcludedFromSkills(player) ? 0 : PlayerData.getSkillXP(player.getUniqueId(), skillType);
                int skillLevel = SkillsConfig.isWorldExcludedFromSkills(player) ? 1 : Math.max(1, SkillXPCalculator.levelFromTotalXP(skillXP));
                ItemTagger.setArrowSkillLevel(projectile, skillLevel);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void trackNonEliteMeleeAttack(EntityDamageByEntityEvent event) {
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
            if (!(event.getDamager() instanceof Player player)) return;
            if (EntityTracker.getEliteMobEntity(event.getEntity()) != null) return;
            PlayerAttackCooldownTracker.recordHit(player);
        }

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobAttacked(EntityDamageByEntityEvent event) {
            if (event.getEntity().getType().equals(EntityType.ENDER_DRAGON) && ((EnderDragon) event.getEntity()).getPhase().equals(EnderDragon.Phase.DYING))
                return;
            LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
            if (livingEntity == null) return;
            if (!livingEntity.getType().equals(EntityType.PLAYER)) return;
            Player player = (Player) livingEntity;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            //Living entity is sometimes null when the damage is dealt to an already dead entity - might happen with mcmmo due to DOTs and stuff
            if (eliteEntity == null || !eliteEntity.isValid()) return;
            //There's at least 1 gun plugin that makes players the projectile themselves.
            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) && !(event.getDamager() instanceof Projectile))
                return;

            /*
            From this point on, the damage is confirmed to be processed by EliteMobs
             */

            // Anti-autoclicker throttle: only count genuine melee swings against elites.
            // Sweep secondaries, thorns, projectiles, and bypass damage (skill bleed/AoE ticks
            // applied via entity.damage()) don't represent player clicks, so they must not count
            // toward the throttle — otherwise one AoE swing into several mobs, or a bleed ticking
            // alongside swings, trips the lockout and zeroes the player's real damage.
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                    && !bypass
                    && com.magmaguy.elitemobs.combatsystem.antiexploit.AutoclickerThrottle.shouldBlockHit(player)) {
                event.setDamage(0);
                event.setCancelled(true);
                return;
            }

            //nullify vanilla reductions, this is needed because boss armor is just cosmetic
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
                if (event.isApplicable(modifier) && modifier != EntityDamageEvent.DamageModifier.BASE)
                    event.setDamage(modifier, 0);

            //Sometimes players are "fake" due to npc plugins
            boolean validPlayer = !player.hasMetadata("NPC") && ElitePlayerInventory.getPlayer(player) != null;

            // Check if breakdown tracking is active for this player
            DamageBreakdown breakdown = validPlayer ? DamageBreakdown.getActiveBreakdown(player) : null;

            double damage;

            if (!validPlayer) {
                // Non-valid players (NPC plugins): use raw vanilla event damage
                damage = event.getDamage();
            } else if (bypass) {
                // Custom/bypass damage: use raw event damage, no formula
                damage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
            } else if (event.getCause().equals(EntityDamageEvent.DamageCause.THORNS)) {
                if (eliteEntity.isScaledCombat()) {
                    int simulatedLevel = getPlayerWeaponSkillLevel(player);
                    if (simulatedLevel <= 0) simulatedLevel = 1;
                    double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(eliteEntity, simulatedLevel);
                    double thornsDamage = baseDamage * getEliteThornsLevel(player) * WeaponOffenseCalculator.THORNS_PERCENT_PER_LEVEL;
                    double simulatedMobHP = NaturalEliteCombatTweak.getTweakedMobHealthForLevel(
                            eliteEntity, simulatedLevel, eliteEntity.getHealthMultiplier());
                    double actualBossMaxHP = eliteEntity.getMaxHealth();
                    damage = thornsDamage * (actualBossMaxHP / simulatedMobHP);
                } else {
                    damage = calculateThornsDamage(player, eliteEntity);
                }
                if (breakdown != null) {
                    breakdown.setThornsDamage(damage);
                    breakdown.setThornsAttack(true);
                    breakdown.setEliteLevel(eliteEntity.getLevel());
                }
            } else if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                    || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
                    || event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                // Main combat formula: melee, sweep, or ranged
                if (eliteEntity.isScaledCombat())
                    damage = scaledPlayerToEliteDamage(player, eliteEntity, event);
                else
                    damage = playerToEliteDamageFormula(player, eliteEntity, event);
            } else {
                // Other damage types: use raw vanilla event damage
                damage = event.getDamage();
            }
            double damageAfterRawFormula = damage;

            // Boss-specific damage modifier
            double damageModifier = 1;
            if (!bypass) {
                if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                    if (CustomProjectileData.getCustomProjectileDataHashMap().get((Projectile) event.getDamager()) == null)
                        damageModifier = getCustomDamageModifier(eliteEntity, null);
                    else
                        damageModifier = getCustomDamageModifier(eliteEntity, CustomProjectileData.getCustomProjectileDataHashMap().get(event.getDamager()).getProjectileShooterMaterial());
                else damageModifier = getCustomDamageModifier(eliteEntity, player.getInventory().getItemInMainHand().getType());
            }

            // Config combat multiplier
            double combatMultiplier = 1;
            if (!bypass) {
                if (eliteEntity.isScaledCombat())
                    combatMultiplier = MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
                else if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
                    combatMultiplier = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
                else
                    combatMultiplier = MobCombatSettingsConfig.getDamageToEliteMultiplier();
            }

            // Apply multipliers
            damage = Round.twoDecimalPlaces(damage * damageModifier * combatMultiplier);

            // Populate breakdown multipliers
            if (breakdown != null) {
                breakdown.setDamageModifier(damageModifier);
                breakdown.setCombatMultiplier(combatMultiplier);
            }

            double damageAfterConfigMultipliers = damage;

            // Critical hit
            boolean criticalHit = false;
            if (validPlayer && !bypass) {
                criticalHit = isCriticalHit(player);
                if (criticalHit) {
                    damage *= 1.5;
                    if (breakdown != null) {
                        breakdown.setCriticalHit(true);
                        breakdown.setCritMultiplier(1.5);
                    }
                }

                // Debug logging for combat balance tuning
                DebugMessage.log(player, "[Combat] SkillLv" + getPlayerWeaponSkillLevel(player) +
                        " vs EliteLv" + eliteEntity.getLevel() +
                        " | Damage: " + String.format("%.1f", damage) +
                        " | Elite HP: " + String.format("%.1f", eliteEntity.getHealth()) +
                        (criticalHit ? " | CRIT" : ""));
            }
            double damageAfterCrit = damage;

            // Per-player diagnostic breakdown (toggle with /em debug)
            if (DebugMessage.isDebugEnabled(player)) {
                String combatPath;
                String configKey;
                if (bypass) {
                    combatPath = "CUSTOM/BYPASS";
                    configKey = "none";
                } else if (eliteEntity.isScaledCombat()) {
                    combatPath = "SCALED";
                    configKey = "scaledDamageToEliteMultiplier";
                } else if (eliteEntity instanceof CustomBossEntity cbForLog && cbForLog.isNormalizedCombat()) {
                    combatPath = "NORMALIZED";
                    configKey = "normalizedDamageToEliteMultiplier";
                } else {
                    combatPath = "DEFAULT (V2)";
                    configKey = "damageToEliteMobMultiplierV2";
                }
                boolean normalizedFlag = eliteEntity instanceof CustomBossEntity cbForFlag && cbForFlag.isNormalizedCombat();
                String mobName = eliteEntity.getLivingEntity() != null
                        ? eliteEntity.getLivingEntity().getType().name() : "?";
                String entityClass = eliteEntity.getClass().getSimpleName();
                DebugMessage.send(player, "§6═════ EM DAMAGE: YOU → ELITE ═════");
                DebugMessage.send(player, "§7Target: §f" + mobName + " §7Lv§f" + eliteEntity.getLevel()
                        + " §8(EliteEntity class: §7" + entityClass + "§8)");
                DebugMessage.send(player, "§7Classification: isNaturalEntity=§f" + eliteEntity.isNaturalEntity()
                        + " §7isScaledCombat=§f" + eliteEntity.isScaledCombat()
                        + " §7isNormalizedCombat=§f" + normalizedFlag);
                DebugMessage.send(player, "§7Per-mob damageMultiplier=§f" + String.format("%.3f", eliteEntity.getDamageMultiplier())
                        + " §7healthMultiplier=§f" + String.format("%.3f", eliteEntity.getHealthMultiplier())
                        + " §8(per-boss config; defaults 1.0 for natural elites)");
                DebugMessage.send(player, "§7Combat path: §e" + combatPath
                        + " §8→ pulls config key §f" + configKey);
                DebugMessage.send(player, "§7Cause: §f" + event.getCause()
                        + " §7validPlayer=§f" + validPlayer
                        + " §7bypass=§f" + bypass);
                DebugMessage.send(player, "§e── Outer-handler multipliers ──");
                DebugMessage.send(player, "§7Raw formula damage = §f" + String.format("%.2f", damageAfterRawFormula)
                        + " §8(output of " + (eliteEntity.isScaledCombat() ? "scaledPlayerToEliteDamage" : "playerToEliteDamageFormula") + " above)");
                DebugMessage.send(player, "§7× damageModifier = §f" + String.format("%.3f", damageModifier)
                        + " §8(weak/resist material lookup from per-boss config)");
                DebugMessage.send(player, "§7× combatMultiplier = §f" + String.format("%.3f", combatMultiplier)
                        + " §8(" + configKey + " in MobCombatSettings.yml)");
                DebugMessage.send(player, "§7= " + String.format("%.2f", damageAfterConfigMultipliers));
                if (criticalHit)
                    DebugMessage.send(player, "§e× Crit (× 1.5) = §f" + String.format("%.2f", damageAfterCrit) + " §6CRIT!");
                else
                    DebugMessage.send(player, "§7× Crit = §f1.000 §8(not a crit)");
                DebugMessage.send(player, "§7Damage entering EliteMobDamagedByPlayerEvent: §f"
                        + String.format("%.2f", damageAfterCrit));
            }

            // Finalize breakdown computation
            if (breakdown != null) {
                breakdown.compute();
            }

            EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent = new EliteMobDamagedByPlayerEvent(eliteEntity, player, event, damage, criticalHit, bypass, damageModifier);

            // For ranged attacks, propagate launch-time weapon data so applySkillBonuses()
            // uses the correct skill type and level (not the player's current mainhand).
            if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile instanceof Trident) {
                    eliteMobDamagedByPlayerEvent.setRangedSkillType(SkillType.TRIDENTS);
                    long tridentXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.TRIDENTS);
                    eliteMobDamagedByPlayerEvent.setRangedSkillLevel(Math.max(1, SkillXPCalculator.levelFromTotalXP(tridentXP)));
                } else {
                    String storedType = ItemTagger.getArrowSkillType(projectile);
                    if (storedType != null) {
                        try {
                            eliteMobDamagedByPlayerEvent.setRangedSkillType(SkillType.valueOf(storedType));
                        } catch (IllegalArgumentException ignored) {
                            // Invalid skill type name in PDC — fall back to mainhand
                        }
                    }
                    int storedLevel = ItemTagger.getArrowSkillLevel(projectile);
                    if (storedLevel >= 0) {
                        eliteMobDamagedByPlayerEvent.setRangedSkillLevel(storedLevel);
                    }
                }
            }

            new EventCaller(eliteMobDamagedByPlayerEvent);

            if (eliteMobDamagedByPlayerEvent.isCancelled()) {
                if (DebugMessage.isDebugEnabled(player)) {
                    DebugMessage.send(player, "§cEvent cancelled by a listener — no damage applied.");
                    DebugMessage.send(player, "§6═════════════════════════════════════");
                }
                event.setCancelled(true);
                return;
            }

            //In case things got modified along the way
            damage = eliteMobDamagedByPlayerEvent.getDamage();
            if (DebugMessage.isDebugEnabled(player)) {
                DebugMessage.send(player, "§7After event listeners (weapon skill bonuses etc.): §f"
                        + String.format("%.2f", damage));
                DebugMessage.send(player, "§a⇒ DAMAGE APPLIED TO MOB: §f" + String.format("%.2f", damage)
                        + " §7HP (mob has §f" + String.format("%.2f", eliteEntity.getHealth()) + "§7 / §f"
                        + String.format("%.2f", eliteEntity.getMaxHealth()) + "§7 HP)");
                // Compact one-line summary — designed to be grep'd from server logs.
                // Captures combat path + which config multiplier was active + final
                // damage so a single grep "[EM-Damage]" lets admins scan across many
                // hits and spot anomalies (e.g. NORMALIZED path firing for a
                // regular world spawn, or combatMultiplier=1.0 despite a config
                // change that should have taken effect).
                String pathTag;
                String keyTag;
                double appliedKeyValue;
                if (bypass) {
                    pathTag = "CUSTOM";
                    keyTag = "bypassDamage";
                    appliedKeyValue = 1.0;
                } else if (eliteEntity.isScaledCombat()) {
                    pathTag = "SCALED";
                    keyTag = "scaledDamageToEliteMultiplier";
                    appliedKeyValue = MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
                } else if (eliteEntity instanceof CustomBossEntity cbTag && cbTag.isNormalizedCombat()) {
                    pathTag = "NORMALIZED";
                    keyTag = "normalizedDamageToEliteMultiplier";
                    appliedKeyValue = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
                } else {
                    pathTag = "DEFAULT_V2";
                    keyTag = "damageToEliteMobMultiplierV2";
                    appliedKeyValue = MobCombatSettingsConfig.getDamageToEliteMultiplier();
                }
                String mobType = eliteEntity.getLivingEntity() != null
                        ? eliteEntity.getLivingEntity().getType().name() : "?";
                DebugMessage.damageSummary(player, String.format(
                        "P->E target=%s Lv%d path=%s %s=%.3f rawFormula=%.2f dmgMod=%.3f cfgMult=%.3f crit=%s finalApplied=%.2f mobHP=%.1f/%.1f",
                        mobType, eliteEntity.getLevel(), pathTag, keyTag, appliedKeyValue,
                        damageAfterRawFormula, damageModifier, combatMultiplier,
                        criticalHit ? "1.5" : "1.0", damage,
                        eliteEntity.getHealth(), eliteEntity.getMaxHealth()));
                DebugMessage.send(player, "§6═════════════════════════════════════");
            }

            if (validPlayer) {
                //Time to deal custom damage!
                eliteEntity.addDamager(player, damage);
            }

            //Dragons need special handling due to their custom deaths
            if (eliteEntity.getLivingEntity() != null && eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON) && eliteEntity.getLivingEntity().getHealth() - damage < 1) {
                if (eliteEntity.isDying()) return;
                damage = 0;
                event.setCancelled(true);
                ((EnderDragon) eliteEntity.getLivingEntity()).setPhase(EnderDragon.Phase.DYING);
                eliteEntity.setDying(true);
                //remove the dragon after it is done with the light show, this death doesn't show up on events
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> EliteMobDeathEvent.callAndRemove(eliteEntity), 200);
            }

            event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);
            // Also set the total directly. For ordinary hits this is a no-op (BASE already
            // equals `damage`, other modifiers were nullified above). But for damage dealt
            // programmatically via LivingEntity#damage(amount, source) — e.g. FreeMinecraftModels
            // forwarding an arrow hit onto a custom-model elite's underlying entity — the
            // BASE-modifier override alone does NOT change the applied amount (the original
            // `amount` lands and one-shots the mob). The total setter forces our value.
            event.setDamage(damage);

            eliteEntity.syncPluginHealth(((LivingEntity) event.getEntity()).getHealth());


            runAntiexploit(eliteEntity, event, eliteMobDamagedByPlayerEvent);
        }

        private void runAntiexploit(EliteEntity eliteEntity, EntityDamageByEntityEvent event, EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
            if (EliteMobsWorld.isEliteMobsWorld(event.getDamager().getWorld().getUID())) return;
            if (eliteEntity.isEnderDragon()) return;
            if (eliteMobDamagedByPlayerEvent.isCustomDamage()) return;
            if (EliteMobs.worldGuardIsEnabled) {
                Boolean regionQuery = WorldGuardFlagChecker.checkNullableFlag(eliteEntity.getLocation(), WorldGuardCompatibility.getELITEMOBS_ANTIEXPLOIT());
                if (regionQuery != null && !regionQuery) return;
            }
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                    event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK &&
                    event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE)
                return;

            if (eliteEntity.isInAntiExploitCooldown() ||
                    eliteEntity.getLivingEntity() == null) return;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerAntiExploitEvent(eliteEntity, eliteMobDamagedByPlayerEvent));
        }
    }

}
