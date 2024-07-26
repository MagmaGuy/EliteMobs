package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class RankSetCommand extends AdvancedCommand {
    public RankSetCommand() {
        super(List.of("rank"));
        addArgument("player", new ArrayList<>());
        addArgument("prestigeLevel", new ArrayList<>());
        addArgument("guildLevel", new ArrayList<>());
        setUsage("/em rank <player> <prestigeLevel> <guildLevel>");
        setPermission("elitemobs.rank.set");
        setDescription("Manually sets a player's guild rank to a the prestige level and guild level specified.");
    }

    @Override
    public void execute(CommandData commandData) {
        GuildRankCommands.setGuildRank(commandData.getCommandSender(),
                commandData.getStringArgument("player"),
                commandData.getIntegerArgument("prestigeLevel"),
                commandData.getIntegerArgument("guildLevel"));
    }
}