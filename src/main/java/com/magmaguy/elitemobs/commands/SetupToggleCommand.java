package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class SetupToggleCommand extends AdvancedCommand {
    public SetupToggleCommand() {
        super(List.of("setup"));
        addLiteral("toggle");
        addArgument("empackages", EMPackage.getEmPackages().values().stream().map(emPackage -> emPackage.getDungeonPackagerConfigFields().getFilename()).collect(Collectors.toUnmodifiableList()));
        setUsage("/em setup toggle <dungeonConfig>");
        setPermission("elitemobs.setup");
        setDescription("Allows you to toggle the installation of specified EliteMobs content.");
    }

    @Override
    public void execute(CommandData commandData) {
        String dungeon = commandData.getStringArgument("empackages");
        if (dungeon.isEmpty() || EMPackage.getEmPackages().get(dungeon) == null)
            Logger.sendMessage(commandData.getCommandSender(), "Not a valid em package!");
        EMPackage emPackage = EMPackage.getEmPackages().get(dungeon);
        if (emPackage.install()) {
            if (emPackage.isInstalled())
                Logger.sendMessage(commandData.getCommandSender(), "Successfully installed content!");
            else
                Logger.sendMessage(commandData.getCommandSender(), "Successfully uninstalled content!");

        }
    }
}