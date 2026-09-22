package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;

import java.util.List;

public class MoneyAddAllCommand extends AdvancedCommand {
    public MoneyAddAllCommand() {
        super(List.of("money"));
        addLiteral("addAll");
        addArgument("amount", new IntegerCommandArgument("<amount>"));
        setUsage("/em money addAll <amount>");
        setPermission("elitemobs.money.admin");
        setDescription("Gives every player on the server the specified amount of money.");
    }

    @Override
    public void execute(CommandData commandData) {
        Double parsedAmount = commandData.getDoubleArgument("amount");
        if (parsedAmount == null) return;
        CurrencyCommandsHandler.addAllCommand(
                commandData.getCommandSender(),
                parsedAmount);
    }
}