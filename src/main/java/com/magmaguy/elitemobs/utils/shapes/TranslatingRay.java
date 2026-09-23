package com.magmaguy.elitemobs.utils.shapes;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.utils.Lerp;
import com.magmaguy.magmacore.scripting.zones.Ray;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class TranslatingRay extends Ray {
    public TranslatingRay(boolean ignoresSolidBlocks, double pointRadius, Location target, Location finalTarget,
                          Location target2, Location finalTarget2, int animationDuration) {
        super(ignoresSolidBlocks, pointRadius, target, target2);
        RotatingRay.validateEndpoints(target, target2, pointRadius, animationDuration);
        Location firstEnd = finalTarget == null ? target : finalTarget;
        Location secondEnd = finalTarget2 == null ? target2 : finalTarget2;
        RotatingRay.validateEndpoints(target, firstEnd, pointRadius, animationDuration);
        RotatingRay.validateEndpoints(target, secondEnd, pointRadius, animationDuration);
        RotatingRay.validateEndpoints(firstEnd, secondEnd, pointRadius, animationDuration);
        centerLocation = target.clone();
        locations = drawLine(centerLocation, target2.clone());
        // Zero duration is a static initial ray, matching the factories' optional-animation default.
        if (animationDuration > 0)
            startAnimation(target.clone(), firstEnd.clone(), target2.clone(), secondEnd.clone(), animationDuration);
    }

    private void startAnimation(Location firstStart, Location firstEnd, Location secondStart, Location secondEnd,
                                int duration) {
        new BukkitRunnable() {
            private int frame;
            @Override
            public void run() {
                try {
                    double progress = ++frame / (double) duration;
                    Location source = Lerp.lerpLocation(firstStart, firstEnd, progress);
                    Location target = Lerp.lerpLocation(secondStart, secondEnd, progress);
                    locations = drawLine(source, target);
                    centerLocation = source;
                    if (frame == duration) cancel();
                } catch (RuntimeException failure) {
                    cancel();
                    com.magmaguy.magmacore.util.Logger.warn("Stopped translating ray after geometry failure: " + failure);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
    }
}
