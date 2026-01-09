package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Central event handler for processing skill bonuses.
 * <p>
 * Delegates to the unified applySkillBonuses() methods on the event classes
 * for clean, centralized skill bonus processing.
 */
public class SkillBonusEventHandler implements Listener {

    // Cooldown tracking: skillId -> Set of player UUIDs on cooldown
    private static final Map<String, Set<UUID>> skillCooldowns = new HashMap<>();

    // Stack tracking: playerUUID -> skillId -> current stacks
    private static final Map<UUID, Map<String, Integer>> playerStacks = new HashMap<>();

    // Active toggle skills: playerUUID -> Set of active skillIds
    private static final Map<UUID, Set<String>> activeToggles = new HashMap<>();

    public SkillBonusEventHandler() {
        // Register ourselves as listener
    }

    /**
     * Handles player dealing damage to elite mobs.
     * Delegates to the event's unified skill bonus processing.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEliteMobDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
        event.applySkillBonuses();
    }

    /**
     * Handles player receiving damage from elite mobs.
     * Delegates to the event's unified skill bonus processing.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDamagedByEliteMob(PlayerDamagedByEliteMobEvent event) {
        event.applySkillBonuses();
    }

    /**
     * Loads player skill selections on join.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay slightly to ensure PlayerData is loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    PlayerSkillSelection.onPlayerJoin(player);
                    SkillBonusRegistry.applyAllBonuses(player);
                    // Apply armor skill health bonus (+1 heart per armor level)
                    ArmorSkillHealthBonus.applyHealthBonus(player);
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 40); // 2 seconds after PlayerData loads
    }

    /**
     * Saves player skill selections and cleans up on quit.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Save and cleanup
        PlayerSkillSelection.onPlayerLeave(player);
        SkillBonusRegistry.removeAllBonuses(player);
        // Remove armor skill health bonus
        ArmorSkillHealthBonus.removeHealthBonus(player);

        // Clear player-specific data
        playerStacks.remove(uuid);
        activeToggles.remove(uuid);

        // Remove from all cooldowns
        for (Set<UUID> cooldownSet : skillCooldowns.values()) {
            cooldownSet.remove(uuid);
        }
    }

    // ==================== COOLDOWN MANAGEMENT ====================

    /**
     * Checks if a skill is on cooldown for a player.
     */
    public static boolean isOnCooldown(String skillId, UUID playerUUID) {
        Set<UUID> cooldownSet = skillCooldowns.get(skillId);
        return cooldownSet != null && cooldownSet.contains(playerUUID);
    }

    /**
     * Starts a cooldown for a skill.
     */
    public static void startCooldown(String skillId, UUID playerUUID, int seconds) {
        Set<UUID> cooldownSet = skillCooldowns.computeIfAbsent(skillId, k -> new HashSet<>());
        cooldownSet.add(playerUUID);

        // Schedule cooldown removal
        new BukkitRunnable() {
            @Override
            public void run() {
                endCooldown(skillId, playerUUID);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, seconds * 20L);
    }

    /**
     * Ends a cooldown for a skill.
     */
    public static void endCooldown(String skillId, UUID playerUUID) {
        Set<UUID> cooldownSet = skillCooldowns.get(skillId);
        if (cooldownSet != null) {
            cooldownSet.remove(playerUUID);
        }
    }

    // ==================== STACK MANAGEMENT ====================

    /**
     * Gets the current stacks for a skill.
     */
    public static int getStacks(UUID playerUUID, String skillId) {
        Map<String, Integer> stacks = playerStacks.get(playerUUID);
        if (stacks == null) return 0;
        return stacks.getOrDefault(skillId, 0);
    }

    /**
     * Sets the stacks for a skill.
     */
    public static void setStacks(UUID playerUUID, String skillId, int count) {
        Map<String, Integer> stacks = playerStacks.computeIfAbsent(playerUUID, k -> new HashMap<>());
        stacks.put(skillId, count);
    }

    /**
     * Resets stacks for a skill.
     */
    public static void resetStacks(UUID playerUUID, String skillId) {
        Map<String, Integer> stacks = playerStacks.get(playerUUID);
        if (stacks != null) {
            stacks.remove(skillId);
        }
    }

    // ==================== TOGGLE MANAGEMENT ====================

    /**
     * Checks if a toggle skill is active for a player.
     */
    public static boolean isToggleActive(UUID playerUUID, String skillId) {
        Set<String> toggles = activeToggles.get(playerUUID);
        return toggles != null && toggles.contains(skillId);
    }

    /**
     * Sets a toggle skill state for a player.
     */
    public static void setToggle(UUID playerUUID, String skillId, boolean active) {
        Set<String> toggles = activeToggles.computeIfAbsent(playerUUID, k -> new HashSet<>());
        if (active) {
            toggles.add(skillId);
        } else {
            toggles.remove(skillId);
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Determines the weapon skill type based on the player's main hand item.
     */
    public static SkillType getWeaponSkillType(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR) return null;

        Material type = mainHand.getType();
        String typeName = type.name();

        // Check weapon types
        if (typeName.endsWith("_SWORD")) return SkillType.SWORDS;
        if (typeName.endsWith("_AXE")) return SkillType.AXES;
        if (type == Material.BOW) return SkillType.BOWS;
        if (type == Material.CROSSBOW) return SkillType.CROSSBOWS;
        if (type == Material.TRIDENT) return SkillType.TRIDENTS;
        if (typeName.endsWith("_HOE")) return SkillType.HOES;

        return null;
    }

    /**
     * Cleans up all static resources on shutdown.
     */
    public static void shutdown() {
        skillCooldowns.clear();
        playerStacks.clear();
        activeToggles.clear();
        PlayerSkillSelection.shutdown();
    }
}
