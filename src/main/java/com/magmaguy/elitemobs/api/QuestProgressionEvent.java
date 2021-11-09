package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestProgressionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final CustomQuest customQuest;
    @Getter
    private final Objective objective;

    public QuestProgressionEvent(Player player, CustomQuest customQuest, Objective objective) {
        this.player = player;
        this.customQuest = customQuest;
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
            event.getCustomQuest().getCustomQuestObjectives().updateQuestStatus(event.getPlayer().getUniqueId());
            if (QuestsConfig.doQuestChatProgression)
                event.getPlayer().sendMessage(QuestsConfig.getKillQuestChatProgressionMessage(event.getObjective()));
            event.getCustomQuest().getCustomQuestObjectives().displayObjectivesScoreboard(event.getPlayer());
        }
    }
}
