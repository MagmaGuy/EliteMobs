package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.commands.setup.SetupMenu;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetupHandler {

    private String regionName = "";
    private Player player;

    public SetupHandler(Player player, String[] args) {

        if (args.length == 1) {
            new SetupMenu(player);
            return;
        }

        if (!EliteMobs.worldguardIsEnabled) {
            player.sendMessage("[EliteMobs] You don't have WorldGuard installed! It is not possible to correctly set " +
                    "up a lair/minidungeon/dungeon without that plugin!");
            return;
        }

        //Syntax: /em setup area [regionName]

        if (args[1].equals("minidungeon")) {
            if (args.length == 4 && args[3].equalsIgnoreCase("noPaste")) {
                Minidungeon minidungeon = Minidungeon.minidungeons.get(args[2]);
                minidungeon.finalizeMinidungeonInstallation(player, false);
                return;
            }

            Minidungeon minidungeon = Minidungeon.minidungeons.get(args[2]);
            minidungeon.finalizeMinidungeonInstallation(player, true);
            player.performCommand("/rotate " + minidungeon.dungeonPackagerConfigFields.getRotation());
            player.performCommand("/paste");
            return;
        }

        if (args[1].equals("unminidungeon")) {
            Minidungeon minidungeon = Minidungeon.minidungeons.get(args[2]);
            minidungeon.uninstallSchematicMinidungeon(player);
            if (!(args.length == 4 && args[3].equalsIgnoreCase("noPaste")))
                player.performCommand("/undo");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("[EliteMobs] Invalid command syntax! Expected syntax:");
            player.sendMessage("/em setup area [regionName]");
            player.sendMessage("[regionName] is the name of your WorldGuard protected cuboid! Create the region first!");
            return;
        }

        regionName = args[2];
        this.player = player;
        protectArea(player, regionName);
    }

    public static void protectArea(Player player, String regionName) {
        if (!WorldGuardCompatibility.protectMinidungeonArea(regionName, player.getLocation())) {
            player.sendMessage(ChatColorConverter.convert("&4[EliteMobs] Failed to protect region! Was the region name correct?"));
            //worldguardextraflags
            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuardExtraFlags")) {
                commandPackage("&2", "fly", "deny", player, regionName);
            } else {
                player.sendMessage(ChatColor.RED + "[EliteMobs] Warning: the WorldGuardExtraFlags plugin is not present. It is recommended for the use of the anti-flight flag.");
            }

            player.performCommand("rg info");
        } else
            player.sendMessage(ChatColorConverter.convert("&2[EliteMobs] Set all WorldGuard flags correctly!"));

    }

    private static void commandPackage(String colorCode, String flagString, String state, Player player, String regionName) {
        player.sendMessage(flagString(colorCode + flagString, player));
        player.performCommand(commandString(flagString, state, player, regionName));
    }

    private static String flagString(String flagString, Player player) {
        return ChatColorConverter.convert("&a[EliteMobs] Adding flag " + flagString);
    }

    private static String commandString(String flagString, String state, Player player, String regionName) {
        return "region flag " + regionName + " " + flagString + " " + state;
    }

}
