package com.magmaguy.elitemobs.skills;

import com.magmaguy.easyminecraftgoals.NMSManager;
import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages combat level text displays above players using packet-based FakeText.
 * <p>
 * The display shows the player's combat level calculated from their skills.
 * Uses FakeText from EasyMinecraftGoals for packet-based display that mounts
 * on players - the client handles positioning automatically.
 */
public class CombatLevelDisplay implements Listener {

    private static final Map<UUID, FakeText> playerDisplays = new ConcurrentHashMap<>();

    /**
     * Creates a combat level display for a player.
     *
     * @param player The player to create the display for
     */
    public static void createDisplay(Player player) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowCombatLevelDisplay()) return;
        if (NMSManager.getAdapter() == null) return;

        // Remove existing display if present
        removeDisplay(player);

        // Create the FakeText display at the player's location (will be mounted)
        FakeText fakeText = NMSManager.getAdapter().fakeTextBuilder()
                .text(ChatColorConverter.convert(CombatLevelCalculator.getFormattedCombatLevel(player.getUniqueId())))
                .billboard(Display.Billboard.CENTER)
                .shadow(true)
                .seeThrough(false)
                .translation(0, 0.5f, 0) // Offset upward
                .build(player.getLocation());

        playerDisplays.put(player.getUniqueId(), fakeText);

        // Attach to the player - this mounts and registers with the global tracker
        // which handles visibility, world changes, respawns, etc. automatically
        fakeText.attachTo(player);
    }

    /**
     * Removes the combat level display from a player.
     *
     * @param player The player to remove the display from
     */
    public static void removeDisplay(Player player) {
        FakeText fakeText = playerDisplays.remove(player.getUniqueId());
        if (fakeText != null) {
            fakeText.detach(); // Unregisters from tracker and hides from all viewers
        }
    }

    /**
     * Updates the combat level display text for a player.
     *
     * @param player The player to update the display for
     */
    public static void updateDisplay(Player player) {
        FakeText fakeText = playerDisplays.get(player.getUniqueId());
        if (fakeText != null) {
            fakeText.setText(ChatColorConverter.convert(CombatLevelCalculator.getFormattedCombatLevel(player.getUniqueId())));
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
        for (FakeText fakeText : playerDisplays.values()) {
            if (fakeText != null) {
                fakeText.detach();
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

        Player joiningPlayer = event.getPlayer();

        // Delay to ensure player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!joiningPlayer.isOnline()) return;

                // Create display for the joining player
                // The global tracker handles showing other players' displays automatically
                createDisplay(joiningPlayer);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20L * 2);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove the quitting player's display
        removeDisplay(event.getPlayer());
        // The global tracker handles hiding other displays from the quitting player
    }
}
