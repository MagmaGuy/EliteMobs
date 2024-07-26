package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.ArenaMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class ArenaCommand extends AdvancedCommand {
    public ArenaCommand() {
        super(List.of("arena"));
        addArgument("arenaID", new ArrayList<>());
        setUsage("/em arena <arenaID>");
        setPermission("elitemobs.event.start");
        setDescription("Open the Arena menu.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        new ArenaMenu().constructArenaMenu(commandData.getPlayerSender(), commandData.getStringArgument("arenaID"));
    }
}
