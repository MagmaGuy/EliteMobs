package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class PartyDeclineCommand extends AdvancedCommand {
    public PartyDeclineCommand() {
        super(List.of("party"));
        addLiteral("decline");
        addArgument("token", new ListStringCommandArgument(PartyConfig.getReadyCheckTokenHint()));
        setUsage("/em party decline <token>");
        setDescription(PartyConfig.getCommandDeclineDescription());
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyDungeonReadyCheckManager.decline(
                commandData.getPlayerSender(),
                commandData.getStringArgument("token"));
    }
}
