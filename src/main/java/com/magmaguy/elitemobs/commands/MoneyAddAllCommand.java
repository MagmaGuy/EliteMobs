package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class MoneyAddAllCommand extends AdvancedCommand {
    public MoneyAddAllCommand() {
        super(List.of("money"));
        addLiteral("add");
        addLiteral("all");
        addArgument("amount", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        setUsage("/em money add all <amount>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Simulates loot drops for the specified amount of times for the specified level and player.");
    }

    @Override
    public void execute() {
        CurrencyCommandsHandler.addAllCommand(
                getCurrentCommandSender(),
                getDoubleArgument("amount"));
    }
}