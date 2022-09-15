package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class QuestAcceptEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final Quest quest;
    private boolean isCancelled = false;

    public QuestAcceptEvent(Player player, Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class QuestAcceptEventHandler implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void questAcceptLimiter(QuestAcceptEvent event) {
            if (PlayerData.getQuests(event.getPlayer().getUniqueId()).size() < QuestsConfig.getMaximumActiveQuests())
                return;
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColorConverter.convert(QuestsConfig.getQuestCapMessage()));
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestAccept(QuestAcceptEvent event) {
            event.getPlayer().sendMessage(QuestsConfig.getQuestJoinMessage().replace("$questName", ChatColorConverter.convert(event.getQuest().getQuestName())));
            event.getQuest().setAccepted(true);
            if (QuestsConfig.isUseQuestAcceptTitles())
                event.getPlayer().sendTitle(
                        ChatColorConverter.convert(QuestsConfig.getQuestStartTitle().replace("$questName", event.getQuest().getQuestName())),
                        ChatColorConverter.convert(QuestsConfig.getQuestStartSubtitle().replace("$questName", event.getQuest().getQuestName())),
                        20, 60, 20);

            if (event.getQuest() instanceof CustomQuest customQuest) {

                customQuest.applyTemporaryPermissions(event.getPlayer());

                if (customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog() != null &&
                        !customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog().isEmpty())
                    for (String dialog : customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog())
                        event.getPlayer().sendMessage(dialog);
            }
            event.getQuest().getQuestObjectives().displayTemporaryObjectivesScoreboard(event.getPlayer());
            PlayerData.addQuest(event.getPlayer().getUniqueId(), event.getQuest());
        }
    }
}
