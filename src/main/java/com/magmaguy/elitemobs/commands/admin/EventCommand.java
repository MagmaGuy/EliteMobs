package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.events.TimedEvent;
import org.bukkit.command.CommandSender;

public class EventCommand {
    private EventCommand() {
    }

    public static void trigger(CommandSender commandSender, String eventName) {
        for (TimedEvent timedEvent : TimedEvent.getBlueprintEvents())
            if (timedEvent.getCustomEventsConfigFields().getFilename().equals(eventName)) {
                timedEvent.instantiateEvent();
                commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] Queued event " + eventName + " for the next valid time it can spawn. &cThis might take a while depending on the start conditions of the event."));
                return;
            }
    }

}
