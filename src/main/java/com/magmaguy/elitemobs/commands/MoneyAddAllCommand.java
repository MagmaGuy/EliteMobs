package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class MoneyAddAllCommand extends AdvancedCommand {
    public MoneyAddAllCommand() {
        super(List.of("money"));
        addLiteral("addAll");
        addArgument("amount", new ArrayList<>());
        setUsage("/em money addAll <amount>");
        setPermission("elitemobs.money.admin");
        setDescription("Gives every player on the server the specified amount of money.");
    }

    @Override
    public void execute(CommandData commandData) {
        CurrencyCommandsHandler.addAllCommand(
                commandData.getCommandSender(),
                commandData.getDoubleArgument("amount"));
    }
}