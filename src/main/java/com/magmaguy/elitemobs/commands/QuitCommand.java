package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class QuitCommand extends AdvancedCommand {
    public QuitCommand() {
        super(List.of("quit"));
        setDescription("When in instanced content, makes the player leave the instance.");
        setUsage("/em quit");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        MatchInstance matchInstance = MatchInstance.getAnyPlayerInstance(commandData.getPlayerSender());
        if (matchInstance != null)
            matchInstance.removeAnyKind(commandData.getPlayerSender());
        else
            // Silence here left players who were stranded in a dying instance
            // spamming the command with no idea whether it even ran.
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getNotQueuedForInstanceMessage());
    }
}