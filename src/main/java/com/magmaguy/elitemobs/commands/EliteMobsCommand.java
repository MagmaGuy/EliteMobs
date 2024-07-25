package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class EliteMobsCommand extends AdvancedCommand {
    public EliteMobsCommand() {
        super(List.of("em", "elitemobs"));
        setDescription("The main command for EliteMobs, opens the main menu.");
        setUsage("/em");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute() {
        if (DefaultConfig.isEmLeadsToStatusMenu())
            new PlayerStatusScreen(getCurrentPlayerSender());
    }
}