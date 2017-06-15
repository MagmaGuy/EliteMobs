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
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.LootCustomConfig;
import com.magmaguy.elitemobs.elitedrops.EliteDropsHandler;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import com.magmaguy.elitemobs.mobscanner.ValidPassiveMobFilter;
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

import java.util.Random;

import static com.magmaguy.elitemobs.elitedrops.EliteDropsHandler.lootList;
import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 21/01/2017.
 */

//This place has been forsaken by God. Leave it while you can.
//TODO: commandSender.sendMessage actually works for console and players without filtering. Remake that. Also remake the title messages to unite them in some method.

public class CommandHandler implements CommandExecutor {

    private EliteMobs plugin;

    public CommandHandler(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        // /elitemobs SpawnMob with variable arg length
        if (args.length > 0 && args[0].equalsIgnoreCase("SpawnMob")) {

            if (commandSender instanceof ConsoleCommandSender || commandSender instanceof Player && commandSender.hasPermission("elitemobs.SpawnMob")) {

                SpawnMobCommandHandler spawnMob = new SpawnMobCommandHandler();

                spawnMob.spawnMob(commandSender, args);

                return true;

            } else if (commandSender instanceof Player && !commandSender.hasPermission("elitemobs.SpawnMob")) {

                Player player = (Player) commandSender;

                if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                    player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                            "You need the following permission: " + "elitemobs.SpawnMob");

                } else {

                    player.sendMessage("You do not have the permission " + "elitemobs.SpawnMob");

                }

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
                    if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                        player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                "You need the following permission: " + "elitemobs.stats");

                    } else {

                        player.sendMessage("You do not have the permission " + "elitemobs.stats");

                    }
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

            // /elitemobs reload config | /elitemobs reload loot | /elitemobs giveloot [player] (for GUI menu) |
            // /elitemobs killall aggressiveelites | /elitemobs killall passiveelites | /elitemobs simloot [level]
            case 2:

                //valid /elitemobs reload config
                if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("configs") &&
                        commandSender.hasPermission("elitemobs.reload.configs")) {

                    Player player = (Player) commandSender;

                    ReloadConfigCommandHandler reloadConfigCommandHandler = new ReloadConfigCommandHandler();
                    reloadConfigCommandHandler.reloadConfiguration();

                    getLogger().info("EliteMobs configs reloaded!");
                    player.sendTitle("EliteMobs config reloaded!", "Reloaded config, loot, mobPowers and translation");

                    return true;

                    //invalid /elitemobs reload config
                } else if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("config") &&
                        !commandSender.hasPermission("elitemobs.reload.config")) {

                    Player player = (Player) commandSender;

                    if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                        player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                "You need the following permission: " + "elitemobs.reload.configs");

                    } else {

                        player.sendMessage("You do not have the permission " + "elitemobs.reload.configs");

                    }

                    return true;


                }

                //valid /elitemobs reload loot
                if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player
                        && args[1].equalsIgnoreCase("loot")
                        && commandSender.hasPermission("elitemobs.reload.loot")) {

                    Player player = (Player) commandSender;
                    LootCustomConfig lootCustomConfig = new LootCustomConfig();
                    lootCustomConfig.reloadLootConfig();

                    EliteDropsHandler eliteDropsHandler = new EliteDropsHandler();
                    eliteDropsHandler.superDropParser();

                    getLogger().info("EliteMobs loot reloaded!");
                    player.sendTitle("EliteMobs loot reloaded!", "Reloaded loot.yml");

                    return true;

                    //invalid /elitemobs reload loot
                }

                if (args[0].equalsIgnoreCase("reload") && commandSender instanceof Player &&
                        args[1].equalsIgnoreCase("loot") &&
                        !commandSender.hasPermission("elitemobs.reload.loot")) {

                    Player player = (Player) commandSender;

                    if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                        player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                "You need the following permission: " + "elitemobs.reload.loot");

                    } else {

                        player.sendMessage("You do not have the permission " + "elitemobs.reload.loot");

                    }

                    return true;


                }

                //valid /elitemobs getloot | /elitemobs gl
                if (args[0].equalsIgnoreCase("getloot") && commandSender instanceof Player &&
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


                }

                //invalid /elitemobs getloot | /elitemobs gl
                if (args[0].equalsIgnoreCase("getloot") && !commandSender.hasPermission("elitemobs.getloot")
                        || args[0].equalsIgnoreCase("gl") && !commandSender.hasPermission("elitemobs.getloot")) {

                    Player player = (Player) commandSender;

                    if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                        player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                "You need the following permission: " + "elitemobs.getloot");

                    } else {

                        player.sendMessage("You do not have the permission " + "elitemobs.getloot");

                    }

                    return true;

                }

                if (args[0].equalsIgnoreCase("killall") && args[1].equalsIgnoreCase("aggressiveelites")) {

                    if (commandSender.hasPermission("elitemobs.killall.aggressiveelites")){

                        int counter = 0;

                        for (World world : EliteMobs.worldList) {

                            for (LivingEntity livingEntity : world.getLivingEntities()) {

                                if (livingEntity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && ValidAgressiveMobFilter.ValidAgressiveMobFilter(livingEntity)) {

                                    livingEntity.remove();
                                    counter++;

                                }

                            }

                        }

                        commandSender.sendMessage("Killed " + counter + " aggressive EliteMobs.");

                        return true;

                    } else if (commandSender instanceof Player) {

                        Player player = ((Player) commandSender);

                        if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                            player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                    "You need the following permission: " + "elitemobs.killall.aggressiveelites");

                        } else {

                            player.sendMessage("You do not have the permission " + "elitemobs.killall.aggressiveelites");

                        }

                    }

                }

                if (args[0].equalsIgnoreCase("killall") && args[1].equalsIgnoreCase("passiveelites")) {

                    if (commandSender.hasPermission("elitemobs.killall.passiveelites")) {

                        for (World world : EliteMobs.worldList) {

                            for (LivingEntity livingEntity : world.getLivingEntities()) {

                                if (livingEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD) && ValidPassiveMobFilter.ValidPassiveMobFilter(livingEntity)) {

                                    livingEntity.remove();

                                }

                            }

                        }

                        return true;

                    } else if (commandSender instanceof Player) {

                        Player player = ((Player) commandSender);

                        if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                            player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                    "You need the following permission: " + "elitemobs.killall.passiveelites");

                        } else {

                            player.sendMessage("You do not have the permission " + "elitemobs.killall.passiveelites");

                        }

                    }

                }

                if (args[0].equalsIgnoreCase("simloot") && commandSender instanceof Player) {

                    Player player = ((Player) commandSender);

                    if (commandSender.hasPermission("elitemobs.simloot")) {

                        SimLootHandler simLootHandler = new SimLootHandler();

                        simLootHandler.simLoot(player, Integer.parseInt(args[1]));

                    }  else if (commandSender instanceof Player) {

                        if (Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean("Use titles to warn players they are missing a permission")) {

                            player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                                    "You need the following permission: " + "elitemobs.simloot");

                        } else {

                            player.sendMessage("You do not have the permission " + "elitemobs.simloot");

                        }

                    }

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
            player.sendMessage("/elitemobs reload configs");
            player.sendMessage("/elitemobs reload loot");
            player.sendMessage("/elitemobs killall aggressiveelites");
            player.sendMessage("/elitemobs killall passiveelites");
            player.sendMessage("/elitemobs getloot (alone to get GUI)");
            player.sendMessage("/elitemobs getloot [loot name]");
            player.sendMessage("/elitemobs simloot [mob level]");
            player.sendMessage("/elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");
            player.sendMessage("/elitemobs SpawnMob [mobType] [mobLevel] [mobPower] [mobPower2(keep adding as many as you'd like)]");

        } else if (commandSender instanceof ConsoleCommandSender) {

            getLogger().info("Command not recognized. Valid commands:");
            getLogger().info("elitemobs stats");
            getLogger().info("elitemobs reload configs");
            getLogger().info("elitemobs reload loot");
            commandSender.sendMessage("/elitemobs killall aggressiveelites");
            commandSender.sendMessage("/elitemobs killall passiveelites");
            getLogger().info("elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");
            getLogger().info("elitemobs SpawnMob [worldName] [x] [y] [z] [mobType] [mobLevel] [mobPower] [mobPower2(keep adding as many as you'd like)]");

        }

    }


    private boolean validPlayer(String arg2) {

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            String currentName = player.getName();

            if (currentName.equals(arg2)) {

                return true;

            }

        }

        //TODO: hunt down stuff that errors when this is changed to false
        return true;

    }

}
