package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.menus.RepairMenu;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class RepairCommand extends AdvancedCommand {
    public RepairCommand() {
        super(List.of("repair"));
        setUsage("/em repair");
        setPermission("elitemobs.repair.command");
        setSenderType(SenderType.PLAYER);
        setDescription("Opens the repair item menu or teleports the player to the Adventurer's Guild Hub.");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
            RepairMenu repairMenu = new RepairMenu();
            repairMenu.constructRepairMenu(commandData.getPlayerSender());
        }
    }
}
