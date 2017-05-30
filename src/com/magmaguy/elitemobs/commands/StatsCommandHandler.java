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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.magmaguy.elitemobs.EliteMobs.worldList;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * Created by MagmaGuy on 01/05/2017.
 */
public class StatsCommandHandler {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    public void statsHandler(CommandSender commandSender) {

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

        for (World world : worldList) {

            for (LivingEntity livingEntity : world.getLivingEntities()) {

                if (livingEntity.hasMetadata(MetadataHandler.ELITE_MOB_MD) ||
                        livingEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

                    totalMobCount++;

                    if (livingEntity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

                        mobLevelSavingsCount += livingEntity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();
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


                    } else if (livingEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

                        //passive EliteMobs only stack at 50 right now
                        //TODO: redo this count at some other stage of this plugin's development
                        mobLevelSavingsCount += ConfigValues.defaultConfig.getInt("Passive EliteMob stack amount");
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
                unsortedMobCountFilter(unsortedMobCount, zombieCount, "zombie");
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
                unsortedMobCountFilter(unsortedMobCount, zombieCount, "zombie");
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

}
