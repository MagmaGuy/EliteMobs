package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;

public class EliteMobsCommand extends AdvancedCommand {
    public EliteMobsCommand() {
        super(new ArrayList<>());
        setDescription("The main command for EliteMobs, opens the main menu.");
        setUsage("/em");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        if (DefaultConfig.isEmLeadsToStatusMenu())
            new PlayerStatusScreen(commandData.getPlayerSender());
    }
}