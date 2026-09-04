package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.menu.ClassSelectionMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public final class ExperimentalClassCommand extends AdvancedCommand {
    public ExperimentalClassCommand() {
        super(List.of("class"));
        setUsage("/em class");
        setDescription("Shows your Experimental Combat class profile.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        ClassSelectionMenu.open(commandData.getPlayerSender());
    }
}
