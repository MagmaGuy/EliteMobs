package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class SpawnBossLevelCommand extends AdvancedCommand {
    public SpawnBossLevelCommand() {
        super(List.of("spawn"));
        addLiteral("boss");
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        addArgument("level", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19));
        setUsage("/em spawn boss <filename> <level>");
        setPermission("elitemobs.spawn.boss.level");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns a custom boss at the location the user is looking at with the specified level.");
    }

    @Override
    public void execute(CommandData commandData) {
        SpawnCommand.spawnCustomBossCommand(
                commandData.getPlayerSender(),
                commandData.getStringArgument("filename"),
                commandData.getIntegerArgument("level"));
    }
}
