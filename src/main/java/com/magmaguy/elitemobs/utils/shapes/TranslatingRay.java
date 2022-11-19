package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.Lerp;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class TranslatingRay extends Ray {
    private Location finalCenterLocation;
    private Location finalTargetLocation;

    public TranslatingRay(boolean ignoresSolidBlocks,
                          double pointRadius,
                          Location target,
                          Location finalTarget,
                          Location target2,
                          Location finalTarget2,
                          int animationDuration) {
        super(ignoresSolidBlocks, pointRadius, target, finalTarget);
        this.finalCenterLocation = finalTarget == null ? target : finalTarget;
        this.finalTargetLocation = finalTarget2 == null ? target2 : finalTarget2;
        locations = drawLine(target, target2);
        startAnimation(target.clone(), finalCenterLocation.clone(), target2.clone(), finalTarget2.clone(), animationDuration);
    }

    private void startAnimation(Location startLocation1,
                                Location endLocation1,
                                Location startLocation2,
                                Location endLocation2,
                                int animationDuration) {
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter > animationDuration) {
                    cancel();
                    return;
                }
                counter++;
                locations = drawLine(
                        Lerp.lerpLocation(startLocation1, endLocation1, counter / (double) animationDuration),
                        Lerp.lerpLocation(startLocation2, endLocation2, counter / (double) animationDuration));
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
    }
}
