package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.scripting.LuaEngine;
import com.magmaguy.magmacore.scripting.ScriptDefinition;
import com.magmaguy.magmacore.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Wires EliteMobs NPC Lua scripts into Magmacore's shared {@link LuaEngine}.
 * <p>
 * NPC scripts now run on the same scripting runtime as FreeMinecraftModels props and
 * EliteMobs bosses, so they inherit every Magmacore primitive (context.world incl.
 * strike_lightning, zones, scheduler, cooldowns, log, ...) automatically. This replaces the
 * old self-contained NPC Lua engine that only exposed a handful of NPC-specific helpers.
 */
public final class NPCScriptManager {

    public static final String NAMESPACE = "elitemobs_npc";

    private static EliteMobsNPCScriptProvider provider;

    private NPCScriptManager() {
    }

    public static void initialize() {
        Path scriptDirectory = getScriptDirectory();
        try {
            Files.createDirectories(scriptDirectory);
        } catch (IOException exception) {
            Logger.warn("Failed to initialize NPC Lua script directory " + scriptDirectory + ".");
        }
        provider = new EliteMobsNPCScriptProvider(scriptDirectory);
        LuaEngine.registerScriptProvider(provider);
    }

    public static Path getScriptDirectory() {
        return MetadataHandler.PLUGIN.getDataFolder().toPath().resolve("npc_scripts");
    }

    public static ScriptDefinition getDefinition(String fileName) {
        return LuaEngine.getDefinition(NAMESPACE, fileName);
    }

    public static void shutdown() {
        if (provider != null) {
            LuaEngine.unregisterScriptProvider(NAMESPACE);
            provider = null;
        }
    }
}
