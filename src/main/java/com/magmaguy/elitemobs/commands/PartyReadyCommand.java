package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class PartyReadyCommand extends AdvancedCommand {
    public PartyReadyCommand() {
        super(List.of("party"));
        addLiteral("ready");
        addArgument("token", new ListStringCommandArgument("ready check token"));
        setUsage("/em party ready <token>");
        setDescription("Accepts an active party dungeon ready check.");
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyDungeonReadyCheckManager.ready(
                commandData.getPlayerSender(),
                commandData.getStringArgument("token"));
    }
}
