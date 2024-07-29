package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class MoneyAddCommand extends AdvancedCommand {
    public MoneyAddCommand() {
        super(List.of("money"));
        addLiteral("add");
        addArgument("player", new ArrayList<>());
        addArgument("amount", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        setUsage("/em money add <player> <amount>");
        setPermission("elitemobs.money.add.player");
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