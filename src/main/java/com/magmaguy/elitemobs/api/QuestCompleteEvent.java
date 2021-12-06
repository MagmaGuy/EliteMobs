package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
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
    private final Quest quest;

    public QuestCompleteEvent(Player player, Quest quest) {
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

    public static class QuestCompleteEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestAccept(QuestCompleteEvent event) {
            event.getQuest().getQuestObjectives().setTurnedIn(true);
            event.getPlayer().sendMessage(QuestsConfig.questCompleteMesage.replace("$questName", event.getQuest().getQuestName()));
            event.getQuest().getQuestObjectives().getQuestReward().doRewards(event.getPlayer().getUniqueId(), event.getQuest().getQuestLevel());
            if (QuestsConfig.useQuestCompleteTitles)
                event.getPlayer().sendTitle(
                        QuestsConfig.questCompleteTitle.replace("$questName", event.getQuest().getQuestName()),
                        QuestsConfig.questCompleteSubtitle.replace("$questName", event.getQuest().getQuestName()),
                        20, 60, 20);
            if (event.getQuest() instanceof CustomQuest) {
                CustomQuest customQuest = (CustomQuest) event.getQuest();
                CustomQuestsConfigFields customQuestsConfigFields = customQuest.getCustomQuestsConfigFields();
                if (!customQuest.getCustomQuestsConfigFields().getTemporaryPermissions().isEmpty()) {
                    PermissionAttachment permissionAttachment = event.getPlayer().addAttachment(MetadataHandler.PLUGIN);
                    for (String permission : customQuest.getCustomQuestsConfigFields().getTemporaryPermissions())
                        permissionAttachment.setPermission(permission, false);
                }
                if (!customQuest.getCustomQuestsConfigFields().getQuestCompleteDialog().isEmpty())
                    for (String dialog : customQuest.getCustomQuestsConfigFields().getQuestCompleteDialog())
                        event.getPlayer().sendMessage(dialog);
                for (String command : customQuestsConfigFields.getQuestCompleteCommands())
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replace("$player", event.getPlayer().getName())
                                    .replace("$getX", event.getPlayer().getLocation().getX() + "")
                                    .replace("$getY", event.getPlayer().getLocation().getY() + "")
                                    .replace("$getZ", event.getPlayer().getLocation().getZ() + ""));
            }
            //todo: add quest reward text for loot obtained
            PlayerData.removeQuest(event.getPlayer().getUniqueId(), event.getQuest());
            PlayerData.incrementQuestsCompleted(event.getPlayer().getUniqueId());
        }
    }
}
