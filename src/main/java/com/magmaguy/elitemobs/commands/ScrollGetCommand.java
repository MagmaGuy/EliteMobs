package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteScroll;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;

public class ScrollGetCommand extends AdvancedCommand {

    public ScrollGetCommand() {
        super(List.of("scrollGet"));
        addArgument("level", new IntegerCommandArgument("<level>"));
        addArgument("amount", new IntegerCommandArgument("<amount>"));
        setUsage("/em scrollGet <level> <amount>");
        setPermission("elitemobs.scroll.get");
        setSenderType(SenderType.PLAYER);
        setDescription("Gives the player a specified number of elite scrolls at a given level");
    }

    @Override
    public void execute(CommandData commandData) {
        // Check if scrolls are enabled in config
        if (!ItemSettingsConfig.isUseEliteItemScrolls()) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollsNotEnabledMessage());
            return;
        }

        int level, amount;
        try {
            level = commandData.getIntegerArgument("level");
            amount = commandData.getIntegerArgument("amount");
        } catch (NumberFormatException e) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollInvalidNumberMessage());
            return;
        }

        if (level <= 0) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollLevelZeroMessage());
            return;
        }

        if (amount <= 0) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollAmountZeroMessage());
            return;
        }

        Player player = commandData.getPlayerSender();

        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(EliteScroll.generateScroll(level, player));
        }

        Logger.sendMessage(commandData.getCommandSender(),
                CommandMessagesConfig.getScrollGaveMessage()
                        .replace("$amount", String.valueOf(amount))
                        .replace("$level", String.valueOf(level))
                        .replace("$player", player.getName()));
    }
}
