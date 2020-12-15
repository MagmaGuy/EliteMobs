package com.magmaguy.elitemobs.adventurersguild;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.utils.Round;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GuildRank {

    //todo: redo documentation here

    public static void setGuildPrestigeRank(Player player, int prestigeRank) {
        PlayerData.setGuildPrestigeLevel(player.getUniqueId(), prestigeRank);
    }

    public static int getGuildPrestigeRank(Player player) {
        return PlayerData.getGuildPrestigeLevel(player.getUniqueId());
    }

    public static int getGuildPrestigeRank(Player player, boolean databaseAccess) {
        return PlayerData.getGuildPrestigeLevel(player.getUniqueId(), databaseAccess);
    }

    public static void setMaxGuildRank(Player player, int maxGuildRank) {
        PlayerData.setMaxGuildLevel(player.getUniqueId(), maxGuildRank);
    }

    public static int getMaxGuildRank(Player player) {
        return PlayerData.getMaxGuildLevel(player.getUniqueId());
    }

    public static int getMaxGuildRank(Player player, boolean databaseAccess) {
        return PlayerData.getMaxGuildLevel(player.getUniqueId(), databaseAccess);
    }

    public static void setActiveGuildRank(Player player, int activeGuildRank) {
        PlayerData.setActiveGuildLevel(player.getUniqueId(), activeGuildRank);
        setMaxHealth(player, activeGuildRank, getGuildPrestigeRank(player));
    }

    public static int getActiveGuildRank(Player player) {
        return PlayerData.getActiveGuildLevel(player.getUniqueId());
    }

    public static int getActiveGuildRank(Player player, boolean databaseAccess) {
        return PlayerData.getActiveGuildLevel(player.getUniqueId(), databaseAccess);
    }

    /**
     * Returns whether the player's maximum guild rank is below or equal to the rank to be compared.
     *
     * @param player        Player whose maximum rank will be checked
     * @param maxGuildLevel Target guild rank
     * @return Whether the player's guild rank is below or equal to the target
     */
    public static boolean isWithinMaxGuildRank(Player player, int prestigeLevel, int maxGuildLevel) {
        return getGuildPrestigeRank(player) >= prestigeLevel &&
                getMaxGuildRank(player) >= maxGuildLevel;
    }

    public static boolean isWithinMaxGuildRankIgnorePrestige(Player player, int maxGuildLevel) {
        return getMaxGuildRank(player) >= maxGuildLevel;
    }


    /**
     * Checks if the player's active guild rank is below or equal to the target rank.
     *
     * @param player           Player whose active guild rank will be checked
     * @param activeGuildLevel Target rank to check
     * @return Whether the target rank is equal to or higher than the player's active guild rank
     */
    public static boolean isWithinActiveGuildRank(Player player, int prestigeLevel, int activeGuildLevel) {
        return getGuildPrestigeRank(player) >= prestigeLevel &&
                getActiveGuildRank(player) >= activeGuildLevel;
    }

    public static boolean isWithinActiveGuildRankIgnorePrestige(Player player, int activeGuildLevel) {
        return getActiveGuildRank(player) >= activeGuildLevel;
    }

    /**
     * Returns the name of the rank as set by the configuration
     *
     * @return Name of the rank
     */
    public static String getRankName(int prestige, int rank) {
        return AdventurersGuildConfig.getRankName(prestige, rank);
    }

    public static void setMaxHealth(Player player, int activeGuildRank, int prestigeRank) {
        if (!AdventurersGuildConfig.enableAdventurersGuild || !AdventurersGuildConfig.addMaxHealth) return;
        double guildRankBonus = healthBonusValue(prestigeRank, activeGuildRank);
        double newMaxHealth = 20 + guildRankBonus;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
    }

    public static double lootTierValue(int activeGuildRank) {
        return activeGuildRank * 10;
    }

    public static double currencyBonusMultiplier(int prestigeLevel) {
        return 1 + prestigeLevel * 5;
    }

    /**
     * Returns the per-tier max health, taking prestige ranks into account
     *
     * @param prestigeLevel
     * @param guildRank
     * @return
     */
    public static double healthBonusValue(int prestigeLevel, int guildRank) {
        if (!AdventurersGuildConfig.addMaxHealth) return 0;
        double prestigeBonus = 0;
        switch (prestigeLevel) {
            case 0:
                prestigeBonus = 0;
                break;
            case 1:
            case 2:
            case 3:
                prestigeBonus = AdventurersGuildConfig.health1;
                break;
            case 4:
            case 5:
            case 6:
                prestigeBonus = AdventurersGuildConfig.health2;
                break;
            case 7:
            case 8:
            case 9:
                prestigeBonus = AdventurersGuildConfig.health3;
                break;
            case 10:
                prestigeBonus = AdventurersGuildConfig.health4;
                break;
        }
        return prestigeBonus * guildRank;
    }

    /**
     * Returns the per-tier dodge multiplier, taking prestige ranks into account
     *
     * @param prestigeLevel
     * @param guildRank
     * @return
     */
    public static double dodgeBonusValue(int prestigeLevel, int guildRank) {
        if (!AdventurersGuildConfig.addDodge) return 0;
        double prestigeBonus = 0;
        switch (prestigeLevel) {
            case 0:
            case 1:
            case 2:
                prestigeBonus = 0;
                break;
            case 3:
            case 4:
            case 5:
                prestigeBonus = AdventurersGuildConfig.dodge1;
                break;
            case 6:
            case 7:
            case 8:
                prestigeBonus = AdventurersGuildConfig.dodge2;
                break;
            case 9:
            case 10:
                prestigeBonus = AdventurersGuildConfig.dodge3;
                break;
        }
        return Round.twoDecimalPlaces(guildRank / (10D + prestigeLevel) * prestigeBonus);
    }

    /**
     * Returns the per-tier crit multiplier, taking prestige ranks into account
     *
     * @param prestigeLevel
     * @param guildRank
     * @return
     */
    public static double critBonusValue(int prestigeLevel, int guildRank) {
        if (!AdventurersGuildConfig.addCrit) return 0;
        double prestigeBonus = 0;
        switch (prestigeLevel) {
            case 0:
            case 1:
                prestigeBonus = 0;
                break;
            case 2:
            case 3:
            case 4:
                prestigeBonus = AdventurersGuildConfig.crit1;
                break;
            case 5:
            case 6:
            case 7:
                prestigeBonus = AdventurersGuildConfig.crit2;
                break;
            case 8:
            case 9:
            case 10:
                prestigeBonus = AdventurersGuildConfig.crit3;
                break;
        }
        return Round.twoDecimalPlaces(guildRank / (10D + prestigeLevel) * prestigeBonus);
    }

    public static class GuildRankEvents implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getPlayer().isOnline())
                        setMaxHealth(event.getPlayer(),
                                GuildRank.getActiveGuildRank(event.getPlayer(), true),
                                GuildRank.getGuildPrestigeRank(event.getPlayer(), true));
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);
        }
    }

}
