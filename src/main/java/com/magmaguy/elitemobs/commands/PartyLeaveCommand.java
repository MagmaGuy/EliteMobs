package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class PartyLeaveCommand extends AdvancedCommand {
    public PartyLeaveCommand() {
        super(List.of("party"));
        addLiteral("leave");
        setUsage("/em party leave");
        setDescription(PartyConfig.getCommandLeaveDescription());
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyManager.leave(commandData.getPlayerSender());
    }
}
