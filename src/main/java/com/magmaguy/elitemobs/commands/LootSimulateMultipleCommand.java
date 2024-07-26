package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class LootSimulateMultipleCommand extends AdvancedCommand {
    public LootSimulateMultipleCommand() {
        super(List.of("loot"));
        addLiteral("simulate");
        addArgument("level", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("times", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("playerName", new ArrayList<>());
        setUsage("/em loot simulate <level> <times> <playerName>");
        setPermission("elitemobs.loot.simulate.times");
        setSenderType(SenderType.PLAYER);
        setDescription("Simulates loot drops for the specified amount of times for the specified level and player.");
    }

    @Override
    public void execute(CommandData commandData) {
        SimLootCommand.runMultipleTimes(
                commandData.getPlayerSender(),
                commandData.getIntegerArgument("level"),
                commandData.getIntegerArgument("times"),
                commandData.getStringArgument("playerName"));
    }
}