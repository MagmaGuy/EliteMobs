package com.magmaguy.elitemobs.scripting;

import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntity;
import com.magmaguy.freeminecraftmodels.customentity.PropEntity;
import com.magmaguy.magmacore.scripting.tables.LuaTableSupport;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * Adds FreeMinecraftModels-specific fields (is_modeled, is_prop, model sub-table)
 * to Lua entity tables built by EliteMobs's bundled Magmacore. Only invoked from
 * {@link LuaEntityEnricher} when FMM is enabled, so the direct FMM imports below
 * are loaded lazily via the runtime plugin check upstream.
 */
final class FreeMinecraftModelsEntityEnricher {

    private FreeMinecraftModelsEntityEnricher() {
    }

    static void enrich(LuaTable table, Entity entity) {
        Map<Entity, ModeledEntity> loaded = ModeledEntity.getLoadedModeledEntitiesWithUnderlyingEntities();
        ModeledEntity modeledEntity = loaded != null ? loaded.get(entity) : null;
        boolean isModeled = modeledEntity != null;
        table.set("is_modeled", LuaValue.valueOf(isModeled));

        boolean isProp = entity instanceof ArmorStand armorStand && PropEntity.isPropEntity(armorStand);
        table.set("is_prop", LuaValue.valueOf(isProp));

        if (!isModeled) return;

        LuaTable modelTable = new LuaTable();
        String modelId = modeledEntity.getEntityID();
        modelTable.set("model_id", modelId != null ? LuaValue.valueOf(modelId) : LuaValue.NIL);

        final ModeledEntity capturedModel = modeledEntity;
        modelTable.set("play_animation", LuaTableSupport.tableMethod(modelTable, args -> {
            String animName = args.checkjstring(1);
            boolean blend = args.optboolean(2, false);
            boolean loop = args.optboolean(3, false);
            return LuaValue.valueOf(capturedModel.playAnimation(animName, blend, loop));
        }));
        modelTable.set("stop_animations", LuaTableSupport.tableMethod(modelTable, args -> {
            capturedModel.stopCurrentAnimations();
            return LuaValue.NIL;
        }));
        modelTable.set("remove", LuaTableSupport.tableMethod(modelTable, args -> {
            capturedModel.remove();
            return LuaValue.NIL;
        }));
        modelTable.set("is_dynamic", LuaValue.valueOf(DynamicEntity.isDynamicEntity(entity)));

        table.set("model", modelTable);
    }
}
