package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class PlaceBossCommand extends AdvancedCommand {
    public PlaceBossCommand() {
        super(List.of("place"));
        addLiteral("boss");
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        setUsage("/em place boss <filename>");
        setPermission("elitemobs.place.boss");
        setSenderType(SenderType.PLAYER);
        setDescription("Add a spawn location for the specified boss at your current location.");
    }

    @Override
    public void execute(CommandData commandData) {
        CustomBossCommandHandler.addSpawnLocation(commandData.getStringArgument("filename"), commandData.getPlayerSender());
    }
}