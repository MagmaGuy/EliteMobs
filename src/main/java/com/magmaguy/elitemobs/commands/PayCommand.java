package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
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
        setPermission("elitemobs.money.pay");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        CurrencyCommandsHandler.payCommand(
               commandData.getPlayerSender(),
                commandData.getStringArgument("player"),
                commandData.getDoubleArgument("amount"));
    }
}