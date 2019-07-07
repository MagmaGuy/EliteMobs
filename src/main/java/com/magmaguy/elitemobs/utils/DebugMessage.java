package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class DebugMessage {

    public DebugMessage(Object message) {
        Bukkit.getLogger().warning("[EliteMobs] Debug message: " + message);
    }

}
