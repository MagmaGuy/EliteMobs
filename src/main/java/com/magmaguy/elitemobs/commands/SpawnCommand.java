package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.SuperMobConstructor;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
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
        HashSet<PowersConfigFields> mobPowers = getPowers(powers, player);
        EliteEntity eliteEntity = new EliteEntity();
        eliteEntity.setLevel(level);
        if (!mobPowers.isEmpty()) eliteEntity.applyPowers(mobPowers);
        else eliteEntity.randomizePowers(EliteMobProperties.getPluginData(livingEntity));
        eliteEntity.setLivingEntity(livingEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
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
        HashSet<PowersConfigFields> mobPowers = new HashSet<>();
        if (powers.isPresent()) mobPowers = getPowers(powers.get(), commandSender);
        EliteEntity eliteEntity = new EliteEntity();
        eliteEntity.setLevel(level);
        eliteEntity.setLivingEntity(livingEntity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        if (!mobPowers.isEmpty()) eliteEntity.applyPowers(mobPowers);
        else eliteEntity.randomizePowers(EliteMobProperties.getPluginData(livingEntity));
    }

    public static void spawnCustomBossCommand(CommandSender commandSender,
                                              String fileName,
                                              String world,
                                              Vector coords) {
        try {
            Location location = new Location(Bukkit.getWorld(world), coords.getX(), coords.getY(), coords.getZ());
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
            if (customBossesConfigFields == null) {
                commandSender.sendMessage("Filename " + fileName + " is not valid! Make sure you are writing the name of a configuration file in the custombosses folder!");
                return;
            }
            CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
            customBossEntity.setSpawnLocation(location);
            customBossEntity.spawn(false);
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
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
            if (customBossesConfigFields == null) {
                commandSender.sendMessage("Filename " + fileName + " is not valid! Make sure you are writing the name of a configuration file in the custombosses folder!");
                return;
            }
            CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
            customBossEntity.setSpawnLocation(location);
            customBossEntity.setLevel(level);
            customBossEntity.spawn(false);
        } catch (Exception e) {
            commandSender.sendMessage("[EliteMobs] World argument was not valid!");
        }

    }

    public static void spawnCustomBossCommand(Player player, String fileName, int level) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
        if (customBossesConfigFields == null) {
            player.sendMessage("Filename " + fileName + " is not valid! Make sure you are writing the name of a configuration file in the custombosses folder!");
            return;
        }
        CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
        customBossEntity.setSpawnLocation(getLocation(player));
        customBossEntity.setLevel(level);
        customBossEntity.spawn(false);
    }

    public static void spawnCustomBossCommand(Player player, String fileName) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(fileName);
        if (customBossesConfigFields == null) {
            player.sendMessage("Filename " + fileName + " is not valid! Make sure you are writing the name of a configuration file in the custombosses folder!");
            return;
        }
        CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
        customBossEntity.setSpawnLocation(getLocation(player));
        customBossEntity.spawn(false);
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

    private static HashSet<PowersConfigFields> getPowers(String[] mobPowers, CommandSender commandSender) {

        HashSet<PowersConfigFields> elitePowers = new HashSet<>();

        if (mobPowers.length > 0)
            for (String string : mobPowers) {
                PowersConfigFields powersConfigFields = PowersConfig.getPower(string);
                if (powersConfigFields == null) {
                    commandSender.sendMessage("[EliteMobs] Power " + string + " is not a valid power! Valid powers:");
                    StringBuilder allPowers = new StringBuilder();
                    for (CustomConfigFields iteratedField : ElitePower.getElitePowers().values())
                        allPowers.append(iteratedField.getFilename()).append(", ");
                    allPowers.append("custom");
                    commandSender.sendMessage(allPowers.toString());
                    return new HashSet<>();
                }
                elitePowers.add(powersConfigFields);
            }
        return elitePowers;
    }

    private static void spawnSuperMob(EntityType entityType, Location location) {
        SuperMobConstructor.constructSuperMob((LivingEntity) location.getWorld().spawnEntity(location, entityType));
    }

}
