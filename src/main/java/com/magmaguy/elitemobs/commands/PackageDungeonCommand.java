package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class PackageDungeonCommand extends AdvancedCommand {
    public PackageDungeonCommand() {
        super(List.of("package"));
        addArgument("dungeonName", new ArrayList<>());
        addArgument("version", new ArrayList<>());
        setUsage("/em package <dungeonName> <version>");
        setPermission("elitemobs.package");
        setDescription("Packages an EliteMobs dungeon for distribution.");
    }

    @Override
    public void execute(CommandData commandData) {
        new PackageCommand(commandData.getCommandSender(),
                commandData.getStringArgument("dungeonName"),
                commandData.getStringArgument("version"));
    }
}