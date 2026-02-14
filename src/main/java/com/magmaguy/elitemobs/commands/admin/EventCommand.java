package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.events.TimedEvent;
import org.bukkit.command.CommandSender;

public class EventCommand {
    private EventCommand() {
    }

    public static void trigger(CommandSender commandSender, String eventName) {
        for (TimedEvent timedEvent : TimedEvent.getBlueprintEvents())
            if (timedEvent.getCustomEventsConfigFields().getFilename().equals(eventName)) {
                TimedEvent.EventStartResult result = timedEvent.instantiateEvent();
                switch (result) {
                    case SUCCESS:
                        commandSender.sendMessage(CommandMessagesConfig.getEventQueuedMessage().replace("$event", eventName));
                        break;
                    case CANCELLED_BY_PLUGIN:
                        commandSender.sendMessage(CommandMessagesConfig.getEventCancelledMessage().replace("$event", eventName));
                        break;
                    case NO_SPAWN_TYPE:
                        commandSender.sendMessage(CommandMessagesConfig.getEventNoSpawnTypeMessage().replace("$event", eventName));
                        break;
                    case INVALID_SPAWN_TYPE:
                        commandSender.sendMessage(CommandMessagesConfig.getEventInvalidSpawnTypeMessage()
                                .replace("$event", eventName)
                                .replace("$spawnType", timedEvent.getCustomEventsConfigFields().getSpawnType()));
                        break;
                    case NO_VALID_BOSSES:
                        commandSender.sendMessage(CommandMessagesConfig.getEventNoValidBossesMessage().replace("$event", eventName));
                        break;
                }
                return;
            }
        commandSender.sendMessage(CommandMessagesConfig.getInvalidEventMessage().replace("$event", eventName));
    }

}
