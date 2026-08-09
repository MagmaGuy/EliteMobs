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

import java.util.concurrent.ThreadLocalRandom;

/** Owns Bukkit interception and formula application for player-to-elite damage. */
public final class EliteMobDamagedByPlayerEventFilter implements Listener {
    private static SkillType getWeaponSkillType(Player player) {
        return EliteMobDamagedByPlayerEvent.getWeaponSkillType(player);
    }

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
        double formulaDamage;
        try {
            formulaDamage = playerToEliteDamageFormula(player, eliteEntity, event);
        } finally {
            // The elite's real level is authoritative state and must survive formula failures.
            eliteEntity.setLevel(realMobLevel);
        }

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
        boolean bypass = CombatDamageContext.consumePlayerToElite().bypass();

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
                    eliteMobDamagedByPlayerEvent.isCriticalStrikeDamageApplied() ? "1.5" : "1.0", damage,
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
