package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatSuggestion;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

/** Dismisses the server-local [Alpha] Advanced Combat System tester reminder without touching player data. */
public final class ExperimentalCombatDismissSuggestionCommand extends AdvancedCommand {
    public ExperimentalCombatDismissSuggestionCommand() {
        super(List.of("experimentalcombat"));
        addLiteral("dismiss");
        setUsage("/em experimentalcombat dismiss");
        setDescription("Dismisses the [Alpha] Advanced Combat System tester reminder.");
        setPermission("elitemobs.experimentalcombat.admin");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        if (ExperimentalCombatSuggestion.dismiss(commandData.getPlayerSender())) {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&7[Alpha] Advanced Combat System tester reminders dismissed.");
        } else {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&cCould not save that reminder dismissal. Check the server log.");
        }
    }
}
