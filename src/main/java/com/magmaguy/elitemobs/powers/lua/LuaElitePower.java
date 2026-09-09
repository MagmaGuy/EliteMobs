package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerActivity;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseState;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.scripting.ScriptHook;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.scripting.ScriptQueryResult;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A boss power backed by a Lua script. Runs on Magmacore's shared {@link ScriptInstance}
 * runtime via {@link ScriptableBoss} — the same runtime used by NPCs and FreeMinecraftModels
 * props. Boss-specific EliteMobs events are mapped to {@link ScriptHook}s and dispatched.
 */
public class LuaElitePower extends ElitePower {

    @Getter
    private final LuaPowerConfigFields luaPowerConfigFields;
    private ScriptInstance instance = null;
    private ScriptableBoss scriptableBoss = null;
    private final Map<String, Long> successfulEventHooks = new LinkedHashMap<>();
    private final Map<String, Long> failedEventHooks = new LinkedHashMap<>();
    private long acceptedMindActions;
    private long deferredMindActions;
    private long rejectedMindActions;
    private final ElitePowerPauseState powerPauseState = new ElitePowerPauseState();

    public LuaElitePower(LuaPowerConfigFields luaPowerConfigFields) {
        super(luaPowerConfigFields);
        this.luaPowerConfigFields = luaPowerConfigFields;
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        initializeInstance(false);
    }

    @Override
    public int getExecutionPriority() {
        return luaPowerConfigFields.getLuaPowerDefinition().getPriority();
    }

    public void check(Event event, EliteEntity eliteEntity, Player player) {
        check(event, eliteEntity, (LivingEntity) player);
    }

    /** Shares the original, mutable damage event with the summoner's ordinary Lua powers. */
    public void checkReinforcementDamage(EliteMobDamagedByPlayerEvent event) {
        EliteEntity owner = getOwnerEntity();
        if (owner == null || event.isCancelled() || powerPauseState.isPaused()
                || owner.getPowerSuppression().isSuppressed()) return;
        ScriptHook hook = ScriptableBoss.ON_REINFORCEMENT_DAMAGED_BY_PLAYER;
        if (!luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) return;
        initializeInstance(false);
        if (instance != null) {
            instance.handleEvent(hook, event, event.getEliteMobEntity().getLivingEntity(), event.getPlayer());
            recordEventHook(hook, !instance.isClosed());
        } else recordEventHook(hook, false);
    }

    public void check(Event event, EliteEntity eliteEntity, LivingEntity directTarget) {
        if (powerPauseState.isPaused() || eliteEntity.getPowerSuppression().isSuppressed()) return;
        ScriptHook hook = mapHook(event);
        // The spawn consumer applies powers before the owner has its live body.
        // Start the tick runtime at the subsequent spawn event even if the script
        // does not declare an optional on_spawn callback.
        if (hook == ScriptHook.ON_SPAWN) initializeInstance(false);
        if (hook == null || !luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
            return;
        }
        initializeInstance(false);
        if (instance != null) {
            instance.handleEvent(hook, event, directTarget, directTarget);
            recordEventHook(hook, !instance.isClosed());
        } else recordEventHook(hook, false);
    }

