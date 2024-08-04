package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class TransitiveBlocksEditAreaCommand extends AdvancedCommand {
    public TransitiveBlocksEditAreaCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("editArea");
        addArgument("filename", CustomBossesConfig.getCustomBosses().keySet().stream().toList());
        addArgument("type", List.of("ON_SPAWN", "ON_REMOVE"));
        setUsage("/em transitiveBlocks editArea <filename> <ON_SPAWN/ON_REMOVE>");
        setPermission("elitemobs.transitiveblocks");
        setDescription("Edits large transitive blocks areas for use by regional bosses.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        TransitiveBlockCommand.processCommand(
                commandData.getPlayerSender(),
                commandData.getStringArgument("filename"),
                commandData.getStringArgument("type"),
                true,
                true);
    }
}