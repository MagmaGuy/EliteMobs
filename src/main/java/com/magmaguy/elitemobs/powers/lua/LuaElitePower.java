package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.scripting.ScriptHook;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

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

    public LuaElitePower(LuaPowerConfigFields luaPowerConfigFields) {
        super(luaPowerConfigFields);
        this.luaPowerConfigFields = luaPowerConfigFields;
    }

    @Override
    public void applyPowers(LivingEntity livingEntity) {
        initializeInstance();
    }

    @Override
    public int getExecutionPriority() {
        return luaPowerConfigFields.getLuaPowerDefinition().getPriority();
    }

    public void check(Event event, EliteEntity eliteEntity, Player player) {
        ScriptHook hook = mapHook(event);
        if (hook == null || !luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
            return;
        }
        initializeInstance();
        if (instance != null) {
            instance.handleEvent(hook, event, player, player);
        }
    }

    public void check(Event event, EliteEntity eliteEntity, LivingEntity directTarget) {
        ScriptHook hook = mapHook(event);
        if (hook == null || !luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
            return;
        }
        initializeInstance();
        if (instance != null) {
            instance.handleEvent(hook, event, directTarget, directTarget);
        }
    }

    public void closeRuntime() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
            scriptableBoss = null;
        }
    }

    private void initializeInstance() {
        EliteEntity ownerEntity = getOwnerEntity();
        if (ownerEntity == null || ownerEntity.getLivingEntity() == null) {
            return;
        }
        if (instance != null && !instance.isClosed()) {
            return;
        }
        try {
            scriptableBoss = new ScriptableBoss(ownerEntity);
            instance = new ScriptInstance(luaPowerConfigFields.getLuaPowerDefinition(), scriptableBoss);
            // Bootstrap the Lua VM + tick registration now (on_spawn arrives later as a separate
            // EliteMobSpawnEvent), so a tick-only boss script still starts its on_game_tick loop.
            instance.start();
        } catch (Exception exception) {
            Logger.warn("Failed to initialize Lua power " + getFileName() + ".");
            exception.printStackTrace();
            instance = null;
            scriptableBoss = null;
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
}
