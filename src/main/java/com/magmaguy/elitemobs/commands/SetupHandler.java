package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.commands.setup.SetupMenu;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetupHandler {

    public static void setupMenuCommand(Player player) {
        new SetupMenu(player);
    }

    public static void setupMinidungeonCommand(Player player, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        emPackage.install(player, true);
    }

    public static void setupMinidungeonNoPasteCommand(Player player, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        emPackage.install(player, false);
    }

    public static void setupUnminidungeonCommand(Player player, String minidungeonName) {
        EMPackage minidungeon = EMPackage.getEmPackages().get(minidungeonName);
        minidungeon.uninstall(player);
        player.performCommand("/undo");
    }

    public static void setupAreaCommand(Player player, String regionName) {
        if (!EliteMobs.worldGuardIsEnabled) {
            player.sendMessage("[EliteMobs] You don't have WorldGuard installed! It is not possible to correctly set " +
                    "up a lair/minidungeon/dungeon without that plugin!");
            return;
        }
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
