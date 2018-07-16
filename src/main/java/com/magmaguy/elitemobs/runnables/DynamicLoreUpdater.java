package com.magmaguy.elitemobs.runnables;

import com.magmaguy.elitemobs.items.DynamicLore;
import org.bukkit.scheduler.BukkitRunnable;

public class DynamicLoreUpdater extends BukkitRunnable {

    @Override
    public void run() {

        DynamicLore.refreshDynamicLore();

    }

}
