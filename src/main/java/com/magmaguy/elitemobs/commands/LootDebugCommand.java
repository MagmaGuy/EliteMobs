package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class LootDebugCommand extends AdvancedCommand {
    public LootDebugCommand() {
        super(List.of("loot"));
        addLiteral("debug");
        addArgument("level", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        setUsage("/em loot test <level>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Simulates loot drops for the specified amount of times for the specified level and player.");
    }

    @Override
    public void execute() {
        GetTierCommand.get(getCurrentPlayerSender(), getIntegerArgument("level"));
    }
}