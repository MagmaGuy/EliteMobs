package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnBossAtCommand extends AdvancedCommand {
    public SpawnBossAtCommand() {
        super(List.of("spawn"));
        addLiteral("bossAt");
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        addArgument("worldName", Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList()));
        addArgument("x", new ArrayList<>());
        addArgument("y", new ArrayList<>());
        addArgument("z", new ArrayList<>());
        setUsage("/em spawn bossAt <filename> <worldName> <x> <y> <z>");
        setPermission("elitemobs.spawn.boss.at");
        setDescription("Spawns a custom boss at the specified location.");
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
                        commandData.getDoubleArgument("z")));
    }
}
