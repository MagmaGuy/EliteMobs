package com.magmaguy.elitemobs.thirdparty.geyser;

import com.magmaguy.magmacore.util.Logger;
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
    private static boolean floodgateFailureLogged = false;
    private static boolean geyserFailureLogged = false;

    private GeyserDetector() {
    }

    public static boolean bedrockPlayer(Player player) {
        if (player == null) return false;
        UUID playerUUID = player.getUniqueId();
        return isFloodgatePlayer(playerUUID) || isGeyserPlayer(playerUUID);
    }

    /**
     * Returns whether this Bukkit server has an API capable of identifying Bedrock players.
     * A Geyser/Floodgate installation which exists only on a proxy does not expose per-player
     * identity to backend plugins, so callers must not create Bedrock-only fallback entities in
     * that arrangement.
     */
    public static boolean canIdentifyBedrockPlayers() {
        if (isFloodgatePluginEnabled()) {
            initializeFloodgate();
            if (floodgateGetInstanceMethod != null && floodgateIsFloodgatePlayerMethod != null)
                return true;
        }
        if (isGeyserPluginEnabled()) {
            initializeGeyser();
            return geyserApiMethod != null &&
                    (geyserConnectionByUuidMethod != null || geyserIsBedrockPlayerMethod != null);
        }
        return false;
    }

    private static boolean isFloodgatePlayer(UUID playerUUID) {
        if (!isFloodgatePluginEnabled()) return false;
        initializeFloodgate();
        if (floodgateGetInstanceMethod == null || floodgateIsFloodgatePlayerMethod == null) return false;

        try {
            Object floodgateApi = floodgateGetInstanceMethod.invoke(null);
            if (floodgateApi == null) return false;
            Object response = floodgateIsFloodgatePlayerMethod.invoke(floodgateApi, playerUUID);
            return response instanceof Boolean && (Boolean) response;
        } catch (IllegalAccessException | InvocationTargetException e) {
            logFloodgateFailure(e);
            return false;
        }
    }

    private static boolean isGeyserPlayer(UUID playerUUID) {
        if (!isGeyserPluginEnabled()) return false;
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
            logGeyserFailure(e);
            return false;
        }

        return false;
    }

    private static synchronized void initializeFloodgate() {
        if (floodgateChecked) return;
        floodgateChecked = true;

        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            floodgateGetInstanceMethod = floodgateApiClass.getMethod("getInstance");
            floodgateIsFloodgatePlayerMethod = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            floodgateGetInstanceMethod = null;
            floodgateIsFloodgatePlayerMethod = null;
            logFloodgateFailure(e);
        }
    }

    private static synchronized void initializeGeyser() {
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
            logGeyserFailure(e);
        }
    }

    private static boolean isFloodgatePluginEnabled() {
        return isPluginEnabled("floodgate") || isPluginEnabled("Floodgate");
    }

    private static boolean isGeyserPluginEnabled() {
        return isPluginEnabled("Geyser-Spigot") || isPluginEnabled("Geyser-Bukkit") || isPluginEnabled("Geyser");
    }

    private static void logFloodgateFailure(Exception exception) {
        if (floodgateFailureLogged) return;
        floodgateFailureLogged = true;
        Logger.warn("Floodgate is enabled, but EliteMobs could not use its player API. Bedrock-specific displays will be disabled: " + failureMessage(exception));
    }

    private static void logGeyserFailure(Exception exception) {
        if (geyserFailureLogged) return;
        geyserFailureLogged = true;
        Logger.warn("Geyser is enabled, but EliteMobs could not use its player API. Bedrock-specific displays will be disabled: " + failureMessage(exception));
    }

    private static String failureMessage(Exception exception) {
        Throwable cause = exception instanceof InvocationTargetException && exception.getCause() != null
                ? exception.getCause()
                : exception;
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private static boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }
}
