package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.permissions.PermissionAttachment;

public class QuestLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final CustomQuest customQuest;

    public QuestLeaveEvent(Player player, CustomQuest customQuest) {
        this.player = player;
        this.customQuest = customQuest;
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
            event.getPlayer().sendMessage(QuestsConfig.questLeaveMessage.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()));
            CustomQuest.getPlayerQuests().remove(event.getPlayer().getUniqueId());
            if (!event.getCustomQuest().getCustomQuestsConfigFields().getTemporaryPermissions().isEmpty()) {
                PermissionAttachment permissionAttachment = event.getPlayer().addAttachment(MetadataHandler.PLUGIN);
                for (String permission : event.getCustomQuest().getCustomQuestsConfigFields().getTemporaryPermissions())
                    permissionAttachment.setPermission(permission, false);
            }
            if (QuestsConfig.useQuestLeaveTitles)
                event.getPlayer().sendTitle(
                        QuestsConfig.questLeaveTitle.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()),
                        QuestsConfig.questLeaveSubtitle.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()),
                        20, 60, 20);
        }
    }
}
