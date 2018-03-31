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
import com.magmaguy.elitemobs.commands.guiconfig.GUIConfigHandler;
import com.magmaguy.elitemobs.commands.shops.CustomShopHandler;
import com.magmaguy.elitemobs.commands.shops.ShopHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.UUIDFilter;
import com.magmaguy.elitemobs.elitedrops.CustomItemConstructor;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import com.magmaguy.elitemobs.events.DeadMoon;
import com.magmaguy.elitemobs.events.SmallTreasureGoblin;
import com.magmaguy.elitemobs.events.mobs.TreasureGoblin;
import com.magmaguy.elitemobs.events.mobs.ZombieKing;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import com.magmaguy.elitemobs.mobscanner.ValidPassiveMobFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

import static com.magmaguy.elitemobs.elitedrops.CustomItemConstructor.customItemList;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * Created by MagmaGuy on 21/01/2017.
 */

public class CommandHandler implements CommandExecutor {

    private final static String STATS = "elitemobs.stats";
    private final static String GETLOOT = "elitemobs.getloot";
    private final static String SIMLOOT = "elitemobs.simloot";
    private final static String RELOAD_CONFIGS = "elitemobs.reload.configs";
    private final static String RELOAD_LOOT = "elitemobs.reload.loot";
    private final static String GIVELOOT = "elitemobs.giveloot";
    private final static String SPAWNMOB = "elitemobs.spawnmob";
    private final static String KILLALL_AGGRESSIVEELITES = "elitemobs.killall.aggressiveelites";
    private final static String KILLALL_PASSIVEELITES = "elitemobs.killall.passiveelites";
    private final static String SHOP = "elitemobs.shop";
    private final static String CUSTOMSHOP = "elitemobs.customshop";
    private final static String CURRENCY_PAY = "elitemobs.currency.pay";
    private final static String CURRENCY_ADD = "elitemobs.currency.add";
    private final static String CURRENCY_SUBTRACT = "elitemobs.currency.subtract";
    private final static String CURRENCY_SET = "elitemobs.currency.set";
    private final static String CURRENCY_CHECK = "elitemobs.currency.check";
    private final static String CURRENCY_WALLET = "elitemobs.currency.wallet";
    private final static String CURRENCY_COINTOP = "elitemobs.currency.cointop";
    private final static String VERSION = "elitemobs.version";
    private final static String CONFIG = "elitemobs.config";
    private final static String EVENT_SMALL_TREASURE_GOBLIN = "elitemobs.event.smalltreasuregoblin";
    private final static String EVENT_DEAD_MOON = "elitemobs.event.deadmoon";

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        // /elitemobs SpawnMob with variable arg length
        if (args.length > 0 && args[0].equalsIgnoreCase("SpawnMob")) {

            if (permCheck(SPAWNMOB, commandSender)) {

                SpawnMobCommandHandler spawnMob = new SpawnMobCommandHandler();

                spawnMob.spawnMob(commandSender, args);

                return true;

            }

        }

