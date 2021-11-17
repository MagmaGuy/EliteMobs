package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestObjectivesCompletedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Quest quest;

    public QuestObjectivesCompletedEvent(Player player, Quest customQuest) {
        this.player = player;
        this.quest = customQuest;
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
                QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(event.getPlayer(), event.getQuest());
                new EventCaller(questCompleteEvent);
            }
        }
    }
}
