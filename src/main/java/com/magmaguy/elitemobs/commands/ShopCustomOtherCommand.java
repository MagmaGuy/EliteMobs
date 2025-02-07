package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.CustomShopMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.util.List;

public class ShopCustomOtherCommand extends AdvancedCommand {
    public ShopCustomOtherCommand() {
        super(List.of("shop"));
        addLiteral("custom");
        addArgument("player", new PlayerCommandArgument());
        setUsage("/em shop custom <player>");
        setPermission("elitemobs.shop.custom.other");
        setDescription("Opens the EliteMobs shop for custom items for the specified player.");
    }

    @Override
    public void execute(CommandData commandData) {
        try {
            CustomShopMenu.customShopConstructor(Bukkit.getPlayer(commandData.getStringArgument("player")));
        } catch (Exception ex) {
            Logger.sendMessage(commandData.getCommandSender(), "Failed to get player with that username!");
        }
    }
}