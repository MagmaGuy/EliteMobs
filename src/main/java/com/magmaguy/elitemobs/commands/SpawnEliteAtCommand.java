package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpawnEliteAtCommand extends AdvancedCommand {
    public SpawnEliteAtCommand() {
        super(List.of("spawn"));
        addLiteral("eliteAt");
        addArgument("world", new ArrayList<>());
        addArgument("x", new ArrayList<>());
        addArgument("y", new ArrayList<>());
        addArgument("z", new ArrayList<>());
        addArgument("entityType", new ArrayList<>());
        addArgument("level", new ArrayList<>());
        addArgument("powers", ElitePower.getElitePowers().keySet().stream().toList());
        setUsage("/em spawn eliteAt <world> <x> <y> <z> <entityType> <level> <power1> <power2> <power3> <...>");
        setPermission("elitemobs.place.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns an elite of the specified type and level, optionally with specified powers, at the chosen location.");
    }

    @Override
    public void execute(CommandData commandData) {
        SpawnCommand.spawnEliteEntityTypeCommand(
                commandData.getPlayerSender(),
                EntityType.valueOf(commandData.getStringArgument("entityType")),
                commandData.getStringArgument("world"),
                new Vector(
                        commandData.getIntegerArgument("x"),
                        commandData.getIntegerArgument("y"),
                        commandData.getIntegerArgument("z")),
                commandData.getIntegerArgument("level"),
                Optional.of(commandData.getStringSequenceArgument("powers")));
    }
}