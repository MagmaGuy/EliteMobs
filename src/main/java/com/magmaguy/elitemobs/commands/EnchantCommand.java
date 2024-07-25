package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class EnchantCommand extends AdvancedCommand {
    public EnchantCommand() {
        super(List.of("enchant"));
        setUsage("/em enchant");
        setPermission("elitemobs.enchant.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the enchantment menu or teleports the player to the Adventurer's Guild Hub");
    }

    @Override
    public void execute() {
        if (!AdventurersGuildCommand.adventurersGuildTeleport(getCurrentPlayerSender()))
            new ItemEnchantmentMenu(getCurrentPlayerSender());
    }
}
