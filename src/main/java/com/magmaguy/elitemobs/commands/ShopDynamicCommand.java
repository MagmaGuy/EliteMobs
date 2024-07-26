package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class ShopDynamicCommand extends AdvancedCommand {
    public ShopDynamicCommand() {
        super(List.of("shop"));
        addLiteral("dynamic");
        setUsage("/em shop dynamic");
        setPermission("elitemobs.shop.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the procedurally generated item shop or teleports the player to the Adventurer's Guild Hub.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender()))
            ProceduralShopMenu.shopInitializer(commandData.getPlayerSender());
    }
}
