package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;

import java.util.List;

public class ShopProceduralOtherCommand extends AdvancedCommand {
    public ShopProceduralOtherCommand() {
        super(List.of("shop"));
        addLiteral("procedural");
        addArgument("player", new PlayerCommandArgument());
        setUsage("/em shop procedural <player>");
        setPermission("elitemobs.*");
        setDescription("Opens the EliteMobs shop for procedurally generated items.");
    }

    @Override
    public void execute(CommandData commandData) {
        try {
            ProceduralShopMenu.shopConstructor(Bukkit.getPlayer(commandData.getStringArgument("player")));
        } catch (Exception ex) {
            Logger.sendMessage(commandData.getCommandSender(), "Failed to get player with that username!");
        }
    }
}