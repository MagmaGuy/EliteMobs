package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class MoneySetCommand extends AdvancedCommand {
    public MoneySetCommand() {
        super(List.of("money"));
        addLiteral("set");
        addArgument("player", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("amount", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        setUsage("/em money set <player> <amount>");
        setPermission("elitemobs.money.set");
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