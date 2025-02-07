package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class PackageDungeonCommand extends AdvancedCommand {
    public PackageDungeonCommand() {
        super(List.of("package"));
        addArgument("dungeonName", new ListStringCommandArgument("<dungeonName>"));
        addArgument("version", new IntegerCommandArgument("<version>"));
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