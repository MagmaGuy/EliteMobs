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
        setPermission("elitemobs.*");
        setDescription("Sets up EliteMobs content!");
        setUsage("/em setup toggle");
        addLiteral("toggle");
        addArgument("empackages", EMPackage.getEmPackages().values().stream().map(emPackage -> emPackage.getDungeonPackagerConfigFields().getFilename()).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public void execute(CommandData commandData) {
        String dungeon = commandData.getStringArgument("empackages");
        if (dungeon.isEmpty() || EMPackage.getEmPackages().get(dungeon) == null)
            Logger.sendMessage(commandData.getCommandSender(), "Not a valid em package!");
        EMPackage emPackage = EMPackage.getEmPackages().get(dungeon);
        if (emPackage.install())
            Logger.sendMessage(commandData.getCommandSender(), "Successfully installed content!");
    }
}