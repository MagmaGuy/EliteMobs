package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.LootCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class LootGiveCommand extends AdvancedCommand {
    public LootGiveCommand() {
        super(List.of("loot"));
        addLiteral("give");
        addArgument("playerName", new PlayerCommandArgument());
        addArgument("filename", new ListStringCommandArgument(CustomItemsConfig.getCustomItems().keySet().stream().toList(), "<filename>"));
        setUsage("/em loot give <player> <filename>");
        setPermission("elitemobs.loot.admin");
        setDescription("Gives the specified loot to a specific player.");
    }

    @Override
    public void execute(CommandData commandData) {
        LootCommand.give(commandData.getCommandSender(), commandData.getStringArgument("playerName"), commandData.getStringArgument("filename"));
    }
}