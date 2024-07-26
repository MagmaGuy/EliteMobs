package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class StatsCommand extends AdvancedCommand {
    public StatsCommand() {
        super(List.of("stats"));
        setUsage("/em stats");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Displays EliteMobs server stats.");
    }

    @Override
    public void execute(CommandData commandData) {
        com.magmaguy.elitemobs.commands.admin.StatsCommand.statsHandler(commandData.getCommandSender());
    }
}