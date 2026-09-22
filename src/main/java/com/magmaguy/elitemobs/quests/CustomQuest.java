package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;

import java.util.List;
import java.util.UUID;

public class CustomQuest extends Quest {
    private static final long serialVersionUID = -797964699830430999L;

    @Getter
    private final String configurationFilename;
    private transient CustomQuestsConfigFields customQuestsConfigFields;
    private transient PermissionAttachment temporaryPermissions;

    public CustomQuest(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
        super(player, new QuestObjectives(new QuestReward(customQuestsConfigFields, player)), customQuestsConfigFields.getQuestLevel());
        this.customQuestsConfigFields = customQuestsConfigFields;
        this.configurationFilename = customQuestsConfigFields.getFilename();
        super.questObjectives.setQuest(this);
        super.questName = customQuestsConfigFields.getQuestName();
        super.questTaker = customQuestsConfigFields.getTurnInNPC();
    }

    public static CustomQuest getQuest(String questFilename, Player player) {
        if (!PlayerData.isInMemory(player)) return null;
        if (CustomQuestsConfig.getCustomQuests().get(questFilename) == null) return null;
        Quest quest = null;
        for (Quest iteratedQuest : PlayerData.getQuests(player.getUniqueId()))
            if (iteratedQuest instanceof CustomQuest && ((CustomQuest) iteratedQuest).getConfigurationFilename().equals(questFilename)) {
                quest = iteratedQuest;
                break;
            }
        if (quest != null)
            return (CustomQuest) quest;
        var fields = CustomQuestsConfig.getCustomQuests().get(questFilename);
        if (!hasPermissionForQuest(player, fields)) return null;
        for (Quest pending : pendingPlayerQuests.getOrDefault(player.getUniqueId(), List.of()))
            if (pending instanceof CustomQuest custom && custom.configurationFilename.equals(questFilename)) return custom;
        return new CustomQuest(player, fields);
    }

    public static Quest startQuest(String questID, Player player) {
        if (!PlayerData.isInMemory(player)) return null;
        List<Quest> pendingQuests = pendingPlayerQuests.get(player.getUniqueId());
        UUID parsedQuestID;
        try {
            parsedQuestID = UUID.fromString(questID);
        } catch (IllegalArgumentException | NullPointerException exception) {
            player.sendMessage(QuestsConfig.getInvalidQuestIdMessage().replace("$questId", String.valueOf(questID)));
            return null;
        }
        if (pendingQuests == null) {
            player.sendMessage(QuestsConfig.getInvalidQuestIdMessage().replace("$questId", questID));
            return null;
        }
        Quest quest = null;
        for (Quest iteratedQuest : pendingQuests)
            if (iteratedQuest.getQuestID().equals(parsedQuestID)) {
                quest = iteratedQuest;
                break;
            }
        if (!canAccept(player, quest)) {
            player.sendMessage(QuestsConfig.getInvalidQuestIdMessage().replace("$questId", questID));
            return null;
        }
        // Reserve the issued offer before callbacks, including callbacks that run another command.
        pendingQuests.remove(quest);
        try {
            QuestAcceptEvent questAcceptEvent = new QuestAcceptEvent(player, quest);
            new EventCaller(questAcceptEvent);
            return questAcceptEvent.isCancelled() || !quest.isAccepted() ? null : quest;
        } finally {
            if (!quest.isAccepted() && pendingPlayerQuests.get(player.getUniqueId()) == pendingQuests
                    && canAccept(player, quest)) pendingQuests.add(quest);
            if (pendingQuests.isEmpty()) pendingPlayerQuests.remove(player.getUniqueId(), pendingQuests);
        }
    }

    public CustomQuestsConfigFields getCustomQuestsConfigFields() {
        if (customQuestsConfigFields == null)
            this.customQuestsConfigFields = CustomQuestsConfig.getCustomQuests().get(configurationFilename);
        if (customQuestsConfigFields == null) {
            Logger.warn("Detected that Custom Quest " + configurationFilename + " got removed even though player "
                    + getPlayerUUID() + " is still trying to complete it. This player's quest will now be wiped.");
            PlayerData.removeQuest(getPlayerUUID(), this);
            return null;
        }
        return customQuestsConfigFields;
    }

