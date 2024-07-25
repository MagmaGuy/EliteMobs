package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class QuitCommand extends AdvancedCommand {
    public QuitCommand() {
        super(List.of("quit"));
        setDescription("When in instanced content, makes the player leave the instance.");
        setUsage("/em quit");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        MatchInstance matchInstance = MatchInstance.getAnyPlayerInstance(getCurrentPlayerSender());
        if (matchInstance != null)
            matchInstance.removeAnyKind(getCurrentPlayerSender());
    }
}