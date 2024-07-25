package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnBossLevelAtCommand extends AdvancedCommand {
    public SpawnBossLevelAtCommand() {
        super(List.of("spawn"));
        addLiteral("bossAt");
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        addArgument("worldName", Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList()));
        addArgument("x", new ArrayList<>());
        addArgument("y", new ArrayList<>());
        addArgument("z", new ArrayList<>());
        addArgument("level", new ArrayList<>());
        setUsage("/setup spawn bossAt <filename> <worldName> <x> <y> <z> <level>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns a custom boss at the location the user is looking at.");
    }

    @Override
    public void execute() {
        SpawnCommand.spawnCustomBossCommand(
                getCurrentPlayerSender(),
                getStringArgument("filename"),
                getStringArgument("worldName"),
                new Vector(
                        getDoubleArgument("x"),
                        getDoubleArgument("y"),
                        getDoubleArgument("z")),
                getIntegerArgument("level"));
    }
}