    public void applyTemporaryPermissions(Player player) {
        if (temporaryPermissions != null) return;
        CustomQuestsConfigFields fields = getCustomQuestsConfigFields();
        if (fields != null && !fields.getTemporaryPermissions().isEmpty()) {
            temporaryPermissions = player.addAttachment(MetadataHandler.PLUGIN);
            for (String permission : fields.getTemporaryPermissions())
                temporaryPermissions.setPermission(permission, true);
        }
    }

    public void releaseTemporaryPermissions() {
        PermissionAttachment owned = temporaryPermissions;
        temporaryPermissions = null;
        if (owned != null) owned.remove();
    }

    public void applyEndPermissions(Player player) {
        releaseTemporaryPermissions();
        CustomQuestsConfigFields fields = getCustomQuestsConfigFields();
        if (fields != null && !fields.getQuestLockoutPermission().isEmpty()) {
            PlayerQuestCooldowns.addCooldown(player,
                    getCustomQuestsConfigFields().getQuestLockoutPermission(),
                    getCustomQuestsConfigFields().getQuestLockoutMinutes());
        }
    }

    public boolean hasPermissionForQuest(Player player) {
        return hasPermissionForQuest(player, getCustomQuestsConfigFields());
    }

    /** Checks availability without creating a pending quest or notifying the player. */
    public static boolean hasPermissionForQuest(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
        if (customQuestsConfigFields == null || !customQuestsConfigFields.isEnabled()) return false;
        if (PlayerQuestCooldowns.bypassesQuestRestrictions(player)) return true;
        if (customQuestsConfigFields.getQuestAcceptPermissions() != null && !
                customQuestsConfigFields.getQuestAcceptPermissions().isEmpty())
            for (String permission : customQuestsConfigFields.getQuestAcceptPermissions())
                if (!player.hasMetadata(permission))
                    return false;
        if (!customQuestsConfigFields.getQuestAcceptPermission().isEmpty() &&
                !player.hasMetadata(customQuestsConfigFields.getQuestAcceptPermission()))
            return false;

        // Check new lockout system (preferred) - if lockoutMinutes is configured, use the new system
        if (customQuestsConfigFields.getQuestLockoutMinutes() > 0) {
            // Only check the new lockout system, ignore old permission-based lockouts
            QuestLockout lockout = PlayerData.getQuestLockout(player.getUniqueId());
            return lockout == null || !lockout.isLockedOut(customQuestsConfigFields.getFilename());
        }

        // If neither lockout system is configured, the quest is available.
        // This handles EM9 quest files that may have a questLockoutPermission set but no functional
        // guild rank system in EM10 to back it - without an active lockout system, the quest is open.
        String lockoutPermission = customQuestsConfigFields.getQuestLockoutPermission();
        if (lockoutPermission == null || lockoutPermission.isEmpty()) return true;

        // Legacy permission-based lockout: only block if the player actually has the lockout metadata,
        // meaning they completed the quest and were given the lockout marker via PlayerQuestCooldowns
        return !player.hasMetadata(lockoutPermission);
    }

    public static class CustomQuestEvents implements Listener {
        @EventHandler
        public void onQuestReward(QuestRewardEvent event) {
            if (event.getQuest() instanceof CustomQuest customQuest) {
                CustomQuestsConfigFields customQuestsConfigFields = customQuest.getCustomQuestsConfigFields();
                customQuest.applyEndPermissions(event.getPlayer());
                if (customQuestsConfigFields == null) return;
                List<String> completeDialog = customQuestsConfigFields.getQuestCompleteDialog();
                if (completeDialog != null && !completeDialog.isEmpty())
                    if (!QuestDialogueBossBarManager.consumeRecentlyShownQuestCompleteDialog(event.getPlayer(), customQuest) &&
                            !QuestDialogueBossBarManager.showRawDialogue(event.getPlayer(), customQuest.getQuestName(), completeDialog, null))
                        completeDialog.forEach(event.getPlayer()::sendMessage);
                for (String command : customQuestsConfigFields.getQuestCompleteCommands())
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replace("$player", event.getPlayer().getName())
                                    .replace("$getWorld", event.getPlayer().getWorld().getName())
                                    .replace("$getX", event.getPlayer().getLocation().getX() + "")
                                    .replace("$getY", event.getPlayer().getLocation().getY() + "")
                                    .replace("$getZ", event.getPlayer().getLocation().getZ() + ""));
            }
        }
    }

}
