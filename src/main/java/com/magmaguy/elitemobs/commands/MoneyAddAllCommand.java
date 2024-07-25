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
        setPermission("elitemobs.money.add.all");
        setSenderType(SenderType.PLAYER);
        setDescription("Gives every player on the server the specified amount of money.");
    }

    @Override
    public void execute() {
        CurrencyCommandsHandler.addAllCommand(
                getCurrentCommandSender(),
                getDoubleArgument("amount"));
    }
}