        switch (args.length) {

            //just /elitemobs
            case 0:

                validCommands(commandSender);
                return true;

            // /elitemobs stats | /elitemobs getloot (for GUI menu) | /elitemobs shop
            case 1:

                if (args[0].equalsIgnoreCase("stats") && permCheck(STATS, commandSender)) {

                    StatsCommandHandler stats = new StatsCommandHandler();

                    stats.statsHandler(commandSender);

                    return true;

                }

                if ((args[0].equalsIgnoreCase("getloot") || args[0].equalsIgnoreCase("gl")) &&
                        userPermCheck(GETLOOT, commandSender)) {

                    LootGUI lootGUI = new LootGUI();
                    lootGUI.lootGUI((Player) commandSender);
                    return true;

                }

                if (args[0].equalsIgnoreCase("shop") && userPermCheck(SHOP, commandSender)) {

                    ShopHandler shopHandler = new ShopHandler();

                    shopHandler.initializeShop((Player) commandSender);

                    return true;

                }

                if ((args[0].equalsIgnoreCase("customShop") || args[0].equalsIgnoreCase("cShop")) &&
                        userPermCheck(CUSTOMSHOP, commandSender)) {

                    CustomShopHandler customShopHandler = new CustomShopHandler();

                    customShopHandler.initializeShop((Player) commandSender);

                    return true;

                }

                if (args[0].equalsIgnoreCase("wallet") && userPermCheck(CURRENCY_WALLET, commandSender)) {

                    String name = commandSender.getName();

                    Double money = CurrencyCommandsHandler.walletCommand(name);

                    commandSender.sendMessage(ChatColor.GREEN + "You have " + money + " " + ConfigValues.economyConfig.getString("Currency name"));

                    return true;

                }

                if (args[0].equalsIgnoreCase("coinTop") && userPermCheck(CURRENCY_COINTOP, commandSender)) {

                    CurrencyCommandsHandler.coinTop(commandSender);

                    return true;

                }

                if (args[0].equalsIgnoreCase("version") && permCheck(VERSION, commandSender)) {

                    commandSender.sendMessage(ChatColor.DARK_GREEN + "[EliteMobs]" + ChatColor.WHITE + " version " + ChatColor.GREEN +
                            Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion());

                    return true;

                }

                if (args[0].equalsIgnoreCase("config") && permCheck(CONFIG, commandSender)) {

                    GUIConfigHandler guiConfigHandler = new GUIConfigHandler();
                    guiConfigHandler.GUIConfigHandler((Player) commandSender);

                }

                validCommands(commandSender);
                return true;

            // /elitemobs reload config | /elitemobs reload loot | /elitemobs giveloot [player] (for GUI menu) |
            // /elitemobs killall aggressiveelites | /elitemobs killall passiveelites | /elitemobs simloot [level] |
            // /elitemobs check [playerName]
            case 2:

                // /elitemobs reload config
                if (args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("configs")
                        && permCheck(RELOAD_CONFIGS, commandSender)) {

                    Player player = (Player) commandSender;

                    ConfigValues.initializeConfigValues();

                    getLogger().info("EliteMobs configs reloaded!");
                    player.sendTitle("EliteMobs config reloaded!", "Reloaded config, loot, mobPowers and translation");

                    return true;

                }

                // /elitemobs reload loot
                if (args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("loot")
                        && permCheck(RELOAD_LOOT, commandSender)) {

                    ConfigValues.initializeConfigValues();

                    CustomItemConstructor.customItemList.clear();
                    CustomItemConstructor.staticCustomItemHashMap.clear();
                    CustomItemConstructor.dynamicRankedItemStacks.clear();
                    UniqueItemConstructor.uniqueItems.clear();

                    CustomItemConstructor customItemConstructor = new CustomItemConstructor();
                    customItemConstructor.superDropParser();

                    UniqueItemConstructor uniqueItemConstructor = new UniqueItemConstructor();
                    uniqueItemConstructor.intializeUniqueItems();

                    commandSender.sendMessage("EliteMobs configs reloaded!");

                    return true;

                }

                // /elitemobs getloot | /elitemobs gl
                if ((args[0].equalsIgnoreCase("getloot") || args[0].equalsIgnoreCase("gl"))
                        && userPermCheck(GETLOOT, commandSender)) {

                    Player player = (Player) commandSender;

                    GetLootCommandHandler getLootCommandHandler = new GetLootCommandHandler();

                    if (getLootCommandHandler.getLootHandler(player, args[1])) {

                        return true;

                    } else {

                        player.sendTitle("", "Could not find that item name.");

                        return true;

                    }

                }

                if (args[0].equalsIgnoreCase("killall") && args[1].equalsIgnoreCase("aggressiveelites") &&
                        permCheck(KILLALL_AGGRESSIVEELITES, commandSender)) {


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

                }

                if (args[0].equalsIgnoreCase("killall") && args[1].equalsIgnoreCase("passiveelites") &&
                        permCheck(KILLALL_PASSIVEELITES, commandSender)) {

                    for (World world : EliteMobs.worldList) {

                        for (LivingEntity livingEntity : world.getLivingEntities()) {

                            if (livingEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD) && ValidPassiveMobFilter.ValidPassiveMobFilter(livingEntity)) {

                                livingEntity.remove();

                            }

                        }

                    }

                    return true;

                }

