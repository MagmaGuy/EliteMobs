package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerService;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;

/** Internal lifecycle owner for EliteMobs' classloader-safe Lua power interface. */
public final class EliteLuaPowerServiceModule {
    private static EliteLuaPowerServiceImpl implementation;

    private EliteLuaPowerServiceModule() {
    }

    public static void initialize() {
        if (implementation != null) return;
        EliteLuaPowerServiceImpl candidate = new EliteLuaPowerServiceImpl();
        try {
            Bukkit.getPluginManager().registerEvents(candidate, MetadataHandler.PLUGIN);
            Bukkit.getServicesManager().register(
                    EliteLuaPowerService.class,
                    candidate,
                    MetadataHandler.PLUGIN,
                    ServicePriority.Normal);
            implementation = candidate;
        } catch (RuntimeException | Error failure) {
            try {
                Bukkit.getServicesManager().unregister(EliteLuaPowerService.class, candidate);
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            try {
                HandlerList.unregisterAll(candidate);
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            try {
                candidate.shutdown();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    public static void shutdown() {
        EliteLuaPowerServiceImpl current = implementation;
        if (current == null) return;
        implementation = null;
        try {
            Bukkit.getServicesManager().unregister(EliteLuaPowerService.class, current);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to unregister the EliteMobs Lua power service: "
                    + exception.getMessage());
        }
        try {
            HandlerList.unregisterAll(current);
        } catch (RuntimeException exception) {
            Logger.warn("Failed to unregister EliteMobs Lua power listeners: "
                    + exception.getMessage());
        }
        try {
            current.shutdown();
        } catch (RuntimeException exception) {
            Logger.warn("Failed to finish EliteMobs Lua power service shutdown: "
                    + exception.getMessage());
        }
    }

    static EliteLuaPowerServiceImpl implementation() {
        return implementation;
    }
}
