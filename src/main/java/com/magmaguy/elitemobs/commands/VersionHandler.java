package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VersionHandler {

    public static void versionCommand(CommandSender commandSender, String[] args){

        commandSender.sendMessage(ChatColor.DARK_GREEN + "[EliteMobs]" + ChatColor.WHITE + " version " + ChatColor.GREEN +
                Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion() + " (1.14 edition)");

    }

}
