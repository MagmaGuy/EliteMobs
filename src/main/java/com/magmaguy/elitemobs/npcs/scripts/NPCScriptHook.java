package com.magmaguy.elitemobs.npcs.scripts;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum NPCScriptHook {
    ON_SPAWN("on_spawn"),
    ON_REMOVE("on_remove"),
    ON_TICK("on_game_tick"),
    ON_INTERACT("on_npc_interact"),
    ON_PROXIMITY_ENTER("on_npc_proximity_enter"),
    ON_PROXIMITY_LEAVE("on_npc_proximity_leave");

    private static final Map<String, NPCScriptHook> BY_KEY = new HashMap<>();

    static {
        for (NPCScriptHook hook : values()) {
            BY_KEY.put(hook.key, hook);
        }
    }

    @Getter
    private final String key;

    NPCScriptHook(String key) {
        this.key = key;
    }

    public static NPCScriptHook fromKey(String key) {
        return BY_KEY.get(key);
    }
}
