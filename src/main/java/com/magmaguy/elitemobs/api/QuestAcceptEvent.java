package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.jetbrains.annotations.NotNull;


public class QuestAcceptEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final CustomQuest customQuest;
    private boolean isCancelled = false;

    public QuestAcceptEvent(Player player, CustomQuest customQuest) {
        this.player = player;
        this.customQuest = customQuest;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class QuestAcceptEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestAccept(QuestAcceptEvent event) {
            new DeveloperMessage("Quest accepted");
            event.getPlayer().sendMessage(QuestsConfig.questJoinMessage.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()));
            if (event.getCustomQuest().getCustomQuestsConfigFields().getQuestAcceptMessage() != null)
                event.getPlayer().sendMessage(event.getCustomQuest().getCustomQuestsConfigFields().getQuestAcceptMessage());
            CustomQuest.getPlayerQuests().put(event.getPlayer().getUniqueId(), event.getCustomQuest());
            event.getCustomQuest().setQuestIsAccepted(true);
        }
    }
}
