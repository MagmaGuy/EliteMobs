package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.List;

public class VersionCommand extends AdvancedCommand {
    public VersionCommand() {
        super(List.of("version"));
        setUsage("/em version");
        setPermission("elitemobs.*");
        setDescription("Checks the server's plugin version.");
    }

    @Override
    public void execute() {
        Logger.sendMessage(
                getCurrentCommandSender(),
                ChatColor.WHITE + " version " + ChatColor.GREEN + Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS).getDescription().getVersion());
    }
}