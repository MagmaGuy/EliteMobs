package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;

import java.util.List;

public class LootDebugUnbreakableCommand extends AdvancedCommand {
    public LootDebugUnbreakableCommand() {
        super(List.of("loot"));
        addLiteral("debug");
        addArgument("level", new IntegerCommandArgument("<level>"));
        addLiteral("unbreakable");
        setUsage("/em loot debug <level> unbreakable");
        setPermission("elitemobs.loot.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Equips players with a complete debug loadout with Unbreaking 99 for extended testing.");
    }

    @Override
    public void execute(CommandData commandData) {
        GetTierCommand.getUnbreakable(commandData.getPlayerSender(), commandData.getIntegerArgument("level"));
    }
}
