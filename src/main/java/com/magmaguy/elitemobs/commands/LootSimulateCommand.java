package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class LootSimulateCommand extends AdvancedCommand {
    public LootSimulateCommand() {
        super(List.of("loot"));
        addLiteral("simulate");
        addArgument("level", new IntegerCommandArgument("<level>"));
        addArgument("playerName", new PlayerCommandArgument());
        setUsage("/em loot simulate <level> <playerName>");
        setPermission("elitemobs.loot.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Simulates loot drops for the specified level and player.");
    }

    @Override
    public void execute(CommandData commandData) {
        SimLootCommand.run(
                commandData.getPlayerSender(),
                commandData.getIntegerArgument("level"),
                commandData.getStringArgument("playerName"));
    }
}
