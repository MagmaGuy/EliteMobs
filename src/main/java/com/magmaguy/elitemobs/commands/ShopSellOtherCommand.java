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
        setUsage("/em shop custom <player>");
        setPermission("elitemobs.*");
        setDescription("Opens the EliteMobs shop for custom items.");
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