package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.ArenaMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class ArenaCommand extends AdvancedCommand {
    public ArenaCommand() {
        super(List.of("arena"));
        addArgument("arenaID", new ListStringCommandArgument("Arena ID (you can't run this manually!)"));
        setUsage("/em arena <arenaID>");
        setPermission("elitemobs.arena.start");
        setDescription("Open the Arena menu.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        new ArenaMenu().constructArenaMenu(commandData.getPlayerSender(), commandData.getStringArgument("arenaID"));
    }
}
