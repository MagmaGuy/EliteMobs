package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class PartyAcceptCommand extends AdvancedCommand {
    public PartyAcceptCommand() {
        super(List.of("party"));
        addLiteral("accept");
        setUsage("/em party accept");
        setDescription("Accepts your pending EliteMobs party invitation.");
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyManager.accept(commandData.getPlayerSender());
    }
}
