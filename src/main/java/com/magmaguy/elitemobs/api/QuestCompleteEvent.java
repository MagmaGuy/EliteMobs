package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.quests.CustomQuest;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.permissions.PermissionAttachment;

public class QuestCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final CustomQuest customQuest;

    public QuestCompleteEvent(Player player, CustomQuest customQuest) {
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

    public static class QuestCompleteEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestAccept(QuestCompleteEvent event) {
            event.getCustomQuest().getCustomQuestObjectives().getCustomQuestReward().doRewards(event.getPlayer().getUniqueId(), event.getCustomQuest().getQuestLevel());

            CustomQuestsConfigFields customQuestsConfigFields = event.getCustomQuest().getCustomQuestsConfigFields();
            event.getPlayer().sendMessage(QuestsConfig.questCompleteMesage.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()));
            CustomQuest.getPlayerQuests().remove(event.getPlayer().getUniqueId());
            if (!event.getCustomQuest().getCustomQuestsConfigFields().getTemporaryPermissions().isEmpty()) {
                PermissionAttachment permissionAttachment = event.getPlayer().addAttachment(MetadataHandler.PLUGIN);
                for (String permission : event.getCustomQuest().getCustomQuestsConfigFields().getTemporaryPermissions())
                    permissionAttachment.setPermission(permission, false);
            }

            if (!event.getCustomQuest().getCustomQuestsConfigFields().getQuestCompleteDialog().isEmpty())
                for (String dialog : event.getCustomQuest().getCustomQuestsConfigFields().getQuestCompleteDialog())
                    event.getPlayer().sendMessage(dialog);

            if (QuestsConfig.useQuestCompleteTitles)
                event.getPlayer().sendTitle(
                        QuestsConfig.questCompleteTitle.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()),
                        QuestsConfig.questCompleteSubtitle.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()),
                        20, 60, 20);

            for (String command : customQuestsConfigFields.getQuestCompleteCommands())
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("$player", event.getPlayer().getName())
                                .replace("$getX", event.getPlayer().getLocation().getX() + "")
                                .replace("$getY", event.getPlayer().getLocation().getY() + "")
                                .replace("$getZ", event.getPlayer().getLocation().getZ() + ""));
        }
    }
}
