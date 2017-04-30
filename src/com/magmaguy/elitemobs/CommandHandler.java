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

package com.magmaguy.elitemobs;

import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import com.magmaguy.elitemobs.mobcustomizer.HealthHandler;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.magmaguy.elitemobs.EliteMobs.worldList;
import static com.magmaguy.elitemobs.elitedrops.EliteDropsHandler.lootList;
import static org.bukkit.Bukkit.*;

/**
 * Created by MagmaGuy on 21/01/2017.
 */

public class CommandHandler implements CommandExecutor {

    private EliteMobs plugin;

    public CommandHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        // /elitemobs spawnmob with variable arg length
        if (args.length > 0 && args[0].equalsIgnoreCase("spawnmob")) {

            if (commandSender instanceof ConsoleCommandSender || commandSender instanceof Player && commandSender.hasPermission("elitemobs.spawnmob")) {

                spawnmob(commandSender, args);

                return true;

            } else if (commandSender instanceof Player && !commandSender.hasPermission("elitemobs.spawnmob")) {

                Player player = (Player) commandSender;
                player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                        "You need the following permission: " + "elitemobs.spawnmob");
                return true;

            }

        }

        switch(args.length) {

            //just /elitemobs
            case 0:

                validCommands(commandSender);
                return true;

            // /elitemobs stats
            case 1:

                if (args[0].equalsIgnoreCase("stats") && commandSender instanceof Player &&
                        commandSender.hasPermission("elitemobs.stats") || args[0].equalsIgnoreCase("stats")
                        && commandSender instanceof ConsoleCommandSender){

                    statsHandler(commandSender);
                    return true;

                } else if (args[0].equalsIgnoreCase("stats") && commandSender instanceof Player
                        && !commandSender.hasPermission("elitemobs.stats")) {

                    Player player = (Player) commandSender;
                    player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                            "You need the following permission: " + "elitemobs.stats");
                    return true;

                }

                validCommands(commandSender);
                return true;

            // /elitemobs reload config | /elitemobs reload loot | /elitemobs
            case 2:

                //valid /elitemobs reload config
                if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("config") &&
                        commandSender.hasPermission("elitemobs.reload.config")) {

                    Player player = (Player) commandSender;
                    Bukkit.getPluginManager().getPlugin("EliteMobs").reloadConfig();
                    plugin.reloadCustomConfig();
                    EliteDropsHandler eliteDropsHandler = new EliteDropsHandler(plugin);
                    eliteDropsHandler.superDropParser();
                    getLogger().info("EliteMobs config reloaded!");
                    player.sendTitle("EliteMobs config reloaded!", "hehehe butts.");

                    return true;

                    //invalid /elitemobs reload config
                } else if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("config") &&
                        !commandSender.hasPermission("elitemobs.reload.config")) {

                    Player player = (Player) commandSender;
                    player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                            "You need the following permission: " + " elitemobs.reload.config");

                    return true;

                    //valid /elitemobs reload loot
                } else if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player
                    && args[1].equalsIgnoreCase("loot")
                        && commandSender.hasPermission("elitemobs.reload.loot")) {

                    Player player = (Player) commandSender;
                    plugin.reloadCustomConfig();
                    getLogger().info("EliteMobs loot reloaded!");
                    player.sendTitle("EliteMobs loot reloaded!", "hehehe booty.");

                    return true;

                    //invalid /elitemobs reload loot
                } else if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("loot") &&
                        !commandSender.hasPermission("elitemobs.reload.loot")) {

                    Player player = (Player) commandSender;
                    player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                            "You need the following permission: " + " elitemobs.reload.loot");

                    return true;

                    //valid /elitemobs getloot | /elitemobs gl
                } else if (args[0].equalsIgnoreCase("getloot") && commandSender instanceof Player &&
                        commandSender.hasPermission("elitemobs.getloot") || args[0].equalsIgnoreCase("gl")
                        && commandSender instanceof Player && commandSender.hasPermission("elitemobs.getloot")) {

                    Player player = (Player) commandSender;

                    if (getLootHandler(player, args[1])) {

                        return true;

                    } else {

                        player.sendTitle("", "Could not find that item name.");

                        return true;

                    }

                    //invalid /elitemobs getloot | /elitemobs gl
                } else if (args[0].equalsIgnoreCase("getloot") && !commandSender.hasPermission("elitemobs.getloot")
                        || args[0].equalsIgnoreCase("gl") && !commandSender.hasPermission("elitemobs.getloot")) {

                    Player player = (Player) commandSender;
                    player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                            "You need the following permission: " + " elitemobs.getloot");

                    return true;

                }

                validCommands(commandSender);
                return true;

            // /elitemobs giveloot [player] [loot]
            case 3:

                if (commandSender instanceof ConsoleCommandSender || commandSender instanceof Player
                        && commandSender.hasPermission("elitemobs.giveloot")) {

                    if (args[0].equalsIgnoreCase("giveloot")) {

                        if (validPlayer(args[1])) {

                            Player receiver = Bukkit.getServer().getPlayer(args[1]);

                            if (args[2].equalsIgnoreCase("random") || args[2].equalsIgnoreCase("r")) {

                                Random random = new Random();

                                int index = random.nextInt(lootList.size());

                                ItemStack itemStack = new ItemStack(lootList.get(index));

                                receiver.getInventory().addItem(itemStack);

                                return true;

                            } else if (getLootHandler(receiver, args[2])) {

                                return true;

                            } else if (!getLootHandler(receiver, args[2])) {

                                if (commandSender instanceof ConsoleCommandSender) {

                                    getLogger().info("Can't give loot to player - loot not found.");

                                    return true;

                                } else if (commandSender instanceof Player) {

                                    Player player = (Player) commandSender;

                                    player.sendTitle("Can't give loot to player - loot not found.","");

                                    return true;

                                }

                            }

                        } else {

                            if (commandSender instanceof ConsoleCommandSender) {

                                getLogger().info("Can't give loot to player - player not found.");

                                return true;

                            } else if (commandSender instanceof Player) {

                                Player player = (Player) commandSender;

                                player.sendTitle("Can't give loot to player - player not found.","");

                                return true;

                            }

                        }

                    }

                }

                validCommands(commandSender);
                return true;

            //invalid commands
            default:

                validCommands(commandSender);
                return true;

        }

    }

    private void spawnmob(CommandSender commandSender, String[] args) {

        World world = null;
        Location location = null;
        String entityInput = null;
        int mobLevel = 0;
        List<String> mobPower = new ArrayList<>();

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            if (args.length == 1) {

                player.sendMessage("Valid command syntax:");
                player.sendMessage("/elitemobs spawnmob [mobType] [mobLevel] [mobPower] [mobPower(you can keep adding these mobPowers as many as you'd like)]");

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
                    player.sendMessage("/elitemobs spawnmob [mobType] [mobLevel] [mobPower] [mobPower(you can keep adding these mobPowers as many as you'd like)]");

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

        } else if (commandSender instanceof ConsoleCommandSender) {

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
                        getConsoleSender().sendMessage("Valid command syntax: /elitemobs spawnmob worldName xCoord yCoord " +
                                "zCoord mobLevel mobPower mobPower(you can keep adding these mobPowers as many as you'd like)");

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

                getConsoleSender().sendMessage("World " + args[1] + "not found. Valid command syntax: /elitemobs spawnmob" +
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

            HealthHandler.passiveHealthHandler(entity, plugin.getConfig().getInt("Passive EliteMob stack amount"));
            NameHandler.customPassiveName(entity, plugin);

            return;

        }

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (mobLevel > 0) {

            entity.setMetadata(metadataHandler.eliteMobMD, new FixedMetadataValue(plugin, mobLevel));

        }


        //todo: add a way to select the powers mobs get, get alt system to spawn unstackable EliteMobs with fixed powers

        if (mobPower.size() > 0) {

            boolean inputError = false;

            int powerCount = 0;

            for (String string : mobPower) {

                switch (string) {

                    case "confusing":
                        entity.setMetadata(metadataHandler.attackConfusingMD, new FixedMetadataValue(plugin, true));
                        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
                        minorPowerPowerStance.attackConfusing(entity);
                        powerCount++;
                        break;
                    case "fireattack":
                        entity.setMetadata(metadataHandler.attackFireMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "freeze":
                        entity.setMetadata(metadataHandler.attackFreezeMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "shulker":
                        entity.setMetadata(metadataHandler.attackGravityMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "poison":
                        entity.setMetadata(metadataHandler.attackPoisonMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "knockback":
                        entity.setMetadata(metadataHandler.attackPushMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "web":
                        entity.setMetadata(metadataHandler.attackWebMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "wither":
                        entity.setMetadata(metadataHandler.attackWitherMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "loot":
                        entity.setMetadata(metadataHandler.bonusLootMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "invisibility":
                        entity.setMetadata(metadataHandler.invisibilityMD, new FixedMetadataValue(plugin, true));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                        powerCount++;
                        break;
                    case "arrowresist":
                        entity.setMetadata(metadataHandler.invulnerabilityArrowMD, new FixedMetadataValue(plugin, true));
                        break;
                    case "fallresist":
                        entity.setMetadata(metadataHandler.invulnerabilityFallDamageMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "fireresist":
                        entity.setMetadata(metadataHandler.invulnerabilityFireMD, new FixedMetadataValue(plugin, true));
                        MinorPowerPowerStance minorPowerPowerStance1 = new MinorPowerPowerStance(plugin);
                        minorPowerPowerStance1.invulnerabilityFireEffect(entity);
                        powerCount++;
                        break;
                    case "heavy":
                        entity.setMetadata(metadataHandler.invulnerabilityKnockbackMD, new FixedMetadataValue(plugin, true));
                        powerCount++;
                        break;
                    case "fast":
                        entity.setMetadata(metadataHandler.movementSpeedMD, new FixedMetadataValue(plugin, true));
                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
                        powerCount++;
                        break;
                    case "custom":
                        entity.setMetadata(metadataHandler.custom, new FixedMetadataValue(plugin, true));
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

            entity.setMetadata(metadataHandler.minorPowerAmountMD, new FixedMetadataValue(plugin, powerCount));
            MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
            minorPowerPowerStance.itemEffect(entity);

            if (inputError) {

                if (commandSender instanceof Player) {

                    Player player = (Player) commandSender;

                    player.sendMessage("Valid powers: confusing fireattack freeze shulker poison knockback web wither loot invisibility " +
                            "arrowresist fallresist fireresist heavy fast custom");

                } else if (commandSender instanceof ConsoleCommandSender) {

                    getConsoleSender().sendMessage("Valid powers: fireattack freeze shulker poison knockback web wither loot invisibility " +
                            "arrowresist fallresist fireresist fast");

                }

            }

        }

    }

    private void validCommands(CommandSender commandSender){

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            player.sendMessage("Valid commands:");
            player.sendMessage("/elitemobs stats");
            player.sendMessage("/elitemobs reload config");
            player.sendMessage("/elitemobs reload loot");
            player.sendMessage("/elitemobs getloot [loot name]");
            player.sendMessage("/elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");

        } else if (commandSender instanceof ConsoleCommandSender) {

            getLogger().info("Command not recognized. Valid commands:");
            getLogger().info("elitemobs stats");
            getLogger().info("elitemobs reload config");
            getLogger().info("elitemobs reload loot");
            getLogger().info("elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");

        }

    }

    private void statsHandler(CommandSender commandSender) {

        int mobLevelSavingsCount = 0;
        int totalMobCount = 0;
        int aggressiveCount = 0;
        int passiveCount = 0;
        int blazeCount = 0;
        int caveSpiderCount = 0;
        int creeperCount = 0;
        int endermanCount = 0;
        int endermiteCount = 0;
        int huskCount = 0;
        int ironGolemCount = 0;
        int pigZombieCount = 0;
        int polarBearCount = 0;
        int silverfishCount = 0;
        int skeletonCount = 0;
        int spiderCount = 0;
        int strayCount = 0;
        int witchCount = 0;
        int witherSkeletonCount = 0;
        int zombieCount = 0;
        int zombieVillagerCount = 0;

        int chickenCount = 0;
        int cowCount = 0;
        int mushroomCowCount = 0;
        int pigCount = 0;
        int sheepCount = 0;

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        for (World world : worldList) {

            for (LivingEntity livingEntity : world.getLivingEntities()) {

                if (livingEntity.hasMetadata(metadataHandler.eliteMobMD) ||
                        livingEntity.hasMetadata(metadataHandler.passiveEliteMobMD)) {

                    totalMobCount++;

                    if (livingEntity.hasMetadata(metadataHandler.eliteMobMD)) {

                        mobLevelSavingsCount += livingEntity.getMetadata(metadataHandler.eliteMobMD).get(0).asInt();
                        aggressiveCount++;

                        switch (livingEntity.getType()) {

                            case ZOMBIE:
                                zombieCount++;
                                break;
                            case HUSK:
                                huskCount++;
                                break;
                            case ZOMBIE_VILLAGER:
                                zombieVillagerCount++;
                                break;
                            case SKELETON:
                                skeletonCount++;
                                break;
                            case WITHER_SKELETON:
                                witherSkeletonCount++;
                                break;
                            case STRAY:
                                strayCount++;
                                break;
                            case PIG_ZOMBIE:
                                pigZombieCount++;
                                break;
                            case CREEPER:
                                creeperCount++;
                                break;
                            case SPIDER:
                                spiderCount++;
                                break;
                            case ENDERMAN:
                                endermanCount++;
                                break;
                            case CAVE_SPIDER:
                                caveSpiderCount++;
                                break;
                            case SILVERFISH:
                                silverfishCount++;
                                break;
                            case BLAZE:
                                blazeCount++;
                                break;
                            case WITCH:
                                witchCount++;
                                break;
                            case ENDERMITE:
                                endermiteCount++;
                                break;
                            case POLAR_BEAR:
                                polarBearCount++;
                                break;
                            case IRON_GOLEM:
                                ironGolemCount++;
                                break;
                            default:
                                getLogger().info("Error: Couldn't assign custom mob name due to unexpected aggressive boss mob (talk to the dev!)");
                                getLogger().info("Missing mob type: " + livingEntity.getType());
                                break;
                        }


                    } else if (livingEntity.hasMetadata(metadataHandler.passiveEliteMobMD)) {

                        //passive EliteMobs only stack at 50 right now
                        //TODO: redo this count at some other stage of this plugin's development
                        mobLevelSavingsCount += plugin.getConfig().getInt("Passive EliteMob stack amount");
                        passiveCount++;

                        switch (livingEntity.getType()) {

                            case CHICKEN:
                                chickenCount++;
                                break;
                            case COW:
                                cowCount++;
                                break;
                            case MUSHROOM_COW:
                                mushroomCowCount++;
                                break;
                            case PIG:
                                pigCount++;
                                break;
                            case SHEEP:
                                sheepCount++;
                                break;
                            default:
                                getLogger().info("Error: Couldn't assign custom mob name due to unexpected passive boss mob (talk to the dev!)");
                                getLogger().info("Missing mob type: " + livingEntity.getType());
                                break;

                        }

                    }

                }

            }

        }

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            player.sendMessage("§5§m-----------------------------------------------------");
            player.sendMessage("§a§lEliteMobs stats:");
            player.sendMessage("There are currently §l§6" + totalMobCount + " §f§rEliteMobs replacing §l§e" +
                    mobLevelSavingsCount + " §f§rregular mobs.");

            if (aggressiveCount > 0) {

                String aggressiveCountMessage = "§c" + aggressiveCount + " §4Aggressive EliteMobs: §f";

                HashMap unsortedMobCount = new HashMap();

                unsortedMobCountFilter(unsortedMobCount, blazeCount, "blazes");
                unsortedMobCountFilter(unsortedMobCount, caveSpiderCount, "cave spiders");
                unsortedMobCountFilter(unsortedMobCount, creeperCount, "creepers");
                unsortedMobCountFilter(unsortedMobCount, endermanCount, "endermen");
                unsortedMobCountFilter(unsortedMobCount, endermiteCount, "endermites");
                unsortedMobCountFilter(unsortedMobCount, huskCount, "husks");
                unsortedMobCountFilter(unsortedMobCount, pigZombieCount, "zombiepigmen");
                unsortedMobCountFilter(unsortedMobCount, polarBearCount, "polar bears");
                unsortedMobCountFilter(unsortedMobCount, silverfishCount, "silverfish");
                unsortedMobCountFilter(unsortedMobCount, skeletonCount, "skeletons");
                unsortedMobCountFilter(unsortedMobCount, spiderCount, "spiders");
                unsortedMobCountFilter(unsortedMobCount, strayCount, "strays");
                unsortedMobCountFilter(unsortedMobCount, witchCount, "witches");
                unsortedMobCountFilter(unsortedMobCount, witherSkeletonCount, "wither skeletons");
                unsortedMobCountFilter(unsortedMobCount, zombieCount, "zombies");
                unsortedMobCountFilter(unsortedMobCount, zombieVillagerCount, "zombie villagers");

                player.sendMessage(messageStringAppender(aggressiveCountMessage, unsortedMobCount));

            }

            if (passiveCount > 0) {

                String passiveCountMessage = "§b" + passiveCount + " §3Passive EliteMobs: §f";

                HashMap unsortedMobCount = new HashMap();

                unsortedMobCountFilter(unsortedMobCount, chickenCount, "chickens");
                unsortedMobCountFilter(unsortedMobCount, cowCount, "cows");
                unsortedMobCountFilter(unsortedMobCount, ironGolemCount, "iron golems");
                unsortedMobCountFilter(unsortedMobCount, mushroomCowCount, "mushroom cows");
                unsortedMobCountFilter(unsortedMobCount, pigCount, "pigs");
                unsortedMobCountFilter(unsortedMobCount, sheepCount, "sheep");

                player.sendMessage(messageStringAppender(passiveCountMessage, unsortedMobCount));

            }

            player.sendMessage("§5§m-----------------------------------------------------");

        } else if (commandSender instanceof ConsoleCommandSender) {

            getServer().getConsoleSender().sendMessage( "§5§m-------------------------------------------------------------");
            getServer().getConsoleSender().sendMessage("§a§lEliteMobs stats:");
            getServer().getConsoleSender().sendMessage("There are currently §l§6" + totalMobCount + " §f§rEliteMobs replacing §l§e" +
                    mobLevelSavingsCount + " §f§rregular mobs.");

            if (aggressiveCount > 0) {

                String aggressiveCountMessage = "§c" + aggressiveCount + " §4Aggressive EliteMobs: §f";

                HashMap unsortedMobCount = new HashMap();

                unsortedMobCountFilter(unsortedMobCount, blazeCount, "blazes");
                unsortedMobCountFilter(unsortedMobCount, caveSpiderCount, "cave spiders");
                unsortedMobCountFilter(unsortedMobCount, creeperCount, "creepers");
                unsortedMobCountFilter(unsortedMobCount, endermanCount, "endermen");
                unsortedMobCountFilter(unsortedMobCount, endermiteCount, "endermites");
                unsortedMobCountFilter(unsortedMobCount, huskCount, "husks");
                unsortedMobCountFilter(unsortedMobCount, ironGolemCount, "iron golems");
                unsortedMobCountFilter(unsortedMobCount, pigZombieCount, "zombiepigmen");
                unsortedMobCountFilter(unsortedMobCount, polarBearCount, "polar bears");
                unsortedMobCountFilter(unsortedMobCount, silverfishCount, "silverfish");
                unsortedMobCountFilter(unsortedMobCount, skeletonCount, "skeletons");
                unsortedMobCountFilter(unsortedMobCount, spiderCount, "spiders");
                unsortedMobCountFilter(unsortedMobCount, strayCount, "strays");
                unsortedMobCountFilter(unsortedMobCount, witchCount, "witches");
                unsortedMobCountFilter(unsortedMobCount, witherSkeletonCount, "wither skeletons");
                unsortedMobCountFilter(unsortedMobCount, zombieCount, "zombies");
                unsortedMobCountFilter(unsortedMobCount, zombieVillagerCount, "zombie villagers");

                getServer().getConsoleSender().sendMessage(messageStringAppender(aggressiveCountMessage, unsortedMobCount));

            }

            if (passiveCount > 0) {

                String passiveCountMessage = "§b" + passiveCount + " §3Passive EliteMobs: §f";

                HashMap unsortedMobCount = new HashMap();

                unsortedMobCountFilter(unsortedMobCount, chickenCount, "chickens");
                unsortedMobCountFilter(unsortedMobCount, cowCount, "cows");
                unsortedMobCountFilter(unsortedMobCount, mushroomCowCount, "mushroom cows");
                unsortedMobCountFilter(unsortedMobCount, pigCount, "pigs");
                unsortedMobCountFilter(unsortedMobCount, sheepCount, "sheep");

                getServer().getConsoleSender().sendMessage(messageStringAppender(passiveCountMessage, unsortedMobCount));

            }

            getServer().getConsoleSender().sendMessage("§5§m-------------------------------------------------------------");

        }



    }

    private boolean getLootHandler(Player player, String args1) {

        for (ItemStack itemStack : lootList) {

            String itemRawName = itemStack.getItemMeta().getDisplayName();

            //TODO: shouldn't happen, weird
            if (itemRawName == null) {

                return false;

            }

            String itemProcessedName = itemRawName.replaceAll(" ", "_").toLowerCase();

            if (itemProcessedName.equalsIgnoreCase(args1) && player.isValid()) {

                player.getInventory().addItem(itemStack);

                return true;

            }

        }

        return false;

    }


    private void unsortedMobCountFilter(HashMap unsortedMobCount, int count, String mobTypeName) {

        if (count > 0) {

            unsortedMobCount.put(mobTypeName, count);

        }

    }

    private String messageStringAppender(String countMessage, HashMap unsortedMobCount) {

        Iterator iterator = unsortedMobCount.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry pair = (Map.Entry) iterator.next();

            String mobCountString = "§6§l" + pair.getValue();
            String commaString = ",";
            String spacing = " ";
            String mobNameString = "§r§f" + pair.getKey();

            countMessage += commaString + spacing + mobCountString + spacing + mobNameString;

        }

        return countMessage;

    }

    private boolean validPlayer(String arg2) {

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            String currentName = player.getName();

            if (currentName.equals(arg2)) {

                return true;

            }

        }

        return false;

    }

}
