package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GuildRank {

    private static void initializeGuildRank(UUID uuid) {
        if (!PlayerData.playerSelectedGuildRank.containsKey(uuid)) {
            PlayerData.playerSelectedGuildRank.put(uuid, 1);
            PlayerData.playerSelectedGuildRankChanged = true;
        }
        if (!PlayerData.playerMaxGuildRank.containsKey(uuid)) {
            PlayerData.playerMaxGuildRank.put(uuid, 1);
            PlayerData.playerMaxGuildRankChanged = true;
        }
    }

    /**
     * Returns whether the player's maximum guild rank is below or equal to the rank to be compared.
     *
     * @param player Player whose maximum rank will be checked
     * @param rank   Target guild rank
     * @return Whether the player's guild rank is below or equal to the target
     */
    public static boolean isWithinRank(Player player, int rank) {
        initializeGuildRank(player.getUniqueId());
        return PlayerData.playerMaxGuildRank.get(player.getUniqueId()) >= rank;
    }

    /**
     * Returns the maximum guild rank for the UUID. If none is found, it creates and entry for its default value.
     *
     * @param player Player uuid to check
     * @return Guild rank between 1-20
     */
    public static int getRank(Player player) {
        initializeGuildRank(player.getUniqueId());
        return PlayerData.playerSelectedGuildRank.get(player.getUniqueId());
    }

    /**
     * Sets the maximum guild rank for this player
     *
     * @param player Player for whom the guild rank will be set
     * @param rank   Guild rank to set
     */
    public static void setRank(Player player, int rank) {
        PlayerData.playerMaxGuildRank.put(player.getUniqueId(), rank);
    }

    /**
     * Returns the rank the player currently has active
     *
     * @param player Player whose active rank will be returned
     * @return Guild rank between 1 and 20
     */
    public static int getActiveRank(Player player) {
        initializeGuildRank(player.getUniqueId());
        return PlayerData.playerSelectedGuildRank.get(player.getUniqueId());
    }

    /**
     * Sets the active rank for the player
     *
     * @param player Player for whom the active rank will be set
     * @param rank
     */
    public static void setActiveRank(Player player, int rank) {
        PlayerData.playerSelectedGuildRank.put(player.getUniqueId(), rank);
        PlayerData.playerCurrencyChanged = true;
    }

    /**
     * Checks if the player's active guild rank is below or equal to the target rank.
     *
     * @param player Player whose active guild rank will be checked
     * @param rank   Target rank to check
     * @return Whether the target rank is equal to or higher than the player's active guild rank
     */
    public static boolean isWithinActiveRank(Player player, int rank) {
        initializeGuildRank(player.getUniqueId());
        return getActiveRank(player) >= rank;
    }

    /**
     * Returns the name of the rank as set by the configuration
     *
     * @return Name of the rank
     */
    public static String getRankName(int rank) {

        switch (rank) {
            case 0:
                return AdventurersGuildConfig.rankNames0;
            case 1:
                return AdventurersGuildConfig.rankNames1;
            case 2:
                return AdventurersGuildConfig.rankNames2;
            case 3:
                return AdventurersGuildConfig.rankNames3;
            case 4:
                return AdventurersGuildConfig.rankNames4;
            case 5:
                return AdventurersGuildConfig.rankNames5;
            case 6:
                return AdventurersGuildConfig.rankNames6;
            case 7:
                return AdventurersGuildConfig.rankNames7;
            case 8:
                return AdventurersGuildConfig.rankNames8;
            case 9:
                return AdventurersGuildConfig.rankNames9;
            case 10:
                return AdventurersGuildConfig.rankNames10;
            case 11:
                return AdventurersGuildConfig.rankNames11;
            default:
                Bukkit.getLogger().warning("Warning: Tried to obtain a rank that does no exist. Report this to the dev! Rank: " + rank);
                return "";
        }

    }

}