    /** Dispatches one native Mind action into this power's existing Lua runtime. */
    public ElitePowerActionResult handleMindAction(ElitePowerActionRequest request) {
        if (powerPauseState.isPaused()
                || (getOwnerEntity() != null && getOwnerEntity().getPowerSuppression().isSuppressed())) {
            return ElitePowerActionResult.REJECTED;
        }
        if (!luaPowerConfigFields.getLuaPowerDefinition().supportsHook(ScriptableBoss.ON_MIND_ACTION)) {
            return ElitePowerActionResult.REJECTED;
        }
        initializeInstance(false);
        if (instance == null || scriptableBoss == null) {
            return recordMindAction(ElitePowerActionResult.REJECTED);
        }

        ScriptQueryResult query = scriptableBoss.handleMindAction(instance, request);
        if (query.kind() == ScriptQueryResult.Kind.UNHANDLED
                || query.kind() == ScriptQueryResult.Kind.NIL
                || query.kind() == ScriptQueryResult.Kind.FAILED) {
            return recordMindAction(ElitePowerActionResult.REJECTED);
        }
        if (query.kind() != ScriptQueryResult.Kind.STRING) {
            Logger.warn("Lua power " + getFileName()
                    + " returned a non-string on_mind_action result; expected accepted, deferred, or rejected.");
            closeRuntime();
            return recordMindAction(ElitePowerActionResult.REJECTED);
        }
        try {
            return recordMindAction(ElitePowerActionResult.valueOf(
                    query.stringValue().orElseThrow().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException invalidResult) {
            Logger.warn("Lua power " + getFileName()
                    + " returned invalid on_mind_action result '"
                    + query.stringValue().orElse("") + "'.");
            closeRuntime();
            return recordMindAction(ElitePowerActionResult.REJECTED);
        }
    }

    /** Creates the public immutable activity view paired with its registered descriptor. */
    public EliteLuaPowerActivity activitySnapshot(EliteLuaPowerProgram program) {
        if (program == null) throw new IllegalArgumentException("program cannot be null");
        return new EliteLuaPowerActivity(
                program.key(),
                program.revision(),
                successfulEventHooks,
                failedEventHooks,
                acceptedMindActions,
                deferredMindActions,
                rejectedMindActions,
                instance != null && !instance.isClosed());
    }

    @Override
    public void closeRuntime() {
        ScriptInstance closing = instance;
        if (closing == null) return;
        instance = null;
        scriptableBoss = null;
        closing.shutdown();
    }

    /**
     * Eagerly starts this power through the normal Lua runtime, but propagates initialization
     * failure so a multi-power actor preparation can roll back atomically.
     */
    public void startRuntimeOrThrow() {
        initializeInstance(true);
    }

    public boolean isRuntimeActive() { return instance != null && !instance.isClosed(); }

    /** Freezes this runtime without discarding its Lua VM, state table, or owned callbacks. */
    public void setRuntimePaused(boolean paused) {
        setRuntimePauseReason(ElitePowerPauseReason.MIND_SERVICE, paused);
    }

    public void setRuntimePauseReason(ElitePowerPauseReason reason, boolean paused) {
        powerPauseState.set(reason, paused);
        if (instance != null && !instance.isClosed()) instance.setPaused(powerPauseState.isPaused());
    }

    private void initializeInstance(boolean failFast) {
        EliteEntity ownerEntity = getOwnerEntity();
        if (ownerEntity == null || ownerEntity.getLivingEntity() == null) {
            if (failFast) {
                throw new IllegalStateException("Lua power has no loaded owner entity");
            }
            return;
        }
        if (instance != null && !instance.isClosed()) {
            return;
        }
        // Script failures close the retained instance. Do not silently reset its state
        // on the next damage hook. Deliberate owner teardown uses closeRuntime(), which
        // clears the reference and permits normal body rehydration to start afresh.
        if (instance != null && instance.isClosed()) {
            if (failFast) throw new IllegalStateException("Lua power previously failed: " + getFileName());
            return;
        }
        try {
            scriptableBoss = new ScriptableBoss(ownerEntity);
            instance = new ScriptInstance(luaPowerConfigFields.getLuaPowerDefinition(), scriptableBoss);
            // Bootstrap the Lua VM + tick registration now (on_spawn arrives later as a separate
            // EliteMobSpawnEvent), so a tick-only boss script still starts its on_game_tick loop.
            instance.start();
            instance.setPaused(powerPauseState.isPaused());
        } catch (Exception exception) {
            if (instance != null) {
                try {
                    instance.shutdown();
                } catch (RuntimeException ignored) {
                    // Preserve the original initialization failure.
                }
            }
            instance = null;
            scriptableBoss = null;
            if (failFast) {
                throw new IllegalStateException(
                        "Failed to initialize Lua power " + getFileName(), exception);
            }
            Logger.warn("Failed to initialize Lua power " + getFileName() + ".");
            exception.printStackTrace();
        }
    }

    private ScriptHook mapHook(Event event) {
        if (event instanceof EliteMobSpawnEvent) return ScriptHook.ON_SPAWN;
        if (event instanceof EliteMobDamagedByPlayerEvent) return ScriptableBoss.ON_DAMAGED_BY_PLAYER;
        if (event instanceof EliteMobDamagedByEliteMobEvent) return ScriptableBoss.ON_DAMAGED_BY_ELITE;
        if (event instanceof EliteMobDamagedEvent) return ScriptableBoss.ON_DAMAGED;
        if (event instanceof PlayerDamagedByEliteMobEvent) return ScriptableBoss.ON_PLAYER_DAMAGED;
        if (event instanceof EliteMobEnterCombatEvent) return ScriptableBoss.ON_ENTER_COMBAT;
        if (event instanceof EliteMobExitCombatEvent) return ScriptableBoss.ON_EXIT_COMBAT;
        if (event instanceof EliteMobHealEvent) return ScriptableBoss.ON_HEAL;
        if (event instanceof EliteMobTargetPlayerEvent) return ScriptableBoss.ON_TARGET;
        if (event instanceof EliteMobDeathEvent) return ScriptableBoss.ON_DEATH;
        if (event instanceof ElitePhaseSwitchEvent) return ScriptableBoss.ON_PHASE_SWITCH;
        if (event instanceof ScriptZoneEnterEvent) return ScriptHook.ON_ZONE_ENTER;
        if (event instanceof ScriptZoneLeaveEvent) return ScriptHook.ON_ZONE_LEAVE;
        return null;
    }

    private void recordEventHook(ScriptHook hook, boolean succeeded) {
        Map<String, Long> counters = succeeded ? successfulEventHooks : failedEventHooks;
        counters.compute(hook.getKey(), (ignored, current) -> increment(current));
    }

    private ElitePowerActionResult recordMindAction(ElitePowerActionResult result) {
        switch (result) {
            case ACCEPTED -> acceptedMindActions = increment(acceptedMindActions);
            case DEFERRED -> deferredMindActions = increment(deferredMindActions);
            case REJECTED -> rejectedMindActions = increment(rejectedMindActions);
        }
        return result;
    }

    private static long increment(Long value) {
        return value == null ? 1L : increment(value.longValue());
    }

    private static long increment(long value) {
        return value == Long.MAX_VALUE ? Long.MAX_VALUE : value + 1L;
    }
}
