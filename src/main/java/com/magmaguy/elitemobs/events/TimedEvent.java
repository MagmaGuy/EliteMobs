package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.CustomEventStartEvent;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class TimedEvent extends CustomEvent implements Listener {

    public static ArrayList<TimedEvent> blueprintEvents = new ArrayList<>();
    public static ArrayList<TimedEvent> timedEvents = new ArrayList<>();

    public static void initializeBlueprintEvents() {
        for (CustomEventsConfigFields customEventsConfigFields : CustomEventsConfig.getCustomEvents().values())
            if (customEventsConfigFields.isEnabled())
                switch (customEventsConfigFields.getEventType()) {
                    case TIMED:
                        blueprintEvents.add(new TimedEvent(customEventsConfigFields));
                }
        startEventPicker();
    }

    private static void startEventPicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < nextEventTrigger) return;
                HashMap<String, Double> weighedProbabilities = new HashMap();
                for (TimedEvent timedEvent : timedEvents) {
                    if (timedEvent.localCooldown < System.currentTimeMillis())
                        weighedProbabilities.put(timedEvent.filename, timedEvent.weight);
                }
                String pickedEvent = WeightedProbability.pickWeighedProbability(weighedProbabilities);
                for (TimedEvent timedEvent : timedEvents)
                    if (timedEvent.filename.equals(pickedEvent)) {
                        timedEvent.instantiateEvent();
                        return;
                    }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 60, 20 * 60);
    }

    /**
     * Just because the event is instantiated, does not necessarily mean it started. If the spawn isn't instant, then
     * it needs to be queued for a later date. If the spawn is instant but no valid location can be found, it should retry
     * on a delay.
     */
    private void instantiateEvent() {
        TimedEvent timedEvent = new TimedEvent(customEventsConfigFields);
        CustomEventStartEvent customEventStartEvent = new CustomEventStartEvent(timedEvent);
        if (customEventStartEvent.isCancelled()) return;

        switch (customEventsConfigFields.getSpawnType()) {
            case ENHANCED_SPAWN:
                break;
            case INSTANT_SPAWN:
                break;
            default:
                new WarningMessage("Failed to instantiate timed event " + customEventStartEvent.getEventName() + " because it did not have a valid Spawn Type!");
                break;
        }

        //global cooldown
        nextEventTrigger = System.currentTimeMillis() + globalCooldown * 60000;

        timedEvents.add(timedEvent);
        timedEvent.queueEvent();
    }

    //stores the time of the last global trigger
    public static double nextEventTrigger = 0;

    public double localCooldown;
    public double nextLocalEventTrigger = 0;
    public double globalCooldown;
    public double weight;
    public String filename;

    public TimedEvent(CustomEventsConfigFields customEventsConfigFields) {
        super(customEventsConfigFields);
        this.localCooldown = customEventsConfigFields.getLocalCooldown();
        this.globalCooldown = customEventsConfigFields.getGlobalCooldown();
        this.weight = customEventsConfigFields.getWeight();
        this.filename = customEventsConfigFields.getFilename();
    }

    public enum SpawnType {
        ENHANCED_SPAWN,
        INSTANT_SPAWN
    }

    /**
     * Queues an event to start when the start conditions are met
     */
    public void queueEvent() {
        new BukkitRunnable() {
            @Override
            public void run() {
                //if (startConditions.areValid())
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    @Override
    public void startModifiers() {
        //Start cooldown for the blueprints, not the instantiated event because that one will be deleted at the end of its runtime
        this.nextLocalEventTrigger = System.currentTimeMillis() + localCooldown * 60000;
    }

    @Override
    public void eventWatchdog() {

    }

    @Override
    public void endModifiers() {
        timedEvents.remove(this);
    }

    public static class TimeEventEvents implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onEliteSpawn() {

        }
    }
}
