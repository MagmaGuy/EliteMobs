package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.LootCommand;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;

import java.util.List;

public class LootRewardCommand extends AdvancedCommand {
    public LootRewardCommand() {
        super(List.of("loot"));
        addLiteral("reward");
        addArgument("playerName", new PlayerCommandArgument());
        addArgument("filename", new ListStringCommandArgument(CustomItemsConfig.getCustomItems().keySet().stream().toList(), "<filename>"));
        setUsage("/em loot reward <player> <filename>");
        setPermission("elitemobs.loot.admin");
        setDescription("Gives the specified loot to a player, scaled to their combat level.");
    }

    @Override
    public void execute(CommandData commandData) {
        LootCommand.reward(commandData.getCommandSender(), commandData.getStringArgument("playerName"), commandData.getStringArgument("filename"));
    }
}
