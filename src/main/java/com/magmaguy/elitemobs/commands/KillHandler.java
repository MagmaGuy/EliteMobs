package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import com.magmaguy.elitemobs.mobscanner.ValidPassiveMobFilter;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class KillHandler {

    public static void killCommand(CommandSender commandSender, String[] args) {

        if (args.length < 2)
            commandSender.sendMessage("[EliteMobs] Incorrect command syntax. Try /em kill aggressive/passive/mobType [radius]");

        /*
        Run with no radius search
         */
        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("aggressive"))
                killAggressiveMobs(commandSender);
            else if (args[1].equalsIgnoreCase("passive"))
                killPassiveMobs(commandSender);
            else
                killEntityType(commandSender, args);
            return;
        }

        /*
        Run radius search
         */
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("aggressive"))
                radiusKillAggressiveMobs(commandSender, args);
            else if (args[1].equalsIgnoreCase("passive"))
                radiusKillPassiveMobs(commandSender, args);
            else
                radiusKillSpecificMobs(commandSender, args);
        } else
            commandSender.sendMessage("[EliteMobs] Incorrect command syntax. Try /em kill aggressive/passive/mobType [radius]");


    }

    private static void killAggressiveMobs(CommandSender commandSender) {

        if (CommandHandler.permCheck(CommandHandler.KILLALL_AGGRESSIVEELITES, commandSender)) {
            int counter = 0;
            for (World world : EliteMobs.validWorldList)
                for (LivingEntity livingEntity : world.getLivingEntities())
                    if (livingEntity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && ValidAgressiveMobFilter.checkValidAggressiveMob(livingEntity)) {
                        livingEntity.remove();
                        counter++;
                    }

            commandSender.sendMessage("Killed " + counter + " Elite Mobs.");

        }

    }

    private static void killPassiveMobs(CommandSender commandSender) {

        if (CommandHandler.permCheck(CommandHandler.KILLALL_PASSIVEELITES, commandSender)) {
            int counter = 0;
            for (World world : EliteMobs.validWorldList)
                for (LivingEntity livingEntity : world.getLivingEntities())
                    if (livingEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD) && ValidPassiveMobFilter.ValidPassiveMobFilter(livingEntity)) {
                        livingEntity.remove();
                        counter++;
                    }

            commandSender.sendMessage("Killed " + counter + " Elite Mobs.");

        }
    }

    private static void killEntityType(CommandSender commandSender, String[] args) {

        if (CommandHandler.permCheck(CommandHandler.KILLALL_SPECIFICENTITY, commandSender)) {

            try {
                EntityType entityType = EntityType.fromName(args[1].toUpperCase());
                int counter = 0;
                for (World world : EliteMobs.validWorldList)
                    for (LivingEntity livingEntity : world.getLivingEntities())
                        if (livingEntity.getType().equals(entityType) &&
                                (livingEntity.hasMetadata(MetadataHandler.ELITE_MOB_MD) ||
                                        livingEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD))) {
                            livingEntity.remove();
                            counter++;
                        }

                commandSender.sendMessage("Killed " + counter + " Elite " + args[1] + ".");

            } catch (Exception e) {
                commandSender.sendMessage("[EliteMobs] Entity type is not valid!");
            }

        }

    }

    private static void radiusKillAggressiveMobs(CommandSender commandSender, String[] args) {

        if (!CommandHandler.userPermCheck(CommandHandler.KILLALL_AGGRESSIVEELITES, commandSender))
            return;

        try {

            int counter = 0;
            for (Entity entity : ((Player) commandSender).getNearbyEntities(Integer.parseInt(args[2]), Integer.parseInt(args[2]), Integer.parseInt(args[2])))
                if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && ValidAgressiveMobFilter.checkValidAggressiveMob(entity)) {
                    entity.remove();
                    counter++;
                }

            commandSender.sendMessage("Killed " + counter + " Elite Mobs.");

        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] Incorrect command syntax. Try /em kill aggressive/passive/mobType [radius]");
        }

    }

    private static void radiusKillPassiveMobs(CommandSender commandSender, String[] args) {

        if (!CommandHandler.userPermCheck(CommandHandler.KILLALL_PASSIVEELITES, commandSender))
            return;

        try {
            int counter = 0;
            for (Entity entity : ((Player) commandSender).getNearbyEntities(Integer.parseInt(args[2]), Integer.parseInt(args[2]), Integer.parseInt(args[2])))
                if (entity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD) && ValidPassiveMobFilter.ValidPassiveMobFilter(entity)) {
                    entity.remove();
                    counter++;
                }
            commandSender.sendMessage("Killed " + counter + " Elite Mobs.");

        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] Incorrect command syntax. Try /em kill aggressive/passive/mobType [radius]");
        }

    }

    private static void radiusKillSpecificMobs(CommandSender commandSender, String[] args) {

        if (!CommandHandler.userPermCheck(CommandHandler.KILLALL_SPECIFICENTITY, commandSender))
            return;

        try {

            EntityType entityType = EntityType.fromName(args[1].toUpperCase());

            int counter = 0;
            for (Entity entity : ((Player) commandSender).getNearbyEntities(Integer.parseInt(args[2]), Integer.parseInt(args[2]), Integer.parseInt(args[2])))
                if (entity.getType().equals(entityType) &&
                        (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) ||
                                entity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD))) {
                    entity.remove();
                    counter++;
                }
            commandSender.sendMessage("Killed " + counter + " Elite " + args[1] + ".");

        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] Incorrect command syntax. Try /em kill aggressive/passive/mobType [radius]");
        }

    }

}
