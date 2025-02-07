package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class SpawnBossLevelCommand extends AdvancedCommand {
    public SpawnBossLevelCommand() {
        super(List.of("spawn"));
        addLiteral("boss");
        addArgument("filename", new ListStringCommandArgument(CustomBossesConfig.getCustomBosses().keySet().stream().toList(),"<filename>"));
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em spawn boss <filename> <level>");
        setPermission("elitemobs.place.admin");
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
