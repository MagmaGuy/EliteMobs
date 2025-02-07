package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.DebugScreen;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;

import java.util.List;

public class DebugCommand extends AdvancedCommand {
    public DebugCommand() {
        super(List.of("debug"));
        addArgument("filename", new ListStringCommandArgument(CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "filename"));
        setUsage("/em debug <player name/boss filename>");
        setPermission("elitemobs.debug");
        setSenderType(SenderType.PLAYER);
        setDescription("Debug bosses or players.");
    }

    @Override
    public void execute(CommandData commandData) {
        DebugScreen.open(commandData.getPlayerSender(), commandData.getStringArgument("filename"));
    }
}
