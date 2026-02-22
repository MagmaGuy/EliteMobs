package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.*;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;

public class SpawnEliteAtCommand extends AdvancedCommand {
    public SpawnEliteAtCommand() {
        super(List.of("spawn"));
        addLiteral("eliteAt");
        addArgument("world", new WorldCommandArgument("<world>"));
        addArgument("x", new DoubleCommandArgument("<x>"));
        addArgument("y", new DoubleCommandArgument("<y>"));
        addArgument("z", new DoubleCommandArgument("<z>"));
        addArgument("entityType", new EntityTypeCommandArgument());
        addArgument("level", new IntegerCommandArgument("<level>"));
        addArgument("powers", new ListStringCommandArgument(ElitePower.getElitePowers().keySet().stream().toList(), "<powers>"));
        setUsage("/em spawn eliteAt <world> <x> <y> <z> <entityType> <level> <power1> <power2> <power3> <...>");
        setPermission("elitemobs.place.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns an elite of the specified type and level, optionally with specified powers, at the chosen location.");
    }

    @Override
    public void execute(CommandData commandData) {
        String powersArg = commandData.getStringSequenceArgument("powers");
        Optional<String> powers = (powersArg == null || powersArg.isBlank()) ? Optional.empty() : Optional.of(powersArg.trim());
        SpawnCommand.spawnEliteEntityTypeCommand(
                commandData.getPlayerSender(),
                EntityType.valueOf(commandData.getStringArgument("entityType")),
                commandData.getStringArgument("world"),
                new Vector(
                        commandData.getIntegerArgument("x"),
                        commandData.getIntegerArgument("y"),
                        commandData.getIntegerArgument("z")),
                commandData.getIntegerArgument("level"),
                powers);
    }
}