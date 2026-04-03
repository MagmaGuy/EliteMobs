package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.scripting.ScriptHook;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.scripting.ScriptableEntity;
import com.magmaguy.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps an {@link EliteEntity} as a {@link ScriptableEntity} so it can be
 * managed by Magmacore's scripting infrastructure.
 * <p>
 * This class coexists with the legacy {@code LuaPower*} system.
 * It exposes a basic boss table via {@link LuaLivingEntityTable}; the full
 * EliteMobs-specific boss table (see {@link LuaBossTableBuilder}) will be
 * wired in a future pass.
 */
public class ScriptableBoss extends ScriptableEntity {

    // ── Boss-specific hooks ─────────────────────────────────────────────
    public static final ScriptHook ON_DAMAGED = new ScriptHook("on_boss_damaged");
    public static final ScriptHook ON_DAMAGED_BY_PLAYER = new ScriptHook("on_boss_damaged_by_player");
    public static final ScriptHook ON_DAMAGED_BY_ELITE = new ScriptHook("on_boss_damaged_by_elite");
    public static final ScriptHook ON_PLAYER_DAMAGED = new ScriptHook("on_player_damaged_by_boss");
    public static final ScriptHook ON_ENTER_COMBAT = new ScriptHook("on_enter_combat");
    public static final ScriptHook ON_EXIT_COMBAT = new ScriptHook("on_exit_combat");
    public static final ScriptHook ON_HEAL = new ScriptHook("on_heal");
    public static final ScriptHook ON_TARGET = new ScriptHook("on_boss_target_changed");
    public static final ScriptHook ON_DEATH = new ScriptHook("on_death");
    public static final ScriptHook ON_PHASE_SWITCH = new ScriptHook("on_phase_switch");

    private static final Set<ScriptHook> SUPPORTED_HOOKS = Set.of(
            ScriptHook.ON_SPAWN, ScriptHook.ON_TICK,
            ScriptHook.ON_ZONE_ENTER, ScriptHook.ON_ZONE_LEAVE,
            ON_DAMAGED, ON_DAMAGED_BY_PLAYER, ON_DAMAGED_BY_ELITE,
            ON_PLAYER_DAMAGED, ON_ENTER_COMBAT, ON_EXIT_COMBAT,
            ON_HEAL, ON_TARGET, ON_DEATH, ON_PHASE_SWITCH
    );

    private final EliteEntity eliteEntity;

    public ScriptableBoss(EliteEntity eliteEntity) {
        this.eliteEntity = eliteEntity;
    }

    // ── ScriptableEntity contract ───────────────────────────────────────

    @Override
    public LuaTable buildContextTable(ScriptInstance instance) {
        // Basic boss table using Magmacore's LuaLivingEntityTable.
        // The full boss table with EliteMobs-specific features
        // (reinforcements, shield wall, tracking fireballs, etc.)
        // will be wired in a future refactoring pass.
        if (eliteEntity.getLivingEntity() == null) return new LuaTable();
        return LuaLivingEntityTable.build(eliteEntity.getLivingEntity());
    }

    @Override
    public String getContextKey() {
        return "boss";
    }

    @Override
    public Set<ScriptHook> getSupportedHooks() {
        return SUPPORTED_HOOKS;
    }

    @Override
    public Entity getBukkitEntity() {
        return eliteEntity.getLivingEntity();
    }

    @Override
    public Location getLocation() {
        return eliteEntity.getLocation();
    }

    @Override
    public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
        // Boss-specific extra context (players, entities, event, script,
        // cooldowns, settings) will be wired in a future pass.
        return LuaValue.NIL;
    }

    // ── Global cooldown (shared per EliteEntity) ─────────────────────────

    private static final Map<EliteEntity, Map<String, Long>> bossGlobalCooldowns = new ConcurrentHashMap<>();

    @Override
    public Map<String, Long> getGlobalCooldownStore() {
        return bossGlobalCooldowns.computeIfAbsent(eliteEntity, k -> new HashMap<>());
    }

    public static void clearGlobalCooldowns(EliteEntity entity) {
        bossGlobalCooldowns.remove(entity);
    }

    // ── EliteMobs-specific accessor ─────────────────────────────────────

    public EliteEntity getEliteEntity() {
        return eliteEntity;
    }
}
