package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.EliteScroll;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ScrollGiveCommand extends AdvancedCommand {

    public ScrollGiveCommand() {
        super(List.of("scrollGive"));
        addArgument("playerName", new PlayerCommandArgument());
        setUsage("/em scrollGive <player>");
        setPermission("elitemobs.scroll.give");
        setDescription("Gives the target player an Elite Scroll scaled to their combat level");
    }

    @Override
    public void execute(CommandData commandData) {
        // Check if scrolls are enabled in config
        if (!ItemSettingsConfig.isUseEliteItemScrolls()) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getScrollsNotEnabledMessage());
            return;
        }

        Player player = Bukkit.getPlayer(commandData.getStringArgument("playerName"));
        if (player == null) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getInvalidPlayerForItemMessage());
            return;
        }

        // Scale the scroll to the target player's combat level so it works as a reward
        int level = CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());

        ItemStack scroll = EliteScroll.generateScroll(level, player);
        // Add to inventory; drop overflow at the player's location if the inventory is full
        for (ItemStack overflow : player.getInventory().addItem(scroll).values())
            player.getWorld().dropItem(player.getLocation(), overflow);

        Logger.sendMessage(commandData.getCommandSender(),
                CommandMessagesConfig.getScrollGaveMessage()
                        .replace("$amount", "1")
                        .replace("$level", String.valueOf(level))
                        .replace("$player", player.getName()));
    }
}
