package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class RemoveCommand extends AdvancedCommand {
    public RemoveCommand() {
        super(List.of("remove"));
        setUsage("/em remove");
        setPermission("elitemobs.remove");
        setSenderType(SenderType.PLAYER);
        setDescription("Toggle removal mode for EliteMobs, which can permanently remove any EliteMobs-related content.");
    }

    @Override
    public void execute(CommandData commandData) {
        com.magmaguy.elitemobs.commands.admin.RemoveCommand.remove(commandData.getPlayerSender());
    }
}