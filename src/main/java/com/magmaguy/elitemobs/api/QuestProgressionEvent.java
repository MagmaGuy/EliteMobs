package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestProgressionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Quest quest;
    @Getter
    private final Objective objective;

    public QuestProgressionEvent(Player player, Quest quest, Objective objective) {
        this.player = player;
        this.quest = quest;
        this.objective = objective;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class QuestProgressionEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestProgression(QuestProgressionEvent event) {
            event.getQuest().getQuestObjectives().updateQuestStatus(event.getPlayer().getUniqueId());
            if (QuestsConfig.doQuestChatProgression)
                event.getPlayer().sendMessage(QuestsConfig.getQuestChatProgressionMessage(event.getObjective()));
            if (!QuestTracking.isTracking(event.player))
                event.getQuest().getQuestObjectives().displayTemporaryObjectivesScoreboard(event.getPlayer());
            PlayerData.updateQuestStatus(event.getPlayer().getUniqueId(), event.getQuest());
        }
    }
}
