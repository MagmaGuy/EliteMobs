package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.SuperMobConstructor;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.powers.ElitePower;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Optional;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class SpawnCommand {

    public static void spawnEliteEntityTypeCommand(Player player, EntityType entityType, Integer level, String[] powers) {
        LivingEntity livingEntity = (LivingEntity) player.getLocation().getWorld().spawnEntity(getLocation(player), entityType);
        HashSet<ElitePower> mobPowers = getPowers(powers, player);
        new EliteMobEntity(livingEntity, level, mobPowers, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public static void spawnEliteEntityTypeCommand(CommandSender commandSender,
                                                   EntityType entityType,
                                                   String world,
                                                   Vector coords,
                                                   Integer level,
                                                   Optional<String[]> powers) {
        try {
            Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
            spawnEliteEntityTypeCommand(commandSender,
                    location,
                    entityType,
                    level,
                    powers);
        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] World argument was not valid!");
        }
    }

    public static void spawnEliteEntityTypeCommand(CommandSender commandSender,
                                                   Location location,
                                                   EntityType entityType,
                                                   Integer level,
                                                   Optional<String[]> powers) {
        if (!EliteMobProperties.getValidMobTypes().contains(entityType)) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Entity type " + entityType.toString() + " can't be an Elite!"));
            return;
        }
        LivingEntity livingEntity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        HashSet<ElitePower> mobPowers = new HashSet<>();
        if (powers.isPresent())
            mobPowers = getPowers(powers.get(), commandSender);
        new EliteMobEntity(livingEntity, level, mobPowers, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    public static void spawnCustomBossCommand(CommandSender commandSender,
                                              String fileName,
                                              String world,
                                              Vector coords) {
        try {
            Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
            CustomBossEntity.constructCustomBoss(fileName, location);
        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] World argument was not valid!");
        }

    }

    public static void spawnCustomBossCommand(CommandSender commandSender,
                                              String fileName,
                                              String world,
                                              Vector coords,
                                              int level) {
        try {
            Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
            CustomBossEntity.constructCustomBoss(fileName, location, level);
        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] World argument was not valid!");
        }

    }

    public static void spawnCustomBossCommand(Player player, String fileName, int level) {
        CustomBossEntity.constructCustomBoss(fileName, getLocation(player), level);
    }

    public static void spawnCustomBossCommand(Player player, String fileName) {
        CustomBossEntity.constructCustomBoss(fileName, getLocation(player));
    }

    public static void spawnSuperMobCommand(Player player, EntityType entityType) {
        if (SuperMobProperties.isValidSuperMobType(entityType))
            spawnSuperMob(entityType, getLocation(player));
        else
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Entity type " + entityType.toString() + " can't be a Super Mob!"));
    }

    private static Location getLocation(Player player) {
        return player.getTargetBlock(null, 30).getLocation().add(0.5, 1, 0.5);
    }

    private static HashSet<ElitePower> getPowers(String[] mobPowers, CommandSender commandSender) {

        HashSet<ElitePower> elitePowers = new HashSet<>();

        if (mobPowers.length > 0)
            for (String string : mobPowers) {
                ElitePower elitePower = ElitePower.getElitePower(string);
                if (elitePower == null) {
                    commandSender.sendMessage("[EliteMobs] Power " + string + " is not a valid power! Valid powers:");
                    StringBuilder allPowers = new StringBuilder();
                    for (ElitePower iteratedPower : ElitePower.elitePowers)
                        allPowers.append(iteratedPower.getName()).append(", ");
                    allPowers.append("custom");
                    commandSender.sendMessage(allPowers.toString());
                    return new HashSet<>();
                }
                elitePowers.add(elitePower);
            }
        return elitePowers;
    }

    private static void spawnSuperMob(EntityType entityType, Location location) {
        SuperMobConstructor.constructSuperMob((LivingEntity) location.getWorld().spawnEntity(location, entityType));
    }

}
