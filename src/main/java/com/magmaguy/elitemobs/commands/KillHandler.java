package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Iterator;

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

            Iterator<EliteMobEntity> eliteMobEntityIterator = EntityTracker.getEliteMobs().values().iterator();

            while (eliteMobEntityIterator.hasNext()) {
                EliteMobEntity eliteMobEntity = eliteMobEntityIterator.next();
                eliteMobEntity.getLivingEntity().remove();
                eliteMobEntityIterator.remove();
                counter++;
            }

            commandSender.sendMessage("Killed " + counter + " Elite Mobs.");

        }

    }

    private static void killPassiveMobs(CommandSender commandSender) {

        if (CommandHandler.permCheck(CommandHandler.KILLALL_PASSIVEELITES, commandSender)) {
            int counter = 0;
            for (World world : EliteMobs.validWorldList)
                for (Iterator<LivingEntity> livingEntityIterator = world.getLivingEntities().iterator(); livingEntityIterator.hasNext(); ) {
                    LivingEntity livingEntity = livingEntityIterator.next();
                    if (EntityTracker.isSuperMob(livingEntity)) {
                        EntityTracker.registerSuperMob(livingEntity);
                        livingEntity.remove();
                        counter++;
                    }
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
                                (EntityTracker.isEliteMob(livingEntity) ||
                                        EntityTracker.isSuperMob(livingEntity))) {
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
                if (EntityTracker.isEliteMob(entity)) {
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
                if (EntityTracker.isSuperMob(entity)) {
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
                        (EntityTracker.isEliteMob(entity) ||
                                EntityTracker.isSuperMob(entity))) {
                    entity.remove();
                    counter++;
                }
            commandSender.sendMessage("Killed " + counter + " Elite " + args[1] + ".");

        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] Incorrect command syntax. Try /em kill aggressive/passive/mobType [radius]");
        }

    }

}
