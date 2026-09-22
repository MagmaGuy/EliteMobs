package com.magmaguy.elitemobs.quests.playercooldowns;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitTask;

import java.io.Serializable;
import java.util.UUID;

public class QuestCooldown implements Serializable {
    private static final long serialVersionUID = 8936646690733915274L;
    @Getter
    private final String permission;
    private final boolean permanent;
    @Getter
    private long targetUnixTime = 0;
    @Getter
    private transient BukkitTask bukkitTask = null;
    private transient PermissionAttachment permissionAttachment;

    public QuestCooldown(int delayInMinutes, String permission, UUID player) {
        this.permanent = delayInMinutes < 1;
        if (!permanent)
            this.targetUnixTime = System.currentTimeMillis() + 60L * 1000 * delayInMinutes;
        this.permission = permission;
    }

    public void startCooldown(UUID player) {
        stop();
        Player session = Bukkit.getPlayer(player);
        if (session == null || isExpired()) return;
        permissionAttachment = session.addAttachment(MetadataHandler.PLUGIN);
        permissionAttachment.setPermission(permission, true);
        session.setMetadata(permission, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
        if (!permanent) scheduleExpiration(session);
    }

    boolean isExpired() {
        return !permanent && targetUnixTime <= System.currentTimeMillis();
    }

    private void scheduleExpiration(Player session) {
        long remaining = targetUnixTime - System.currentTimeMillis();
        long delay = Math.max(1L, remaining / 50L + (remaining % 50L > 0 ? 1L : 0L));
        bukkitTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            bukkitTask = null;
            if (Bukkit.getPlayer(session.getUniqueId()) != session) {
                stop();
                return;
            }
            if (!isExpired()) {
                scheduleExpiration(session);
                return;
            }
            PlayerQuestCooldowns owner = PlayerData.getPlayerQuestCooldowns(session.getUniqueId());
            if (owner != null) owner.expire(this, session);
            else stop();
        }, delay);
    }

    void stop() {
        if (bukkitTask != null) bukkitTask.cancel();
        bukkitTask = null;
        PermissionAttachment owned = permissionAttachment;
        permissionAttachment = null;
        if (owned != null) owned.remove();
    }

}
