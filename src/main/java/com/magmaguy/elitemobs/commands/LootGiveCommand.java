package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.LootCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class LootGiveCommand extends AdvancedCommand {
    public LootGiveCommand() {
        super(List.of("loot"));
        addLiteral("give");
        addArgument("filename", new ArrayList<>(CustomItemsConfig.getCustomItems().keySet()));
        addArgument("playerName", new ArrayList<>());
        setUsage("/em loot give <filename> <player>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Gives loot to a player.");
    }

    @Override
    public void execute() {
        LootCommand.give(getCurrentCommandSender(), getStringArgument("playerName"), getStringArgument("filename"));
    }
}