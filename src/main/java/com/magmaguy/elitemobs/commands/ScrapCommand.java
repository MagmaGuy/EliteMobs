package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.menus.ScrapperMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class ScrapCommand extends AdvancedCommand {
    public ScrapCommand() {
        super(List.of("scrap"));
        setUsage("/em scrap");
        setPermission("elitemobs.scrap.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the scrap menu or teleports the player to the Adventurer's Guild Hub");
    }

    @Override
    public void execute() {
        if (!AdventurersGuildCommand.adventurersGuildTeleport(getCurrentPlayerSender())) {
            ScrapperMenu scrapperMenu = new ScrapperMenu();
            scrapperMenu.constructScrapMenu(getCurrentPlayerSender());
        }
    }
}
