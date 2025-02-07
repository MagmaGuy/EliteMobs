package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class TransitiveBlocksEditCommand extends AdvancedCommand {
    public TransitiveBlocksEditCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("edit");
        addArgument("filename", new ListStringCommandArgument(CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
        addArgument("type", new ListStringCommandArgument(List.of("ON_SPAWN", "ON_REMOVE"), "<ON_SPAWN>/<ON_REMOVE>"));
        setUsage("/em transitiveBlocks edit <filename> <ON_SPAWN/ON_REMOVE>");
        setDescription("Edits transitive blocks for use by regional bosses.");
        setSenderType(SenderType.PLAYER);
        setPermission("elitemobs.transitiveblocks");
    }

    @Override
    public void execute(CommandData commandData) {
        TransitiveBlockCommand.processCommand(
                commandData.getPlayerSender(),
                commandData.getStringArgument("filename"),
                commandData.getStringArgument("type"),
                true);
    }
}