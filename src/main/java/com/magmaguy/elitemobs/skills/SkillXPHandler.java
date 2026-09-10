package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.antiexploit.FarmingProtection;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteSkillXpGainEvent;
import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.combatsystem.displays.BossHealthDisplay;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.progression.AwardResult;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.MessageThrottler;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Handles skill XP awards when elite mobs are killed.
 * <p>
 * Players earn weapon XP based on the weapon they used to deal damage,
 * and armor XP at 1/3 the rate on every kill (so it always trails behind weapons).
 */
public class SkillXPHandler implements Listener {

    /**
     * Awards skill XP to all players who contributed to killing an elite mob.
     * <p>
     * XP is distributed proportionally based on damage contribution.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;

        EliteEntity eliteEntity = event.getEliteEntity();

        // Skip if anti-exploit triggered or no damagers
        if (eliteEntity.isTriggeredAntiExploit()) return;
        if (eliteEntity.getDamagers().isEmpty()) return;

        // Mounts and reinforcements never award skill XP. They are not InstancedBossEntities,
        // so dungeon lockout never applies to them, and GLOBAL reinforcements respawn on a timer —
        // either way they would otherwise be an uncapped XP farm that bypasses boss cooldowns.
        if (eliteEntity.isReinforcementOrMount()) return;

        // Custom bosses can disable skill XP
        if (eliteEntity instanceof CustomBossEntity customBoss) {
            if (!customBoss.getCustomBossesConfigFields().isDropsSkillXP()) return;
        }

        // Capture the death location for XP popups (before entity becomes invalid)
        Location deathLocation = eliteEntity.getLocation();

        // Calculate total damage for proportional XP distribution
        double totalDamage = eliteEntity.getDamagers().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalDamage <= 0) return;

        // Dungeon-boss lockout: players already on cooldown for this boss
        // should not earn skill XP (DungeonBossLockoutHandler runs at LOW
        // and populates lockoutPlayers before this NORMAL-priority handler).
        Set<Player> lockedOutPlayers = (eliteEntity instanceof InstancedBossEntity instancedBoss)
                ? instancedBoss.getLockoutPlayers()
                : Set.of();

        // Class participation includes meaningful healing, mitigation and threat in addition to
        // damage. Foundation skills keep their established damage-proportional distribution.
        Set<Player> meaningfulParticipants = ExperimentalCombatModule.isInitialized()
                ? ExperimentalCombatModule.get().meaningfulParticipants(eliteEntity)
                : new LinkedHashSet<>(eliteEntity.getDamagers().keySet());

        // Class progression belongs to the encounter, not to a participant's combat level or
        // damage share. Resolve it once so every eligible damage/healing/mitigation/threat
        // contributor receives the exact same raw class-XP award. Classes deliberately level at
        // exactly half the weapon-skill rate.
        double bossMultiplier = SkillXPCalculator.calculateBossXPMultiplier(
                eliteEntity.getHealthMultiplier(), eliteEntity.getDamageMultiplier());
        long rawClassReward = Math.max(0L, (long) (
                SkillXPCalculator.calculateMobXP(Math.max(1, eliteEntity.getLevel())) * bossMultiplier / 2D));

        // Award XP to each meaningful participant.
        for (Player player : meaningfulParticipants) {
            double damageDealt = eliteEntity.getDamagers().entrySet().stream()
                    .filter(entry -> entry.getKey().getUniqueId().equals(player.getUniqueId()))
                    .mapToDouble(Map.Entry::getValue)
                    .sum();

            // Skip NPCs and players not in memory
            if (player.hasMetadata("NPC")) continue;
            if (!PlayerData.isInMemory(player.getUniqueId())) continue;
            if (lockedOutPlayers.contains(player)) continue;
            if (SkillsConfig.isWorldExcludedFromSkills(player)) continue;
            if (deathLocation != null && deathLocation.getWorld() != null &&
                    SkillsConfig.isWorldExcludedFromSkills(deathLocation.getWorld().getName())) continue;

            int rewardLevel = ScaledCombatRewardResolver.getRewardLevel(eliteEntity, player);

            // Check farming protection for natural elites
            if (!(eliteEntity instanceof CustomBossEntity)) {
                if (!FarmingProtection.recordNaturalEliteKill(player, eliteEntity)) {
                    continue; // Player is capped, no XP
                }
            }

            AwardResult classAward = ExperimentalCombatModule.isInitialized()
                    && classXpInRange(player, rewardLevel, eliteEntity.isScaledCombat())
                    ? ExperimentalCombatModule.get().awardClassXp(player, rawClassReward)
                    : null;
            long classXpEarned = classAward == null ? 0L : classAward.appliedXp();

            // Get effective mob level (capped at +5 above combat level)
            int effectiveMobLevel = FarmingProtection.getEffectiveMobLevelForXP(player, rewardLevel);

            // Combat-level reward cap has no lower boundary. Individual skills
            // decide whether the mob is too low for that specific skill below.
            double xpMultiplier = FarmingProtection.getXPMultiplier(player, rewardLevel);
            if (xpMultiplier <= 0) {
                if (classXpEarned > 0 && deathLocation != null)
                    BossHealthDisplay.createXPPopup(deathLocation, player, classXpEarned);
                continue;
            }

            // Notify if XP is capped due to high mob level
            if (effectiveMobLevel < rewardLevel) {
                notifyCombatLevelCap(player, rewardLevel);
            }

            // Calculate base XP using effective mob level
            long baseXP = SkillXPCalculator.calculateMobXP(effectiveMobLevel);

            // Apply boss multiplier based on HP and damage multipliers
            baseXP = (long) (baseXP * bossMultiplier);

            // Calculate proportional XP based on damage contribution
            double damagePercent = damageDealt / totalDamage;
            long earnedXP = (long) (baseXP * damagePercent);

            if (earnedXP <= 0) {
                if (classXpEarned > 0 && deathLocation != null)
                    BossHealthDisplay.createXPPopup(deathLocation, player, classXpEarned);
                continue;
            }

            // Award weapon XP based on main hand weapon
            long weaponXP = awardWeaponXP(
                    player, eliteEntity, earnedXP, damageDealt, rewardLevel, eliteEntity.isScaledCombat());

            // Award armor XP (always, at 1/3 rate)
            long armorXP = awardArmorXP(player, eliteEntity, earnedXP, rewardLevel, eliteEntity.isScaledCombat());

            // Show XP popup with total XP earned (weapon + armor)
            long totalXPEarned = saturatedSum(saturatedSum(weaponXP, armorXP), classXpEarned);
            if (totalXPEarned > 0 && deathLocation != null) {
                BossHealthDisplay.createXPPopup(deathLocation, player, totalXPEarned);
            }
        }
    }

    /**
     * Awards weapon XP to a player based on their equipped weapon.
     *
     * @return The amount of XP awarded, or 0 if no weapon skill applies
     */
    private long awardWeaponXP(
            Player player,
            EliteEntity eliteEntity,
            long playerEarnedXP,
            double playerDamage,
            int rewardLevel,
            boolean scaledCombat) {
        if (!PlayerData.isDataLoaded(player.getUniqueId())) return 0;
        Map<SkillType, Double> contributions =
                eliteEntity.getSkillDamageContributions(player.getUniqueId());
        // Never infer a weapon at death. Class abilities are intentionally source-less, and a
        // player may have switched items since any delayed projectile launched. Only damage that
        // carried a hit-time or launch-time skill identity is eligible for weapon progression.
        if (contributions.isEmpty()) return 0;

        Map<SkillType, Long> shares = WeaponXpAttribution.distribute(
                playerEarnedXP, playerDamage, contributions);
        long totalAwarded = 0L;
        for (Map.Entry<SkillType, Long> entry : shares.entrySet()) {
            totalAwarded = saturatedSum(totalAwarded, awardSkillXP(
                    player, eliteEntity, entry.getKey(), entry.getValue(), rewardLevel, scaledCombat));
        }
        return totalAwarded;
    }

