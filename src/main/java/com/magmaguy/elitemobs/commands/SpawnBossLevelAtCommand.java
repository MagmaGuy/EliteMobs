package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.WorldCommandArgument;
import org.bukkit.util.Vector;

import java.util.List;

public class SpawnBossLevelAtCommand extends AdvancedCommand {
    public SpawnBossLevelAtCommand() {
        super(List.of("spawn"));
        addLiteral("bossAt");
        addArgument("filename", new ListStringCommandArgument(CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
        addArgument("worldName", new WorldCommandArgument("<worldName>"));
        addArgument("x", new IntegerCommandArgument("<x>"));
        addArgument("y", new IntegerCommandArgument("<y>"));
        addArgument("z", new IntegerCommandArgument("<z>"));
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em spawn bossAt <filename> <worldName> <x> <y> <z> <level>");
        setPermission("elitemobs.place.admin");
        setDescription("Spawns a custom boss at the specified location with the specified level.");
    }

    @Override
    public void execute(CommandData commandData) {
        SpawnCommand.spawnCustomBossCommand(
                commandData.getCommandSender(),
                commandData.getStringArgument("filename"),
                commandData.getStringArgument("worldName"),
                new Vector(
                        commandData.getDoubleArgument("x"),
                        commandData.getDoubleArgument("y"),
                        commandData.getDoubleArgument("z")),
                commandData.getIntegerArgument("level"));
    }
}
