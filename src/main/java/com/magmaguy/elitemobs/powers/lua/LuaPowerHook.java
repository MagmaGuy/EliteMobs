package com.magmaguy.elitemobs.powers.lua;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum LuaPowerHook {
    ON_SPAWN("on_spawn"),
    ON_TICK("on_game_tick"),
    ON_DAMAGED("on_boss_damaged"),
    ON_DAMAGED_BY_PLAYER("on_boss_damaged_by_player"),
    ON_DAMAGED_BY_ELITE("on_boss_damaged_by_elite"),
    ON_PLAYER_DAMAGED("on_player_damaged_by_boss"),
    ON_ENTER_COMBAT("on_enter_combat"),
    ON_EXIT_COMBAT("on_exit_combat"),
    ON_HEAL("on_heal"),
    ON_TARGET("on_boss_target_changed"),
    ON_DEATH("on_death"),
    ON_PHASE_SWITCH("on_phase_switch"),
    ON_ZONE_ENTER("on_zone_enter"),
    ON_ZONE_LEAVE("on_zone_leave");

    private static final Map<String, LuaPowerHook> BY_KEY = new HashMap<>();

    static {
        for (LuaPowerHook hook : values()) {
            BY_KEY.put(hook.key, hook);
        }
    }

    @Getter
    private final String key;

    LuaPowerHook(String key) {
        this.key = key;
    }

    public static LuaPowerHook fromKey(String key) {
        return BY_KEY.get(key);
    }
}
