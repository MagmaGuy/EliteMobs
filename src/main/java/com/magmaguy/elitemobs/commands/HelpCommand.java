package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
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
    public void execute() {
        Logger.sendMessage(getCurrentCommandSender(), "Commands:");
        CommandManager.getInstance().commands.forEach(command -> {
            if (getCurrentCommandSender() instanceof Player player) {
                player.spigot().sendMessage(Logger.hoverMessage(command.usage, command.description));
            } else {
                Logger.sendSimpleMessage(getCurrentCommandSender(), command.usage);
                Logger.sendSimpleMessage(getCurrentCommandSender(), command.description);
            }
        });
    }
}