package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;

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
    public void execute() {
        new PackageCommand(getCurrentCommandSender(), getStringArgument("dungeonName"), getStringArgument("version"));
    }
}