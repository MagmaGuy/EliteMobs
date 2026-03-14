package com.magmaguy.elitemobs.powers.lua;

import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;

import java.util.HashMap;
import java.util.function.BiConsumer;

public final class LuaFallingBlockRegistry {

    private static final HashMap<FallingBlock, BiConsumer<FallingBlock, Location>> callbacks = new HashMap<>();

    private LuaFallingBlockRegistry() {
    }

    public static void register(FallingBlock fallingBlock, BiConsumer<FallingBlock, Location> callback) {
        callbacks.put(fallingBlock, callback);
    }

    /**
     * Attempts to handle a falling block landing. Returns true if a Lua callback was registered and executed.
     */
    public static boolean handle(FallingBlock fallingBlock, Location landingLocation) {
        BiConsumer<FallingBlock, Location> callback = callbacks.remove(fallingBlock);
        if (callback == null) {
            return false;
        }
        callback.accept(fallingBlock, landingLocation);
        return true;
    }

    public static void clear() {
        callbacks.clear();
    }
}
