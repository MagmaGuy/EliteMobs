package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages combat level text displays that ride on top of players.
 * <p>
 * The display shows the player's combat level calculated from their skills.
 */
public class CombatLevelDisplay implements Listener {

    private static final Map<UUID, TextDisplay> playerDisplays = new HashMap<>();

    /**
     * Creates a combat level display for a player.
     *
     * @param player The player to create the display for
     */
    public static void createDisplay(Player player) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;

        // Remove existing display if present
        removeDisplay(player);

        // Create the text display
        TextDisplay display = player.getWorld().spawn(player.getLocation(), TextDisplay.class, textDisplay -> {
            textDisplay.setText(ChatColorConverter.convert(CombatLevelCalculator.getFormattedCombatLevel(player.getUniqueId())));
            textDisplay.setPersistent(false);
            textDisplay.setInterpolationDelay(0);
            textDisplay.setInterpolationDuration(0);
            textDisplay.setBillboard(Display.Billboard.CENTER);
            textDisplay.setShadowed(true);
            textDisplay.setSeeThrough(false);
            // Offset above the player's head
            org.bukkit.util.Transformation transformation = textDisplay.getTransformation();
            textDisplay.setTransformation(new org.bukkit.util.Transformation(
                    new org.joml.Vector3f(0, 0.7f, 0), // Translation: 0.7 blocks up
                    transformation.getLeftRotation(),
                    transformation.getScale(),
                    transformation.getRightRotation()
            ));
        });

        EntityTracker.registerVisualEffects(display);

        // Make the display ride the player
        player.addPassenger(display);

        playerDisplays.put(player.getUniqueId(), display);
    }

    /**
     * Removes the combat level display from a player.
     *
     * @param player The player to remove the display from
     */
    public static void removeDisplay(Player player) {
        TextDisplay display = playerDisplays.remove(player.getUniqueId());
        if (display != null && display.isValid()) {
            player.removePassenger(display);
            display.remove();
        }
    }

    /**
     * Updates the combat level display for a player.
     *
     * @param player The player to update the display for
     */
    public static void updateDisplay(Player player) {
        TextDisplay display = playerDisplays.get(player.getUniqueId());
        if (display != null && display.isValid()) {
            display.setText(ChatColorConverter.convert(CombatLevelCalculator.getFormattedCombatLevel(player.getUniqueId())));
        } else {
            // Recreate if missing
            createDisplay(player);
        }
    }

    /**
     * Updates the display for a player by UUID.
     *
     * @param playerUUID The player's UUID
     */
    public static void updateDisplay(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null && player.isOnline()) {
            updateDisplay(player);
        }
    }

    /**
     * Cleans up all displays.
     * Called on plugin shutdown.
     */
    public static void shutdown() {
        for (Map.Entry<UUID, TextDisplay> entry : playerDisplays.entrySet()) {
            TextDisplay display = entry.getValue();
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        playerDisplays.clear();
    }

    /**
     * Initializes displays for all online players.
     * Called on plugin startup.
     */
    public static void initialize() {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;

        // Delay initialization to ensure players are fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    createDisplay(player);
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;

        // Delay to ensure player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    createDisplay(event.getPlayer());
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L * 2);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeDisplay(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;

        // Delay to ensure player is fully respawned and in the correct location
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    createDisplay(event.getPlayer());
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 5L);
    }

    /**
     * Removes the display before teleport to prevent teleport issues with passengers.
     * Runs at LOW priority to ensure the display is removed before the teleport is processed.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerTeleportRemove(PlayerTeleportEvent event) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;

        // Remove the display before teleport to prevent issues with passengers blocking teleportation
        removeDisplay(event.getPlayer());
    }

    /**
     * Recreates the display after teleport completes.
     * Runs at MONITOR priority to ensure the teleport has completed.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportRecreate(PlayerTeleportEvent event) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;

        // Recreate the display after teleport
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    createDisplay(event.getPlayer());
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 5L);
    }
}
