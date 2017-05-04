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

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.LootCustomConfig;
import com.magmaguy.elitemobs.config.MobPowersCustomConfig;
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static com.magmaguy.elitemobs.elitedrops.EliteDropsHandler.lootList;
import static org.bukkit.Bukkit.getLogger;

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

                SpawnMobCommandHandler spawnMob = new SpawnMobCommandHandler();

                spawnMob.spawnMob(commandSender, args);

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

            // /elitemobs stats | /elitemobs getloot (for GUI menu)
            case 1:

                if (args[0].equalsIgnoreCase("stats") && commandSender instanceof Player &&
                        commandSender.hasPermission("elitemobs.stats") || args[0].equalsIgnoreCase("stats")
                        && commandSender instanceof ConsoleCommandSender){

                    StatsCommandHandler stats = new StatsCommandHandler();

                    stats.statsHandler(commandSender);

                    return true;

                } else if (args[0].equalsIgnoreCase("stats") && commandSender instanceof Player
                        && !commandSender.hasPermission("elitemobs.stats")) {

                    Player player = (Player) commandSender;
                    player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                            "You need the following permission: " + "elitemobs.stats");
                    return true;

                } else if (args[0].equalsIgnoreCase("getloot") && commandSender instanceof Player &&
                        commandSender.hasPermission("elitemobs.getloot") || args[0].equalsIgnoreCase("gl")
                        && commandSender instanceof Player && commandSender.hasPermission("elitemobs.getloot")) {

                    LootGUI lootGUI = new LootGUI();
                    lootGUI.lootGUI((Player)commandSender);
                    return true;

                }

                validCommands(commandSender);
                return true;

            // /elitemobs reload config | /elitemobs reload loot | /elitemobs giveloot [player] (for GUI menu)
            case 2:

                //valid /elitemobs reload config
                if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("config") &&
                        commandSender.hasPermission("elitemobs.reload.config")) {

                    Player player = (Player) commandSender;
                    Bukkit.getPluginManager().getPlugin("EliteMobs").reloadConfig();
                    MobPowersCustomConfig mobPowersCustomConfig = new MobPowersCustomConfig();
                    mobPowersCustomConfig.reloadCustomConfig();
                    LootCustomConfig lootCustomConfig = new LootCustomConfig();
                    lootCustomConfig.reloadLootConfig();
                    EliteDropsHandler eliteDropsHandler = new EliteDropsHandler();
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
                    LootCustomConfig lootCustomConfig = new LootCustomConfig();
                    lootCustomConfig.reloadLootConfig();
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

                    GetLootCommandHandler getLootCommandHandler = new GetLootCommandHandler();

                    if (getLootCommandHandler.getLootHandler(player, args[1])) {

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

                } else if (commandSender instanceof ConsoleCommandSender || commandSender instanceof Player
                    && commandSender.hasPermission("elitemobs.giveloot")) {

                    if (args[0].equalsIgnoreCase("giveloot")) {

                        if (validPlayer(args[1])) {

                            Player receiver = Bukkit.getServer().getPlayer(args[1]);

                            //TODO: add GUI to giveloot command

                        }

                    }

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

                            GetLootCommandHandler getLootCommandHandler = new GetLootCommandHandler();

                            if (args[2].equalsIgnoreCase("random") || args[2].equalsIgnoreCase("r")) {

                                Random random = new Random();

                                int index = random.nextInt(lootList.size());

                                ItemStack itemStack = new ItemStack(lootList.get(index));

                                receiver.getInventory().addItem(itemStack);

                                return true;

                            } else if (getLootCommandHandler.getLootHandler(receiver, args[2])) {

                                return true;

                            } else if (!getLootCommandHandler.getLootHandler(receiver, args[2])) {

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


    private void validCommands(CommandSender commandSender){

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            player.sendMessage("Valid commands:");
            player.sendMessage("/elitemobs stats");
            player.sendMessage("/elitemobs reload config");
            player.sendMessage("/elitemobs reload loot");
            player.sendMessage("/elitemobs getloot (alone to get GUI)");
            player.sendMessage("/elitemobs getloot [loot name]");
            player.sendMessage("/elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");
            player.sendMessage("/elitemobs spawnmob [mobType] [mobLevel] [mobPower] [mobPower2(keep adding as many as you'd like)]");

        } else if (commandSender instanceof ConsoleCommandSender) {

            getLogger().info("Command not recognized. Valid commands:");
            getLogger().info("elitemobs stats");
            getLogger().info("elitemobs reload config");
            getLogger().info("elitemobs reload loot");
            getLogger().info("elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");
            getLogger().info("elitemobs spawnmob [worldName] [x] [y] [z] [mobType] [mobLevel] [mobPower] [mobPower2(keep adding as many as you'd like)]");

        }

    }


    private boolean validPlayer(String arg2) {

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            String currentName = player.getName();

            if (currentName.equals(arg2)) {

                return true;

            }

        }

        //TODO: hunt downstuff that errors when this is changed to false
        return true;

    }

}