    private static boolean classXpInRange(Player player, int rewardLevel, boolean scaledCombat) {
        if (!FarmingProtection.isLevelRewardProtectionEnabled() || scaledCombat) return true;
        return ExperimentalCombatModule.get().profile(player.getUniqueId())
                .flatMap(snapshot -> snapshot.optionalActiveLineage())
                .map(active -> FarmingProtection.isSkillXPInRange(
                        active.activeEffectiveLevel(), rewardLevel, false))
                .orElse(true);
    }

    private long awardSkillXP(
            Player player,
            EliteEntity eliteEntity,
            SkillType skillType,
            long baseXP,
            int rewardLevel,
            boolean scaledCombat) {

        if (!PlayerData.isDataLoaded(player.getUniqueId())) return 0;

        // Get current XP before adding
        long oldXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        int previousLevel = SkillXPCalculator.levelFromTotalXP(oldXP);

        if (FarmingProtection.isLevelRewardProtectionEnabled() &&
                !FarmingProtection.isSkillXPInRange(previousLevel, rewardLevel, scaledCombat)) {
            notifySkillXPTooLow(player, skillType, rewardLevel, previousLevel);
            return 0;
        }

        // Keep eligibility in the native award path. Permission perks and listeners cannot
        // turn an excluded kill or out-of-range skill into a reward.
        long xpToAdd = SkillXPCalculator.applySkillMultiplier(skillType, baseXP);
        xpToAdd = SkillXpPerks.apply(player, skillType, xpToAdd);
        EliteSkillXpGainEvent event = new EliteSkillXpGainEvent(player, skillType, eliteEntity, xpToAdd);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || event.getXp() == 0 || !PlayerData.isDataLoaded(player.getUniqueId())) return 0;

