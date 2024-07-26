package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;

public class HelpCommand extends AdvancedCommand {
    public HelpCommand() {
        super(List.of("help"));
        setUsage("/em help");
        setPermission("elitemobs.help");
        setDescription("Lists all commands.");
    }

    @Override
    public void execute(CommandData commandData) {
        Logger.sendMessage(commandData.getCommandSender(), "Commands:");
        CommandManager.getInstance().commands.forEach(command -> {
            if (commandData.getCommandSender() instanceof Player player) {
                player.spigot().sendMessage(Logger.hoverMessage(command.getUsage(), command.getDescription()));
            } else {
                Logger.sendSimpleMessage(commandData.getCommandSender(), command.getUsage());
                Logger.sendSimpleMessage(commandData.getCommandSender(), command.getDescription());
            }
        });
    }
}