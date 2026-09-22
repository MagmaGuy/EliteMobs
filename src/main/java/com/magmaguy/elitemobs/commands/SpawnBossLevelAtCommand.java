package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.magmacore.command.arguments.WorldCommandArgument;
import org.bukkit.util.Vector;

import java.util.List;

public class SpawnBossLevelAtCommand extends AdvancedCommand {
    public SpawnBossLevelAtCommand() {
        super(List.of("spawn"));
        addLiteral("bossAt");
        addArgument("filename", new DynamicListStringCommandArgument(() -> CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
        addArgument("worldName", new WorldCommandArgument("<worldName>"));
        addArgument("x", new DoubleCommandArgument("<x>"));
        addArgument("y", new DoubleCommandArgument("<y>"));
        addArgument("z", new DoubleCommandArgument("<z>"));
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em spawn bossAt <filename> <worldName> <x> <y> <z> <level>");
        setPermission("elitemobs.place.admin");
        setDescription("Spawns a custom boss at the specified location with the specified level.");
    }

    @Override
    public void execute(CommandData commandData) {
        Double parsedX = commandData.getDoubleArgument("x");
        Double parsedY = commandData.getDoubleArgument("y");
        Double parsedZ = commandData.getDoubleArgument("z");
        Integer parsedLevel = commandData.getIntegerArgument("level");
        if (parsedX == null || parsedY == null || parsedZ == null || parsedLevel == null) return;
        SpawnCommand.spawnCustomBossCommand(
                commandData.getCommandSender(),
                commandData.getStringArgument("filename"),
                commandData.getStringArgument("worldName"),
                new Vector(
                        parsedX,
                        parsedY,
                        parsedZ),
                parsedLevel);
    }
}
