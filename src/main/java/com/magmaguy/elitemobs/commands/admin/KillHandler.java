package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class KillHandler {

    public static void killAggressiveMobs(CommandSender commandSender) {
        int counter = 0;
        Iterator<EliteMobEntity> eliteMobEntityIterator = EntityTracker.getEliteMobs().values().iterator();
        while (eliteMobEntityIterator.hasNext()) {
            EliteMobEntity eliteMobEntity = eliteMobEntityIterator.next();
            EntityTracker.unregister(eliteMobEntity.uuid, RemovalReason.KILL_COMMAND);
            eliteMobEntityIterator.remove();
            counter++;
        }
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Elite Mobs."));
    }


    public static void killPassiveMobs(CommandSender commandSender) {
        int counter = 0;
        Iterator<LivingEntity> eliteMobEntityIterator = EntityTracker.getSuperMobs().values().iterator();
        while (eliteMobEntityIterator.hasNext()) {
            LivingEntity superMobEntity = eliteMobEntityIterator.next();
            EntityTracker.unregister(superMobEntity.getUniqueId(), RemovalReason.KILL_COMMAND);
            eliteMobEntityIterator.remove();
            counter++;
        }
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Super Mobs."));
    }

    public static void killEntityType(CommandSender commandSender, EntityType entityType) {
        if (EliteMobProperties.getValidMobTypes().contains(entityType)) {
            int counter = 0;
            Iterator<EliteMobEntity> eliteMobEntityIterator = EntityTracker.getEliteMobs().values().iterator();
            while (eliteMobEntityIterator.hasNext()) {
                EliteMobEntity eliteMobEntity = eliteMobEntityIterator.next();
                if (!eliteMobEntity.getLivingEntity().getType().equals(entityType)) continue;
                EntityTracker.unregister(eliteMobEntity.uuid, RemovalReason.KILL_COMMAND);
                eliteMobEntityIterator.remove();
                counter++;
            }
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Elite " + entityType.toString() + "."));
        } else if (SuperMobProperties.superMobTypeList.contains(entityType)) {
            int counter = 0;
            Iterator<LivingEntity> eliteMobEntityIterator = EntityTracker.getSuperMobs().values().iterator();
            while (eliteMobEntityIterator.hasNext()) {
                LivingEntity superMobEntity = eliteMobEntityIterator.next();
                if (!superMobEntity.getType().equals(entityType)) continue;
                EntityTracker.unregister(superMobEntity.getUniqueId(), RemovalReason.KILL_COMMAND);
                eliteMobEntityIterator.remove();
                counter++;
            }
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Super " + entityType.toString() + "."));
        } else
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cNot a valid entity type for EliteMobs!"));
    }

    public static void radiusKillAggressiveMobs(Player player, int radius) {
        int counter = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius))
            if (EntityTracker.isEliteMob(entity)) {
                EntityTracker.unregister(entity, RemovalReason.KILL_COMMAND);
                counter++;
            }
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Elite Mobs."));
    }

    public static void radiusKillPassiveMobs(Player player, int radius) {
        int counter = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius))
            if (EntityTracker.isSuperMob(entity)) {
                entity.remove();
                counter++;
            }
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Super Mobs."));
    }

    public static void radiusKillSpecificMobs(Player player, EntityType entityType, int radius) {
        int counter = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius))
            if (entity.getType().equals(entityType) &&
                    (EntityTracker.isEliteMob(entity) ||
                            EntityTracker.isSuperMob(entity))) {
                EntityTracker.unregister(entity, RemovalReason.KILL_COMMAND);
                counter++;
            }
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Super Mobs."));
    }

}
