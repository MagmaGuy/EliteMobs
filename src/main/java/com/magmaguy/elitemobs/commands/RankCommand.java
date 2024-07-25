package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class RankCommand extends AdvancedCommand {
    public RankCommand() {
        super(List.of("rank"));
        setUsage("/em rank");
        setDescription("Opens the EliteMobs rank menu.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(getCurrentPlayerSender()))
            AdventurersGuildCommand.adventurersGuildCommand(getCurrentPlayerSender());
    }
}