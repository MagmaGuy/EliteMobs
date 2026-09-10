package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatSuggestion;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

/** Dismisses the server-local [Alpha] Advanced Combat System tester reminder without touching player data. */
public final class AdvancedCombatDismissSuggestionCommand extends AdvancedCommand {
    public AdvancedCombatDismissSuggestionCommand() {
        super(List.of("advancedcombat"));
        addLiteral("dismiss");
        setUsage("/em advancedcombat dismiss");
        setDescription("Dismisses the [Alpha] Advanced Combat System tester reminder.");
        setPermission("elitemobs.advancedcombat.admin");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        if (AdvancedCombatSuggestion.dismiss(commandData.getPlayerSender())) {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&7[Alpha] Advanced Combat System tester reminders dismissed.");
        } else {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&cCould not save that reminder dismissal. Check the server log.");
        }
    }
}
