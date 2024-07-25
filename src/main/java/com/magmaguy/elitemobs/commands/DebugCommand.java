package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.DebugScreen;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.SenderType;

import java.util.ArrayList;
import java.util.List;

public class DebugCommand extends AdvancedCommand {
    public DebugCommand() {
        super(List.of("debug"));
        addArgument("filename", new ArrayList<>(CustomBossesConfig.getCustomBosses().keySet()));
        setUsage("/em debug <player name/boss filename>");
        setPermission("elitemobs.*");
        setSenderType(SenderType.PLAYER);
        setDescription("Toggles whether the setup message will show up.");
    }

    @Override
    public void execute() {
        DebugScreen.open(getCurrentPlayerSender(), getStringArgument("filename"));
    }
}
