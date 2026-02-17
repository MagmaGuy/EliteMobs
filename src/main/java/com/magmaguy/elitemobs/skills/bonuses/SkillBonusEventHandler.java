package com.magmaguy.elitemobs.skills.bonuses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.OverdrawSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.SteadyAimSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FlurrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central event handler for processing skill bonuses.
 * <p>
 * Delegates to the unified applySkillBonuses() methods on the event classes
 * for clean, centralized skill bonus processing.
 */
public class SkillBonusEventHandler implements Listener {

    // Cooldown tracking: skillId -> Set of player UUIDs on cooldown
    private static final Map<String, Set<UUID>> skillCooldowns = new ConcurrentHashMap<>();

    // Stack tracking: playerUUID -> skillId -> current stacks
    private static final Map<UUID, Map<String, Integer>> playerStacks = new ConcurrentHashMap<>();

    public SkillBonusEventHandler() {
        // Register ourselves as listener
    }

    /**
     * Starts a cooldown for a skill.
     */
    public static void startCooldown(String skillId, UUID playerUUID, int seconds) {
        Set<UUID> cooldownSet = skillCooldowns.computeIfAbsent(skillId, k -> ConcurrentHashMap.newKeySet());
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
     * Sets the stacks for a skill.
     */
    public static void setStacks(UUID playerUUID, String skillId, int count) {
        Map<String, Integer> stacks = playerStacks.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        stacks.put(skillId, count);
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
     * Gets weapon skill type from an item stack (for weapon switch handling).
     */
    private static SkillType getWeaponSkillTypeFromItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        String typeName = item.getType().name();
        if (typeName.endsWith("_SWORD")) return SkillType.SWORDS;
        if (typeName.endsWith("_AXE")) return SkillType.AXES;
        if (item.getType() == Material.BOW) return SkillType.BOWS;
        if (item.getType() == Material.CROSSBOW) return SkillType.CROSSBOWS;
        if (item.getType() == Material.TRIDENT) return SkillType.TRIDENTS;
        if (typeName.endsWith("_HOE")) return SkillType.HOES;
        try {
            if (item.getType() == Material.MACE) return SkillType.MACES;
        } catch (NoSuchFieldError e) { /* pre-1.21 */ }
        if (typeName.endsWith("_SPEAR")) return SkillType.SPEARS;
        return null;
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

    // ==================== WEAPON SKILL EVENT HOOKS ====================

    /**
     * Handles player dealing damage to elite mobs.
     * Delegates to the event's unified skill bonus processing.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEliteMobDamagedByPlayer(EliteMobDamagedByPlayerEvent event) {
        // Skip skill processing for bypass/custom damage events (DOT ticks, AOE secondary hits, etc.)
        // These are intentionally flagged to prevent recursive skill activation
        if (event.isCustomDamage()) return;
        event.applySkillBonuses();
    }

    /**
     * Handles weapon switching - applies/removes passive weapon skill effects.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Determine old and new weapon types
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        SkillType oldType = getWeaponSkillTypeFromItem(oldItem);
        SkillType newType = getWeaponSkillTypeFromItem(newItem);

        // Remove effects from old weapon type
        if (oldType == SkillType.SWORDS) {
            if (SwiftStrikesSkill.hasActiveSkill(uuid)) {
                SwiftStrikesSkill.removeSpeedBonus(player);
            }
            if (PoiseSkill.hasActiveSkill(uuid)) {
                PoiseSkill.removeKnockbackResistance(player);
            }
            if (FlurrySkill.hasActiveSkill(uuid)) {
                FlurrySkill.removeAttackSpeedModifier(player);
            }
        }
        if (oldType == SkillType.HOES) {
            GrimReachSkill.removeReachBonus(player);
        }
        if (oldType == SkillType.SPEARS) {
            LongReachSkill.removeReachBonus(player);
            PolearmMasterySkill.removeAttackSpeedBonus(player);
        }
        if (oldType == SkillType.BOWS) {
            OverdrawSkill.stopDrawing(uuid);
        }

        // Apply effects for new weapon type
        if (newType == SkillType.SWORDS) {
            if (SwiftStrikesSkill.hasActiveSkill(uuid)) {
                int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
                SwiftStrikesSkill.applySpeedBonus(player, skillLevel);
            }
            if (PoiseSkill.hasActiveSkill(uuid)) {
                int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
                PoiseSkill.applyKnockbackResistance(player, skillLevel);
            }
        }
        if (newType == SkillType.HOES) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.HOES);
            GrimReachSkill.applyReachBonus(player, skillLevel);
            SkillBonus grimReach = SkillBonusRegistry.getSkillById(GrimReachSkill.SKILL_ID);
            if (grimReach != null && grimReach.isActive(player)) {
                SkillBonus.sendSkillActionBar(player, grimReach);
            }
        }
        if (newType == SkillType.SPEARS) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SPEARS);
            LongReachSkill.applyReachBonus(player, skillLevel);
            SkillBonus longReach = SkillBonusRegistry.getSkillById(LongReachSkill.SKILL_ID);
            if (longReach != null && longReach.isActive(player)) {
                SkillBonus.sendSkillActionBar(player, longReach);
            }
            if (PolearmMasterySkill.hasActiveSkill(uuid)) {
                PolearmMasterySkill.applyAttackSpeedBonus(player, skillLevel);
            }
        }
    }

    /**
     * Handles bow draw start for Overdraw skill.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.BOW) return;

        UUID uuid = event.getPlayer().getUniqueId();
        if (OverdrawSkill.hasActiveSkill(uuid)) {
            OverdrawSkill.startDrawing(uuid);
        }
    }

    /**
     * Handles bow shoot for Overdraw skill - snapshots draw duration for the damage event.
     * We do NOT clear the draw time here; the arrow damage event needs it.
     * The draw time is cleared on next draw start or weapon switch instead.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            OverdrawSkill.snapshotDrawDuration(player.getUniqueId());
        }
    }

    /**
     * Handles player movement for SteadyAim and IronStance skills.
     * Only tracks actual movement (not just head rotation).
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        UUID uuid = event.getPlayer().getUniqueId();

        // Check if player actually moved (not just head rotation)
        boolean moved = event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ();

        if (SteadyAimSkill.hasActiveSkill(uuid)) {
            SteadyAimSkill.updatePlayerMovement(uuid, moved);
        }
        if (moved) {
            IronStanceSkill.updatePlayerMovement(uuid, true);
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

    /**
     * Cleans up all static resources on shutdown.
     */
    public static void shutdown() {
        skillCooldowns.clear();
        playerStacks.clear();
        PlayerSkillSelection.shutdown();
    }
}
