package com.magmaguy.elitemobs.transport;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

/** Loads the optional FMM dependency only when a route requests a model. */
final class TransportModel {
    static AutoCloseable attach(LivingEntity carrier, TransportRoute route) {
        if (route.model().isBlank()) return () -> {};
        if (!Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels"))
            throw new IllegalArgumentException("This route requires FreeMinecraftModels");
        return Fmm.attach(carrier, route);
    }
    private static final class Fmm {
        static AutoCloseable attach(LivingEntity carrier, TransportRoute route) {
            var model = com.magmaguy.freeminecraftmodels.customentity.DynamicEntity
                    .createWithInvisibility(route.model(), carrier);
            if (model == null) throw new IllegalArgumentException("Unknown transport model: " + route.model());
            try {
                if (!route.animation().isBlank()) model.playAnimation(route.animation(), true, true);
            } catch (RuntimeException failure) { model.remove(); throw failure; }
            return model::remove;
        }
    }
}
