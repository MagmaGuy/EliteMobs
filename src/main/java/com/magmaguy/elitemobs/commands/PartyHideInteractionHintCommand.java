package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyInteractionHint;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class PartyHideInteractionHintCommand extends AdvancedCommand {
    public PartyHideInteractionHintCommand() {
        super(List.of("party"));
        addLiteral("hideinteractionhint");
        setUsage("/em party hideinteractionhint");
        setDescription(PartyConfig.getCommandHideInteractionHintDescription());
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyInteractionHint.disable(commandData.getPlayerSender());
    }
}
