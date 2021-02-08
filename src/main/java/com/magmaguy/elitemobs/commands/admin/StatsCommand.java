package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.entitytracker.TrackedEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.Round;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class StatsCommand {

    public static void statsHandler(CommandSender commandSender) {

        int aggressiveCount = 0;
        int passiveCount = 0;

        HashMap<EntityType, Integer> entitiesCounted = new HashMap<>();

        for (World world : EliteMobs.validWorldList)
            for (LivingEntity livingEntity : world.getLivingEntities())
                if (EntityTracker.isEliteMob(livingEntity)) {
                    aggressiveCount++;
                    if (!entitiesCounted.containsKey(livingEntity.getType()))
                        entitiesCounted.put(livingEntity.getType(), 1);
                    else
                        entitiesCounted.put(livingEntity.getType(), entitiesCounted.get(livingEntity.getType()) + 1);
                } else if (EntityTracker.isSuperMob(livingEntity)) {
                    passiveCount++;
                    if (!entitiesCounted.containsKey(livingEntity.getType()))
                        entitiesCounted.put(livingEntity.getType(), 1);
                    else
                        entitiesCounted.put(livingEntity.getType(), entitiesCounted.get(livingEntity.getType()) + 1);
                }

        StringBuilder breakdownString = new StringBuilder("&2Breakdown: &a");

        for (EntityType entityType : entitiesCounted.keySet()) {
            breakdownString.append(entitiesCounted.get(entityType)).append(" ").append(entityType).append("&2, &a");
        }

        double highestThreat = 0;
        String highestThreatUser = "";
        double threatAverage = 0;
        int highestGuildRank = 0;
        String highestGuildUser = "";
        double guildRankAverage = 0;
        Player highestGuildRankPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            double currentTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
            threatAverage += currentTier;
            int currentGuildRank = GuildRank.getActiveGuildRank(player);
            guildRankAverage += currentGuildRank;
            if (currentTier > highestThreat) {
                highestThreat = currentTier;
                highestThreatUser = player.getDisplayName();
            }
            if (currentGuildRank > highestGuildRank) {
                highestGuildRankPlayer = player;
                highestGuildRank = currentGuildRank;
                highestGuildUser = player.getDisplayName();
            }
        }
        threatAverage /= Bukkit.getOnlinePlayers().size();
        guildRankAverage /= Bukkit.getOnlinePlayers().size();

        commandSender.sendMessage(ChatColorConverter.convert(
                "§5§m-----------------------------------------------------"));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] §a§lEliteMobs v. " + Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion() + " stats:"));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2There are currently §l§6" + (aggressiveCount + passiveCount) + " §f§2EliteMobs mobs entities in the world, of which &a"
                        + aggressiveCount + " &2are Elite Mobs and &a" + passiveCount + " &2are Super Mobs."));
        commandSender.sendMessage(ChatColorConverter.convert(breakdownString.toString()));
        if (highestGuildRankPlayer == null) return;
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2Highest online threat tier: &a" + highestThreatUser + " &2at total threat tier &a" + highestThreat));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2Average threat tier: &a" + Round.twoDecimalPlaces(threatAverage)));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2Highest adventurer's guild tier: &a" + highestGuildUser + " &2at guild rank &a" + GuildRank.getRankName(GuildRank.getGuildPrestigeRank(highestGuildRankPlayer), highestGuildRank) + " &2(&a" + highestGuildRank + "&2)"));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2Average guild rank: &a" + Round.twoDecimalPlaces(guildRankAverage)));
        commandSender.sendMessage(ChatColorConverter.convert(
                "§5§m-----------------------------------------------------"));
        commandSender.sendMessage("Tracked entity count: " + TrackedEntity.trackedEntities.size());
    }

}
