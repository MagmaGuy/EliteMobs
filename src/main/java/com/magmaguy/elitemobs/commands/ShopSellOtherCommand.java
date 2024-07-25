package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.SellMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ShopSellOtherCommand extends AdvancedCommand {
    public ShopSellOtherCommand() {
        super(List.of("shop"));
        addLiteral("sell");
        addArgument("player", new ArrayList<>());
        setUsage("/em shop custom <player>");
        setPermission("elitemobs.*");
        setDescription("Opens the EliteMobs shop for custom items.");
    }

    @Override
    public void execute() {
        SellMenu sellMenu = new SellMenu();
        try {
            sellMenu.constructSellMenu(Bukkit.getPlayer(getStringArgument("player")));
        } catch (Exception ex) {
            Logger.sendMessage(getCurrentCommandSender(), "Failed to get player with that username!");
        }
    }
}