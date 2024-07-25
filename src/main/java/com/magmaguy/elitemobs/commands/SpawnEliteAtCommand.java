package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.command.AdvancedCommand;
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
        setUsage("/em spawn elite <entityType> <level> <power1> <power2> <power3> <...>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns an elite of the specified type and level, and optionally with the specified powers.");
    }

    @Override
    public void execute() {
        SpawnCommand.spawnEliteEntityTypeCommand(
                getCurrentPlayerSender(),
                EntityType.valueOf(getStringArgument("entityType")),
                getStringArgument("world"),
                new Vector(
                        getIntegerArgument("x"),
                        getIntegerArgument("y"),
                        getIntegerArgument("z")),
                getIntegerArgument("level"),
                Optional.of(getStringSequenceArgument("powers")));
    }
}