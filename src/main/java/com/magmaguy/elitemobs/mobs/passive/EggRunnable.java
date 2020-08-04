package com.magmaguy.elitemobs.mobs.passive;

import org.bukkit.scheduler.BukkitRunnable;

public class EggRunnable extends BukkitRunnable {

    static final ChickenHandler CHICKEN_HANDLER = new ChickenHandler();

    @Override
    public void run() {

        //drops 1 egg for every loaded super chicken
        CHICKEN_HANDLER.dropEggs();

    }
}
