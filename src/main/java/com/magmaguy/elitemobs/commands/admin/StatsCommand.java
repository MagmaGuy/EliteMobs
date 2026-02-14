package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Round;
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
                }

        StringBuilder breakdownString = new StringBuilder(CommandMessagesConfig.getStatsBreakdownPrefix());

        for (EntityType entityType : entitiesCounted.keySet()) {
            breakdownString.append(entitiesCounted.get(entityType)).append(" ").append(entityType).append("&2, &a");
        }

        double highestThreat = 0;
        String highestThreatUser = "";
        double threatAverage = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            double currentTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(true);
            threatAverage += currentTier;
            if (currentTier > highestThreat) {
                highestThreat = currentTier;
                highestThreatUser = player.getDisplayName();
            }
        }
        threatAverage /= Bukkit.getOnlinePlayers().size();

        commandSender.sendMessage(CommandMessagesConfig.getStatsSeparator());
        commandSender.sendMessage(
                CommandMessagesConfig.getStatsVersionHeader()
                        .replace("$version", Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion()));
        commandSender.sendMessage(
                CommandMessagesConfig.getStatsEntityCountMessage()
                        .replace("$total", String.valueOf(aggressiveCount + passiveCount))
                        .replace("$aggressive", String.valueOf(aggressiveCount))
                        .replace("$passive", String.valueOf(passiveCount)));
        commandSender.sendMessage(ChatColorConverter.convert(breakdownString.toString()));
        commandSender.sendMessage(
                CommandMessagesConfig.getStatsHighestThreatMessage()
                        .replace("$player", highestThreatUser)
                        .replace("$threat", String.valueOf(highestThreat)));
        commandSender.sendMessage(
                CommandMessagesConfig.getStatsAverageThreatMessage()
                        .replace("$average", String.valueOf(Round.twoDecimalPlaces(threatAverage))));
        // Guild rank stats removed
        commandSender.sendMessage(CommandMessagesConfig.getStatsSeparator());
        commandSender.sendMessage(CommandMessagesConfig.getTrackedBossCountMessage() + EntityTracker.getEliteMobEntities().size());
        commandSender.sendMessage(CommandMessagesConfig.getTrackedNpcCountMessage() + EntityTracker.getNpcEntities().size());
        int loadedCounter = 0;
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntities())
            if (regionalBossEntity.isValid())
                loadedCounter++;
        commandSender.sendMessage(CommandMessagesConfig.getRegionalBossStatsMessage().replace("$total", String.valueOf(RegionalBossEntity.getRegionalBossEntities().size())).replace("$loaded", String.valueOf(loadedCounter)));
    }

}
