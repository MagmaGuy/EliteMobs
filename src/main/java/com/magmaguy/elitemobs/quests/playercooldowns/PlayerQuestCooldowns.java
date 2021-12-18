package com.magmaguy.elitemobs.quests.playercooldowns;

import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerQuestCooldowns implements Serializable {

    private static final HashMap<UUID, PlayerQuestCooldowns> cooldowns = new HashMap<>();
    private final List<QuestCooldown> questCooldowns = new ArrayList<>();

    /**
     * Initializes cooldowns from scratch, assuming no preexisting player data
     */
    public PlayerQuestCooldowns() {
    }

    /**
     * Initializes cooldowns based on previously stored data
     *
     * @param questCooldown Previously stored data
     */
    public PlayerQuestCooldowns(QuestCooldown questCooldown) {
        this.questCooldowns.add(questCooldown);
    }

    public static PlayerQuestCooldowns initializePlayer() {
        return new PlayerQuestCooldowns();
    }

    public static void addCooldown(Player player, String permission, int delayInMinutes) {
        if (!cooldowns.containsKey(player.getUniqueId()))
            cooldowns.put(player.getUniqueId(), new PlayerQuestCooldowns(new QuestCooldown(delayInMinutes, permission, player.getUniqueId())));
        else
            cooldowns.get(player.getUniqueId()).questCooldowns.add(new QuestCooldown(delayInMinutes, permission, player.getUniqueId()));
    }

    public static void initializePlayer(UUID player, PlayerQuestCooldowns questCooldowns) {
        cooldowns.put(player, questCooldowns);
        questCooldowns.startCooldowns(player);
    }

    public static void flushPlayer(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return;
        for (QuestCooldown questCooldown : cooldowns.get(player.getUniqueId()).questCooldowns)
            questCooldown.getBukkitTask().cancel();
    }

    public void startCooldowns(UUID player) {
        for (QuestCooldown questCooldown : questCooldowns)
            questCooldown.startCooldown(player);
    }

}
