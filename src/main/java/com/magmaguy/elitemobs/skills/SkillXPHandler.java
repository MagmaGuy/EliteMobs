package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.antiexploit.FarmingProtection;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

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
        EliteEntity eliteEntity = event.getEliteEntity();

        // Skip if anti-exploit triggered or no damagers
        if (eliteEntity.isTriggeredAntiExploit()) return;
        if (eliteEntity.getDamagers().isEmpty()) return;

        // Custom bosses can disable skill XP - also no XP if they don't drop EliteMobs loot
        if (eliteEntity instanceof CustomBossEntity customBoss) {
            if (!customBoss.getCustomBossesConfigFields().isDropsSkillXP()) return;
            if (!customBoss.getCustomBossesConfigFields().isDropsEliteMobsLoot()) return;
        }

        int mobLevel = eliteEntity.getLevel();

        // Calculate total damage for proportional XP distribution
        double totalDamage = eliteEntity.getDamagers().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalDamage <= 0) return;

        // Award XP to each player who contributed
        for (Map.Entry<Player, Double> entry : eliteEntity.getDamagers().entrySet()) {
            Player player = entry.getKey();
            double damageDealt = entry.getValue();

            // Skip NPCs and players not in memory
            if (player.hasMetadata("NPC")) continue;
            if (!PlayerData.isInMemory(player.getUniqueId())) continue;

            // Check farming protection for natural elites
            if (!(eliteEntity instanceof CustomBossEntity)) {
                if (!FarmingProtection.recordNaturalEliteKill(player, eliteEntity)) {
                    continue; // Player is capped, no XP
                }
            }

            // Get effective mob level (capped at +5 above combat level)
            int effectiveMobLevel = FarmingProtection.getEffectiveMobLevelForXP(player, mobLevel);

            // Check if mob is too low level for XP (5+ levels below)
            double xpMultiplier = FarmingProtection.getXPMultiplier(player, mobLevel);
            if (xpMultiplier <= 0) {
                // Notify player that mob is too low level
                notifyLevelDifferenceLimit(player, mobLevel, true);
                continue; // No XP for low-level mobs
            }

            // Notify if XP is capped due to high mob level
            if (effectiveMobLevel < mobLevel) {
                notifyLevelDifferenceLimit(player, mobLevel, false);
            }

            // Calculate base XP using effective mob level
            long baseXP = SkillXPCalculator.calculateMobXP(effectiveMobLevel);

            // Calculate proportional XP based on damage contribution
            double damagePercent = damageDealt / totalDamage;
            long earnedXP = (long) (baseXP * damagePercent);

            if (earnedXP <= 0) continue;

            // Award weapon XP based on main hand weapon
            awardWeaponXP(player, earnedXP);

            // Award armor XP (always, at 1/3 rate)
            awardArmorXP(player, earnedXP);
        }
    }

    /**
     * Awards weapon XP to a player based on their equipped weapon.
     */
    private void awardWeaponXP(Player player, long baseXP) {
        Material weaponMaterial = player.getInventory().getItemInMainHand().getType();
        SkillType skillType = SkillType.fromMaterial(weaponMaterial);

        // No XP if not holding a recognized weapon
        if (skillType == null) return;

        // Get current XP before adding
        long oldXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
        int previousLevel = SkillXPCalculator.levelFromTotalXP(oldXP);

        // Add XP with the skill's multiplier (weapons have 1.0x)
        long xpToAdd = SkillXPCalculator.applySkillMultiplier(skillType, baseXP);
        long newXP = PlayerData.addSkillXP(player.getUniqueId(), skillType, xpToAdd);

        // Show XP bar animation
        SkillXPBar.showXPGain(player, skillType, oldXP, newXP, xpToAdd);

        // Check for level up
        int newLevel = SkillXPCalculator.levelFromTotalXP(newXP);
        if (newLevel > previousLevel) {
            notifyLevelUp(player, skillType, newLevel);
        }
    }

    /**
     * Awards armor XP to a player at 1/3 the rate of weapon XP.
     * Armor XP is always awarded on kills regardless of equipped gear,
     * but at a reduced rate so it trails behind weapon skills.
     */
    private void awardArmorXP(Player player, long baseXP) {
        // Get current XP before adding
        long oldXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
        int previousLevel = SkillXPCalculator.levelFromTotalXP(oldXP);

        // Armor XP is at 1/3 rate (always awarded on kills)
        long armorXP = SkillXPCalculator.applySkillMultiplier(SkillType.ARMOR, baseXP);
        long newXP = PlayerData.addSkillXP(player.getUniqueId(), SkillType.ARMOR, armorXP);

        // Show XP bar animation
        SkillXPBar.showXPGain(player, SkillType.ARMOR, oldXP, newXP, armorXP);

        // Check for level up
        int newLevel = SkillXPCalculator.levelFromTotalXP(newXP);
        if (newLevel > previousLevel) {
            notifyLevelUp(player, SkillType.ARMOR, newLevel);
        }
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
                "\u00A76\u00A7lLEVEL UP!",
                "\u00A7e" + skillType.getDisplayName() + " \u00A77\u2192 \u00A7aLevel " + newLevel,
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

        // Server-wide announcement
        String announcement = "\u00A76" + player.getName() + " \u00A77reached \u00A7e" +
                skillType.getDisplayName() + " \u00A77level \u00A7a" + newLevel + "\u00A77!";
        Bukkit.broadcastMessage(announcement);

        // Update combat level display
        CombatLevelDisplay.updateDisplay(player);

        // Update armor health bonus if armor skill leveled up
        if (skillType == SkillType.ARMOR) {
            ArmorSkillHealthBonus.updateHealthBonus(player);
        }

        DebugMessage.log(player, "Player " + player.getName() + " reached " + skillType.getDisplayName() + " level " + newLevel);
    }

    /**
     * Notifies a player that their XP gain is limited due to level difference.
     *
     * @param player The player to notify
     * @param mobLevel The level of the mob killed
     * @param noXP True if no XP is given (mob too low), false if XP is capped (mob too high)
     */
    private void notifyLevelDifferenceLimit(Player player, int mobLevel, boolean noXP) {
        int combatLevel = CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());

        String message;
        if (noXP) {
            int levelDiff = combatLevel - mobLevel;
            message = "&7[&eEliteMobs&7] &cNo XP gained &7- this mob (level &f" + mobLevel +
                    "&7) is &f" + levelDiff + " levels &7below your combat level (&f" + combatLevel + "&7).";
        } else {
            int cappedLevel = combatLevel + 5;
            message = "&7[&eEliteMobs&7] &6XP capped &7- this mob (level &f" + mobLevel +
                    "&7) is too high. XP calculated as if level &f" + cappedLevel + "&7.";
        }

        player.sendMessage(message.replace("&", "\u00A7"));
    }
}
