package com.magmaguy.elitemobs.thirdparty.placeholderapi;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor() {
        return MetadataHandler.PLUGIN.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier() {
        return "elitemobs";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     * <p>
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion() {
        return MetadataHandler.PLUGIN.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param player     A {@link org.bukkit.OfflinePlayer Player}.
     * @param identifier A String containing the identifier/value.
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (player == null)
            return "Only online players!";

        /*
        There is a weird bug with PAPI where, under certain circumstances, it seems to fail to see cached values and
        queries the database. This is usually fine for something like chat, but it quickly becomes a massive issue for
        tablists, scoreboards and other places that might display several placeholders simultaneously and update them
        every tick, quickly racking up hundreds or thousands of queries per tick, on top of the normal runtime accesses
        that EliteMobs does. At scale, this causes large issues. Hence, at least for now, PAPI does not have access to the
        databases for safety purposes. All of the queries should be in memory regardless.
         */

        switch (identifier) {
            case "player_combat_tier":
                return "" + ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
            case "player_active_guild_rank_numerical":
                return "" + GuildRank.getActiveGuildRank(player, false);
            case "player_maximum_guild_rank_numerical":
                return "" + GuildRank.getMaxGuildRank(player, false);
            case "player_active_guild_rank_name":
                return GuildRank.getRankName(GuildRank.getGuildPrestigeRank(player, false), GuildRank.getActiveGuildRank(player, false));
            case "player_maximum_guild_rank_name":
                return GuildRank.getRankName(GuildRank.getGuildPrestigeRank(player, false), GuildRank.getMaxGuildRank(player, false));
            case "player_prestige_guild_rank_numerical":
                return "" + GuildRank.getGuildPrestigeRank(player, false);
            case "player_money":
                return "" + EconomyHandler.checkCurrency(player.getUniqueId());
            case "player_top_tier":
                double highestThreat = 0;
                for (Player iteratedPlayer : Bukkit.getOnlinePlayers()) {
                    double currentTier = ElitePlayerInventory.playerInventories.get(iteratedPlayer.getUniqueId()).getFullPlayerTier(true);
                    if (currentTier > highestThreat)
                        highestThreat = currentTier;
                }
                return "" + highestThreat;
            case "player_top_guild_rank":
                int highestGuildRank = 0;
                String highestGuildUser = "";
                for (Player iteratedPlayer : Bukkit.getOnlinePlayers()) {
                    int currentGuildRank = GuildRank.getMaxGuildRank(iteratedPlayer, false);
                    if (currentGuildRank > highestGuildRank) {
                        highestGuildRank = currentGuildRank;
                        highestGuildUser = iteratedPlayer.getDisplayName();
                    }
                }
                return highestGuildUser;
            case "player_shortened_guild_rank":
                return AdventurersGuildConfig.getShortenedRankName(GuildRank.getGuildPrestigeRank(player, false), GuildRank.getActiveGuildRank(player, false));
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }

}
