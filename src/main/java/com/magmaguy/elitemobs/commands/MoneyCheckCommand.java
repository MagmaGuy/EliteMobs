package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class MoneyCheckCommand extends AdvancedCommand {
    public MoneyCheckCommand() {
        super(List.of("money"));
        addLiteral("check");
        setUsage("/em money check");
        setPermission("elitemobs.money.check.self");
        setSenderType(SenderType.PLAYER);
        setDescription("Checks your EliteMobs currency.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (DefaultConfig.isOtherCommandsLeadToEMStatusMenu())
            new PlayerStatusScreen(commandData.getPlayerSender());
        else
            CurrencyCommandsHandler.walletCommand(commandData.getPlayerSender());
    }
}
