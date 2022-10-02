package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.CustomEventStartEvent;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimedEvent extends CustomEvent implements Listener {

    @Getter
    protected static List<TimedEvent> blueprintEvents = new ArrayList<>();
    protected static List<TimedEvent> timedEvents = new ArrayList<>();
    //stores the time of the last global trigger
    @Getter
    private static double nextEventTrigger = System.currentTimeMillis() + 5D * 60D * 1000D;
    private final double localCooldown;
    private final double globalCooldown;
    private final double weight;
    private final String filename;
    private double nextLocalEventTrigger = 0;
    private CustomSpawn customSpawn;
    private boolean silentRetry = false;

    public TimedEvent(CustomEventsConfigFields customEventsConfigFields) {
        super(customEventsConfigFields);
        this.localCooldown = customEventsConfigFields.getLocalCooldown();
        this.globalCooldown = customEventsConfigFields.getGlobalCooldown();
        this.weight = customEventsConfigFields.getWeight();
        this.filename = customEventsConfigFields.getFilename();
    }

    public static void shutdown() {
        blueprintEvents.clear();
        timedEvents.clear();
    }

    public static void initializeBlueprintEvents() {
        if (!EventsConfig.isTimedEventsEnabled()) return;
        for (CustomEventsConfigFields customEventsConfigFields : CustomEventsConfig.getCustomEvents().values())
            if (customEventsConfigFields.isEnabled() && customEventsConfigFields.getEventType() == EventType.TIMED)
                blueprintEvents.add(new TimedEvent(customEventsConfigFields));
        startEventPicker();
    }

    private static void startEventPicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getServer().getOnlinePlayers().isEmpty()) return;
                if (System.currentTimeMillis() < nextEventTrigger) return;
                pickEvent();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L * 60L * 5L, 20L * 60L);
    }

    private static void pickEvent() {
        HashMap<String, Double> weighedProbabilities = new HashMap<>();
        for (TimedEvent timedEvent : blueprintEvents) {
            boolean isRunning = false;
            for (TimedEvent activeTimedEvent : timedEvents)
                if (activeTimedEvent != null &&
                        activeTimedEvent.getCustomEventsConfigFields().getFilename().equals(timedEvent.getCustomEventsConfigFields().getFilename())) {
                    isRunning = true;
                    break;
                }
            if (isRunning)
                continue;
            if (timedEvent.nextLocalEventTrigger < System.currentTimeMillis())
                weighedProbabilities.put(timedEvent.filename, timedEvent.weight);
        }
        String pickedEvent = WeightedProbability.pickWeighedProbability(weighedProbabilities);
        for (TimedEvent timedEvent : blueprintEvents)
            if (timedEvent.filename.equals(pickedEvent)) {
                timedEvent.nextLocalEventTrigger = System.currentTimeMillis() + timedEvent.localCooldown * 60 * 1000;
                timedEvent.instantiateEvent();
                return;
            }
    }

    /**
     * Just because the event is instantiated, does not necessarily mean it started. If the spawn isn't instant, then
     * it needs to be queued for a later date. If the spawn is instant but no valid location can be found, it should retry
     * on a delay.
     */
    public void instantiateEvent() {
        new InfoMessage("Event " + getCustomEventsConfigFields().getFilename() + " has been queued!");
        TimedEvent timedEvent = new TimedEvent(customEventsConfigFields);
        CustomEventStartEvent customEventStartEvent = new CustomEventStartEvent(timedEvent);
        if (customEventStartEvent.isCancelled()) return;

        timedEvent.customSpawn = new CustomSpawn(customEventsConfigFields.getCustomSpawn(),
                customEventsConfigFields.getBossFilenames(),
                timedEvent);

        //Failed to initialize event
        if (timedEvent.customSpawn.getCustomSpawnConfigFields() == null)
            return;

        //This handles the elitemobs-events flag
        timedEvent.customSpawn.setEvent(true);

        //Note: this will finish running at an arbitrary time in the future
        timedEvent.customSpawn.queueSpawn();

        //global cooldown - 60 seconds right now
        setNextEventTrigger();

        timedEvents.add(timedEvent);
    }

    private void setNextEventTrigger() {
        setNextEventTrigger(globalCooldown);
    }

    private void setNextEventTrigger(double timeMinutes) {
        nextEventTrigger = System.currentTimeMillis() + timeMinutes * 60 * 1000;
    }

    /**
     * Queues an event to start when the start conditions are met
     */
    public void queueEvent() {
        this.primaryEliteMobs = customSpawn.getCustomBossEntities();
        setEventStartLocation(customSpawn.getSpawnLocation());

        for (CustomBossEntity customBossEntity : primaryEliteMobs)
            if (!customBossEntity.exists()) {
                if (!silentRetry) {
                    new InfoMessage("Boss " + customBossEntity.getCustomBossesConfigFields().getFilename() + " for event " +
                            getCustomEventsConfigFields().getFilename() + " wasn't considered to be valid. Trying spawn again .");
                    new InfoMessage("Note: further failures will be silent. EliteMobs can only predict WorldGuard protections," +
                            " so it will keep trying to spawn things until plugins preventing spawning allow it to do so. This might take a while.");
                    silentRetry = true;
                }

                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                    customSpawn.setSpawnLocation(null);
                    customSpawn.queueSpawn();
                }, 1);
                return;
            }

        primaryEliteMobs.forEach(CustomBossEntity::announceSpawn);

        //Hardcoded 5 minute minimum wait time between events before the next event can get queued
        setNextEventTrigger(5);
        start();
    }

    @Override
    public void startModifiers() {
        //Start cooldown for the blueprints, not the instantiated event because that one will be deleted at the end of its runtime
        this.nextLocalEventTrigger = System.currentTimeMillis() + localCooldown * 60000;
    }

    @Override
    public void eventWatchdog() {
        //No specific checks are currently required while the timed event occurs
    }

    @Override
    public void endModifiers() {
        timedEvents.remove(this);
    }
}
