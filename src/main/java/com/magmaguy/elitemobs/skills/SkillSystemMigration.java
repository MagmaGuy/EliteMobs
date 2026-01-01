package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles migration from the old guild/prestige system to the new skill system.
 * <p>
 * This class:
 * - Detects first-time skill system activation
 * - Creates a migration flag file to track migration state
 * - Warns players on login about the data reset
 */
public class SkillSystemMigration {

    private static final String MIGRATION_FLAG_FILE = "skill_system_migrated.flag";
    private static final Set<UUID> warnedPlayers = new HashSet<>();
    private static boolean migrationComplete = false;

    private SkillSystemMigration() {
        // Static utility class
    }

    /**
     * Initializes the migration system.
     * Called on plugin startup.
     */
    public static void initialize() {
        if (!SkillsConfig.isSkillSystemEnabled()) {
            return;
        }

        File flagFile = getMigrationFlagFile();

        if (flagFile.exists()) {
            // Migration already happened
            migrationComplete = true;
            Logger.info("[Skills] Skill system migration already complete.");
        } else {
            // First time enabling skill system - create flag file
            migrationComplete = false;
            createMigrationFlag();
            Logger.warn("[Skills] =================================================");
            Logger.warn("[Skills] SKILL SYSTEM ACTIVATED FOR THE FIRST TIME");
            Logger.warn("[Skills] All players will start fresh with new skill data.");
            Logger.warn("[Skills] Old guild/prestige data is preserved but unused.");
            Logger.warn("[Skills] =================================================");
        }
    }

    /**
     * Gets the migration flag file.
     */
    private static File getMigrationFlagFile() {
        return new File(MetadataHandler.PLUGIN.getDataFolder(), MIGRATION_FLAG_FILE);
    }

    /**
     * Creates the migration flag file.
     */
    private static void createMigrationFlag() {
        File flagFile = getMigrationFlagFile();
        try {
            if (!flagFile.getParentFile().exists()) {
                flagFile.getParentFile().mkdirs();
            }
            if (flagFile.createNewFile()) {
                Logger.info("[Skills] Created migration flag file.");
            }
        } catch (IOException e) {
            Logger.warn("[Skills] Could not create migration flag file: " + e.getMessage());
        }
    }

    /**
     * Checks if a player should receive a migration warning.
     *
     * @param player The player to check
     * @return true if the player should be warned
     */
    public static boolean shouldWarnPlayer(Player player) {
        if (migrationComplete) {
            return false;
        }
        return !warnedPlayers.contains(player.getUniqueId());
    }

    /**
     * Marks a player as having been warned.
     *
     * @param player The player to mark
     */
    public static void markPlayerWarned(Player player) {
        warnedPlayers.add(player.getUniqueId());
    }

    /**
     * Sends the migration warning message to a player.
     *
     * @param player The player to warn
     */
    public static void sendMigrationWarning(Player player) {
        player.sendMessage(ChatColorConverter.convert(""));
        player.sendMessage(ChatColorConverter.convert("&8&m------------------------------------------------"));
        player.sendMessage(ChatColorConverter.convert("&5&lSKILL SYSTEM ACTIVATED"));
        player.sendMessage(ChatColorConverter.convert("&7EliteMobs now uses a new skill leveling system!"));
        player.sendMessage(ChatColorConverter.convert(""));
        player.sendMessage(ChatColorConverter.convert("&eYou now level individual skills by killing mobs:"));
        player.sendMessage(ChatColorConverter.convert("&7- Swords, Axes, Bows, Crossbows, Tridents, Hoes"));
        player.sendMessage(ChatColorConverter.convert("&7- Armor levels on every kill at 1/3 XP rate"));
        player.sendMessage(ChatColorConverter.convert(""));
        player.sendMessage(ChatColorConverter.convert("&cAll players start fresh with Level 1 in all skills."));
        player.sendMessage(ChatColorConverter.convert("&7Type &e/em &7and click &5Skills &7to view your progress!"));
        player.sendMessage(ChatColorConverter.convert("&8&m------------------------------------------------"));
        player.sendMessage(ChatColorConverter.convert(""));
    }

    /**
     * Clears migration state.
     * Called on plugin shutdown.
     */
    public static void shutdown() {
        warnedPlayers.clear();
    }

    /**
     * Listener for migration-related events.
     */
    public static class MigrationEvents implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            if (!SkillsConfig.isSkillSystemEnabled()) {
                return;
            }

            Player player = event.getPlayer();

            // Delay the message slightly so it appears after other join messages
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }

                    if (shouldWarnPlayer(player)) {
                        sendMigrationWarning(player);
                        markPlayerWarned(player);
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20L * 2); // 2 second delay
        }
    }
}
