package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class TransitiveBlocksCancelCommand extends AdvancedCommand {
    public TransitiveBlocksCancelCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("cancel");
        setUsage("/em transitiveBlocks cancel");
        setPermission("elitemobs.transitiveblocks.cancel");
        setDescription("Cancels transitive block registration.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        TransitiveBlockCommand.processCommand(commandData.getPlayerSender());
    }
}