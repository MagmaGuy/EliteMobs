/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobcustomizer.HealthHandler;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.magmaguy.elitemobs.EliteMobs.worldList;
import static org.bukkit.Bukkit.getConsoleSender;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class SpawnMobCommandHandler {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
    MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance();

    public void spawnMob(CommandSender commandSender, String[] args) {

        World world = null;
        Location location = null;
        String entityInput = null;
        int mobLevel = 0;
        List<String> mobPower = new ArrayList<>();

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            if (args.length == 1) {

                player.sendMessage("Valid command syntax:");
                player.sendMessage("/elitemobs SpawnMob [mobType] [mobLevel] [mobPower] [mobPower(you can keep adding these mobPowers as many as you'd like)]");

            }

            world = player.getWorld();
            location = player.getTargetBlock((HashSet<Byte>) null, 30).getLocation().add(0, 1, 0);

            entityInput = args[1].toLowerCase();

            if (args.length > 2) {

                try {

                    mobLevel = Integer.valueOf(args[2]);

                } catch (NumberFormatException ex) {

                    player.sendMessage("Not a valid level.");
                    player.sendMessage("Valid command syntax:");
                    player.sendMessage("/elitemobs SpawnMob [mobType] [mobLevel] [mobPower] [mobPower(you can keep adding these mobPowers as many as you'd like)]");

                }

            }

            if (args.length > 3) {

                int index = 0;

                for (String arg : args) {

                    //mob powers start after arg 2
                    if (index > 2) {

                        mobPower.add(arg);

                    }

                    index++;

                }

            }

        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            for (World worldIterator : worldList) {

                //find world
                if (worldIterator.getName().equals(args[1])) {

                    world = worldIterator;

                    //find x coord
                    try {

                        double xCoord = Double.parseDouble(args[2]);
                        double yCoord = Double.parseDouble(args[3]);
                        double zCoord = Double.parseDouble(args[4]);

                        location = new Location(worldIterator, xCoord, yCoord, zCoord);

                        entityInput = args[5].toLowerCase();

                        break;

                    } catch (NumberFormatException ex) {

                        getConsoleSender().sendMessage("At least one of the coordinates (x:" + args[2] + ", y:" +
                                args[3] + ", z:" + args[4] + ") is not valid");
                        getConsoleSender().sendMessage("Valid command syntax: /elitemobs SpawnMob worldName xCoord yCoord " +
                                "zCoord mobType mobLevel mobPower mobPower(you can keep adding these mobPowers as many as you'd like)");

                    }

                    if (args.length > 6) {

                        int index = 0;

                        for (String arg : args) {

                            //mob powers start after arg 2
                            if (index > 2) {

                                mobPower.add(arg);

                            }

                            index++;

                        }

                    }

                }

            }

            if (world == null) {

                getConsoleSender().sendMessage("World " + args[1] + "not found. Valid command syntax: /elitemobs SpawnMob" +
                        " [worldName] [xCoord] [yCoord] [zCoord] [mobType] [mobLevel] [mobPower] [mobPower(you can keep adding these " +
                        "mobPowers as many as you'd like)]");

            }

        }

        EntityType entityType = null;

        switch (entityInput) {

            case "blaze":
                entityType = EntityType.BLAZE;
                break;
            case "cavespider":
                entityType = EntityType.CAVE_SPIDER;
                break;
            case "creeper":
                entityType = EntityType.CREEPER;
                break;
            case "enderman":
                entityType = EntityType.ENDERMAN;
                break;
            case "endermite":
                entityType = EntityType.ENDERMITE;
                break;
            case "husk":
                entityType = EntityType.HUSK;
                break;
            case "irongolem":
                entityType = EntityType.IRON_GOLEM;
                break;
            case "pigzombie":
                entityType = EntityType.PIG_ZOMBIE;
                break;
            case "polarbear":
                entityType = EntityType.POLAR_BEAR;
                break;
            case "silverfish":
                entityType = EntityType.SILVERFISH;
                break;
            case "skeleton":
                entityType = EntityType.SKELETON;
                break;
            case "spider":
                entityType = EntityType.SPIDER;
                break;
            case "stray":
                entityType = EntityType.STRAY;
                break;
            case "witch":
                entityType = EntityType.WITCH;
                break;
            case "witherskeleton":
                entityType = EntityType.WITHER_SKELETON;
                break;
            case "zombie":
                entityType = EntityType.ZOMBIE;
                break;
            case "chicken":
                entityType = EntityType.CHICKEN;
                break;
            case "cow":
                entityType = EntityType.COW;
                break;
            case "mushroomcow":
                entityType = EntityType.MUSHROOM_COW;
                break;
            case "pig":
                entityType = EntityType.PIG;
                break;
            case "sheep":
                entityType = EntityType.SHEEP;
                break;
            default:
                if (commandSender instanceof Player) {

                    ((Player) commandSender).getPlayer().sendTitle("Could not spawn mob type " + entityInput,
                            "If this is incorrect, please contact the dev.");

                } else if (commandSender instanceof ConsoleCommandSender) {

                    getConsoleSender().sendMessage("Could not spawn mob type " + entityInput + ". If this is incorrect, " +
                            "please contact the dev.");

                }
                break;

        }

        Entity entity = null;

        if (entityType != null) {

            entity = world.spawnEntity(location, entityType);

        }

        if (entityType == EntityType.CHICKEN || entityType == EntityType.COW || entityType == EntityType.MUSHROOM_COW ||
                entityType == EntityType.PIG || entityType == EntityType.SHEEP) {

            HealthHandler.passiveHealthHandler(entity, ConfigValues.defaultConfig.getInt("Passive EliteMob stack amount"));
            NameHandler.customPassiveName(entity, plugin);

            return;

        }

        if (mobLevel > 0) {

            entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, mobLevel));

        }


        //todo: add a way to select the powers mobs get, get alt system to spawn unstackable EliteMobs with fixed powers

        if (mobPower.size() > 0) {

            boolean inputError = false;

            int powerCount = 0;

            MetadataHandler metadataHandler = new MetadataHandler();

            for (String string : mobPower) {

                switch (string) {
                    //major powers
                    case MetadataHandler.ZOMBIE_FRIENDS_H:
                        if (entity instanceof Zombie){
                            entity.setMetadata(MetadataHandler.ZOMBIE_FRIENDS_MD, new FixedMetadataValue(plugin, true));
                            powerCount++;
                        }
                        break;
                    case MetadataHandler.ZOMBIE_NECRONOMICON_H:
                        if (entity instanceof Zombie){
                            entity.setMetadata(MetadataHandler.ZOMBIE_NECRONOMICON_MD, new FixedMetadataValue(plugin, true));
                            powerCount++;
                        }
                        break;
                    case MetadataHandler.ZOMBIE_TEAM_ROCKET_H:
                        if (entity instanceof Zombie){
                            entity.setMetadata(MetadataHandler.ZOMBIE_TEAM_ROCKET_MD, new FixedMetadataValue(plugin, true));
                            powerCount++;
                        }
                        break;
                    case MetadataHandler.ZOMBIE_PARENTS_H:
                        if (entity instanceof Zombie){
                            entity.setMetadata(MetadataHandler.ZOMBIE_PARENTS_MD, new FixedMetadataValue(plugin, true));
                            powerCount++;
                        }
                        break;
                    //minor powers
                    case MetadataHandler.ATTACK_ARROW_H:
                        entity.setMetadata(MetadataHandler.ATTACK_ARROW_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_BLINDING_H:
                        entity.setMetadata(MetadataHandler.ATTACK_BLINDING_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_CONFUSING_H:
                        entity.setMetadata(MetadataHandler.ATTACK_CONFUSING_MD, new FixedMetadataValue(plugin, true));
//                        minorPowerPowerStance.attackConfusing(entity);
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_FIRE_H:
                        entity.setMetadata(MetadataHandler.ATTACK_FIRE_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_FIREBALL_H:
                        entity.setMetadata(MetadataHandler.ATTACK_FIREBALL_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_FREEZE_H:
                        entity.setMetadata(MetadataHandler.ATTACK_FREEZE_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_GRAVITY_H:
                        entity.setMetadata(MetadataHandler.ATTACK_GRAVITY_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_POISON_H:
                        entity.setMetadata(MetadataHandler.ATTACK_POISON_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_PUSH_H:
                        entity.setMetadata(MetadataHandler.ATTACK_PUSH_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_WEAKNESS_H:
                        entity.setMetadata(MetadataHandler.ATTACK_WEAKNESS_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_WEB_H:
                        entity.setMetadata(MetadataHandler.ATTACK_WEB_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.ATTACK_WITHER_H:
                        entity.setMetadata(MetadataHandler.ATTACK_WITHER_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.BONUS_LOOT_H:
                        entity.setMetadata(MetadataHandler.BONUS_LOOT_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.DOUBLE_DAMAGE_H:
                        if (!(entity instanceof IronGolem)) {
                            entity.setMetadata(MetadataHandler.DOUBLE_DAMAGE_MD, new FixedMetadataValue(plugin, true));
                        }
                        powerCount++;
                        break;
                    case MetadataHandler.DOUBLE_HEALTH_H:
                        if (!(entity instanceof IronGolem)) {
                            entity.setMetadata(MetadataHandler.DOUBLE_HEALTH_MD, new FixedMetadataValue(plugin, true));
                        }
                        powerCount++;
                        break;
                    case MetadataHandler.INVISIBILITY_H:
                        entity.setMetadata(MetadataHandler.INVISIBILITY_MD, new FixedMetadataValue(plugin, true));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                        powerCount++;
                        break;
                    case MetadataHandler.INVULNERABILITY_ARROW_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_ARROW_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.INVULNERABILITY_FALL_DAMAGE_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_FALL_DAMAGE_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.INVULNERABILITY_FIRE_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_FIRE_MD, new FixedMetadataValue(plugin, true));
//                        minorPowerPowerStance.invulnerabilityFireEffect(entity);
                        powerCount++;
                        break;
                    case MetadataHandler.INVULNERABILITY_KNOCKBACK_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_KNOCKBACK_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case MetadataHandler.MOVEMENT_SPEED_H:
                        entity.setMetadata(MetadataHandler.MOVEMENT_SPEED_MD, new FixedMetadataValue(plugin, true));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        powerCount++;
                        break;
                    case MetadataHandler.TAUNT_H:
                        entity.setMetadata(MetadataHandler.TAUNT_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "custom":
                        entity.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    default:
                        if (commandSender instanceof Player) {

                            Player player = (Player) commandSender;

                            player.sendMessage(string + " is not a valid power.");

                        } else if (commandSender instanceof ConsoleCommandSender) {

                            getConsoleSender().sendMessage(string + " is not a valid power.");

                        }

                        inputError = true;

                }

            }

            entity.setMetadata(MetadataHandler.MINOR_POWER_AMOUNT_MD, new FixedMetadataValue(plugin, powerCount));

            minorPowerPowerStance.itemEffect(entity);
            majorPowerPowerStance.itemEffect(entity);

            if (inputError) {

                if (commandSender instanceof Player) {

                    Player player = (Player) commandSender;

                    player.sendMessage("Valid powers: " + MetadataHandler.powerListHumanFormat() + " custom");

                } else if (commandSender instanceof ConsoleCommandSender) {

                    getConsoleSender().sendMessage("Valid powers: " + MetadataHandler.powerListHumanFormat() + MetadataHandler.majorPowerList()+ " custom");

                }

            }

        }

    }

}
