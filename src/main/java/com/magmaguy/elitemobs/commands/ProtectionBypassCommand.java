package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.instance.InstanceProtector;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class ProtectionBypassCommand extends AdvancedCommand {
    public ProtectionBypassCommand() {
        super(List.of("protection"));
        addLiteral("bypass");
        setPermission("elitemobs.protection.bypass");
        setDescription("Allows admins to toggle bypassing protections");
        setUsage("/em protection bypass");
        setSenderType(SenderType.PLAYER);
    }

    @Override
    public void execute(CommandData commandData) {
        boolean outcome = InstanceProtector.toggleBypass(commandData.getPlayerSender().getUniqueId());
        Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getProtectionBypassMessage().replace("$status", String.valueOf(outcome)));
    }
}
