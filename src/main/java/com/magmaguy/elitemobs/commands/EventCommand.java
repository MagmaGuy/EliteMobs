package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;

import java.util.List;

public class EventCommand extends AdvancedCommand {
    public EventCommand() {
        super(List.of("event"));
        addArgument("filename", TimedEvent.getBlueprintEvents().stream().map(timedEvent -> timedEvent.getCustomEventsConfigFields().getFilename()).toList());
        setUsage("/em event <event filename>");
        setPermission("elitemobs.event.start");
        setSenderType(SenderType.PLAYER);
        setDescription("Triggers a timed event to start at the next available time and place.");
    }

    @Override
    public void execute(CommandData commandData) {
        com.magmaguy.elitemobs.commands.admin.EventCommand.trigger(commandData.getCommandSender(), commandData.getStringArgument("filename"));
    }
}
