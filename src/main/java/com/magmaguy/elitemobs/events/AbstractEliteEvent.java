package com.magmaguy.elitemobs.events;


import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;

public abstract class AbstractEliteEvent {

    /**
     * Runs when the event is first launched
     *
     * @param location
     */
    public abstract void activateEvent(Location location);

    /**
     * Runs when the event should end
     */
    public abstract void endEvent();

    /**
     * Runs when a valid spawn event is detected
     *
     * @param event
     */
    public abstract void spawnEventHandler(CreatureSpawnEvent event);

    public abstract void bossDeathEventHandler(EliteMobDeathEvent event);

    /**
     * Runs a repeating task on a timer to ascertain whether the event should end for any external reason (e.g. timer)
     */
    public abstract void eventWatchdog();

}