                if (args[0].equalsIgnoreCase("simloot") && commandSender instanceof Player &&
                        userPermCheck(SIMLOOT, commandSender)) {


                    SimLootHandler simLootHandler = new SimLootHandler();

                    simLootHandler.simLoot((Player) commandSender, Integer.parseInt(args[1]));

                    return true;

                }

                if (args[0].equalsIgnoreCase("check") && permCheck(CURRENCY_CHECK, commandSender)) {

                    try {

                        Double money = CurrencyCommandsHandler.checkCommand(args[1]);

                        commandSender.sendMessage(args[1] + " has " + money + " " + ConfigValues.economyConfig.get("Currency name"));

                    } catch (Exception e) {

                        commandSender.sendMessage("Input not valid. Command format: /em set [playerName] [amount]");

                    }

                    return true;

                }

                if (args[0].equalsIgnoreCase("event") && args[1].equalsIgnoreCase("smalltreasuregoblin") &&
                        permCheck(EVENT_SMALL_TREASURE_GOBLIN, commandSender)) {

                    SmallTreasureGoblin.initializeEvent();
                    commandSender.sendMessage("Queued small treasure goblin event for next valid zombie spawn");

                    return true;

                }

                if (args[0].equalsIgnoreCase("event") && args[1].equalsIgnoreCase("deadmoon") &&
                        permCheck(EVENT_DEAD_MOON, commandSender)) {

                    DeadMoon.initializeEvent();
                    commandSender.sendMessage("Queued deadmoon event for next new moon");

                    return true;

                }

                if (args[0].equalsIgnoreCase("spawnbossmob") && userPermCheck(SPAWNMOB, commandSender)) {

                    Player player = (Player) commandSender;
                    Location cursorLocation = player.getTargetBlock(null, 5).getLocation().add(new Vector(0.5, 2, 0.5));

                    if (args[1].equalsIgnoreCase("treasuregoblin")) {

                        Zombie zombie = (Zombie) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.ZOMBIE);
                        TreasureGoblin.createGoblin(zombie);
                        commandSender.sendMessage("Spawned treasure goblin");

                    }

                    if (args[1].equalsIgnoreCase("zombieking")) {

                        Zombie zombie = (Zombie) cursorLocation.getWorld().spawnEntity(cursorLocation, EntityType.ZOMBIE);
                        ZombieKing.spawnZombieKing(zombie);
                        commandSender.sendMessage("Spawned zombie king");

                    }

