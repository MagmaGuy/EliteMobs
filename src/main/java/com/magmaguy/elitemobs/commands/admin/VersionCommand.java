package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VersionCommand {

    public static void getVersion(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.DARK_GREEN + "[EliteMobs]" + ChatColor.WHITE + " version " + ChatColor.GREEN +
                Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion());
    }

}
