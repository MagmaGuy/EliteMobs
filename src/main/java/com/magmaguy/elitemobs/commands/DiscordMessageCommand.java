package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class DiscordMessageCommand extends AdvancedCommand {
    public DiscordMessageCommand() {
        super(List.of("discord"));
        addArgument("message", new ArrayList<>());
        setUsage("/em discord <message>");
        setPermission("elitemobs.*");
        setDescription("Sends a message to the Discord via DiscordSRV, for debugging purposes");
    }

    @Override
    public void execute() {
        new DiscordSRVAnnouncement(getStringSequenceArgument("message"));
        Logger.sendMessage(getCurrentCommandSender(), "&aAttempted to send a message to Discord!");
    }
}