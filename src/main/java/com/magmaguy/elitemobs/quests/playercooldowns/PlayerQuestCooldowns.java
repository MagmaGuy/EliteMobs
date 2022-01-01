package com.magmaguy.elitemobs.quests.playercooldowns;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerQuestCooldowns implements Serializable {

    public static PlayerQuestCooldowns initializePlayer() {
        return new PlayerQuestCooldowns();
    }

    @Getter
    private final List<QuestCooldown> questCooldowns = new ArrayList<>();

    /**
     * Initializes cooldowns from scratch, assuming no preexisting player data
     */
    public PlayerQuestCooldowns() {
        //This just initializes the cooldown list
    }

    public static void addCooldown(Player player, String permission, int delayInMinutes) {
        PlayerQuestCooldowns playerQuestCooldowns = PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
        if (playerQuestCooldowns == null) {
            playerQuestCooldowns = new PlayerQuestCooldowns();
            new WarningMessage("For some reason the player cooldowns failed to read, warn the dev!", true);
        }
        playerQuestCooldowns.questCooldowns.add(new QuestCooldown(delayInMinutes, permission, player.getUniqueId()));
        PlayerData.updatePlayerQuestCooldowns(player.getUniqueId(), playerQuestCooldowns);
    }

    public static void flushPlayer(Player player) {
        PlayerQuestCooldowns playerQuestCooldowns =  PlayerData.getPlayerQuestCooldowns(player.getUniqueId());
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
