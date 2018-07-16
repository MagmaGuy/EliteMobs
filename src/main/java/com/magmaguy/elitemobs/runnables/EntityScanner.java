package com.magmaguy.elitemobs.runnables;

import com.magmaguy.elitemobs.mobscanner.MobScanner;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityScanner extends BukkitRunnable {

    static MobScanner mobScanner = new MobScanner();

    @Override
    public void run() {

        mobScanner.scanMobs();

    }

}
