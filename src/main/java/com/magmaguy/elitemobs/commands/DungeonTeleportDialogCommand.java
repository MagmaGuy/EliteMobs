package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class DungeonTeleportDialogCommand extends AdvancedCommand {
    public DungeonTeleportDialogCommand() {
        super(List.of("dungeontpdialog"));
        addArgument("dungeonID", new ListStringCommandArgument("Dungeon ID (dialog navigation only)"));
        setPermission("elitemobs.dungeon.tp");
        setDescription("Internal dialog teleport navigation command.");
        setUsage("/em dungeontpdialog <dungeonID>");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        DungeonCommands.teleport(commandData.getPlayerSender(),
                commandData.getStringArgument("dungeonID"),
                DungeonCommands.TeleportMenuSource.DIALOGUE);
    }
}
