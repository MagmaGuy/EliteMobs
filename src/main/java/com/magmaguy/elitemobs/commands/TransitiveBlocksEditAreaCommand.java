package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;

import java.util.List;

public class TransitiveBlocksEditAreaCommand extends AdvancedCommand {
    public TransitiveBlocksEditAreaCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("editArea");
        addArgument("filename", CustomBossesConfig.getCustomBosses().keySet().stream().toList());
        addArgument("type", List.of("ON_SPAWN", "ON_REMOVE"));
        setUsage("/em transitiveBlocks cancel");
        setPermission("elitemobs.*");
        setDescription("Edits large transitive blocks areas for use by regional bosses.");
    }

    @Override
    public void execute() {
        TransitiveBlockCommand.processCommand(getCurrentPlayerSender(), getStringArgument("filename"), getStringArgument("type"), true, true);
    }
}