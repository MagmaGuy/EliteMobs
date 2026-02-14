package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class StartCommand extends AdvancedCommand {
    public StartCommand() {
        super(List.of("start"));
        setUsage("/em start");
        setDescription("When in instanced content, makes the player start the instance.");
        setSenderType(SenderType.PLAYER);
        setPermission("elitemobs.instance.start");
    }

    @Override
    public void execute(CommandData commandData) {
        MatchInstance matchInstance = MatchInstance.getPlayerInstance(commandData.getPlayerSender());
        if (matchInstance != null) {
            matchInstance.countdownMatch();
        } else
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getNotQueuedForInstanceMessage());
    }
}
