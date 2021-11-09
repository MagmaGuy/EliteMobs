package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.permissions.PermissionAttachment;

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
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onQuestAccept(QuestAcceptEvent event) {
            event.getPlayer().sendMessage(QuestsConfig.questJoinMessage.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()));
            CustomQuest.getPlayerQuests().put(event.getPlayer().getUniqueId(), event.getCustomQuest());
            event.getCustomQuest().setQuestIsAccepted(true);

            if (!event.getCustomQuest().getCustomQuestsConfigFields().getTemporaryPermissions().isEmpty()) {
                PermissionAttachment permissionAttachment = event.getPlayer().addAttachment(MetadataHandler.PLUGIN);
                for (String permission : event.getCustomQuest().getCustomQuestsConfigFields().getTemporaryPermissions())
                    permissionAttachment.setPermission(permission, true);
            }

            if (!event.getCustomQuest().getCustomQuestsConfigFields().getQuestAcceptDialog().isEmpty())
                for (String dialog : event.getCustomQuest().getCustomQuestsConfigFields().getQuestAcceptDialog())
                    event.getPlayer().sendMessage(dialog);

            if (QuestsConfig.useQuestAcceptTitles)
                event.getPlayer().sendTitle(
                        QuestsConfig.questStartTitle.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()),
                        QuestsConfig.questStartSubtitle.replace("$questName", event.getCustomQuest().getCustomQuestsConfigFields().getQuestName()),
                        20, 60, 20);

        }
    }
}
