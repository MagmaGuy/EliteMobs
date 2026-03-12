package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class LuaElitePower extends ElitePower {

    @Getter
    private final LuaPowerConfigFields luaPowerConfigFields;
    private LuaPowerInstance instance = null;

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
        LuaPowerHook hook = mapHook(event);
        if (!luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
            return;
        }
        initializeInstance();
        if (instance != null) {
            instance.handleEvent(hook, event, player, player);
        }
    }

    public void check(Event event, EliteEntity eliteEntity, LivingEntity directTarget) {
        LuaPowerHook hook = mapHook(event);
        if (!luaPowerConfigFields.getLuaPowerDefinition().supportsHook(hook)) {
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
            instance = new LuaPowerInstance(luaPowerConfigFields.getLuaPowerDefinition(), ownerEntity);
        } catch (Exception exception) {
            Logger.warn("Failed to initialize Lua power " + getFileName() + ".");
            exception.printStackTrace();
            instance = null;
        }
    }

    private LuaPowerHook mapHook(Event event) {
        if (event instanceof EliteMobSpawnEvent) return LuaPowerHook.ON_SPAWN;
        if (event instanceof EliteMobDamagedByPlayerEvent) return LuaPowerHook.ON_DAMAGED_BY_PLAYER;
        if (event instanceof EliteMobDamagedByEliteMobEvent) return LuaPowerHook.ON_DAMAGED_BY_ELITE;
        if (event instanceof EliteMobDamagedEvent) return LuaPowerHook.ON_DAMAGED;
        if (event instanceof PlayerDamagedByEliteMobEvent) return LuaPowerHook.ON_PLAYER_DAMAGED;
        if (event instanceof EliteMobEnterCombatEvent) return LuaPowerHook.ON_ENTER_COMBAT;
        if (event instanceof EliteMobExitCombatEvent) return LuaPowerHook.ON_EXIT_COMBAT;
        if (event instanceof EliteMobHealEvent) return LuaPowerHook.ON_HEAL;
        if (event instanceof EliteMobTargetPlayerEvent) return LuaPowerHook.ON_TARGET;
        if (event instanceof EliteMobDeathEvent) return LuaPowerHook.ON_DEATH;
        if (event instanceof ElitePhaseSwitchEvent) return LuaPowerHook.ON_PHASE_SWITCH;
        if (event instanceof ScriptZoneEnterEvent) return LuaPowerHook.ON_ZONE_ENTER;
        if (event instanceof ScriptZoneLeaveEvent) return LuaPowerHook.ON_ZONE_LEAVE;
        return null;
    }
}
