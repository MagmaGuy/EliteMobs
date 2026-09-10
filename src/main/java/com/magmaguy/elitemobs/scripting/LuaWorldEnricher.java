package com.magmaguy.elitemobs.scripting;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.magmacore.scripting.tables.LuaTableSupport;
import com.magmaguy.magmacore.scripting.tables.LuaWorldTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;

/** Exposes the existing EM custom-boss spawn owner to ordinary shared Lua contexts. */
public final class LuaWorldEnricher {
    private static boolean registered;
    private LuaWorldEnricher() { }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        LuaWorldTable.registerEnricher((table, world) -> {
            table.set("lightning_effect_at_location", LuaTableSupport.tableMethod(table, args -> {
                var location = LuaTableSupport.tableToLocation(args.checktable(1), world);
                if (location != null && location.getWorld() != null
                        && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
                    com.magmaguy.elitemobs.collateralminecraftchanges.LightningSpawnBypass.strikeLightningIgnoreProtections(location);
                return LuaValue.NIL;
            }));
            table.set("spawn_npc_at_location", LuaTableSupport.tableMethod(table, args -> {
                var location = LuaTableSupport.tableToLocation(args.checktable(2), world);
                if (location == null || location.getWorld() == null
                        || !location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
                    return LuaValue.NIL;
                var npc = new com.magmaguy.elitemobs.npcs.NPCEntity(location, args.checkjstring(1));
                if (!npc.isValid()) {
                    npc.remove(com.magmaguy.elitemobs.api.internal.RemovalReason.OTHER);
                    return LuaValue.NIL;
                }
                var handle = new com.magmaguy.shaded.luaj.vm2.LuaTable();
                handle.set("remove", LuaTableSupport.tableMethod(handle, ignored -> {
                    npc.remove(com.magmaguy.elitemobs.api.internal.RemovalReason.OTHER);
                    return LuaValue.NIL;
                }));
                return handle;
            }));
            table.set("spawn_boss_at_location",
                LuaTableSupport.tableMethod(table, args -> {
                    var location = LuaTableSupport.tableToLocation(args.checktable(2), world);
                    int level = args.checkint(3);
                    if (location == null || location.getWorld() == null
                            || !location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
                        return LuaValue.NIL;
                    var boss = CustomBossEntity.createCustomBossEntity(args.checkjstring(1));
                    if (boss == null) return LuaValue.NIL;
                    boss.spawn(location, level, false);
                    return boss.getLivingEntity() == null || !boss.getLivingEntity().isValid()
                            ? LuaValue.NIL : LuaLivingEntityTable.build(boss.getLivingEntity());
                }));
        });
    }
}
