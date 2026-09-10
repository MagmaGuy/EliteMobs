package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/** Resets a class subtree so administrators can repeat its unlock progression. */
public final class ExperimentalClassTestForgetCommand extends AdvancedCommand {
    public ExperimentalClassTestForgetCommand() {
        super(List.of("class"));
        addLiteral("test");
        addLiteral("forget");
        addArgument("player", new PlayerCommandArgument());
        addArgument("class", new DynamicListStringCommandArgument(
                () -> ExperimentalClassCommandSupport.catalog().forms().stream()
                        .map(ClassFormDefinition::id).toList(), "<class>"));
        setUsage("/em class test forget <player> <class>");
        setDescription("Resets a class and its descendants, removing their unlocks and class XP.");
        setPermission("elitemobs.experimentalcombat.admin");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!ExperimentalCombatModule.isInitialized()) {
            Logger.sendMessage(commandData.getCommandSender(), "&c[Alpha] Advanced Combat System is disabled on this server.");
            return;
        }
        Player player = Bukkit.getPlayer(commandData.getStringArgument("player"));
        if (player == null) {
            Logger.sendMessage(commandData.getCommandSender(), "&cThat player is not online.");
            return;
        }
        String formId = commandData.getStringArgument("class");
        var result = ExperimentalCombatModule.get().forgetClassForAdministration(player, formId);
        switch (result.status()) {
            case APPLIED -> {
                String message = "&aForgot &f" + formId + " &aand its descendants for &f" + player.getName()
                        + "&a. Reset unlocks and class XP for &f" + result.formsReset() + " &aforms."
                        + (result.selectionCleared() ? " The affected active class was deactivated." : "");
                Logger.sendMessage(commandData.getCommandSender(), message);
                if (!commandData.getCommandSender().equals(player))
                    Logger.sendMessage(player, "&dClass debug: &f" + formId
                            + " &7and its descendants were forgotten. Their trials must be completed again.");
            }
            case NOT_READY -> Logger.sendMessage(commandData.getCommandSender(),
                    "&eThat player's class profile is still loading.");
            case UNKNOWN_FORM -> Logger.sendMessage(commandData.getCommandSender(), "&cUnknown class: &f" + formId);
            case RUN_LOCKED -> Logger.sendMessage(commandData.getCommandSender(),
                    "&cThat player must leave their current run before forgetting a class.");
        }
    }
}
