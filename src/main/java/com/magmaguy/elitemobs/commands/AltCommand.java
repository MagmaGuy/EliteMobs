package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class AltCommand extends AdvancedCommand {
    public AltCommand() {
        super(List.of("alt"));
        setUsage("/em alt");
        setDescription("Changes the style of the /em menu.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        PlayerData.setUseBookMenus(getCurrentPlayerSender(), !PlayerData.getUseBookMenus(getCurrentPlayerSender().getUniqueId()));
        Logger.sendMessage(getCurrentCommandSender(), DefaultConfig.getSwitchEMStyleMessage());
    }
}
