package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
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
            event.getPlayer().sendMessage(QuestsConfig.questLeaveMessage.replace("$questName", event.getQuest().getQuestName()));
            if (QuestsConfig.useQuestLeaveTitles)
                event.getPlayer().sendTitle(
                        QuestsConfig.questLeaveTitle.replace("$questName", event.getQuest().getQuestName()),
                        QuestsConfig.questLeaveSubtitle.replace("$questName", event.getQuest().getQuestName()),
                        20, 60, 20);
            if (event.getQuest() instanceof CustomQuest) {
                CustomQuest customQuest = (CustomQuest) event.getQuest();
                if (!customQuest.getCustomQuestsConfigFields().getTemporaryPermissions().isEmpty()) {
                    PermissionAttachment permissionAttachment = event.getPlayer().addAttachment(MetadataHandler.PLUGIN);
                    for (String permission : customQuest.getCustomQuestsConfigFields().getTemporaryPermissions())
                        permissionAttachment.setPermission(permission, false);
                }
            }
        }
    }
}
