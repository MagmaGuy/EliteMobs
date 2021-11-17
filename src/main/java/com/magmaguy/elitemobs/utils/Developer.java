package com.magmaguy.elitemobs.utils;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class Developer {
    private Developer() {
    }

    public static void message(String message) {
        Bukkit.getLogger().log(Level.SEVERE, () -> "[EliteMobs] Developer message: " + message);
    }
}
