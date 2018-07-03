package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.playerdata.PlayerData;

import java.util.UUID;

public class GuildRank {

    public static int getGuildRank (UUID uuid) {

        if (PlayerData.playerSelectedGuildRank.containsKey(uuid))
            return PlayerData.playerSelectedGuildRank.get(uuid);

        PlayerData.playerSelectedGuildRank.put(uuid, 10);

        return PlayerData.playerSelectedGuildRank.get(uuid);

    }

}
