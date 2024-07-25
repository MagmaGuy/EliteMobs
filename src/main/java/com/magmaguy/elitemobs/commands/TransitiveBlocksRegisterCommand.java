package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.List;

public class TransitiveBlocksRegisterCommand extends AdvancedCommand {
    public TransitiveBlocksRegisterCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("register");
        addArgument("filename", CustomBossesConfig.getCustomBosses().keySet().stream().toList());
        addArgument("type", List.of("ON_SPAWN", "ON_REMOVE"));
        setUsage("/em transitiveBlocks cancel");
        setPermission("elitemobs.*");
        setDescription("Cancels transitive block registration.");
    }

    @Override
    public void execute() {
        TransitiveBlockCommand.processCommand(getCurrentPlayerSender(), getStringArgument("filename"), getStringArgument("type"), false);
    }
}