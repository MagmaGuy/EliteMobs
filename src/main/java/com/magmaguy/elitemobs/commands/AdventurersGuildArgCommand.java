package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class AdventurersGuildArgCommand  extends AdvancedCommand {
    public AdventurersGuildArgCommand() {
        super(List.of("ag", "adventurersguild"));
        setUsage("/em adventurersguild");
        setPermission("elitemobs.adventurersguild.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Teleports players to the Adventurer's Guild Hub or opens the Adventurer's Guild menu.");
    }

    @Override
    public void execute(CommandData commandData) {
        com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildCommand(commandData.getPlayerSender());
    }
}