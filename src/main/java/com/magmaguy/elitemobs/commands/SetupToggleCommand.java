package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class SetupToggleCommand extends AdvancedCommand {
    public SetupToggleCommand() {
        super(List.of("setup"));
        addLiteral("toggle");
        addArgument("empackages", new ListStringCommandArgument(EMPackage.getEmPackages().values().stream().map(emPackage -> emPackage.getContentPackagesConfigFields().getFilename()).collect(Collectors.toUnmodifiableList()),"empackages"));
        setUsage("/em setup toggle <dungeonConfig>");
        setPermission("elitemobs.setup");
        setDescription("Allows you to toggle the installation of specified EliteMobs content.");
    }

    @Override
    public void execute(CommandData commandData) {
        String dungeon = commandData.getStringArgument("empackages");
        if (dungeon.isEmpty() || EMPackage.getEmPackages().get(dungeon) == null)
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupNotValidPackageMessage());
        EMPackage emPackage = EMPackage.getEmPackages().get(dungeon);
        emPackage.doInstall(null);
        if (emPackage.isInstalled())
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupInstalledMessage());
        else
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupUninstalledMessage());
    }
}