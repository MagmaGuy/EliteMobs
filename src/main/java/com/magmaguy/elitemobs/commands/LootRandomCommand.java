package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;

import java.util.ArrayList;
import java.util.List;

public class LootRandomCommand extends AdvancedCommand {
    public LootRandomCommand() {
        super(List.of("loot"));
        addLiteral("random");
        addArgument("level", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("playerName", new ArrayList<>());
        setUsage("/em loot random <level> <player>");
        setPermission("elitemobs.loot.random");
        setDescription("Gives the player random loot of the specified level.");
    }

    @Override
    public void execute(CommandData commandData) {
        SimLootCommand.forcePositiveLoot(commandData.getCommandSender(), commandData.getStringArgument("playerName"), commandData.getIntegerArgument("level"));
    }
}