package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class TransitiveBlocksRegisterAreaCommand extends AdvancedCommand {
    public TransitiveBlocksRegisterAreaCommand() {
        super(List.of("transitiveBlocks"));
        addLiteral("registerArea");
        addArgument("filename", CustomBossesConfig.getCustomBosses().keySet().stream().toList());
        addArgument("type", List.of("ON_SPAWN", "ON_REMOVE"));
        setUsage("/em transitiveBlocks cancel");
        setPermission("elitemobs.*");
        setDescription("Registers large transitive blocks areas for use by regional bosses.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        TransitiveBlockCommand.processCommand(
                commandData.getPlayerSender(),
                commandData.getStringArgument("filename"),
                commandData.getStringArgument("type"),
                false,
                true);
    }
}