package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class PlaceBossCommand extends AdvancedCommand {
    public PlaceBossCommand() {
        super(List.of("place"));
        addLiteral("boss");
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        setUsage("/em addSpawn boss <filename>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Toggles whether the setup message will show up.");
    }

    @Override
    public void execute() {
        CustomBossCommandHandler.addSpawnLocation(getStringArgument("filename"), getCurrentPlayerSender());
    }
}