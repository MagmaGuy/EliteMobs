package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.pets.PetsMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class PetsCommand extends AdvancedCommand {
    public PetsCommand() {
        super(List.of("pets"));
        setUsage("/em pets");
        setPermission("elitemobs.pets");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens your pets: bring one along or send it home.");
    }

    @Override
    public void execute(CommandData commandData) {
        PetsMenu.open(commandData.getPlayerSender());
    }
}
