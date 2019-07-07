package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

public class WarningMessage {

    public WarningMessage(String message) {
        Bukkit.getLogger().warning("[EliteMobs] " + message);
    }

}
