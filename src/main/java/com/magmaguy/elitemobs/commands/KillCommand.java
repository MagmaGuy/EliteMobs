package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.List;

public class KillCommand extends AdvancedCommand {
    public KillCommand() {
        super(List.of("kill"));
        setUsage("/em kill");
        setPermission("elitemobs.*");
        setDescription("Kills all elites.");
    }

    @Override
    public void execute() {
        KillHandler.killAggressiveMobs(getCurrentCommandSender());
    }
}