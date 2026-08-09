package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class PartyInviteCommand extends AdvancedCommand {
    public PartyInviteCommand() {
        super(List.of("party"));
        addLiteral("invite");
        addArgument("player", new PlayerCommandArgument());
        setUsage("/em party invite <player>");
        setDescription("Invites an online player to your EliteMobs party.");
        setPermission("elitemobs.party");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        PartyManager.invite(commandData.getPlayerSender(), commandData.getStringArgument("player"));
    }
}
