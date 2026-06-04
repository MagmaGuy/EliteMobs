package com.magmaguy.elitemobs.npcs.scripts;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.magmacore.scripting.ScriptHook;
import com.magmaguy.magmacore.scripting.ScriptInstance;
import com.magmaguy.magmacore.scripting.ScriptableEntity;
import com.magmaguy.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Set;

/**
 * Adapts an {@link NPCEntity} for the shared Magmacore Lua scripting runtime.
 * <p>
 * Provides only the NPC-specific (tier-3) context table ({@code context.npc}); the generic
 * scripting surface — {@code context.world} (incl. strike_lightning), zones, scheduler,
 * cooldowns, log, event — is injected automatically by {@link ScriptInstance}, the same way
 * FreeMinecraftModels props receive it. This replaces the old self-contained NPC script
 * runtime that could not reach any of those primitives.
 */
public class ScriptableNPC extends ScriptableEntity {

    // NPC-specific hooks (the generic on_spawn / on_game_tick / on_zone_* come from ScriptHook).
    public static final ScriptHook ON_REMOVE = new ScriptHook("on_remove");
    public static final ScriptHook ON_INTERACT = new ScriptHook("on_npc_interact");
    public static final ScriptHook ON_PROXIMITY_ENTER = new ScriptHook("on_npc_proximity_enter");
    public static final ScriptHook ON_PROXIMITY_LEAVE = new ScriptHook("on_npc_proximity_leave");

    private static final Set<ScriptHook> SUPPORTED_HOOKS = Set.of(
            ScriptHook.ON_SPAWN,
            ScriptHook.ON_TICK,
            ScriptHook.ON_ZONE_ENTER,
            ScriptHook.ON_ZONE_LEAVE,
            ON_REMOVE,
            ON_INTERACT,
            ON_PROXIMITY_ENTER,
            ON_PROXIMITY_LEAVE
    );

    private final NPCEntity npcEntity;

    public ScriptableNPC(NPCEntity npcEntity) {
        this.npcEntity = npcEntity;
    }

    public NPCEntity getNpcEntity() {
        return npcEntity;
    }

    @Override
    public LuaTable buildContextTable(ScriptInstance instance) {
        return LuaNPCTable.build(npcEntity, instance);
    }

    @Override
    public String getContextKey() {
        return "npc";
    }

    @Override
    public Set<ScriptHook> getSupportedHooks() {
        return SUPPORTED_HOOKS;
    }

    @Override
    public Entity getBukkitEntity() {
        return npcEntity.getVillager();
    }

    @Override
    public Location getLocation() {
        if (npcEntity.getVillager() != null && npcEntity.getVillager().isValid())
            return npcEntity.getVillager().getLocation();
        return npcEntity.getSpawnLocation();
    }

    /**
     * Exposes {@code context.player} as the actor of the current event (interact / proximity),
     * built from Magmacore's living-entity table so NPC scripts get the full player API.
     */
    @Override
    public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
        if ("player".equals(key)) {
            LivingEntity actor = instance.getCurrentEventActor();
            if (actor != null) return LuaLivingEntityTable.build(actor);
        }
        return LuaValue.NIL;
    }
}
