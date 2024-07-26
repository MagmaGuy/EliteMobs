package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class RankCommand extends AdvancedCommand {
    public RankCommand() {
        super(List.of("rank"));
        setUsage("/em rank");
        setDescription("Opens the EliteMobs rank menu or teleports you to the Adventurer's Guild.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender()))
            AdventurersGuildCommand.adventurersGuildCommand(commandData.getPlayerSender());
    }
}