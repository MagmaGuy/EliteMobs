package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class DungeonTeleportCommand extends AdvancedCommand {
    public DungeonTeleportCommand() {
        super(List.of("dungeontp"));
        addArgument("dungeonID", new ArrayList<>());
        setPermission("elitemobs.dungeontp");
        setDescription("Teleports players to Lairs, Minidungeons and Dungeons.");
        setUsage("/em dungeontp <dungeonID>");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        DungeonCommands.teleport(getCurrentPlayerSender(), getStringArgument("dungeonID"));
    }
}
