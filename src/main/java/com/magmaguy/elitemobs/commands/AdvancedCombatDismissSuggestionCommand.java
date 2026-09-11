package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

/** Permanently disables the developer's login announcement in the server configuration. */
public final class AdvancedCombatDismissSuggestionCommand extends AdvancedCommand {
    public AdvancedCombatDismissSuggestionCommand() {
        super(List.of("advancedcombat"));
        addLiteral("dismiss");
        setUsage("/em advancedcombat dismiss");
        setDescription("Disables the combat developer message for all administrators on this server.");
        setPermission("elitemobs.advancedcombat.admin");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        if (AdvancedCombatSystemConfig.dismissDeveloperMessage()) {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&7Developer message dismissed for this server. &fshowDeveloperMessage &7is now false in &fAdvancedCombatSystem.yml&7.");
        } else {
            Logger.sendMessage(commandData.getCommandSender(),
                    "&cCould not save the developer message dismissal. Check the server log.");
        }
    }
}
