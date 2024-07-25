package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class PayCommand extends AdvancedCommand {
    public PayCommand() {
        super(List.of("pay"));
        addArgument("player", new ArrayList<>());
        addArgument("amount", new ArrayList<>());
        setDescription("Send money to a player, minus tax.");
        setUsage("/em pay <player> <amount>");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        CurrencyCommandsHandler.payCommand(
                getCurrentPlayerSender(),
                getStringArgument("player"),
                getDoubleArgument("amount"));
    }
}