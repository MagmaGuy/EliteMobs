package com.magmaguy.elitemobs.commands;

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
            Logger.sendMessage(commandData.getCommandSender(),
                    "Elite Scrolls are not currently enabled on this server! " +
                            "An admin must enable them in ~/plugins/EliteMobs/ItemSettings.yml by setting useEliteItemScrolls to true.");
            return;
        }

        int level, amount;
        try {
            level = commandData.getIntegerArgument("level");
            amount = commandData.getIntegerArgument("amount");
        } catch (NumberFormatException e) {
            Logger.sendMessage(commandData.getCommandSender(), "Level and amount must be valid integers.");
            return;
        }

        if (level <= 0) {
            Logger.sendMessage(commandData.getCommandSender(), "Scroll level must be greater than zero.");
            return;
        }

        if (amount <= 0) {
            Logger.sendMessage(commandData.getCommandSender(), "Amount must be greater than zero.");
            return;
        }

        Player player = commandData.getPlayerSender();

        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(EliteScroll.generateScroll(level, player));
        }

        Logger.sendMessage(commandData.getCommandSender(),
                "Gave you " + amount + " elite scroll" + (amount == 1 ? "" : "s") + " of level " + level + ".");
    }
}
