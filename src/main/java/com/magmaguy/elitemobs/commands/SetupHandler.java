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

    public static void setupMenuCommand(Player player) {
        new SetupMenu(player);
    }

    public static void setupMinidungeonCommand(Player player, String minidungeonName) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonName);
        minidungeon.finalizeMinidungeonInstallation(player, true);
        player.performCommand("/rotate " + minidungeon.dungeonPackagerConfigFields.getRotation());
        player.performCommand("/paste");
    }

    public static void setupMinidungeonNoPasteCommand(Player player, String minidungeonName) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonName);
        minidungeon.finalizeMinidungeonInstallation(player, false);
    }

    public static void setupUnminidungeonCommand(Player player, String minidungeonName) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonName);
        minidungeon.uninstallSchematicMinidungeon(player);
        player.performCommand("/undo");
    }

    public static void setupUnminidungeonNoPasteCommand(Player player, String minidungeonName) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonName);
        minidungeon.finalizeMinidungeonInstallation(player, false);
    }

    public static void setupAreaCommand(Player player, String regionName) {
        if (!EliteMobs.worldguardIsEnabled) {
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
