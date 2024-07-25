package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class MoneyCheckCommand extends AdvancedCommand {
    public MoneyCheckCommand() {
        super(List.of("money"));
        addLiteral("check");
        setUsage("/em money check");
        setPermission("elitemobs.money.check");
        setSenderType(SenderType.PLAYER);
        setDescription("Checks the EliteMobs currency");
    }

    @Override
    public void execute() {
        if (DefaultConfig.isOtherCommandsLeadToEMStatusMenu())
            new PlayerStatusScreen(getCurrentPlayerSender());
        else
            CurrencyCommandsHandler.walletCommand(getCurrentPlayerSender());
    }
}
