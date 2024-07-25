package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.List;

public class TransitiveBlocksCancelCommand extends AdvancedCommand {
    public TransitiveBlocksCancelCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("cancel");
        setUsage("/em transitiveBlocks cancel");
        setPermission("elitemobs.*");
        setDescription("Cancels transitive block registration.");
    }

    @Override
    public void execute() {
        TransitiveBlockCommand.processCommand(getCurrentPlayerSender());
    }
}