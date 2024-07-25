package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ShopProceduralOtherCommand extends AdvancedCommand {
    public ShopProceduralOtherCommand() {
        super(List.of("shop"));
        addLiteral("procedural");
        addArgument("player", new ArrayList<>());
        setUsage("/em shop procedural <player>");
        setPermission("elitemobs.*");
        setDescription("Opens the EliteMobs shop for procedurally generated items.");
    }

    @Override
    public void execute() {
        try {
            ProceduralShopMenu.shopConstructor(Bukkit.getPlayer(getStringArgument("player")));
        } catch (Exception ex) {
            Logger.sendMessage(getCurrentCommandSender(), "Failed to get player with that username!");
        }
    }
}