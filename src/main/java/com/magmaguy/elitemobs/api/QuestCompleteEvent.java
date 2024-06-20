package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestCompleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Quest quest;
    private boolean cancelled;

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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static class QuestCompleteEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestComplete(QuestCompleteEvent event) {
            new EventCaller(new QuestRewardEvent(event.getPlayer(), event.quest));
            if (event.getQuest() instanceof CustomQuest customQuest &&
                    customQuest.getCustomQuestsConfigFields().getQuestCompleteSound() != null)
                Bukkit.getPlayer(customQuest.getPlayerUUID()).playSound(
                        Bukkit.getPlayer(customQuest.getPlayerUUID()),
                        customQuest.getCustomQuestsConfigFields().getQuestCompleteSound(),
                        1f, 1f);
            else
                event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questCompleteSound, 1, 1);
        }
    }
}
