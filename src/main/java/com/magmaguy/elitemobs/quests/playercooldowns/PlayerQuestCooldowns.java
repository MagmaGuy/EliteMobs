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
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.*;

public class PlayerQuestCooldowns implements Serializable {
    private static final long serialVersionUID = -8904960635278676718L;

    @Getter
    private static final HashSet<UUID> bypassedPlayers = new HashSet<>();

    public static void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) flushPlayer(player);
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
        Objects.requireNonNull(player);
        removeActiveQuestState(player);
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns != null) {
            for (QuestCooldown questCooldown : playerQuestCooldowns.getQuestCooldowns()) {
                questCooldown.stop();
                player.removeMetadata(questCooldown.getPermission(), MetadataHandler.PLUGIN);
            }
            playerQuestCooldowns.getQuestCooldowns().clear();
        }
        removeConfiguredQuestMarkers(player);
        PlayerData.resetQuests(player.getUniqueId());
        PlayerData.resetPlayerQuestCooldowns(player.getUniqueId());
        PlayerData.resetQuestLockouts(player.getUniqueId());
    }

    private static void removeActiveQuestState(Player player) {
        QuestTracking questTracking = QuestTracking.getPlayerTrackingQuests().get(player.getUniqueId());
        if (questTracking != null)
            questTracking.stop();

        Quest.releasePlayerSession(player.getUniqueId());
    }

    private static void removeConfiguredQuestMarkers(Player player) {
        for (CustomQuestsConfigFields customQuestsConfigFields : CustomQuestsConfig.getCustomQuests().values()) {
            removeQuestMarker(player, customQuestsConfigFields.getQuestLockoutPermission());
        }
    }

    private static void removeQuestMarker(Player player, String permission) {
        if (permission == null || permission.isEmpty()) return;
        player.removeMetadata(permission, MetadataHandler.PLUGIN);
    }

    public static void resetPlayerQuestCooldown(Player player, CustomQuestsConfigFields customQuestsConfigFields) {
        Objects.requireNonNull(player);
        QuestLockout questLockout = PlayerData.getQuestLockout(player.getUniqueId());
        if (questLockout != null) {
            questLockout.getLockouts().remove(customQuestsConfigFields.getFilename());
            PlayerData.updateQuestLockout(player.getUniqueId(), questLockout);
        }
        String lockoutPermission = customQuestsConfigFields.getQuestLockoutPermission();
        if (lockoutPermission == null || lockoutPermission.isEmpty()) return;
        removeQuestMarker(player, lockoutPermission);
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) return;
        Iterator<QuestCooldown> questCooldownIterator = playerQuestCooldowns.getQuestCooldowns().iterator();
        while (questCooldownIterator.hasNext()) {
            QuestCooldown questCooldown = questCooldownIterator.next();
            if (questCooldown.getPermission().equals(lockoutPermission)) {
                questCooldown.stop();
                questCooldownIterator.remove();
            }
        }
        PlayerData.updatePlayerQuestCooldowns(player.getUniqueId(), playerQuestCooldowns);
    }

    public static void addCooldown(Player player, String permission, int delayInMinutes) {
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) {
            playerQuestCooldowns = new PlayerQuestCooldowns();
            Logger.warn("For some reason the player cooldowns failed to read, warn the dev!", true);
        }
        playerQuestCooldowns.pruneExpired(player);
        QuestCooldown cooldown = new QuestCooldown(delayInMinutes, permission, player.getUniqueId());
        playerQuestCooldowns.questCooldowns.add(cooldown);
        PlayerData.updatePlayerQuestCooldowns(player.getUniqueId(), playerQuestCooldowns);
        cooldown.startCooldown(player.getUniqueId());
    }

    public static void flushPlayer(Player player) {
        if (!PlayerData.isInMemory(player)) return;
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) return;
        for (QuestCooldown questCooldown : playerQuestCooldowns.questCooldowns) {
            questCooldown.stop();
            removeQuestMarker(player, questCooldown.getPermission());
        }
    }

    public void startCooldowns(UUID player) {
        Player session = Bukkit.getPlayer(player);
        if (session == null) return;
        if (pruneExpired(session)) PlayerData.updatePlayerQuestCooldowns(player, this);
        for (QuestCooldown questCooldown : questCooldowns)
            questCooldown.startCooldown(player);
    }

    void expire(QuestCooldown cooldown, Player player) {
        cooldown.stop();
        if (!questCooldowns.remove(cooldown)) return;
        removeUnownedMarker(player, cooldown.getPermission());
        PlayerData.updatePlayerQuestCooldowns(player.getUniqueId(), this);
    }

    private boolean pruneExpired(Player player) {
        Set<String> expiredPermissions = new HashSet<>();
        questCooldowns.removeIf(cooldown -> {
            if (!cooldown.isExpired()) return false;
            cooldown.stop();
            expiredPermissions.add(cooldown.getPermission());
            return true;
        });
        expiredPermissions.forEach(permission -> removeUnownedMarker(player, permission));
        return !expiredPermissions.isEmpty();
    }

    private void removeUnownedMarker(Player player, String permission) {
        if (questCooldowns.stream().noneMatch(cooldown -> cooldown.getPermission().equals(permission)
                && !cooldown.isExpired())) removeQuestMarker(player, permission);
    }

}
