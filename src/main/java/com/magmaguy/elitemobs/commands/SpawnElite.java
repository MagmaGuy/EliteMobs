package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;

import java.util.List;
import java.util.Optional;

public class SpawnElite extends AdvancedCommand {
    public SpawnElite() {
        super(List.of("spawn"));
        addLiteral("elite");
        addArgument("entityType", new EliteEntityTypeCommandArgument());
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em spawn elite <entityType> <level>");
        setPermission("elitemobs.place.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns an elite of the specified type and level.");
    }

    @Override
    public void execute(CommandData commandData) {
        SpawnCommand.spawnEliteEntityTypeCommand(
                commandData.getPlayerSender(),
                EliteEntityTypeCommandArgument.parse(commandData.getStringArgument("entityType")),
                commandData.getIntegerArgument("level"),
                Optional.empty());
    }
}