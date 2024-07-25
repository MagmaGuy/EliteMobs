package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class ArenaCommand extends AdvancedCommand {
    public ArenaCommand() {
        super(List.of("arena"));
        addArgument("arenaID", new ArrayList<>());
        setUsage("/em arena <arenaID>");
        setDescription("When in instanced content, makes the player start the instance.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        ArenaCommands.openArenaMenu(getCurrentPlayerSender(), getStringArgument("arenaID"));
    }
}
