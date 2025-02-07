package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.WorldCommandArgument;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SpawnBossAtCommand extends AdvancedCommand {
    public SpawnBossAtCommand() {
        super(List.of("spawn"));
        addLiteral("bossAt");
        addArgument("filename", new ListStringCommandArgument(new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()), "<filename>"));
        addArgument("worldName", new WorldCommandArgument("<worldName>"));
        addArgument("x", new DoubleCommandArgument("<x>"));
        addArgument("y", new DoubleCommandArgument("<y>"));
        addArgument("z", new DoubleCommandArgument("<z>"));
        setUsage("/em spawn bossAt <filename> <worldName> <x> <y> <z>");
        setPermission("elitemobs.place.admin");
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
