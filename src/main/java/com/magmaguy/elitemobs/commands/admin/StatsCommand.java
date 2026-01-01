package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
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

        StringBuilder breakdownString = new StringBuilder("&2Breakdown: &a");

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

        commandSender.sendMessage(ChatColorConverter.convert(
                "§5§m-----------------------------------------------------"));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] §a§lEliteMobs v. " + Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion() + " stats:"));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2There are currently §l§6" + (aggressiveCount + passiveCount) + " §f§2EliteMobs mobs entities in the world, of which &a"
                        + aggressiveCount + " &2are Elite Mobs and &a" + passiveCount + " &2are Super Mobs."));
        commandSender.sendMessage(ChatColorConverter.convert(breakdownString.toString()));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2Highest online threat tier: &a" + highestThreatUser + " &2at total threat tier &a" + highestThreat));
        commandSender.sendMessage(ChatColorConverter.convert(
                "&7[EM] &2Average threat tier: &a" + Round.twoDecimalPlaces(threatAverage)));
        // Guild rank stats removed
        commandSender.sendMessage(ChatColorConverter.convert(
                "§5§m-----------------------------------------------------"));
        commandSender.sendMessage("Tracked boss count: " + EntityTracker.getEliteMobEntities().size());
        commandSender.sendMessage("Tracked NPC count: " + EntityTracker.getNpcEntities().size());
        int loadedCounter = 0;
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntities())
            if (regionalBossEntity.isValid())
                loadedCounter++;
        commandSender.sendMessage(ChatColorConverter.convert("&7[EM] &2There are &c" + RegionalBossEntity.getRegionalBossEntities().size() + " &2Regional Bosses installed, of which &c" + loadedCounter + " &2are currently loaded."));
    }

}
