package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class DismissCommand extends AdvancedCommand {
    public DismissCommand() {
        super(List.of("dismiss"));
        setDescription("Dismisses /em menu message.");
        setUsage("/em dismiss");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PlayerData.setDismissEMStatusScreenMessage(commandData.getPlayerSender(), !PlayerData.getDismissEMStatusScreenMessage(commandData.getPlayerSender().getUniqueId()));
    }
}
