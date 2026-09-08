package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Method;

/** Optional disguise presentation; no LibsDisguises classes are linked by the combat actor. */
final class TrialPoses {
    private static boolean warned;
    private TrialPoses() {}

    static boolean available() {
        return Bukkit.getPluginManager().isPluginEnabled("LibsDisguises");
    }

    static void apply(LivingEntity entity, boolean main, boolean off) {
        if (!available()) return;
        try {
            DisguisePoses.apply(entity, main, off);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            if (!warned) {
                warned = true;
                Logger.warn("Class-trial disguise poses are unavailable; combat continues with ordinary animations: "
                        + failure.getMessage());
            }
        }
    }

    /** Loaded only after the optional plugin is enabled. Older versions can omit the pose API. */
    private static final class DisguisePoses {
        private static final Method MAIN = poseMethod("setMainHandRaised");
        private static final Method OFF = poseMethod("setOffhandRaised");

        private static Method poseMethod(String name) {
            try {
                return me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher.class
                        .getMethod(name, boolean.class);
            } catch (NoSuchMethodException unavailable) {
                return null;
            }
        }

        private static void apply(LivingEntity entity, boolean main, boolean off)
                throws ReflectiveOperationException {
            var disguise = me.libraryaddict.disguise.DisguiseAPI.getDisguise(entity);
            if (disguise == null) return;
            var watcher = disguise.getWatcher();
            if (!(watcher instanceof me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher)) return;
            if (MAIN != null) MAIN.invoke(watcher, main);
            if (OFF != null) OFF.invoke(watcher, off);
        }
    }
}
