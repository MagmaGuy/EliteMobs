package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class LootDebugCommand extends AdvancedCommand {
    public LootDebugCommand() {
        super(List.of("loot"));
        addLiteral("debug");
        addArgument("level", new IntegerCommandArgument("<level>"));
        setUsage("/em loot debug <level>");
        setPermission("elitemobs.loot.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Equips players with a complete armor set, weapons set of the specified level, and food for testing purposes.");
    }

    @Override
    public void execute(CommandData commandData) {
        int level = commandData.getIntegerArgument("level");
        GetTierCommand.getUnbreakable(commandData.getPlayerSender(), level);
        if (!AdvancedCombatModule.isInitialized()) return;
        int formsAtLevel = AdvancedCombatModule.get()
                .scaleAllClassesForAdministration(commandData.getPlayerSender(), level);
        if (formsAtLevel > 0)
            Logger.sendMessage(commandData.getPlayerSender(), "&7Scaled &f" + formsAtLevel
                    + " &7class branch(es) to level &f" + level + "&7. Pick one with &f/em class&7.");
    }
}
