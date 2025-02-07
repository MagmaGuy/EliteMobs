package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;

import java.util.List;

public class LootDebugCommand extends AdvancedCommand {
    public LootDebugCommand() {
        super(List.of("loot"));
        addLiteral("debug");
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em loot debug <level>");
        setPermission("elitemobs.loot.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Equips players with a complete armor set, weapons set of the specified level, and food for testing purposes.");
    }

    @Override
    public void execute(CommandData commandData) {
        GetTierCommand.get(commandData.getPlayerSender(), commandData.getIntegerArgument("level"));
    }
}