        // A listener may have made an administrative XP write. Use the actual pre-commit
        // total, cap the increment to its remaining capacity, and report only the real delta.
        oldXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        if (oldXP < 0) return 0;
        previousLevel = SkillXPCalculator.levelFromTotalXP(oldXP);
        xpToAdd = Math.min(event.getXp(), Long.MAX_VALUE - oldXP);
        if (xpToAdd == 0) return 0;
        long newXP = PlayerData.addSkillXP(player.getUniqueId(), skillType, xpToAdd);
        long appliedXP = newXP - oldXP;
        if (appliedXP <= 0) return 0;

        // Show XP bar animation
        SkillXPBar.showXPGain(player, skillType, oldXP, newXP, appliedXP);

        // Check for level up
        int newLevel = SkillXPCalculator.levelFromTotalXP(newXP);
        if (newLevel > previousLevel) {
            notifyLevelUp(player, skillType, newLevel);
        }

        return appliedXP;
    }

    /**
     * Awards armor XP to a player at 1/3 the rate of weapon XP.
     * Armor XP is always awarded on kills regardless of equipped gear,
     * but at a reduced rate so it trails behind weapon skills.
     *
     * @return The amount of XP awarded
     */
    private long awardArmorXP(Player player, EliteEntity eliteEntity, long baseXP, int rewardLevel, boolean scaledCombat) {
        if (!PlayerData.isDataLoaded(player.getUniqueId())) return 0;
        return awardSkillXP(player, eliteEntity, SkillType.ARMOR, baseXP, rewardLevel, scaledCombat);
    }

    private static long saturatedSum(long first, long second) {
        return first > Long.MAX_VALUE - second ? Long.MAX_VALUE : first + second;
    }

    /**
     * Notifies a player that they leveled up a skill with full effects.
     * <p>
     * Effects include:
     * - Title display showing the level up
     * - Level-up sound effect
     * - Particle burst around the player
     * - Server-wide announcement
     */
    private void notifyLevelUp(Player player, SkillType skillType, int newLevel) {
        // Title display for every level up
        player.sendTitle(
                DungeonsConfig.getSkillLevelUpTitle(),
                DungeonsConfig.getSkillLevelUpSubtitle()
                        .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
                        .replace("$level", String.valueOf(newLevel)),
                10, 70, 20
        );

        // Play level-up sound
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        // Spawn celebratory particles
        player.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0),
                50,  // count
                0.5, 1.0, 0.5,  // offset x, y, z
                0.1  // speed
        );

        // Server-wide announcement (only on milestone levels — multiples of 5)
        if (DungeonsConfig.isSkillLevelUpBroadcastEnabled() && newLevel % 5 == 0) {
            String announcement = DungeonsConfig.getSkillLevelUpBroadcast()
                    .replace("$player", player.getName())
                    .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
                    .replace("$level", String.valueOf(newLevel));
            Bukkit.broadcastMessage(announcement);
        }

        // Update combat level display
        CombatLevelDisplay.updateDisplay(player);

        // Update armor health bonus if armor skill leveled up
        if (skillType == SkillType.ARMOR) {
            ArmorSkillHealthBonus.updateHealthBonus(player);
        }

        DebugMessage.log(player, "Player " + player.getName() + " reached " + SkillBonusMenuConfig.getSkillTypeDisplayName(skillType) + " level " + newLevel);
    }

    /**
     * Notifies a player that their combat-level reward is capped because the mob
     * reward level is more than five levels above their combat level.
     */
    private void notifyCombatLevelCap(Player player, int mobLevel) {
        int combatLevel = CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
        int cappedLevel = FarmingProtection.getEffectiveRewardLevel(combatLevel, mobLevel);
        String message = DungeonsConfig.getSkillXpCappedMessage()
                .replace("$mobLevel", String.valueOf(mobLevel))
                .replace("$skill", "combat")
                .replace("$playerLevel", String.valueOf(combatLevel))
                .replace("$xp", String.valueOf(cappedLevel));
        player.sendMessage(ChatColorConverter.convert(message));
    }

    private void notifySkillXPTooLow(Player player, SkillType skillType, int mobLevel, int skillLevel) {
        String message = DungeonsConfig.getSkillXpNoGainMessage()
                .replace("$mobLevel", String.valueOf(mobLevel))
                .replace("$skill", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
                .replace("$playerLevel", String.valueOf(skillLevel));
        MessageThrottler.pushNoXp(player, ChatColorConverter.convert(message));
    }
}
