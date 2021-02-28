package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CustomBossEscapeMechanism {
    public static BukkitTask startEscape(int timeout, CustomBossEntity customBossEntity) {
        if (timeout < 1) return null;
        return new BukkitRunnable() {

            @Override
            public void run() {
                if (customBossEntity.customBossConfigFields.getAnnouncementPriority() < 1) return;
                if (customBossEntity.customBossConfigFields.getEscapeMessage() != null)
                    for (Player player : Bukkit.getOnlinePlayers())
                        if (player.getWorld().equals(customBossEntity.getLivingEntity().getWorld()))
                            player.sendMessage(ChatColorConverter.convert(customBossEntity.customBossConfigFields.getEscapeMessage()));
                if (customBossEntity.customBossConfigFields.getAnnouncementPriority() < 3) return;
                new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossEntity.customBossConfigFields.getEscapeMessage()));
                EntityTracker.unregister(customBossEntity.uuid, RemovalReason.BOSS_TIMEOUT);
            }

        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * timeout);
    }
}
