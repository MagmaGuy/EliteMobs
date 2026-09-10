package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.menu.ClassSelectionMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public final class AdvancedClassCommand extends AdvancedCommand {
    public AdvancedClassCommand() {
        super(List.of("class"));
        setUsage("/em class");
        setDescription("Shows your [Alpha] Advanced Combat System class profile.");
        setPermission("elitemobs.command");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        ClassSelectionMenu.open(commandData.getPlayerSender());
    }
}
