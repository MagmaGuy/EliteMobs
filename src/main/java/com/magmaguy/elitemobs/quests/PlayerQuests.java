package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerQuests implements Serializable {

    public static PlayerQuests getData(Player player) {
        return PlayerData.getQuestStatus(player.getUniqueId());
    }

    public static void addQuest(Player player, EliteQuest eliteQuest) {
        EliteQuest.addPlayerInQuests(player, eliteQuest);
        if (getData(player).quests != null) {
            getData(player).quests.add(eliteQuest);
            //todo check if this modifies it or is a deep copy
        } else {
            ArrayList<EliteQuest> quests = new ArrayList<>();
            quests.add(eliteQuest);
            getData(player).quests = quests;
        }
        //todo: updateDatabase(player, getData(player));
    }

    public static void removeQuest(Player player, UUID uuid) {
        PlayerData.removeQuest(player.getUniqueId(), uuid);
    }

    public static boolean hasQuest(Player player, EliteQuest eliteQuest) {
        return getData(player).hasQuest(eliteQuest.getUuid());
    }

    public static void updateDatabase(Player player, PlayerQuests playerQuests) {
        PlayerData.setQuestStatus(player.getUniqueId(), playerQuests);
    }

    public Player player;
    public ArrayList<EliteQuest> quests = new ArrayList<>();

    public PlayerQuests(Player player) {
        this.player = player;
    }

    public boolean hasQuest(UUID uuid) {
        if (quests != null)
            for (EliteQuest eliteQuest : quests)
                if (eliteQuest.getUuid().equals(uuid))
                    return true;
        return false;
    }

}
