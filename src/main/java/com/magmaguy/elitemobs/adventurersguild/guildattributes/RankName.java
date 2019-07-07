package com.magmaguy.elitemobs.adventurersguild.guildattributes;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;

public class RankName {

    public static String getNameFromRank(int rank) {
        if (rank == 0)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_0));
        if (rank == 1)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_1));
        if (rank == 2)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_2));
        if (rank == 3)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_3));
        if (rank == 4)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_4));
        if (rank == 5)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_5));
        if (rank == 6)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_6));
        if (rank == 7)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_7));
        if (rank == 8)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_8));
        if (rank == 9)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_9));
        if (rank == 10)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_10));
        if (rank == 11)
            return ChatColorConverter.convert(AdventurersGuildConfig.getString(AdventurersGuildConfig.RANK_NAMES_11));
        return null;
    }

}
