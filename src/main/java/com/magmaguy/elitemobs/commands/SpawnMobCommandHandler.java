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
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import com.magmaguy.elitemobs.mobcustomizer.HealthHandler;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import com.magmaguy.elitemobs.mobpowers.majorpowers.SkeletonTrackingArrow;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.AttackArrow;
import com.magmaguy.elitemobs.mobpowers.offensivepowers.AttackFireball;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class SpawnMobCommandHandler {

    public static void spawnMob(CommandSender commandSender, String[] args) {

        Location location = getLocation(commandSender, args);
        EntityType entityType = getEntityType(commandSender, args);
        Integer mobLevel = getLevel(commandSender, args);

        if (location == null || entityType == null) return;

        ArrayList<String> mobPowers = getPowers(commandSender, args);

        spawnEliteMob(mobLevel, entityType, location, mobPowers, commandSender);

    }

    private static Location getLocation(CommandSender commandSender, String args[]) {

        Location location = null;

        if (commandSender instanceof Player) {

            location = ((Player) commandSender).getTargetBlock(null, 30).getLocation().add(0.5, 1, 0.5);

        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            World world = null;

            try {

                for (World worldIterator : validWorldList) {

                    if (worldIterator.getName().equals(args[1])) {

                        world = worldIterator;

                    }

                }

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Error when trying to obtain world name of console / command" +
                        "block spawned Elite Mob");
                return null;

            }

            double xCoord, yCoord, zCoord;

            try {

                xCoord = Double.parseDouble(args[2]);
                yCoord = Double.parseDouble(args[3]);
                zCoord = Double.parseDouble(args[4]);

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Error when trying to obtain X Y Z coordinates of console / " +
                        "command block spawned Elite Mob");
                return null;

            }

            location = new Location(world, xCoord, yCoord, zCoord);

        }

        return location;

    }

    private static final String VALID_MOB_TYPES = "Valid mob types: blaze, cavespider, creeper, enderman, endermite, husk," +
            " irongolem, pigzombie, polarbear, silverfish, skeleton, spider, stray, witch, witherskeleton," +
            "zombie, chicken, cow, mushroomcow, pig, sheep";

    private static EntityType getEntityType(CommandSender commandSender, String args[]) {

        EntityType entityType = null;
        String entityInput = "";

        if (commandSender instanceof Player) {

            try {

                entityInput = args[1].toLowerCase();

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Error tryin to obtain mob type.");
                commandSender.sendMessage(VALID_MOB_TYPES);

            }


        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {


            try {

                entityInput = args[5].toLowerCase();

            } catch (Exception e) {

                commandSender.sendMessage("[EliteMobs] Error trying to obtain mob type.");
                commandSender.sendMessage(VALID_MOB_TYPES);

            }


        }

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
            case "vex":
                entityType = EntityType.VEX;
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
                commandSender.sendMessage("Could not spawn mob type " + entityInput +
                        " If this is incorrect, please contact the dev.");
                commandSender.sendMessage(VALID_MOB_TYPES);
                break;

        }

        return entityType;

    }

    private static Integer getLevel(CommandSender commandSender, String args[]) {

        int mobLevel = 0;

        if (commandSender instanceof Player) {

            if (args.length > 2) {

                try {

                    mobLevel = Integer.valueOf(args[2]);

                } catch (Error error) {

                    commandSender.sendMessage("Error assigning mob level to Elite Mob. Mob level must be above 0.");

                }

            }


        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            if (args.length > 6) {

                try {

                    mobLevel = Integer.valueOf(args[6]);

                } catch (Error error) {

                    commandSender.sendMessage("Error assigning mob level to Elite Mob. Mob level must be above 0.");

                }

            }

        }

        return mobLevel;

    }

    private static ArrayList<String> getPowers (CommandSender commandSender, String args[]) {

        ArrayList<String> mobPowers = new ArrayList();

        if (commandSender instanceof Player){

            if (args.length > 3) {

                int index = 0;

                for (String arg : args) {

                    //mob powers start after arg 2
                    if (index > 2) {

                        mobPowers.add(arg);

                    }

                    index++;

                }

            }

        } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof BlockCommandSender) {

            if (args.length > 6) {

                int index = 0;

                for (String arg : args) {

                    //mob powers start after arg 6
                    if (index > 6) {

                        mobPowers.add(arg);

                    }

                    index++;

                }

            }

        }

        return mobPowers;

    }

    private static Entity spawnEliteMob(int mobLevel, EntityType mobType, Location location, ArrayList<String> mobPowers, CommandSender commandSender) {

        Entity entity = location.getWorld().spawnEntity(location, mobType);

        if (mobType == EntityType.CHICKEN || mobType == EntityType.COW || mobType == EntityType.MUSHROOM_COW ||
                mobType == EntityType.PIG || mobType == EntityType.SHEEP) {

            HealthHandler.passiveHealthHandler(entity, ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT));
            NameHandler.customPassiveName(entity, MetadataHandler.PLUGIN);

            return entity;

        } else {

            if (mobLevel > 0) {

                entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, mobLevel));
                applyPowers(entity, mobPowers, commandSender);

                AggressiveEliteMobConstructor.constructAggressiveEliteMob(entity);

            } else {

                commandSender.sendMessage("[EliteMobs] Elite Mob level must be over 0!");

            }

        }

        return entity;

    }

    private static void applyPowers(Entity entity, ArrayList<String> mobPowers, CommandSender commandSender) {

        if (mobPowers.size() > 0) {

            for (String string : mobPowers) {

                switch (string) {
                    //major powers
                    case MetadataHandler.ZOMBIE_FRIENDS_H:
                        if (entity instanceof Zombie) {
                            entity.setMetadata(MetadataHandler.ZOMBIE_FRIENDS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.ZOMBIE_NECRONOMICON_H:
                        if (entity instanceof Zombie) {
                            entity.setMetadata(MetadataHandler.ZOMBIE_NECRONOMICON_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.ZOMBIE_TEAM_ROCKET_H:
                        if (entity instanceof Zombie) {
                            entity.setMetadata(MetadataHandler.ZOMBIE_TEAM_ROCKET_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.ZOMBIE_PARENTS_H:
                        if (entity instanceof Zombie) {
                            entity.setMetadata(MetadataHandler.ZOMBIE_PARENTS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.ZOMBIE_BLOAT_H:
                        if (entity instanceof Zombie) {
                            entity.setMetadata(MetadataHandler.ZOMBIE_BLOAT_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.SKELETON_TRACKING_ARROW_H:
                        if (entity instanceof Skeleton) {
                            SkeletonTrackingArrow skeletonTrackingArrow = new SkeletonTrackingArrow();
                            skeletonTrackingArrow.applyPowers(entity);
                        }
                    case MetadataHandler.SKELETON_PILLAR_H:
                        if (entity instanceof Skeleton){
                            entity.setMetadata(MetadataHandler.SKELETON_PILLAR_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        //minor powers
                    case MetadataHandler.ATTACK_ARROW_H:
                        AttackArrow attackArrow = new AttackArrow();
                        attackArrow.applyPowers(entity);
                        break;
                    case MetadataHandler.ATTACK_BLINDING_H:
                        entity.setMetadata(MetadataHandler.ATTACK_BLINDING_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_CONFUSING_H:
                        entity.setMetadata(MetadataHandler.ATTACK_CONFUSING_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_FIRE_H:
                        entity.setMetadata(MetadataHandler.ATTACK_FIRE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_FIREBALL_H:
                        AttackFireball attackFireball = new AttackFireball();
                        attackFireball.applyPowers(entity);
                        break;
                    case MetadataHandler.ATTACK_FREEZE_H:
                        entity.setMetadata(MetadataHandler.ATTACK_FREEZE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_GRAVITY_H:
                        entity.setMetadata(MetadataHandler.ATTACK_GRAVITY_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_POISON_H:
                        entity.setMetadata(MetadataHandler.ATTACK_POISON_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_PUSH_H:
                        entity.setMetadata(MetadataHandler.ATTACK_PUSH_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_WEAKNESS_H:
                        entity.setMetadata(MetadataHandler.ATTACK_WEAKNESS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_WEB_H:
                        entity.setMetadata(MetadataHandler.ATTACK_WEB_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.ATTACK_WITHER_H:
                        entity.setMetadata(MetadataHandler.ATTACK_WITHER_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.BONUS_LOOT_H:
                        entity.setMetadata(MetadataHandler.BONUS_LOOT_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.DOUBLE_DAMAGE_H:
                        if (!(entity instanceof IronGolem)) {
                            entity.setMetadata(MetadataHandler.DOUBLE_DAMAGE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.DOUBLE_HEALTH_H:
                        if (!(entity instanceof IronGolem)) {
                            entity.setMetadata(MetadataHandler.DOUBLE_HEALTH_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        }
                        break;
                    case MetadataHandler.INVISIBILITY_H:
                        entity.setMetadata(MetadataHandler.INVISIBILITY_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                        break;
                    case MetadataHandler.INVULNERABILITY_ARROW_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_ARROW_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.INVULNERABILITY_FALL_DAMAGE_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_FALL_DAMAGE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.INVULNERABILITY_FIRE_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_FIRE_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.INVULNERABILITY_KNOCKBACK_H:
                        entity.setMetadata(MetadataHandler.INVULNERABILITY_KNOCKBACK_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case MetadataHandler.MOVEMENT_SPEED_H:
                        entity.setMetadata(MetadataHandler.MOVEMENT_SPEED_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        break;
                    case MetadataHandler.TAUNT_H:
                        entity.setMetadata(MetadataHandler.TAUNT_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    case "custom":
                        entity.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(MetadataHandler.PLUGIN, true));
                        break;
                    default:
                        commandSender.sendMessage(string + " is not a valid power.");
                        commandSender.sendMessage("Valid powers: " + MetadataHandler.powerListHumanFormat + MetadataHandler.majorPowerList + " custom");
                        break;

                }

            }

            MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
            MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance();

            minorPowerPowerStance.itemEffect(entity);
            majorPowerPowerStance.itemEffect(entity);

        }

    }

}
