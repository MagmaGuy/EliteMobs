package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestObjectivesCompletedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final CustomQuest customQuest;

    public QuestObjectivesCompletedEvent(Player player, CustomQuest customQuest) {
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

    public static class QuestObjectivesCompletedEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onObjectivesCompleted(QuestObjectivesCompletedEvent event) {
            if (!QuestsConfig.requireQuestTurnIn) {
                QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(event.getPlayer(), event.getCustomQuest());
                new EventCaller(questCompleteEvent);
            }
        }
    }
}
