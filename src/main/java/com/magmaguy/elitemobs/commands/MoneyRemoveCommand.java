package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class MoneyRemoveCommand extends AdvancedCommand {
    public MoneyRemoveCommand() {
        super(List.of("money"));
        addLiteral("remove");
        addArgument("player", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("amount", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        setUsage("/em money remove <player> <amount>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Deducts the specified amount of currency from a player.");
    }

    @Override
    public void execute() {
        CurrencyCommandsHandler.subtractCommand(
                getStringArgument("player"),
                getDoubleArgument("amount"));
    }
}