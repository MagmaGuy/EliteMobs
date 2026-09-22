package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.MessageThrottler;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestProgressionEvent extends Event {
    private static final java.util.Set<java.util.UUID> batchingPlayers = new java.util.HashSet<>();
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

    public static void fireBatch(Player player, java.util.List<QuestProgressionEvent> events) {
        if (events.isEmpty()) return;
        boolean ownsBatch = batchingPlayers.add(player.getUniqueId());
        try {
            for (QuestProgressionEvent event : events)
                if (!event.quest.getQuestObjectives().isTurnedIn()) new com.magmaguy.elitemobs.utils.EventCaller(event);
        } finally {
            if (ownsBatch) {
                batchingPlayers.remove(player.getUniqueId());
                if (PlayerData.isInMemory(player)) PlayerData.updateQuestStatus(player.getUniqueId());
            }
        }
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class QuestProgressionEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestProgression(QuestProgressionEvent event) {
            event.getQuest().getQuestObjectives().updateQuestStatus(event.getPlayer().getUniqueId());
            if (event.getQuest().getQuestObjectives().isTurnedIn()) return;
            if (QuestsConfig.isDoQuestChatProgression())
                MessageThrottler.pushQuestProgress(event.getPlayer(), event.getObjective(),
                        QuestsConfig.getQuestChatProgressionMessage(event.getObjective()));
            if (!QuestTracking.isTracking(event.player))
                event.getQuest().getQuestObjectives().displayTemporaryObjectivesScoreboard(event.getPlayer());
            if (!batchingPlayers.contains(event.getPlayer().getUniqueId()))
                PlayerData.updateQuestStatus(event.getPlayer().getUniqueId(), event.getQuest());
            event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questProgressionSound, 1, 1);
        }
    }
}
