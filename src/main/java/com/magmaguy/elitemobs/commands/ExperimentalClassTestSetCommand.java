package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatModule;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.experimentalcombat.progression.ClassProgressionSetResult;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/** Permission-gated fixture for reaching deep class branches during the experimental test phase. */
public final class ExperimentalClassTestSetCommand extends AdvancedCommand {

    public ExperimentalClassTestSetCommand() {
        super(List.of("class"));
        addLiteral("test");
        addLiteral("set");
        addArgument("player", new PlayerCommandArgument());
        addArgument("class", new DynamicListStringCommandArgument(
                () -> ExperimentalClassCommandSupport.catalog().forms().stream()
                        .map(ClassFormDefinition::id).toList(),
                "<class>"));
        addArgument("level", new IntegerCommandArgument("<effectiveLevel>"));
        setUsage("/em class test set <player> <class> <effectiveLevel>");
        setDescription("Sets and selects an exact Experimental Combat branch level for testing.");
        setPermission("elitemobs.experimentalcombat.admin");
    }

    @Override
    public void execute(CommandData commandData) {
        if (!ExperimentalCombatModule.isInitialized()) {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&cExperimental Combat is disabled on this server.");
            return;
        }
        String playerName = commandData.getStringArgument("player");
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&cPlayer &f" + playerName + " &cis not online.");
            return;
        }
        String formId = commandData.getStringArgument("class");
        int level = commandData.getIntegerArgument("level");
        ClassProgressionSetResult result = ExperimentalCombatModule.get()
                .setClassLevelForAdministration(player, formId, level);
        report(commandData, player, result);
    }

    private static void report(
            CommandData commandData,
            Player player,
            ClassProgressionSetResult result) {
        switch (result.status()) {
            case APPLIED -> {
                String message = "&aSet and selected &f" + result.formId() + " "
                        + result.requestedEffectiveLevel() + " &afor &f" + player.getName()
                        + "&a. Ancestor bands were completed for this branch.";
                Logger.sendMessage(commandData.getCommandSender(), message);
                if (!commandData.getCommandSender().equals(player))
                    Logger.sendMessage(player, "&dExperimental tester setup: &f" + result.formId()
                            + " " + result.requestedEffectiveLevel() + " &7is now selected.");
            }
            case NOT_READY -> Logger.sendMessage(commandData.getCommandSender(),
                    "&eThat player's class profile is still loading.");
            case UNKNOWN_FORM -> Logger.sendMessage(commandData.getCommandSender(),
                    "&cUnknown class form: &f" + result.formId());
            case LEVEL_OUTSIDE_FORM_BAND -> {
                ClassFormDefinition form = ExperimentalClassCommandSupport.catalog().require(result.formId());
                Logger.sendMessage(commandData.getCommandSender(),
                        "&c" + form.displayName() + " accepts effective levels &f"
                                + form.band().effectiveStart() + "-"
                                + (form.band().isTerminal() ? "100+" : form.band().effectiveEnd()) + "&c.");
            }
            case FOUNDATION_SKILL_CAP -> {
                ClassFormDefinition blocker = ExperimentalClassCommandSupport.catalog()
                        .require(result.blockingFormId());
                String skills = String.join(" and ", result.limitingSkills().stream()
                        .map(skill -> skill.getDisplayName()).toList());
                Logger.sendMessage(commandData.getCommandSender(),
                        "&cCannot prepare that branch: &f" + blocker.displayName()
                                + " &cis capped at &f" + result.effectiveCap()
                                + " &cby &f" + skills + "&c. Raise the foundation skills first; "
                                + "for broad testing use &f/em skill setAll " + player.getName() + " 100&c.");
            }
            case RUN_LOCKED -> Logger.sendMessage(commandData.getCommandSender(),
                    "&cThat player is in a class-locked dungeon run. Have them leave before changing test progression.");
        }
    }
}
