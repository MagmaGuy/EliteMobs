package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class AltCommand extends AdvancedCommand {
    public AltCommand() {
        super(List.of("alt"));
        setUsage("/em alt");
        setDescription("Changes the style of the /em menu.");
        setPermission("elitemobs.alt");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PlayerData.setUseBookMenus(commandData.getPlayerSender(), !PlayerData.getUseBookMenus(commandData.getPlayerSender().getUniqueId()));
        Logger.sendMessage(commandData.getCommandSender(), DefaultConfig.getSwitchEMStyleMessage());
    }
}
