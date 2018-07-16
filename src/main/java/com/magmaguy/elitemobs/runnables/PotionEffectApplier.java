package com.magmaguy.elitemobs.runnables;

import org.bukkit.scheduler.BukkitRunnable;

public class PotionEffectApplier extends BukkitRunnable {

    public static com.magmaguy.elitemobs.items.PotionEffectApplier potionEffectApplier = new com.magmaguy.elitemobs.items.PotionEffectApplier();

    @Override
    public void run() {

        potionEffectApplier.potionEffectApplier();

    }

}
