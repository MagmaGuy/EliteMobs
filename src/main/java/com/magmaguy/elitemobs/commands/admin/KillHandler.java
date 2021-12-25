package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class KillHandler {

    //TODO: FIX NPE ISSUE YOU FUCKING MORON

    public static void killAggressiveMobs(CommandSender commandSender) {
        int counter = 0;
        for (EliteEntity eliteEntity : new ArrayList<>(EntityTracker.getEliteMobEntities().values())) {
            eliteEntity.remove(RemovalReason.REMOVE_COMMAND);
            counter++;
        }
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Elite Mobs."));
    }


    public static void killPassiveMobs(CommandSender commandSender) {
        int counter = 0;
        for (LivingEntity superMobEntity : EntityTracker.getSuperMobs()) {
            superMobEntity.remove();
            counter++;
        }
        commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Super Mobs."));
    }

    public static void killEntityType(CommandSender commandSender, EntityType entityType) {
        if (EliteMobProperties.getValidMobTypes().contains(entityType)) {
            int counter = 0;
            for (EliteEntity eliteEntity : EntityTracker.getEliteMobEntities().values()) {
                if (!eliteEntity.getLivingEntity().getType().equals(entityType)) continue;
                eliteEntity.remove(RemovalReason.REMOVE_COMMAND);
                counter++;
            }
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Elite " + entityType.toString() + "."));
        } else if (SuperMobProperties.superMobTypeList.contains(entityType)) {
            int counter = 0;
            for (LivingEntity superMobEntity : EntityTracker.getSuperMobs()) {
                if (!superMobEntity.getType().equals(entityType)) continue;
                superMobEntity.remove();
                counter++;
            }
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Super " + entityType.toString() + "."));
        } else
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cNot a valid entity type for EliteMobs!"));
    }

    public static void radiusKillAggressiveMobs(Player player, int radius) {
        int counter = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
            if (eliteEntity != null)
                eliteEntity.remove(RemovalReason.REMOVE_COMMAND);
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
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity.getType().equals(entityType) && (EntityTracker.isEliteMob(entity))) {
                ((EliteEntity) entity).remove(RemovalReason.KILL_COMMAND);
                counter++;
            } else if (entity.getType().equals(entityType) && EntityTracker.isSuperMob(entity)) {
                EntityTracker.unregister(entity, RemovalReason.KILL_COMMAND);
                counter++;
            }
        }
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Killed " + counter + " Elite Mobs."));
    }

}
