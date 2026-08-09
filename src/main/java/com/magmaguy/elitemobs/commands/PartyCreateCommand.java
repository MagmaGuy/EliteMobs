package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class PartyCreateCommand extends AdvancedCommand {
    public PartyCreateCommand() {
        super(List.of("party"));
        addLiteral("create");
        setUsage("/em party create");
        setDescription(PartyConfig.getCommandCreateDescription());
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyManager.create(commandData.getPlayerSender());
    }
}
