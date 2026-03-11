package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;

import java.util.List;

public class LootDebugLimitedCommand extends AdvancedCommand {
    public LootDebugLimitedCommand() {
        super(List.of("loot"));
        addLiteral("debug");
        addArgument("level", new IntegerCommandArgument("<level>"));
        addLiteral("limited");
        setUsage("/em loot debug <level> limited");
        setPermission("elitemobs.loot.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Equips players with a limited debug loadout of the specified level for faster testing.");
    }

    @Override
    public void execute(CommandData commandData) {
        GetTierCommand.getLimited(commandData.getPlayerSender(), commandData.getIntegerArgument("level"));
    }
}
