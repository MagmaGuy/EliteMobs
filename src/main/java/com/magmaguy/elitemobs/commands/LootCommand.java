package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.menus.LootMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class LootCommand extends AdvancedCommand {
    public LootCommand() {
        super(List.of("loot"));
        setUsage("/em loot");
        setDescription("Open the loot menu for group loot.");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        LootMenu.openMenu(getCurrentPlayerSender());
    }
}
