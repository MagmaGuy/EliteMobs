package com.magmaguy.elitemobs.quests.playercooldowns;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestLockout;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.Serializable;
import java.util.*;

public class PlayerQuestCooldowns implements Serializable {

    @Getter
    private static final HashSet<UUID> bypassedPlayers = new HashSet<>();

    public static void shutdown() {
        bypassedPlayers.clear();
    }
    @Getter
    private final List<QuestCooldown> questCooldowns = new ArrayList<>();

    /**
     * Initializes cooldowns from scratch, assuming no preexisting player data
     */
    public PlayerQuestCooldowns() {
        //This just initializes the cooldown list
    }

    public static void toggleBypass(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!bypassedPlayers.contains(playerUUID))
            bypassedPlayers.add(playerUUID);
        else
            bypassedPlayers.remove(playerUUID);
    }

    public static boolean bypassesQuestRestrictions(Player player) {
        return bypassedPlayers.contains(player.getUniqueId());
    }

    public static PlayerQuestCooldowns initializePlayer() {
        return new PlayerQuestCooldowns();
    }

    public static void resetPlayerQuests(Player player) {
        PermissionAttachment permissionAttachment = Objects.requireNonNull(player).addAttachment(MetadataHandler.PLUGIN);
        removeActiveQuestState(player, permissionAttachment);
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns != null) {
            for (QuestCooldown questCooldown : playerQuestCooldowns.getQuestCooldowns()) {
                if (questCooldown.getBukkitTask() != null)
                    questCooldown.getBukkitTask().cancel();
                permissionAttachment.setPermission(questCooldown.getPermission(), false);
                player.removeMetadata(questCooldown.getPermission(), MetadataHandler.PLUGIN);
            }
            playerQuestCooldowns.getQuestCooldowns().clear();
        }
        removeConfiguredQuestMarkers(player, permissionAttachment);
        PlayerData.resetQuests(player.getUniqueId());
        PlayerData.resetPlayerQuestCooldowns(player.getUniqueId());
        PlayerData.resetQuestLockouts(player.getUniqueId());
    }

    private static void removeActiveQuestState(Player player, PermissionAttachment permissionAttachment) {
        QuestTracking questTracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
        if (questTracking != null)
            questTracking.stop();

        for (Quest quest : new ArrayList<>(PlayerData.getQuests(player.getUniqueId()))) {
            if (!(quest instanceof CustomQuest customQuest)) continue;
            CustomQuestsConfigFields customQuestsConfigFields = customQuest.getCustomQuestsConfigFields();
            if (customQuestsConfigFields == null) continue;
            for (String permission : customQuestsConfigFields.getTemporaryPermissions())
                permissionAttachment.setPermission(permission, false);
        }
    }

    private static void removeConfiguredQuestMarkers(Player player, PermissionAttachment permissionAttachment) {
        for (CustomQuestsConfigFields customQuestsConfigFields : CustomQuestsConfig.getCustomQuests().values()) {
            removeQuestMarker(player, permissionAttachment, customQuestsConfigFields.getQuestLockoutPermission());
            for (String permission : customQuestsConfigFields.getTemporaryPermissions())
                removeQuestMarker(player, permissionAttachment, permission);
        }
    }

    private static void removeQuestMarker(Player player, PermissionAttachment permissionAttachment, String permission) {
        if (permission == null || permission.isEmpty()) return;
        permissionAttachment.setPermission(permission, false);
        player.removeMetadata(permission, MetadataHandler.PLUGIN);
    }

    public static void resetPlayerQuestCooldown(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
        PermissionAttachment permissionAttachment = Objects.requireNonNull(player).addAttachment(MetadataHandler.PLUGIN);
        QuestLockout questLockout = PlayerData.getQuestLockout(player.getUniqueId());
        if (questLockout != null) {
            questLockout.getLockouts().remove(customQuestsConfigFields.getFilename());
            PlayerData.updateQuestLockout(player.getUniqueId(), questLockout);
        }
        String lockoutPermission = customQuestsConfigFields.getQuestLockoutPermission();
        if (lockoutPermission == null || lockoutPermission.isEmpty()) return;
        removeQuestMarker(player, permissionAttachment, lockoutPermission);
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) return;
        Iterator<QuestCooldown> questCooldownIterator = playerQuestCooldowns.getQuestCooldowns().iterator();
        while (questCooldownIterator.hasNext()) {
            QuestCooldown questCooldown = questCooldownIterator.next();
            if (questCooldown.getPermission().equals(lockoutPermission)) {
                if (questCooldown.getBukkitTask() != null)
                    questCooldown.getBukkitTask().cancel();
                permissionAttachment.setPermission(questCooldown.getPermission(), false);
                player.removeMetadata(questCooldown.getPermission(), MetadataHandler.PLUGIN);
                questCooldownIterator.remove();
                PlayerData.updatePlayerQuestCooldowns(player.getUniqueId(), playerQuestCooldowns);
                return;
            }
        }
    }

    public static void addCooldown(Player player, String permission, int delayInMinutes) {
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) {
            playerQuestCooldowns = new PlayerQuestCooldowns();
            Logger.warn("For some reason the player cooldowns failed to read, warn the dev!", true);
        }
        playerQuestCooldowns.questCooldowns.add(new QuestCooldown(delayInMinutes, permission, player.getUniqueId()));
        PlayerData.updatePlayerQuestCooldowns(player.getUniqueId(), playerQuestCooldowns);
    }

    public static void flushPlayer(Player player) {
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) return;
        for (QuestCooldown questCooldown : Objects.requireNonNull(PlayerData.getPlayerQuestCooldowns(player.getUniqueId())).questCooldowns)
            if (questCooldown.getBukkitTask() != null)
                questCooldown.getBukkitTask().cancel();
    }

    public void startCooldowns(UUID player) {
        for (QuestCooldown questCooldown : questCooldowns)
            questCooldown.startCooldown(player);
    }

}
