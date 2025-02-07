package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class MoneyRemoveCommand extends AdvancedCommand {
    public MoneyRemoveCommand() {
        super(List.of("money"));
        addLiteral("remove");
        addArgument("player", new PlayerCommandArgument());
        addArgument("amount", new DoubleCommandArgument("<amount>"));
        setUsage("/em money remove <player> <amount>");
        setPermission("elitemobs.money.admin");
        setDescription("Deducts the specified amount of currency from a player.");
    }

    @Override
    public void execute(CommandData commandData) {
        CurrencyCommandsHandler.subtractCommand(
                commandData.getStringArgument("player"),
                commandData.getDoubleArgument("amount"));
    }
}