package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.List;

public class KillCommand extends AdvancedCommand {
    public KillCommand() {
        super(List.of("kill"));
        setUsage("/em kill");
        setPermission("elitemobs.kill.kill");
        setDescription("Kills all elites.");
    }

    @Override
    public void execute(CommandData commandData) {
        KillHandler.killAggressiveMobs(commandData.getCommandSender());
    }
}