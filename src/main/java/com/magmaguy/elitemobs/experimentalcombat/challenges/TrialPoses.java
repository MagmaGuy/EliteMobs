package com.magmaguy.elitemobs.experimentalcombat.challenges;

import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;

import java.lang.reflect.Method;

/** LibsDisguises' optional modern pose API, resolved once instead of linking older supported builds. */
final class TrialPoses {
    private static Method mainHand;
    private static Method offHand;
    private TrialPoses() {}

    static void validate() {
        if (mainHand != null && offHand != null) return;
        try {
            mainHand = LivingWatcher.class.getMethod("setMainHandRaised", boolean.class);
            offHand = LivingWatcher.class.getMethod("setOffhandRaised", boolean.class);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Authored class trials require LibsDisguises with raised-hand poses", failure);
        }
    }

    static void apply(LivingWatcher watcher, boolean main, boolean off) {
        validate();
        try {
            mainHand.invoke(watcher, main);
            offHand.invoke(watcher, off);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("LibsDisguises could not apply a trial pose", failure);
        }
    }
}
