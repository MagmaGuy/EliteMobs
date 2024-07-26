package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class SpawnBossCommand extends AdvancedCommand {
    public SpawnBossCommand() {
        super(List.of("spawn"));
        addLiteral("boss");
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        setUsage("/setup spawn boss <filename>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Spawns a custom boss at the location the user is looking at.");
    }

    @Override
    public void execute(CommandData commandData) {
        SpawnCommand.spawnCustomBossCommand(
                commandData.getPlayerSender(),
                commandData.getStringArgument("filename"));
    }
}
