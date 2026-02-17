package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.utils.DiscordLinks;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class DiscordCommand extends AdvancedCommand {
    public DiscordCommand() {
        super(List.of("discord"));
        setUsage("/em discord");
        setPermission("elitemobs.discord.link");
        setDescription("Links to the EliteMobs discord.");
    }

    @Override
    public void execute(CommandData commandData) {
        Logger.sendMessage(commandData.getPlayerSender(), CommandMessagesConfig.getDiscordMessage() + DiscordLinks.mainLink);
    }
}
