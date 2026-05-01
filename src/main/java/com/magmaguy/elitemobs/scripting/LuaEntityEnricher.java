package com.magmaguy.elitemobs.scripting;

import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.magmacore.scripting.tables.LuaEntityTable;
import com.magmaguy.magmacore.scripting.tables.LuaTableSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;

/**
 * Installed on EliteMobs enable to contribute elite/boss-specific fields to every
 * Lua entity table built by EliteMobs's bundled Magmacore copy. Forwards to
 * {@link FreeMinecraftModelsEntityEnricher} when FMM is present so EliteMobs scripts
 * can inspect modeled/prop state without Magmacore doing cross-plugin reflection.
 */
public final class LuaEntityEnricher {

    private static boolean registered = false;

    private LuaEntityEnricher() {
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        LuaEntityTable.registerEnricher(LuaEntityEnricher::enrich);
    }

    private static void enrich(LuaTable table, Entity entity) {
        addEliteFields(table, entity);
        if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
            try {
                FreeMinecraftModelsEntityEnricher.enrich(table, entity);
            } catch (NoClassDefFoundError ignored) {
                // FMM API not on classpath despite plugin being enabled — skip enrichment.
            }
        }
    }

    private static void addEliteFields(LuaTable table, Entity entity) {
        boolean isElite = EntityTracker.isEliteMob(entity);
        table.set("is_elite", LuaValue.valueOf(isElite));

        if (!isElite) {
            table.set("is_custom_boss", LuaValue.FALSE);
            table.set("is_significant_boss", LuaValue.FALSE);
            return;
        }

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
        if (eliteEntity == null) {
            table.set("is_custom_boss", LuaValue.FALSE);
            table.set("is_significant_boss", LuaValue.FALSE);
            return;
        }

        LuaTable eliteTable = new LuaTable();
        eliteTable.set("level", LuaValue.valueOf(eliteEntity.getLevel()));
        String name = eliteEntity.getName();
        eliteTable.set("name", name != null ? LuaValue.valueOf(name) : LuaValue.NIL);
        eliteTable.set("health", LuaValue.valueOf(eliteEntity.getHealth()));
        eliteTable.set("max_health", LuaValue.valueOf(eliteEntity.getMaxHealth()));

        boolean isCustomBoss = eliteEntity.isCustomBossEntity();
        eliteTable.set("is_custom_boss", LuaValue.valueOf(isCustomBoss));
        table.set("is_custom_boss", LuaValue.valueOf(isCustomBoss));

        double healthMultiplier = eliteEntity.getHealthMultiplier();
        double damageMultiplier = eliteEntity.getDamageMultiplier();
        eliteTable.set("health_multiplier", LuaValue.valueOf(healthMultiplier));
        eliteTable.set("damage_multiplier", LuaValue.valueOf(damageMultiplier));
        table.set("is_significant_boss", LuaValue.valueOf(isCustomBoss && healthMultiplier > 1));

        final EliteEntity capturedElite = eliteEntity;
        eliteTable.set("remove", LuaTableSupport.tableMethod(eliteTable, args -> {
            capturedElite.remove(RemovalReason.OTHER);
            return LuaValue.NIL;
        }));

        table.set("elite", eliteTable);
    }
}
