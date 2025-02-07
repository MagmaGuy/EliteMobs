package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class MoneyAddCommand extends AdvancedCommand {
    public MoneyAddCommand() {
        super(List.of("money"));
        addLiteral("add");
        addArgument("player" ,new PlayerCommandArgument());
        addArgument("amount", new IntegerCommandArgument("<amount>"));
        setUsage("/em money add <player> <amount>");
        setPermission("elitemobs.money.admin");
        setDescription("Gives the specified amount of money to the designated player.");
    }

    @Override
    public void execute(CommandData commandData) {
        CurrencyCommandsHandler.addCommand(
                commandData.getCommandSender(),
                commandData.getStringArgument("player"),
                commandData.getDoubleArgument("amount"));
    }
}