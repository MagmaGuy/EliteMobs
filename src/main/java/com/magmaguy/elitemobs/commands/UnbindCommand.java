package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.menus.UnbindMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class UnbindCommand extends AdvancedCommand {
    public UnbindCommand() {
        super(List.of("unbind"));
        setUsage("/em unbind");
        setPermission("elitemobs.unbind.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the unbind menu or teleports the player to the Adventurer's Guild Hub.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
            UnbindMenu unbindMenu = new UnbindMenu();
            unbindMenu.constructUnbinderMenu(commandData.getPlayerSender());
        }
    }
}
