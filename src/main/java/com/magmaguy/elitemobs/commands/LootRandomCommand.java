package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class LootRandomCommand extends AdvancedCommand {
    public LootRandomCommand() {
        super(List.of("loot"));
        addLiteral("random");
        addArgument("level", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("playerName", new ArrayList<>());
        setUsage("/em loot random <level> <player>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Gives loot to a player.");
    }

    @Override
    public void execute() {
        SimLootCommand.forcePositiveLoot(getCurrentCommandSender(), getStringArgument("playerName"), getIntegerArgument("level"));
    }
}