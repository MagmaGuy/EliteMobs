package com.magmaguy.elitemobs.thirdparty.geyser;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class GeyserDetector {
    private static boolean floodgateChecked = false;
    private static Method floodgateGetInstanceMethod;
    private static Method floodgateIsFloodgatePlayerMethod;
    private static boolean geyserChecked = false;
    private static Method geyserApiMethod;
    private static Method geyserConnectionByUuidMethod;
    private static Method geyserIsBedrockPlayerMethod;

    private GeyserDetector() {
    }

    public static boolean bedrockPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        return isFloodgatePlayer(playerUUID) || isGeyserPlayer(playerUUID);
    }

    private static boolean isFloodgatePlayer(UUID playerUUID) {
        if (!isPluginEnabled("floodgate") && !isPluginEnabled("Floodgate")) return false;
        initializeFloodgate();
        if (floodgateGetInstanceMethod == null || floodgateIsFloodgatePlayerMethod == null) return false;

        try {
            Object floodgateApi = floodgateGetInstanceMethod.invoke(null);
            if (floodgateApi == null) return false;
            Object response = floodgateIsFloodgatePlayerMethod.invoke(floodgateApi, playerUUID);
            return response instanceof Boolean && (Boolean) response;
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    private static boolean isGeyserPlayer(UUID playerUUID) {
        if (!isPluginEnabled("Geyser-Spigot") && !isPluginEnabled("Geyser-Bukkit") && !isPluginEnabled("Geyser")) {
            return false;
        }
        initializeGeyser();
        if (geyserApiMethod == null) return false;

        try {
            Object geyserApi = geyserApiMethod.invoke(null);
            if (geyserApi == null) return false;

            if (geyserConnectionByUuidMethod != null) {
                return geyserConnectionByUuidMethod.invoke(geyserApi, playerUUID) != null;
            }

            if (geyserIsBedrockPlayerMethod != null) {
                Object response = geyserIsBedrockPlayerMethod.invoke(geyserApi, playerUUID);
                return response instanceof Boolean && (Boolean) response;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            return false;
        }

        return false;
    }

    private static void initializeFloodgate() {
        if (floodgateChecked) return;
        floodgateChecked = true;

        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            floodgateGetInstanceMethod = floodgateApiClass.getMethod("getInstance");
            floodgateIsFloodgatePlayerMethod = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            floodgateGetInstanceMethod = null;
            floodgateIsFloodgatePlayerMethod = null;
        }
    }

    private static void initializeGeyser() {
        if (geyserChecked) return;
        geyserChecked = true;

        try {
            Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            geyserApiMethod = geyserApiClass.getMethod("api");
            for (Method method : geyserApiClass.getMethods()) {
                if (method.getParameterCount() != 1 || !UUID.class.equals(method.getParameterTypes()[0])) continue;
                if (method.getName().equals("connectionByUuid")) {
                    geyserConnectionByUuidMethod = method;
                } else if (method.getName().equals("isBedrockPlayer")) {
                    geyserIsBedrockPlayerMethod = method;
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            geyserApiMethod = null;
            geyserConnectionByUuidMethod = null;
            geyserIsBedrockPlayerMethod = null;
        }
    }

    private static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}
