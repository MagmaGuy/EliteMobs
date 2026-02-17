package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
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
        Logger.sendMessage(commandSender, CommandMessagesConfig.getReloadStartMessage());
        MetadataHandler.pendingReloadSender = commandSender;
        MetadataHandler.PLUGIN.onDisable();
        MetadataHandler.PLUGIN.onLoad();
        MetadataHandler.PLUGIN.onEnable();
    }

    @Override
    public void execute(CommandData commandData) {
        reload(commandData.getCommandSender());
    }
}