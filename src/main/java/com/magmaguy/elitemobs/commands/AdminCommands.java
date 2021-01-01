package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.commands.admin.DebugScreen;
import com.magmaguy.elitemobs.commands.admin.StatsCommand;
import com.magmaguy.elitemobs.commands.admin.npc.NPCCommands;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminCommands {

    public static boolean parseAdminCommand(CommandSender commandSender, String[] args) {

        switch (args[0]) {

            //autosetup
            case "setup":
                if (CommandHandler.userPermCheck("elitemobs.*", commandSender))
                    new SetupHandler((Player) commandSender, args);
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
                if (CommandHandler.userPermCheck(CommandHandler.GETLOOT, commandSender))
                    if (args.length == 1) {
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
                        SimLootHandler.simLoot((Player) commandSender, Integer.parseInt(args[1]), args.length == 3 ? Integer.parseInt(args[2]) : 0);
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
                return true;

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

            case "unbind":
                if (CommandHandler.userPermCheck("elitemobs.*", commandSender)) {
                    Player player = (Player) commandSender;
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    if (ItemTagger.isEliteItem(itemStack))
                        SoulbindEnchantment.removeEnchantment(itemStack);
                }
                return true;

            case "relativecoord":
                if (CommandHandler.userPermCheck("elitemobs.*", commandSender)) {
                    if (args.length < 2) {
                        commandSender.sendMessage("Syntax: /em relativecoord dungeonfilename");
                        return true;
                    }
                    try {
                        Minidungeon minidungeon = Minidungeon.minidungeons.get(args[1]);
                        Location anchorpoint = minidungeon.dungeonPackagerConfigFields.getAnchorPoint();
                        Player player = (Player) commandSender;
                        String relativePosition = player.getLocation().clone().subtract(anchorpoint).getBlockX() + ", "
                                + player.getLocation().clone().subtract(anchorpoint).getBlockY() + ", "
                                + player.getLocation().clone().subtract(anchorpoint).getBlockZ();
                        player.sendMessage(ChatColorConverter.convert(
                                "[EliteMobs] Relative position to anchor point of " + minidungeon.dungeonPackagerConfigFields.getName() + ": " + relativePosition));
                    } catch (Exception ex) {
                        commandSender.sendMessage("[EliteMobs] Failed to run command! Syntax: /em relativecoord [filename]");
                    }
                }
                return true;

            default:
                return false;
        }

    }

}
