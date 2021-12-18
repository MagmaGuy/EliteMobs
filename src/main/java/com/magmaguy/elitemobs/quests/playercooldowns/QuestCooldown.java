package com.magmaguy.elitemobs.quests.playercooldowns;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.Serializable;
import java.util.UUID;

public class QuestCooldown implements Serializable {
    @Getter
    private long targetUnixTime;
    @Getter
    private String permission;
    @Getter
    private transient BukkitTask bukkitTask = null;

    public QuestCooldown(int delayInMinutes, String permission, UUID player) {
        this.targetUnixTime = System.currentTimeMillis() + 60L * 1000 * delayInMinutes;
        this.permission = permission;
        startCooldown(player);
    }

    public void startCooldown(UUID player) {
        PlayerData.updatePlayerQuestCooldowns(player);
        long delay = Math.min((targetUnixTime - System.currentTimeMillis()) / 1000L * 20L, 0L);
        bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPlayer(player) == null) return;
                PermissionAttachment permissionAttachment = Bukkit.getPlayer(player).addAttachment(MetadataHandler.PLUGIN);
                permissionAttachment.setPermission(permission, false);
                PlayerData.updatePlayerQuestCooldowns(player);

            }
        }.runTaskLater(MetadataHandler.PLUGIN, delay);
    }

}
