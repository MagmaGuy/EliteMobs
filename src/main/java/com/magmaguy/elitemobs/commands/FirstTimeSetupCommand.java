package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.setup.EliteFirstTimeSetupMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class FirstTimeSetupCommand extends AdvancedCommand {
    public FirstTimeSetupCommand() {
        super(List.of("initialize"));
        setUsage("/em initialize");
        setPermission("elitemobs.initialize");
        setDescription("Does the first time setup of the plugin.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        EliteFirstTimeSetupMenu.createMenu(commandData.getPlayerSender());
    }
}
