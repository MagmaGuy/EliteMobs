package com.magmaguy.elitemobs.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.ArrayList;
import java.util.List;

public class RankSetCommand extends AdvancedCommand {
    public RankSetCommand() {
        super(List.of("rank"));
        addArgument("player", new PlayerCommandArgument());
        addArgument("prestigeLevel", new IntegerCommandArgument(new ArrayList<>(List.of(0,1,2,3,4,5,6,7,8,9,10)),"prestigeLevel"));
        addArgument("guildLevel", new IntegerCommandArgument(new ArrayList<>(List.of(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20)),"prestigeLevel"));
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