                    return true;

                }

                validCommands(commandSender);
                return true;

            // /elitemobs giveloot [player] [loot] || /elitemobs paycoins [player] [amount] || /elitemobs subtractcoins [player] [amount]
            // || /elitemobs setcoins [player] [amount]
            case 3:

                if (args[0].equalsIgnoreCase("giveloot") && permCheck(GIVELOOT, commandSender)) {

                    if (validPlayer(args[1])) {

                        Player receiver = Bukkit.getServer().getPlayer(args[1]);

                        GetLootCommandHandler getLootCommandHandler = new GetLootCommandHandler();

                        if (args[2].equalsIgnoreCase("random") || args[2].equalsIgnoreCase("r")) {

                            Random random = new Random();

                            int index = random.nextInt(customItemList.size());

                            ItemStack itemStack = new ItemStack(customItemList.get(index));

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

                                player.sendTitle("Can't give loot to player - loot not found.", "");

                                return true;

                            }

                        }

                    } else {

                        if (commandSender instanceof ConsoleCommandSender) {

                            getLogger().info("Can't give loot to player - player not found.");

                            return true;

                        } else if (commandSender instanceof Player) {

                            Player player = (Player) commandSender;

                            player.sendTitle("Can't give loot to player - player not found.", "");

                            return true;

                        }

                    }

                }

                if (args[0].equals("pay") && userPermCheck(CURRENCY_PAY, commandSender)) {

                    try {

                        if (Double.parseDouble(args[2]) > 0 && Integer.parseInt(args[2]) <= EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName()))) {

                            CurrencyCommandsHandler.payCommand(args[1], Integer.parseInt(args[2]));
                            CurrencyCommandsHandler.subtractCommand(commandSender.getName(), Integer.parseInt(args[2]));

                            commandSender.sendMessage("You have paid " + args[2] + " " + ConfigValues.economyConfig.get("Currency name") + " to " + args[1]);
                            commandSender.sendMessage("You now have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName())) + " " + ConfigValues.economyConfig.get("Currency name"));

                            Player recipient = getServer().getPlayer(args[1]);
                            recipient.sendMessage("You have received " + args[2] + " " + ConfigValues.economyConfig.get("Currency name") + " from " + ((Player) commandSender).getDisplayName());

                        } else if (Double.parseDouble(args[2]) < 0) {

                            commandSender.sendMessage("Nice try. This plugin doesn't make the same mistake as banks have in the past.");

                        } else {

                            commandSender.sendMessage("You don't have enough " + ConfigValues.economyConfig.get("Currency name") + " to do that!");

                        }


                    } catch (Exception e) {

                        commandSender.sendMessage("Input not valid. Command format: /em pay [playerName] [amount]");

                    }

                    return true;

                }

                if (args[0].equals("add") && userPermCheck(CURRENCY_ADD, commandSender)) {

                    try {

                        CurrencyCommandsHandler.addCommand(args[1], Integer.parseInt(args[2]));

                        commandSender.sendMessage("You have added " + args[2] + " to " + args[1]);
                        commandSender.sendMessage("They now have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName())));

                    } catch (Exception e) {

                        commandSender.sendMessage("Input not valid. Command format: /em subtract [playerName] [amount]");

                    }

                    return true;

                }

                if (args[0].equals("subtract") && userPermCheck(CURRENCY_SUBTRACT, commandSender)) {

                    try {

                        CurrencyCommandsHandler.subtractCommand(args[1], Integer.parseInt(args[2]));

                        commandSender.sendMessage("You have subtracted " + args[2] + " from " + args[1]);
                        commandSender.sendMessage("They now have " + EconomyHandler.checkCurrency(UUIDFilter.guessUUI(commandSender.getName())));

                    } catch (Exception e) {

                        commandSender.sendMessage("Input not valid. Command format: /em subtract [playerName] [amount]");

                    }

                    return true;

                }

                if (args[0].equals("set") && permCheck(CURRENCY_SET, commandSender)) {

                    try {

                        CurrencyCommandsHandler.setCommand(args[1], Integer.parseInt(args[2]));

                        commandSender.sendMessage("You set " + args[1] + "'s " + ConfigValues.economyConfig.get("Currency name") + " to " + args[2]);

                    } catch (Exception e) {

                        commandSender.sendMessage("Input not valid. Command format: /em set [playerName] [amount]");

                    }

                    return true;

                }

                validCommands(commandSender);
                return true;

            //invalid commands
            default:

                validCommands(commandSender);
                return true;

        }

    }

    private boolean permCheck(String permission, CommandSender commandSender) {

        if (commandSender.hasPermission(permission)) {

            return true;

        }

        if (commandSender instanceof Player &&
                Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getConfig().getBoolean(DefaultConfig.ENABLE_PERMISSION_TITLES)) {

            Player player = (Player) commandSender;

            player.sendTitle("I'm afraid I can't let you do that, " + player.getDisplayName() + ".",
                    "You need the following permission: " + permission);

        } else {

            commandSender.sendMessage("[EliteMobs] You may not run this command.");
            commandSender.sendMessage("[EliteMobs] You don't have the permission " + permission);

        }

        return false;

    }

    private boolean userPermCheck(String permission, CommandSender commandSender) {

        if (commandSender instanceof Player) {

            return permCheck(permission, commandSender);

        }

        commandSender.sendMessage("[EliteMobs] You may not run this command.");
        commandSender.sendMessage("[EliteMobs] This is a user command.");
        return false;

    }

    private void validCommands(CommandSender commandSender) {

        if (commandSender instanceof Player) {

            Player player = (Player) commandSender;

            player.sendMessage("[EliteMobs] Valid commands:");
            if (silentPermCheck(STATS, commandSender)) {
                player.sendMessage("/elitemobs stats");
            }
            if (silentPermCheck(SHOP, commandSender)) {
                player.sendMessage("/elitemobs shop");
            }
            if (silentPermCheck(CUSTOMSHOP, commandSender)) {
                player.sendMessage("/elitemobs customshop");
            }
            if (silentPermCheck(CURRENCY_WALLET, commandSender)) {
                player.sendMessage("/elitemobs wallet");
            }
            if (silentPermCheck(CURRENCY_COINTOP, commandSender)) {
                player.sendMessage("/elitemobs cointop");
            }
            if (silentPermCheck(CURRENCY_PAY, commandSender)) {
                player.sendMessage("/elitemobs pay [username]");
            }
            if (silentPermCheck(CURRENCY_ADD, commandSender)) {
                player.sendMessage("/elitemobs add [username]");
            }
            if (silentPermCheck(CURRENCY_SUBTRACT, commandSender)) {
                player.sendMessage("/elitemobs subtract [username]");
            }
            if (silentPermCheck(CURRENCY_SET, commandSender)) {
                player.sendMessage("/elitemobs set [username]");
            }
            if (silentPermCheck(CURRENCY_CHECK, commandSender)) {
                player.sendMessage("/elitemobs check [username]");
            }
            if (silentPermCheck(RELOAD_CONFIGS, commandSender)) {
                player.sendMessage("/elitemobs reload configs");
            }
            if (silentPermCheck(RELOAD_LOOT, commandSender)) {
                player.sendMessage("/elitemobs reload loot");
            }
            if (silentPermCheck(KILLALL_AGGRESSIVEELITES, commandSender)) {
                player.sendMessage("/elitemobs killall aggressiveelites");
            }
            if (silentPermCheck(KILLALL_PASSIVEELITES, commandSender)) {
                player.sendMessage("/elitemobs killall passiveelites");
            }
            if (silentPermCheck(SIMLOOT, commandSender)) {
                player.sendMessage("/elitemobs simloot [mob level]");
            }
            if (silentPermCheck(GETLOOT, commandSender)) {
                player.sendMessage("/elitemobs getloot [loot name (no loot name = GUI)]");
            }
            if (silentPermCheck(GIVELOOT, commandSender)) {
                player.sendMessage("/elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");
            }
            if (silentPermCheck(SPAWNMOB, commandSender)) {
                player.sendMessage("/elitemobs SpawnMob [mobType] [mobLevel] [mobPower] [mobPower2(keep adding as many as you'd like)]");
            }


        } else if (commandSender instanceof ConsoleCommandSender) {

            commandSender.sendMessage("[EliteMobs] Command not recognized. Valid commands:");
            commandSender.sendMessage("elitemobs stats");
            commandSender.sendMessage("elitemobs reload loot");
            commandSender.sendMessage("elitemobs reload configs");
            commandSender.sendMessage("elitemobs check [username]");
            commandSender.sendMessage("elitemobs set [username]");
            commandSender.sendMessage("elitemobs add [username]");
            commandSender.sendMessage("elitemobs subtract [username]");
            commandSender.sendMessage("elitemobs killall passiveelites");
            commandSender.sendMessage("elitemobs killall aggressiveelites");
            commandSender.sendMessage("elitemobs giveloot [player name] random/[loot_name_underscore_for_spaces]");
            commandSender.sendMessage("elitemobs SpawnMob [worldName] [x] [y] [z] [mobType] [mobLevel] [mobPower] [mobPower2(keep adding as many as you'd like)]");

        }

    }

    private boolean silentPermCheck(String permission, CommandSender commandSender) {

        return commandSender.hasPermission(permission);

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
