package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.menus.EliteScrollMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class EliteScrollCommand extends AdvancedCommand {
    public EliteScrollCommand() {
        super(List.of("scroll"));
        setUsage("/em scroll");
        setPermission("elitemobs.scroll.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the elite scroll menu or teleports the player to the Adventurer's Guild Hub");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!ItemSettingsConfig.isUseEliteItemScrolls()) {
            Logger.sendMessage(commandData.getCommandSender(), "Elite Scrolls are not currently enabled on this server! They should only be used if the server uses an item system other than EliteMobs' built in item system. To enable elite scrolls, an admin has to set them to true in the ~/plugins/EliteMobs/ItemSettings.yml and set useEliteItemScrolls to true.");
            return;
        }
        if (!AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender()))
            new EliteScrollMenu(commandData.getPlayerSender());
    }
}
