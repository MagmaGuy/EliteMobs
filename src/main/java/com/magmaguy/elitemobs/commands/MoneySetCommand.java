package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class MoneySetCommand extends AdvancedCommand {
    public MoneySetCommand() {
        super(List.of("money"));
        addLiteral("set");
        addArgument("player", new PlayerCommandArgument());
        addArgument("amount", new DoubleCommandArgument("<amount>"));
        setUsage("/em money set <player> <amount>");
        setPermission("elitemobs.money.admin");
        setDescription("Sets the specified amount of currency for the specified player.");
    }

    @Override
    public void execute(CommandData commandData) {
        CurrencyCommandsHandler.setCommand(
                commandData.getCommandSender(),
                commandData.getStringArgument("player"),
                commandData.getDoubleArgument("amount"));
    }
}