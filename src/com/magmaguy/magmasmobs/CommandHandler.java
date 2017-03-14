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

package com.magmaguy.magmasmobs;

import com.magmaguy.magmasmobs.superdrops.SuperDropsHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static com.magmaguy.magmasmobs.MagmasMobs.worldList;
import static com.magmaguy.magmasmobs.superdrops.SuperDropsHandler.lootList;
import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 21/01/2017.
 */

public class CommandHandler implements CommandExecutor {

    private MagmasMobs plugin;

    public CommandHandler(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (args.length == 0 && sender instanceof Player) {

            Player player = (Player) sender;

            player.sendMessage("Valid commands:");
            player.sendMessage("/magmasmobs stats");
            player.sendMessage("/magmasmobs reload config");
            player.sendMessage("/magmasmobs getloot [loot name]");
            player.sendMessage("/magmasmobs giveloot [player name] random/[loot name]");
            getLogger().info("test");

            return true;

        } else if (args[0].equalsIgnoreCase("stats") && sender instanceof Player
                && sender.hasPermission("magmasmobs.stats")) {

            Player player = (Player) sender;
            statsHandler(player);
            return true;

        } else if (args[0].equalsIgnoreCase("stats") && sender instanceof Player
                && !sender.hasPermission("magmasmobs.stats")) {

            Player player = (Player) sender;
            player.sendMessage("I'm afraid I can't let you do that, " + player.getDisplayName() + ".");
            player.sendMessage("You need the following permission: " + "magmasmobs.stats");

        } else if (args[0].equalsIgnoreCase("reload") && sender instanceof Player
                && args[1].equalsIgnoreCase("config")
                && sender.hasPermission("magmasmobs.reload.config")) {

            Player player = (Player) sender;
            Bukkit.getPluginManager().getPlugin("MagmasMobs").reloadConfig();
            SuperDropsHandler superDropsHandler = new SuperDropsHandler(plugin);
            superDropsHandler.superDropParser();
            getLogger().info("MagmasMobs config reloaded!");
            player.sendTitle("MagmasMobs config reloaded!", "hehehe butts.");

            return true;
        } else if (args[0].equalsIgnoreCase("reload") && sender instanceof Player
                && args[1].equalsIgnoreCase("config")
                && !sender.hasPermission("magmasmobs.reload.config")) {

            Player player = (Player) sender;
            player.sendMessage("I'm afraid I can't let you do that, " + player.getDisplayName() + ".");
            player.sendMessage("You need the following permission: " + " magmasmobs.reload.config");

        } else if (args[0].equalsIgnoreCase("getloot") && sender instanceof Player && sender.hasPermission("magmasmobs.getloot")
                && args.length == 2 || args[0].equalsIgnoreCase("gl") && sender instanceof Player
                && sender.hasPermission("magmasmobs.getloot") && args.length == 2) {

            Player player = (Player) sender;

            if (getLootHandler(player, args[1])) {

                return true;

            } else {

                player.sendTitle("", "Could not find that item name.");

                return true;

            }

        } else if (args[0].equalsIgnoreCase("getloot") && !sender.hasPermission("magmasmobs.getloot")
                && args.length == 2 || args[0].equalsIgnoreCase("gl")
                && !sender.hasPermission("magmasmobs.getloot") && args.length == 2) {

            Player player = (Player) sender;
            player.sendMessage("I'm afraid I can't let you do that, " + player.getDisplayName() + ".");
            player.sendMessage("You need the following permission: " + " magmasmobs.getloot");

        } else if (args.length == 3) {

            if (sender instanceof ConsoleCommandSender || sender instanceof Player
                    && sender.hasPermission("magmasmobs.giveloot")) {

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

                        }

                    } else {

                        if (sender instanceof ConsoleCommandSender) {

                            getLogger().info("Can't give loot to player - player not found.");

                            return true;

                        } else if (sender instanceof Player) {

                            Player player = (Player) sender;

                            player.sendMessage("Can't give loot to player - player not found.");

                            return true;

                        }

                    }

                }

            }

        } else if (sender instanceof Player) {

            Player player = ((Player) sender).getPlayer();

            player.sendMessage("Command not recognized. Valid commands:");
            player.sendMessage("/magmasmobs stats");
            player.sendMessage("/magmasmobs reload config");
            player.sendMessage("/magmasmobs getloot [loot name]");
            player.sendMessage("/magmasmobs giveloot [player name] random/[loot name]");

            return true;

        }

        return false;

    }

    private void statsHandler(Player player) {

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
        int ironGolemCount = 0;
        int mushroomCowCount = 0;
        int pigCount = 0;
        int sheepCount = 0;

        for (World world : worldList) {

            for (LivingEntity livingEntity : world.getLivingEntities()) {

                if (livingEntity.hasMetadata("MagmasSuperMob") ||
                        livingEntity.hasMetadata("MagmasPassiveSupermob")) {

                    totalMobCount++;

                    if (livingEntity.hasMetadata("MagmasSuperMob")) {

                        mobLevelSavingsCount += livingEntity.getMetadata("MagmasSuperMob").get(0).asInt();
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
                            default:
                                getLogger().info("Error: Couldn't assign custom mob name due to unexpected aggressive boss mob (talk to the dev!)");
                                getLogger().info("Missing mob type: " + livingEntity.getType());
                                break;
                        }


                    } else if (livingEntity.hasMetadata("MagmasPassiveSupermob")) {

                        //passive supermobs only stack at 50 right now
                        //TODO: redo this count at some other stage of this plugin's development
                        mobLevelSavingsCount += 50;
                        passiveCount++;

                        switch (livingEntity.getType()) {

                            case CHICKEN:
                                chickenCount++;
                                break;
                            case COW:
                                cowCount++;
                                break;
                            case IRON_GOLEM:
                                ironGolemCount++;
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

        player.sendMessage("§5§m-----------------------------------------------------");
        player.sendMessage("§a§lMagmasMobs stats:");
        player.sendMessage("There are currently §l§6" + totalMobCount + " §f§rSuperMobs replacing §l§e" +
                mobLevelSavingsCount + " §f§rregular mobs.");

        if (aggressiveCount > 0) {

            String aggressiveCountMessage = "§c" + aggressiveCount + " §4Aggressive SuperMobs: §f";

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

            String passiveCountMessage = "§b" + passiveCount + " §3Passive SuperMobs: §f";

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

    }

    private boolean getLootHandler(Player player, String args1) {

        for (ItemStack itemStack : lootList) {

            getLogger().info(itemStack.getItemMeta().getDisplayName() + "");

            String itemRawName = itemStack.getItemMeta().getDisplayName();
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
