package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class EventCaller {
    public EventCaller(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }
}
