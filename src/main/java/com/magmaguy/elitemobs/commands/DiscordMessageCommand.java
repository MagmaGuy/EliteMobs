package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class DiscordMessageCommand extends AdvancedCommand {
    public DiscordMessageCommand() {
        super(List.of("discord"));
        addArgument("message", new ListStringCommandArgument("Message to send to discord"));
        setUsage("/em discord <message>");
        setPermission("elitemobs.discord.message");
        setDescription("Sends a message to the Discord via DiscordSRV, for debugging purposes");
    }

    @Override
    public void execute(CommandData commandData) {
        new DiscordSRVAnnouncement(commandData.getStringSequenceArgument("message"));
        Logger.sendMessage(commandData.getCommandSender(), "&aAttempted to send a message to Discord!");
    }
}
