package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.permissions.PermissionAttachment;

public class QuestLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Quest quest;

    public QuestLeaveEvent(Player player, Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class QuestLeaveEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestLeave(QuestLeaveEvent event) {
            PlayerData.removeQuest(event.getPlayer().getUniqueId(), event.getQuest());
            event.getPlayer().sendMessage(QuestsConfig.getQuestLeaveMessage().replace("$questName", event.getQuest().getQuestName()));
            if (QuestsConfig.isUseQuestLeaveTitles())
                event.getPlayer().sendTitle(
                        QuestsConfig.getQuestLeaveTitle().replace("$questName", event.getQuest().getQuestName()),
                        QuestsConfig.getQuestLeaveSubtitle().replace("$questName", event.getQuest().getQuestName()),
                        20, 60, 20);
            if (event.getQuest() instanceof CustomQuest customQuest) {
                if (!customQuest.getCustomQuestsConfigFields().getTemporaryPermissions().isEmpty()) {
                    PermissionAttachment permissionAttachment = event.getPlayer().addAttachment(MetadataHandler.PLUGIN);
                    for (String permission : customQuest.getCustomQuestsConfigFields().getTemporaryPermissions())
                        permissionAttachment.setPermission(permission, false);
                }

                if (QuestTracking.getPlayerTrackingQuests().containsKey(event.getPlayer())) {
                    QuestTracking questTracking = QuestTracking.getPlayerTrackingQuests().get(event.getPlayer());
                    if (questTracking.getCustomQuest().getCustomQuestsConfigFields().equals(customQuest.getCustomQuestsConfigFields()))
                        questTracking.stop();
                }
            }

            event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questAbandonSound, 1, 1);
        }
    }
}
