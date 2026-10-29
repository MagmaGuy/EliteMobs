package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.menus.SellMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

/** Scrap is retired; /em scrap now sells unwanted elite items and old scrap for coins. */
public class ScrapCommand extends AdvancedCommand {
    public ScrapCommand() {
        super(List.of("scrap"));
        setUsage("/em scrap");
        setPermission("elitemobs.scrap.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the sell menu or teleports the player to the Adventurer's Guild Hub.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
            SellMenu sellMenu = new SellMenu();
            sellMenu.constructSellMenu(commandData.getPlayerSender());
        }
    }
}
