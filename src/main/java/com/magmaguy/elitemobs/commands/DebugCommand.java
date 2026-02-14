package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

/**
 * Toggles debug mode for the player.
 * When enabled, combat damage calculations and other debug info will be shown.
 */
public class DebugCommand extends AdvancedCommand {
    public DebugCommand() {
        super(List.of("debug"));
        setUsage("/em debug");
        setPermission("elitemobs.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Toggles debug mode to show combat damage calculations.");
    }

    @Override
    public void execute(CommandData commandData) {
        boolean enabled = DebugMessage.toggleDebug(commandData.getPlayerSender());
        if (enabled) {
            Logger.sendMessage(commandData.getPlayerSender(), CommandMessagesConfig.getDebugEnabledMessage());
        } else {
            Logger.sendMessage(commandData.getPlayerSender(), CommandMessagesConfig.getDebugDisabledMessage());
        }
    }
}
