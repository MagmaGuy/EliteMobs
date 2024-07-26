package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.SellMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ShopSellOtherCommand extends AdvancedCommand {
    public ShopSellOtherCommand() {
        super(List.of("shop"));
        addLiteral("sell");
        addArgument("player", new ArrayList<>());
        setUsage("/em shop sell <player>");
        setPermission("elitemobs.shop.sell.player");
        setDescription("Opens the EliteMobs shop sell menu for the specified player.");
    }

    @Override
    public void execute(CommandData commandData) {
        SellMenu sellMenu = new SellMenu();
        try {
            sellMenu.constructSellMenu(Bukkit.getPlayer(commandData.getStringArgument("player")));
        } catch (Exception ex) {
            Logger.sendMessage(commandData.getCommandSender(), "Failed to get player with that username!");
        }
    }
}