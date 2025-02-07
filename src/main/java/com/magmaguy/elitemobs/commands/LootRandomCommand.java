package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class LootRandomCommand extends AdvancedCommand {
    public LootRandomCommand() {
        super(List.of("loot"));
        addLiteral("random");
        addArgument("level", new IntegerCommandArgument("<level>"));
        addArgument("playerName", new PlayerCommandArgument());
        setUsage("/em loot random <level> <player>");
        setPermission("elitemobs.loot.admin");
        setDescription("Gives the player random loot of the specified level.");
    }

    @Override
    public void execute(CommandData commandData) {
        SimLootCommand.forcePositiveLoot(commandData.getCommandSender(), commandData.getStringArgument("playerName"), commandData.getIntegerArgument("level"));
    }
}