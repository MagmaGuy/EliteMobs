package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.setup.SetupMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class SetupCommand extends AdvancedCommand {
    public SetupCommand() {
        super(List.of("setup"));
        setPermission("elitemobs.setup.main");
        setSenderType(SenderType.PLAYER);
        setDescription("The main command for setting up EliteMobs!");
        setUsage("/em setup");
    }

    @Override
    public void execute(CommandData commandData) {
        new SetupMenu(commandData.getPlayerSender());
    }
}
