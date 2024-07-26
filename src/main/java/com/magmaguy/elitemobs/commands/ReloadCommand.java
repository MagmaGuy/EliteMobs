package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends AdvancedCommand {
    public ReloadCommand() {
        super(List.of("reload"));
        setUsage("/em reload");
        setPermission("elitemobs.reload");
        setDescription("Reloads EliteMobs.");
    }

    public static void reload(CommandSender commandSender) {
        MetadataHandler.PLUGIN.onDisable();
        MetadataHandler.PLUGIN.onLoad();
        MetadataHandler.PLUGIN.onEnable();
        Logger.sendMessage(commandSender, "Plugin reloaded!");
    }

    @Override
    public void execute(CommandData commandData) {
        reload(commandData.getCommandSender());
    }
}