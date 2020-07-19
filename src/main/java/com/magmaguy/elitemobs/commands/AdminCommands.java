package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.DebugScreen;
import com.magmaguy.elitemobs.commands.admin.StatsCommand;
import com.magmaguy.elitemobs.commands.admin.npc.NPCCommands;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class AdminCommands {

    public static boolean parseAdminCommand(CommandSender commandSender, String[] args) {

        switch (args[0]) {

            //autosetup
            case "autosetup":
                if (!CommandHandler.permCheck(CommandHandler.AUTOSETUP, commandSender)) return true;
                File folder = new File(Bukkit.getWorldContainer().getAbsolutePath());
                File[] listOfFiles = folder.listFiles();
                boolean worldFolderExists = false;

                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isDirectory() &&
                            listOfFile.getName().equals(AdventurersGuildConfig.guildWorldName)) {
                        commandSender.sendMessage("[EliteMobs] World " + AdventurersGuildConfig.guildWorldName + " found! Loading it in...");
                        worldFolderExists = true;
                        break;
                    }
                }

                if (!worldFolderExists) {
                    commandSender.sendMessage("[EliteMobs] Could not import world " + AdventurersGuildConfig.guildWorldName + " ! " +
                            "It is not in your worlds directory. If you wish to use the default world, you can find the link to download it on the resource page over at https://www.spigotmc.org/resources/%E2%9A%94elitemobs%E2%9A%94.40090/");
                    return true;
                }

                Bukkit.createWorld(new WorldCreator(AdventurersGuildConfig.guildWorldName));
                commandSender.sendMessage("[EliteMobs] Successfully imported the world!");
                commandSender.sendMessage("[EliteMobs] Now all you need to do is add the permission elitemobs.user to your users and you're all set!");
                return true;

            //spawn
            case "spawn":
            case "spawnmob":
                if (CommandHandler.permCheck(CommandHandler.SPAWNMOB, commandSender))
                    SpawnCommand.spawnMob(commandSender, args);
                return true;

            //customboss
            case "customboss":
                if (!CommandHandler.userPermCheck(CommandHandler.CUSTOMBOSS, commandSender)) return true;
                CustomBossCommandHandler.handleCommand((Player) commandSender, args);
                return true;

            case "debug":
                if (!CommandHandler.userPermCheck(CommandHandler.DEBUG, commandSender)) return true;
                new DebugScreen((Player) commandSender, args);
                return true;

            //events
            case "event":
            case "launchevent":
            case "startevent":
            case "triggerevent":
                if (!CommandHandler.permCheck(CommandHandler.EVENTS, commandSender)) return true;
                TriggerEventHandler.triggerEventCommand(commandSender, args);
                return true;

            //npcs
            case ("npc"):
                if (!CommandHandler.permCheck(CommandHandler.NPC, commandSender)) return true;
                NPCCommands.parseNPCCommand(commandSender, args);
                return true;

            //stats
            case "stats":
                if (CommandHandler.permCheck(CommandHandler.STATS, commandSender))
                    StatsCommand.statsHandler(commandSender);
                return true;

            //items
            case "getloot":
            case "gl":
                if (CommandHandler.userPermCheck(CommandHandler.GETLOOT, commandSender) && args.length == 1) {
                    getLootMenu getLootMenu = new getLootMenu();
                    getLootMenu.lootGUI((Player) commandSender);
                } else {
                    if (GetLootCommandHandler.getLoot(((Player) commandSender), args[1]))
                        return true;
                    else
                        ((Player) commandSender).sendTitle("", "Could not find that item name.");
                }
                return true;

            case "checkmaxtier":
            case "maxtier":
                if (CommandHandler.permCheck(CommandHandler.CHECK_MAX_TIER, commandSender))
                    CheckMaxItemTierCommand.checkMaxItemTier(commandSender);
                return true;

            case "giveloot":
                if (CommandHandler.permCheck(CommandHandler.GIVELOOT, commandSender))
                    GiveLootHandler.giveLootCommand(commandSender, args);
                return true;

            case "simloot":
            case "simulateloot":
            case "simdrop":
            case "simulatedrop":
                if (commandSender instanceof Player) {
                    if (CommandHandler.userPermCheck(CommandHandler.SIMLOOT, commandSender))
                        SimLootHandler.simLoot((Player) commandSender, Integer.parseInt(args[1]));
                } else if (CommandHandler.permCheck(CommandHandler.SIMLOOT, commandSender))
                    SimLootHandler.simLoot(commandSender, args);
                return true;

            //version
            case "version":
                if (CommandHandler.permCheck(CommandHandler.VERSION, commandSender))
                    VersionHandler.versionCommand(commandSender, args);
                return true;

            //reload
            case "reload":
            case "restart":
                if (CommandHandler.permCheck(CommandHandler.RELOAD, commandSender))
                    ReloadHandler.reload(commandSender);
                return true;

            //killall
            case "killall":
            case "kill":
                KillHandler.killCommand(commandSender, args);
                return true;


            case "gettier":
            case "spawntier":
                if (CommandHandler.userPermCheck(CommandHandler.GET_TIER, commandSender))
                    TierSetSpawner.spawnTierItem(Integer.parseInt(args[1]), (Player) commandSender);
                return true;


            //currency
            case "add":
                if (CommandHandler.permCheck(CommandHandler.CURRENCY_ADD, commandSender))
                    CurrencyCommandsHandler.addCommand(commandSender, args);
                return true;
            case "addall":
                if (CommandHandler.permCheck(CommandHandler.CURRENCY_ADD, commandSender))
                    CurrencyCommandsHandler.addAllCommand(commandSender, args);
                return true;
            case ("subtract"):
                if (CommandHandler.permCheck(CommandHandler.CURRENCY_SUBTRACT, commandSender))
                    CurrencyCommandsHandler.subtractCommand(commandSender, args);
                return true;
            case ("set"):
                if (CommandHandler.permCheck(CommandHandler.CURRENCY_SET, commandSender))
                    CurrencyCommandsHandler.setCommand(commandSender, args);


                //guild system
            case "setrank":
                if (!CommandHandler.userPermCheck(CommandHandler.SET_RANK, commandSender)) return true;
                GuildRankCommands.setGuildRank(args);
                return true;

            //discord alerts or discord link
            case "discord":
                if (!CommandHandler.userPermCheck(CommandHandler.DISCORD, commandSender)) return true;
                if (args.length < 2) {
                    commandSender.sendMessage("EliteMobs discord room: https://discord.gg/9f5QSka");
                    commandSender.sendMessage("If you're trying to send a message via DiscordSRV, the correct commands is /em discord [message]");
                } else {
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < args.length; i++)
                        if (i > 0)
                            message.append(args[i] + " ");
                    new DiscordSRVAnnouncement(message.toString());
                }
                return true;

            default:
                return false;
        }

    }

}
