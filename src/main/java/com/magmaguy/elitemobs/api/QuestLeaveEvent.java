package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

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
        public void onQuestAccept(QuestLeaveEvent event) {
            event.getPlayer().sendMessage(QuestsConfig.questLeaveMessage.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()));
            CustomQuest.getPlayerQuests().remove(event.getPlayer().getUniqueId());
        }
    }
}
