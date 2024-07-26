package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class MoneyRemoveCommand extends AdvancedCommand {
    public MoneyRemoveCommand() {
        super(List.of("money"));
        addLiteral("remove");
        addArgument("player", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("amount", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        setUsage("/em money remove <player> <amount>");
        setPermission("elitemobs.money.remove");
        setDescription("Deducts the specified amount of currency from a player.");
    }

    @Override
    public void execute(CommandData commandData) {
        CurrencyCommandsHandler.subtractCommand(
                commandData.getStringArgument("player"),
                commandData.getDoubleArgument("amount"));
    }
}