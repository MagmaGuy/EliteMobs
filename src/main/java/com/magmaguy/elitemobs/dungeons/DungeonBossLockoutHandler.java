package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles dungeon boss lockouts - tracking kills and preventing loot drops for locked out players.
 */
public class DungeonBossLockoutHandler implements Listener {

    /**
     * Creates a unique boss identifier from an InstancedBossEntity.
     * <p>
     * For phase bosses, {@link PhaseBossEntity#switchPhase} mutates both
     * {@code customBossesConfigFields} and {@code spawnLocation} as the fight
     * progresses (see PhaseBossEntity.java:66, 71, 81). The handler below
     * runs at LOW priority after the boss dies, by which point the live
     * config + spawn refer to the LAST phase, not the canonical phase-1
     * identity the dungeon config knows the boss by. Using the live values
     * leaks the identifier to a phase-N key (e.g. "phase3_filename:x,y,z")
     * that nothing else reads, so the lockout effectively disappears.
     * <p>
     * {@link DungeonKillTargetObjective.DungeonKillTargetObjectiveListener}
     * already canonicalises this way (DungeonKillTargetObjective.java:77-79).
     * Doing the same here keeps the identifier stable across phase switches.
     */
    public static String getBossIdentifier(InstancedBossEntity boss) {
        String filename;
        if (boss.getPhaseBossEntity() != null
                && boss.getPhaseBossEntity().getPhase1Config() != null) {
            filename = boss.getPhaseBossEntity().getPhase1Config().getFilename();
        } else {
            filename = boss.getCustomBossesConfigFields().getFilename();
        }
        // Use the boss's spawn location, stripping world name
        if (boss.getSpawnLocation() != null) {
            int x = boss.getSpawnLocation().getBlockX();
            int y = boss.getSpawnLocation().getBlockY();
            int z = boss.getSpawnLocation().getBlockZ();
            return filename + ":" + x + "," + y + "," + z;
        }
        // Fallback to current location if spawn location not set
        if (boss.getLocation() != null) {
            int x = boss.getLocation().getBlockX();
            int y = boss.getLocation().getBlockY();
            int z = boss.getLocation().getBlockZ();
            return filename + ":" + x + "," + y + "," + z;
        }
        return filename + ":unknown";
    }

    /**
     * Checks if a player is locked out from a specific boss and returns the set of locked out players.
     * Also handles adding new lockouts for players who aren't locked out.
     *
     * @param boss           The instanced boss that was killed
     * @param damagers       The players who damaged the boss
     * @param lockoutMinutes The lockout duration in minutes
     * @return Set of players who are locked out from this boss
     */
    public static Set<Player> processLockouts(InstancedBossEntity boss, Set<Player> damagers, int lockoutMinutes) {
        Set<Player> lockedOutPlayers = new HashSet<>();
        String bossIdentifier = getBossIdentifier(boss);

        for (Player player : damagers) {
            if (player.hasMetadata("NPC")) continue;
            if (!PlayerData.isInMemory(player.getUniqueId())) continue;

            DungeonBossLockout lockout = PlayerData.getDungeonBossLockout(player.getUniqueId());
            if (lockout == null) {
                lockout = new DungeonBossLockout();
            }

            if (lockout.isLockedOut(bossIdentifier)) {
                lockedOutPlayers.add(player);
                notifyLockout(player, boss, lockout, bossIdentifier);
            } else {
                lockout.addLockout(bossIdentifier, lockoutMinutes);
                PlayerData.updateDungeonBossLockout(player.getUniqueId(), lockout);
            }
        }

        return lockedOutPlayers;
    }

    /**
     * Notifies a player that they are locked out from boss loot.
     */
    private static void notifyLockout(Player player, InstancedBossEntity boss, DungeonBossLockout lockout, String bossIdentifier) {
        // Show subtitle
        String subtitle = DungeonsConfig.getDungeonLockoutSubtitle();
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(subtitle));

        // Send title with empty title and just subtitle
        player.sendTitle(DungeonsConfig.getDungeonLockoutTitle(), subtitle, 10, 70, 20);

        // Send chat message
        String bossName = boss.getName();
        String remainingTime = lockout.getFormattedRemainingTime(bossIdentifier);
        String chatMessage = DungeonsConfig.getDungeonLockoutChatMessage()
                        .replace("$bossName", bossName)
                        .replace("$remainingTime", remainingTime);
        player.sendMessage(chatMessage);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInstancedBossDeath(EliteMobDeathEvent event) {
        if (!(event.getEliteEntity() instanceof InstancedBossEntity instancedBoss)) return;

        DungeonInstance dungeonInstance = instancedBoss.getDungeonInstance();
        if (dungeonInstance == null) return;

        ContentPackagesConfigFields config = dungeonInstance.getContentPackagesConfigFields();
        if (config == null) return;

        int lockoutMinutes = config.getDungeonLockoutMinutes();
        if (lockoutMinutes <= 0) return;

        Set<Player> damagers = instancedBoss.getDamagers().keySet();
        Set<Player> lockedOutPlayers = processLockouts(instancedBoss, damagers, lockoutMinutes);

        // Store locked out players on the boss so CustomBossDeath can skip their loot
        instancedBoss.setLockoutPlayers(lockedOutPlayers);
    }
}
