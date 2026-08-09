package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.ArmorDefenseCalculator;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.combatsystem.PotionCombatModifierCalculator;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.DivineShieldSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PhalanxSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ParrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.AttributeManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobEvent extends EliteDamageEvent {

    /**
     * Hard ceiling on the <b>total</b> damage reduction every skill source combined may apply to a
     * single hit.
     * <p>
     * Value: 0.85 (85%) — a fully stacked defensive build always eats at least 15% of the incoming
     * hit. {@link SkillBonus#MAX_DEFENSIVE_REDUCTION} bounds one skill; it says nothing about their
     * combination. Defensive skills compose multiplicatively (each branch feeds the already-reduced
     * {@link #getDamage()} back through {@code damage * (1 - reduction)}), so three sources at the
     * per-skill ceiling produce {@code 0.2 * 0.2 * 0.2} = 99.2% total reduction: effective immunity
     * with every individual clamp intact. Per-source clamps are a guard rail, this is the invariant.
     */
    public static final double MAX_AGGREGATE_DEFENSIVE_REDUCTION = 0.85;

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final Player player;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final Projectile projectile;
    @Getter
    private boolean playerBlocking;

    public PlayerDamagedByEliteMobEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, Projectile projectile, double damage) {
        super(damage, event);
        this.entity = event.getEntity();
        this.eliteEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.projectile = projectile;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteEntity getEliteMobEntity() {
        return this.eliteEntity;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return this.entityDamageByEntityEvent;
    }

    /**
     * Gets the attacker entity from this event.
     * Handles both direct melee and projectile attacks.
     *
     * @return The living entity that caused the damage, or null if not applicable
     */
    public LivingEntity getAttacker() {
        if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            return eliteEntity.getLivingEntity();
        }
        return null;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Applies a damage value produced by a defensive skill, floored at zero.
     * <p>
     * Defensive skills reduce damage by multiplying it by {@code (1 - reduction)}. A reduction
     * above 1.0 from any source, present or future, flips the sign and would heal the player for
     * being hit. Individual skills clamp their own reduction to
     * {@link SkillBonus#MAX_DEFENSIVE_REDUCTION}; this is the last line of defence covering every
     * skill-driven damage assignment regardless of where the reduction came from.
     *
     * @param modifiedDamage The damage value returned by a defensive skill
     */
    private void setSkillModifiedDamage(double modifiedDamage) {
        setDamage(Math.max(0, modifiedDamage));
    }

    /**
     * Applies the aggregate defensive ceiling to a post-skill damage value.
     * <p>
     * Pure function: compares the damage that entered the skill pipeline against the damage that
     * came out and refuses to let the ratio drop below
     * {@code 1 - }{@link #MAX_AGGREGATE_DEFENSIVE_REDUCTION}. Working on the ratio rather than on a
     * running sum of reductions is what makes this robust — the branches are differently shaped
     * (some multiply, some assign, some stack, some read config), and a future skill only has to
     * route its damage through this pipeline to be covered. Order and count of contributors are
     * irrelevant to the result.
     *
     * @param incomingDamage The damage before any skill touched it
     * @param reducedDamage  The damage after every skill has been applied
     * @return The damage to actually deal, never negative and never below the aggregate floor
     */
    public static double capAggregateReduction(double incomingDamage, double reducedDamage) {
        double flooredDamage = Math.max(0, reducedDamage);
        if (incomingDamage <= 0) return flooredDamage;
        // Strictly positive whenever the hit itself was: incomingDamage * 0.15 > 0.
        return Math.max(flooredDamage, incomingDamage * (1 - MAX_AGGREGATE_DEFENSIVE_REDUCTION));
    }

    /**
     * Unified method to apply all active ARMOR skill bonuses to this damage event.
     * This is the single entry point for the skill bonus system to modify incoming damage.
     * <p>
     * Processes all active armor skills, applying damage reduction from
     * PASSIVE, CONDITIONAL, COOLDOWN, and PROC skills, then enforces
     * {@link #MAX_AGGREGATE_DEFENSIVE_REDUCTION} on their combined effect.
     *
     * @return true if damage was completely negated (e.g., by dodge or death prevention), false otherwise
     */
    public boolean applySkillBonuses() {
        double incomingDamage = getDamage();
        try {
            return applyDefensiveSkillBonuses();
        } finally {
            // Single point of final application: every reduction above has already been folded into
            // getDamage() by now, whichever branch produced it, so one ratio check bounds the lot.
            // Full negation (Evasion, Last Stand, Divine Shield) cancels the event instead of
            // reducing damage — that is an all-or-nothing mechanic, not a stacked reduction, so it
            // deliberately stays outside the ceiling.
            //
            // TODO: replace the flat aggregate cap with diminishing returns per stacked reduction (design sketch in git history).
            if (!isCancelled()) setDamage(capAggregateReduction(incomingDamage, getDamage()));
        }
    }

    private boolean applyDefensiveSkillBonuses() {
        if (player == null || player.hasMetadata("NPC")) return false;
        if (SkillsConfig.isWorldExcludedFromSkills(player)) return false;
        if (!ElitePlayerInventory.playerInventories.containsKey(player.getUniqueId())) return false;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), SkillType.ARMOR);

        // Custom armor skill handling - process skills with custom trigger methods
        for (String skillId : activeSkillIds) {
            SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
            if (skill == null || !skill.isEnabled()) continue;
            if (!skill.meetsLevelRequirement(skillLevel)) continue;

            // Evasion - chance to completely dodge
            if (skill instanceof EvasionSkill evasion) {
                if (evasion.tryEvade(player, this)) {
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                    setCancelled(true);
                    return true;
                }
                continue; // Skip generic processing for this skill
            }

            // Retaliation - chance to reflect damage back
            if (skill instanceof RetaliationSkill retaliation) {
                LivingEntity attacker = getAttacker();
                if (attacker != null) {
                    retaliation.onDamageTaken(player, attacker, getDamage());
                }
                continue;
            }

            // Fortify - stacking damage reduction (always applies, action bar handled internally)
            if (skill instanceof FortifySkill fortify) {
                double modifiedDamage = fortify.modifyIncomingDamage(player, getDamage());
                setSkillModifiedDamage(modifiedDamage);
                skill.incrementProcCount(player);
                continue;
            }

            // ReactiveShielding - check trigger + apply shield reduction
            if (skill instanceof ReactiveShieldingSkill reactiveShielding) {
                // Check if this hit should trigger the shield
                double damagePercent = getDamage() / player.getMaxHealth();
                reactiveShielding.checkTrigger(player, damagePercent);
                // Apply shield reduction if active (might have been activated by this hit or a previous one)
                double modifiedDamage = reactiveShielding.modifyIncomingDamage(player, getDamage());
                if (modifiedDamage != getDamage()) {
                    setSkillModifiedDamage(modifiedDamage);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                continue;
            }

            // AdrenalineSurge - buffs when health drops below threshold, plus a timed damage
            // reduction while the surge runs.
            //
            // That reduction used to be a RESISTANCE potion effect, which
            // PotionCombatModifierCalculator applies at step 6 of eliteToPlayerDamageFormula -
            // before this event exists. It was therefore baked into the incoming damage that
            // capAggregateReduction measures against, so the ceiling capped the rest of the stack
            // at 85% of an already-reduced number and a full defensive build reached ~88% total
            // reduction. Applying it here instead puts it inside the pipeline, on the same footing
            // as every other skill reduction, with no change to how RESISTANCE from any other
            // source behaves.
            if (skill instanceof AdrenalineSurgeSkill adrenaline) {
                // Reduction before trigger, mirroring the old potion ordering: the potion was
                // resolved before the event ran, so it never reduced the hit that started it.
                double modifiedDamage = adrenaline.modifyIncomingDamage(player, getDamage());
                if (modifiedDamage != getDamage()) setSkillModifiedDamage(modifiedDamage);
                double newHealthPercent = (player.getHealth() - getDamage()) / player.getMaxHealth();
                adrenaline.checkTrigger(player, newHealthPercent);
                continue;
            }

            // SecondWind - heal when health drops below threshold
            if (skill instanceof SecondWindSkill secondWind) {
                double newHealthPercent = (player.getHealth() - getDamage()) / player.getMaxHealth();
                secondWind.checkTrigger(player, newHealthPercent);
                continue;
            }

            // LastStand - prevent fatal damage
            if (skill instanceof LastStandSkill lastStand) {
                boolean fatal = player.getHealth() - getDamage() <= 0;
                if (fatal) {
                    if (lastStand.preventDeath(player, getDamage())) {
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                        setCancelled(true);
                        return true;
                    }
                }
                continue;
            }

            // IronStance - damage reduction when standing still (custom movement check)
            if (skill instanceof IronStanceSkill ironStance) {
                double modifiedDamage = ironStance.modifyIncomingDamage(player, getDamage(), this);
                if (modifiedDamage != getDamage()) {
                    setSkillModifiedDamage(modifiedDamage);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                continue;
            }

            // Grit - scaling damage reduction based on health (custom health-based scaling)
            if (skill instanceof GritSkill grit) {
                double modifiedDamage = grit.modifyIncomingDamage(player, getDamage(), this);
                if (modifiedDamage != getDamage()) {
                    setSkillModifiedDamage(modifiedDamage);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                continue;
            }

            // For any remaining skills (PASSIVE like BattleHardened),
            // use the generic handler
            double multiplier = processPassiveDefensiveSkill(skill, skillLevel);
            if (multiplier != 1.0) {
                setSkillModifiedDamage(getDamage() * multiplier);
            }
        }

        // Check weapon-type defensive skills (Parry - sword blocking)
        double parryDamage = ParrySkill.applyParryReduction(player, this, getDamage());
        if (parryDamage != getDamage()) {
            setSkillModifiedDamage(parryDamage);
            SkillBonus parrySkill = SkillBonusRegistry.getSkillById(ParrySkill.SKILL_ID);
            if (parrySkill != null) SkillBonus.sendSkillActionBar(player, parrySkill);
        }

        // Phalanx - frontal damage reduction when holding spear
        double phalanxDamage = PhalanxSkill.applyFrontalReduction(player, this, getDamage());
        if (phalanxDamage != getDamage()) {
            setSkillModifiedDamage(phalanxDamage);
        }

        // Check death prevention skills from weapon types
        if (player.getHealth() - getDamage() <= 0) {
            if (DeathsEmbraceSkill.preventDeath(player)) {
                setCancelled(true);
                return true;
            }
            if (DivineShieldSkill.preventDeath(player, getDamage())) {
                setCancelled(true);
                return true;
            }
        }

        return false;
    }

    /**
     * Generic fallback for armor skills without a bespoke branch in
     * {@link #applyDefensiveSkillBonuses()}.
     * <p>
     * Every armor skill except BattleHardened has an instanceof branch above that ends in
     * {@code continue}, and BattleHardened is PASSIVE, so only PASSIVE skills can reach this
     * point. Skill registration is a closed, compile-time list ({@code SkillBonusInitializer}),
     * which is what makes this reduction safe. If a new armor skill of another bonus type is
     * added, give it a bespoke branch above.
     * <p>
     * The reduction is run through {@link SkillBonus#clampDefensiveReduction(double)} before it
     * becomes a multiplier, so a bonus value that scales past 100% cannot produce a negative
     * multiplier and heal the player on hit.
     */
    private double processPassiveDefensiveSkill(SkillBonus skill, int skillLevel) {
        skill.incrementProcCount(player); // Track activation
        return 1.0 - SkillBonus.clampDefensiveReduction(skill.getBonusValue(skillLevel));
    }

    //Thing that launches the event
    public static class PlayerDamagedByEliteMobEventFilter implements Listener {
        /**
         * Calculates boss damage to player using the redesigned defensive formula.
         * <p>
         * No pre-compensation. Three multiplicative layers:
         * <ol>
         *   <li><b>Base damage</b> = playerMaxHP / {@link LevelScaling#TARGET_HITS_TO_KILL_PLAYER}</li>
         *   <li><b>Skill adjustment</b> = 2^((mobLevel - armorSkillLevel) / {@link LevelScaling#SKILL_SCALING_RATE})
         *       — exponential scaling from skill vs mob level difference</li>
         *   <li><b>Gear adjustment</b> = 2.0 * (1 - gearReduction)
         *       — damage-type-aware, from {@link ArmorDefenseCalculator}</li>
         * </ol>
         * <p>
         * Expected outcomes:
         * <ul>
         *   <li>Naked vs same level: ~2.5 hits to kill (gear adjustment = 2.0)</li>
         *   <li>Matching gear + skill vs same level: ~5 hits to kill (gear adjustment = 1.0)</li>
         *   <li>Peak gear vs same level: ~10 hits to kill (gear adjustment = 0.5)</li>
         *   <li>+7.5 levels above skill: damage doubles</li>
         * </ul>
         * Final damage is capped at maxHP - 1 (1-shot protection).
         */
        private static double eliteToPlayerDamageFormula(Player player, EliteEntity eliteEntity,
                                                         EntityDamageByEntityEvent event,
                                                         double specialMultiplier) {
            if (ElitePlayerInventory.getPlayer(player) == null) return 0;

            // 1. Player stats
            boolean skillsExcluded = SkillsConfig.isWorldExcludedFromSkills(player);
            long armorSkillXP = skillsExcluded ? 0 : PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
            int armorSkillLevel = skillsExcluded ? 1 : Math.max(1, SkillXPCalculator.levelFromTotalXP(armorSkillXP));
            double playerMaxHealth = ArmorSkillHealthBonus.getConfiguredMaxHealthForPlayer(player, armorSkillLevel);
            int mobLevel = eliteEntity.getLevel();

            // Scaled combat: simulate the boss at the player's armor skill level
            if (eliteEntity.isScaledCombat()) {
                mobLevel = armorSkillLevel;
            }

            // 2. Base damage (no pre-compensation)
            double baseDamage = playerMaxHealth / LevelScaling.TARGET_HITS_TO_KILL_PLAYER;

            // 3. Skill adjustment (exponential, replaces old level modifier + skill reduction)
            double skillAdjustment = Math.pow(2.0, (mobLevel - armorSkillLevel) / LevelScaling.SKILL_SCALING_RATE);

            // 4. Gear adjustment (damage-type-aware)
            ArmorDefenseCalculator.DamageType damageType = ArmorDefenseCalculator.fromEvent(event);
            double gearScore = ArmorDefenseCalculator.getGearScore(player, damageType);
            double gearAdjustment = ArmorDefenseCalculator.getGearAdjustment(gearScore, mobLevel);

            double scaledDamage = baseDamage * skillAdjustment * gearAdjustment;

            // 5. Distance attenuation for explosions (creeper, ghast)
            if (eliteEntity.getLivingEntity() != null && player.isValid() &&
                    player.getLocation().getWorld().equals(eliteEntity.getLivingEntity().getWorld())) {
                if (eliteEntity.getLivingEntity().getType().equals(EntityType.CREEPER)) {
                    Creeper creeper = (Creeper) eliteEntity.getLivingEntity();
                    double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                    double distanceAttenuation = Math.max(0, 1 - distance / creeper.getExplosionRadius());
                    scaledDamage *= distanceAttenuation;
                } else if (eliteEntity.getLivingEntity().getType().equals(EntityType.GHAST) &&
                        event.getDamager().getType().equals(EntityType.FIREBALL)) {
                    double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                    double distanceAttenuation = Math.max(0, 1 - distance / ((Fireball) event.getDamager()).getYield());
                    scaledDamage *= distanceAttenuation;
                }
            }

            // 6. Resistance potion effect (percentage-based)
            double potionMultiplier = PotionCombatModifierCalculator.getIncomingDamageMultiplier(player);

            // 7. Boss damage multiplier (for custom bosses with increased damage)
            double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();

            // Config multipliers
            double configMultiplier;
            if (eliteEntity.isScaledCombat())
                configMultiplier = MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier();
            else if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
                configMultiplier = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
            else
                configMultiplier = MobCombatSettingsConfig.getDamageToPlayerMultiplier();

            // Capture specialMultiplier before it is auto-reset (used in chat breakdown below)
            double usedSpecialMultiplier = specialMultiplier;

            // Calculate final damage
            double preCapDamage = Math.max(scaledDamage, 1)
                    * potionMultiplier
                    * customBossDamageMultiplier
                    * specialMultiplier
                    * configMultiplier;

            // 8. 1-shot protection
            double actualMaxHealth = AttributeManager.getAttributeValue(player, "generic_max_health");
            double finalDamage = Math.min(preCapDamage, actualMaxHealth - 1);

            // Per-player diagnostic breakdown (toggle with /em debug)
            if (DebugMessage.isDebugEnabled(player)) {
                String combatPath;
                String configKey;
                if (eliteEntity.isScaledCombat()) {
                    combatPath = "SCALED";
                    configKey = "scaledDamageToPlayerMultiplier";
                } else if (eliteEntity instanceof CustomBossEntity cbForLog && cbForLog.isNormalizedCombat()) {
                    combatPath = "NORMALIZED";
                    configKey = "normalizedDamageToPlayerMultiplier";
                } else {
                    combatPath = "DEFAULT (V2)";
                    configKey = "damageToPlayerMultiplierV2";
                }
                boolean normalizedFlag = eliteEntity instanceof CustomBossEntity cbForFlag && cbForFlag.isNormalizedCombat();
                String mobName = eliteEntity.getLivingEntity() != null
                        ? eliteEntity.getLivingEntity().getType().name() : "?";
                String entityClass = eliteEntity.getClass().getSimpleName();
                DebugMessage.send(player, "§6═════ EM DAMAGE: ELITE → YOU ═════");
                DebugMessage.send(player, "§7Mob: §f" + mobName + " §7Lv§f" + eliteEntity.getLevel()
                        + " §8(EliteEntity class: §7" + entityClass + "§8)");
                DebugMessage.send(player, "§7Classification: isNaturalEntity=§f" + eliteEntity.isNaturalEntity()
                        + " §7isScaledCombat=§f" + eliteEntity.isScaledCombat()
                        + " §7isNormalizedCombat=§f" + normalizedFlag);
                DebugMessage.send(player, "§7Per-mob damageMultiplier=§f" + String.format("%.3f", eliteEntity.getDamageMultiplier())
                        + " §7healthMultiplier=§f" + String.format("%.3f", eliteEntity.getHealthMultiplier())
                        + " §8(from per-boss config field; defaults 1.0 for natural elites)");
                DebugMessage.send(player, "§7Combat path: §e" + combatPath
                        + " §8→ pulls config key §f" + configKey);
                DebugMessage.send(player, "§e── Formula (eliteToPlayerDamageFormula) ──");
                DebugMessage.send(player, "§7Player armor skill level: §f" + armorSkillLevel
                        + " §7Effective mob level used in formula: §f" + mobLevel
                        + " §8(armor level if scaled, otherwise real mob level)");
                DebugMessage.send(player, "§7Base = playerMaxHP / TARGET_HITS_TO_KILL_PLAYER ("
                        + LevelScaling.TARGET_HITS_TO_KILL_PLAYER + ")");
                DebugMessage.send(player, "§7   = " + String.format("%.2f", playerMaxHealth) + " / "
                        + LevelScaling.TARGET_HITS_TO_KILL_PLAYER + " = §f" + String.format("%.2f", baseDamage)
                        + " §8(damage needed per hit to kill in TARGET_HITS hits)");
                DebugMessage.send(player, "§7× Skill adjustment = 2^((mobLv − armorLv)/"
                        + String.format("%.1f", LevelScaling.SKILL_SCALING_RATE) + ")");
                DebugMessage.send(player, "§7   = 2^((" + mobLevel + " − " + armorSkillLevel + ")/"
                        + String.format("%.1f", LevelScaling.SKILL_SCALING_RATE)
                        + ") = §f" + String.format("%.3f", skillAdjustment)
                        + " §8(>1 if mob over-level, <1 if you out-skill)");
                DebugMessage.send(player, "§7× Gear adjustment = §f" + String.format("%.3f", gearAdjustment)
                        + " §8(2.0×(1−gearReduction); 0.5 fully matched, 2.0 naked; damageType=" + damageType + ", gearScore=" + String.format("%.2f", gearScore) + ")");
                DebugMessage.send(player, "§7= scaledDamage (after explosion attenuation if any) = §f"
                        + String.format("%.2f", scaledDamage));
                DebugMessage.send(player, "§e── Outer multipliers ──");
                DebugMessage.send(player, "§7× max(scaledDamage, 1) = §f"
                        + String.format("%.2f", Math.max(scaledDamage, 1))
                        + " §8(floor so trivial hits still register 1)");
                DebugMessage.send(player, "§7× Potion multiplier (incoming) = §f"
                        + String.format("%.3f", potionMultiplier)
                        + " §8(resistance/weakness on you)");
                DebugMessage.send(player, "§7× Per-mob damageMultiplier = §f"
                        + String.format("%.3f", customBossDamageMultiplier)
                        + " §8(eliteEntity.getDamageMultiplier(); per-boss YAML 'damageMultiplier')");
                DebugMessage.send(player, "§7× Special multiplier = §f"
                        + String.format("%.3f", usedSpecialMultiplier)
                        + " §8(transient ability modifier; auto-resets to 1 after use)");
                DebugMessage.send(player, "§7× Config multiplier = §f"
                        + String.format("%.3f", configMultiplier)
                        + " §8(" + configKey + " in MobCombatSettings.yml)");
                DebugMessage.send(player, "§e── Result ──");
                DebugMessage.send(player, "§7Pre-cap product = §f" + String.format("%.2f", preCapDamage));
                DebugMessage.send(player, "§71-shot cap (actualMaxHP − 1 = "
                        + String.format("%.2f", actualMaxHealth - 1) + ") → final = §f"
                        + String.format("%.2f", finalDamage));
                DebugMessage.send(player, "§a⇒ Formula returns: §f" + String.format("%.2f", finalDamage)
                        + " §8(may still be adjusted by blocking/bypass/skill bonuses after this)");
            }

            return finalDamage;
        }

        //Remove potion effects of creepers when they blow up because Minecraft passes those effects to players, and they are infinite
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void explosionEvent(EntityExplodeEvent event) {
            if (event.getEntity().getType().equals(EntityType.CREEPER) && EntityTracker.isEliteMob(event.getEntity())) {
                //by default minecraft spreads potion effects
                Set<PotionEffect> potionEffects = new HashSet<>(((Creeper) event.getEntity()).getActivePotionEffects());
                potionEffects.forEach(potionEffectType -> ((Creeper) event.getEntity()).removePotionEffect(potionEffectType.getType()));
            }
        }

        @EventHandler
        public void onEliteDamagePlayer(EntityDamageByEntityEvent event) {
            CombatDamageContext.DamageOverride damageOverride = CombatDamageContext.consumeEliteToPlayer();
            boolean bypass = damageOverride.bypass();
            double specialMultiplier = damageOverride.specialMultiplier();

            if (event.isCancelled()) {
                if (!(event.getDamager() instanceof Explosive))
                    return;
            }
            if (!(event.getEntity() instanceof Player player)) return;

            //citizens
            if (player.hasMetadata("NPC") || ElitePlayerInventory.getPlayer(player) == null) return;

            Projectile projectile = null;

            EliteEntity eliteEntity = null;
            if (event.getDamager() instanceof LivingEntity)
                eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
                eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getDamager()).getShooter());
                projectile = (Projectile) event.getDamager();
            } else if (event.getDamager().getType().equals(EntityType.EVOKER_FANGS))
                if (((EvokerFangs) event.getDamager()).getOwner() != null)
                    eliteEntity = EntityTracker.getEliteMobEntity(((EvokerFangs) event.getDamager()).getOwner());

            if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

            //By this point, it is guaranteed that this kind of damage should have custom EliteMobs behavior

            // Dodge chance removed with guild rank system

            boolean blocking = false;

            //Blocking reduces melee damage and nullifies most ranged damage at the cost of shield durability
            if (player.isBlocking() || (com.magmaguy.elitemobs.testing.CombatSimulator.isTestingActive() && com.magmaguy.elitemobs.testing.CombatSimulator.isBlockingOverride())) {
                blocking = true;
                damageBlockingShield(player);

                if (event.getDamager() instanceof Projectile) {
                    event.getDamager().remove();
                    return;
                }

                // Trigger Riposte skill on successful block (melee only)
                if (!SkillsConfig.isWorldExcludedFromSkills(player)) RiposteSkill.onPlayerBlock(player);
            }

            //Calculate the damage for the event
            double newDamage = eliteToPlayerDamageFormula(player, eliteEntity, event, specialMultiplier);
            double damageAfterFormula = newDamage;
            // Test damage override: bypass defense formula during automated testing
            boolean testOverrideHit = CombatSimulator.isTestingActive() && CombatSimulator.getTestDamageOverride() >= 0;
            if (testOverrideHit) {
                newDamage = CombatSimulator.getTestDamageOverride();
            }
            double damageAfterTestOverride = newDamage;
            //Blocking reduces damage by 80%
            if (blocking)
                newDamage = newDamage - newDamage * MobCombatSettingsConfig.getBlockingDamageReduction();
            double damageAfterBlocking = newDamage;
            //nullify vanilla reductions
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
                if (event.isApplicable(modifier) && modifier != EntityDamageEvent.DamageModifier.ABSORPTION)
                    event.setDamage(modifier, 0);

            //Check if we should be doing raw damage, which some powers have

            boolean bypassTaken = bypass;
            double rawBypassDamage = 0;
            if (bypass) {
                //Use raw damage in case of bypass
                rawBypassDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
                newDamage = rawBypassDamage;
            }
            double damageEnteringEvent = newDamage;

            //Run the event, see if it will get cancelled or suffer further damage modifications
            PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent = new PlayerDamagedByEliteMobEvent(eliteEntity, player, event, projectile, newDamage);
            playerDamagedByEliteMobEvent.playerBlocking = blocking;
            new EventCaller(playerDamagedByEliteMobEvent);

            //In case damage got modified along the way
            newDamage = playerDamagedByEliteMobEvent.getDamage();
            if (DebugMessage.isDebugEnabled(player)) {
                DebugMessage.send(player, "§6── Post-formula adjustments ──");
                DebugMessage.send(player, "§7After formula: §f" + String.format("%.2f", damageAfterFormula));
                if (testOverrideHit)
                    DebugMessage.send(player, "§7Test override (CombatSimulator) forced damage to §f"
                            + String.format("%.2f", damageAfterTestOverride));
                if (blocking)
                    DebugMessage.send(player, "§7Blocking (× " + String.format("%.2f", 1 - MobCombatSettingsConfig.getBlockingDamageReduction())
                            + ", blockingDamageReduction=" + String.format("%.2f", MobCombatSettingsConfig.getBlockingDamageReduction())
                            + "): §f" + String.format("%.2f", damageAfterBlocking));
                else
                    DebugMessage.send(player, "§7Blocking: §cnot blocking §8(no reduction)");
                if (bypassTaken)
                    DebugMessage.send(player, "§cbypass=true §7→ formula damage discarded; raw event damage = §f"
                            + String.format("%.2f", rawBypassDamage)
                            + " §8(power flagged this hit to skip the formula)");
                else
                    DebugMessage.send(player, "§7bypass=false §8(formula damage kept)");
                DebugMessage.send(player, "§7Entering PlayerDamagedByEliteMobEvent: §f" + String.format("%.2f", damageEnteringEvent));
                DebugMessage.send(player, "§7After event listeners (skill bonuses, etc.): §f" + String.format("%.2f", newDamage)
                        + " §8(Evasion/Fortify/Reactive-Shielding/Parry/Phalanx etc. fire here)");
                DebugMessage.send(player, "§7Event cancelled? §f" + playerDamagedByEliteMobEvent.isCancelled());
                if (!playerDamagedByEliteMobEvent.isCancelled())
                    DebugMessage.send(player, "§a⇒ DAMAGE APPLIED TO YOU: §f" + String.format("%.2f", newDamage) + " §7HP");
                // Compact one-line summary, see EliteMobDamagedByPlayerEvent for rationale.
                String pathTag;
                String keyTag;
                double appliedKeyValue;
                if (eliteEntity.isScaledCombat()) {
                    pathTag = "SCALED";
                    keyTag = "scaledDamageToPlayerMultiplier";
                    appliedKeyValue = MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier();
                } else if (eliteEntity instanceof CustomBossEntity cbTag && cbTag.isNormalizedCombat()) {
                    pathTag = "NORMALIZED";
                    keyTag = "normalizedDamageToPlayerMultiplier";
                    appliedKeyValue = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
                } else {
                    pathTag = "DEFAULT_V2";
                    keyTag = "damageToPlayerMultiplierV2";
                    appliedKeyValue = MobCombatSettingsConfig.getDamageToPlayerMultiplier();
                }
                String mobType = eliteEntity.getLivingEntity() != null
                        ? eliteEntity.getLivingEntity().getType().name() : "?";
                DebugMessage.damageSummary(player, String.format(
                        "E->P source=%s Lv%d path=%s %s=%.3f formula=%.2f blocking=%s bypass=%s entered=%.2f finalApplied=%.2f cancelled=%s",
                        mobType, eliteEntity.getLevel(), pathTag, keyTag, appliedKeyValue,
                        damageAfterFormula, blocking, bypassTaken,
                        damageEnteringEvent, newDamage,
                        playerDamagedByEliteMobEvent.isCancelled()));
                DebugMessage.send(player, "§6═════════════════════════════════════");
            }

            if (playerDamagedByEliteMobEvent.isCancelled()) {
                return;
            }

            //Set the final damage value
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

            //Deal with the player getting killed todo: this is a bit busted, fix
            if (player.getHealth() - event.getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, eliteEntity));

        }

        private static void damageBlockingShield(Player player) {
            boolean mainHand = player.getInventory().getItemInMainHand().getType() == Material.SHIELD;
            boolean offHand = player.getInventory().getItemInOffHand().getType() == Material.SHIELD;
            if (!mainHand && !offHand) return;

            org.bukkit.inventory.ItemStack shield = mainHand
                    ? player.getInventory().getItemInMainHand()
                    : player.getInventory().getItemInOffHand();
            ItemMeta itemMeta = shield.getItemMeta();
            if (!(itemMeta instanceof Damageable damageable)) return;

            int unbreakingLevel = itemMeta.getEnchantLevel(Enchantment.UNBREAKING);
            if (unbreakingLevel > 0 && ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) != 0) return;

            int newDamage = damageable.getDamage() + 5;
            if (newDamage >= shield.getType().getMaxDurability()) {
                if (mainHand) player.getInventory().setItemInMainHand(null);
                else player.getInventory().setItemInOffHand(null);
                return;
            }

            damageable.setDamage(newDamage);
            shield.setItemMeta(itemMeta);
        }

    }

}
