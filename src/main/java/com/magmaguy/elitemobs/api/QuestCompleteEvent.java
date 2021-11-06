package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

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
            new DeveloperMessage("Quest ending");
            event.getPlayer().sendMessage(QuestsConfig.questCompleteMesage.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()));
            if (event.getCustomQuest().getCustomQuestsConfigFields().getQuestAcceptMessage() != null)
                event.getPlayer().sendMessage(event.getCustomQuest().getCustomQuestsConfigFields().getQuestAcceptMessage());
            CustomQuest.getPlayerQuests().remove(event.getPlayer().getUniqueId());
        }
    }
}
