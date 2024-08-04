package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class KillRadiusCommand extends AdvancedCommand {
    public KillRadiusCommand() {
        super(List.of("kill"));
        addArgument("radius", new ArrayList<>());
        setUsage("/em kill <radius>");
        setPermission("elitemobs.kill.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Kills all elites in the specified radius.");
    }

    @Override
    public void execute(CommandData commandData) {
        KillHandler.radiusKillAggressiveMobs(commandData.getPlayerSender(), commandData.getIntegerArgument("radius"));
    }
}