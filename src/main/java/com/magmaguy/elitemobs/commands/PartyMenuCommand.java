package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyInventoryMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class PartyMenuCommand extends AdvancedCommand {
    public PartyMenuCommand() {
        super(List.of("party"));
        addLiteral("menu");
        setUsage("/em party menu");
        setDescription(PartyConfig.getCommandMenuDescription());
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyInventoryMenu.open(commandData.getPlayerSender());
    }
}
