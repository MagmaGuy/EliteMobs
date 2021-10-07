package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class VisualItemProcessor {

    private final boolean hasValidEffect;

    public VisualItemProcessor(Object[][] multiDimensionalTrailTracker, Vector[][] cachedVectorPositions,
                               boolean visualEffectBoolean, int pointsPerRotation, EliteEntity eliteEntity) {

        this.hasValidEffect = visualEffectBoolean;
        if (multiDimensionalTrailTracker.length < 1)
            return;

        rotateExistingEffects(multiDimensionalTrailTracker, cachedVectorPositions, pointsPerRotation, eliteEntity);

    }

    /*
    Adjusts the position in each item track based on the amount of powers currently in that track
     */
    public static int adjustTrackPosition(double pointsPerRotation, int totalEffectQuantity,
                                          int sectionCounter, int globalCounter) {
        int location = (int) (pointsPerRotation / totalEffectQuantity * sectionCounter + globalCounter);
        if (location >= 30)
            location -= 30;
        return location;
    }

    private void rotateExistingEffects(Object[][] multiDimensionalTrailTracker, Vector[][] cachedVectorPositions,
                                       int pointsPerRotation, EliteEntity eliteEntity) {

        new BukkitRunnable() {

            final boolean isObfuscated = eliteEntity.isVisualEffectObfuscated();
            int counter = 0;

            @Override
            public void run() {

                if (!eliteEntity.isValid() || !hasValidEffect) {
                    VisualItemRemover.removeItems(multiDimensionalTrailTracker);
                    cancel();
                    return;
                }

                for (int i = 0; i < multiDimensionalTrailTracker.length; i++) {
                    int sectionCounter = 0;
                    for (int j = 0; j < multiDimensionalTrailTracker[i].length; j++) {

                        int adjustedEffectPositionInRotation = adjustTrackPosition(
                                pointsPerRotation,
                                multiDimensionalTrailTracker[i].length,
                                sectionCounter,
                                counter);

                        Vector vector = cachedVectorPositions[i][adjustedEffectPositionInRotation];

                        if (multiDimensionalTrailTracker[i][j] instanceof Item)
                            rotateItem(multiDimensionalTrailTracker[i][j], vector, eliteEntity);

                        if (multiDimensionalTrailTracker[i][j] instanceof Particle)
                            rotateParticle(multiDimensionalTrailTracker[i][j], vector, eliteEntity);

                        sectionCounter++;
                        if (sectionCounter >= pointsPerRotation)
                            sectionCounter = 0;

                    }


                }

                counter++;
                if (counter >= pointsPerRotation)
                    counter = 0;

                /*
                Check if the effect has ceased being obfuscated
                 */
                if (isObfuscated != eliteEntity.isVisualEffectObfuscated()) {
                    VisualItemRemover.removeItems(multiDimensionalTrailTracker);
                    cancel();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            eliteEntity.setVisualEffectObfuscated(false);
                            if (Arrays.deepEquals(cachedVectorPositions, MinorPowerStanceMath.cachedVectors)) {
                                eliteEntity.setMinorVisualEffect(false);
                                new MinorPowerPowerStance(eliteEntity);
                            }
                            if (Arrays.deepEquals(cachedVectorPositions, MajorPowerStanceMath.cachedVectors)) {
                                eliteEntity.setMajorVisualEffect(false);
                                new MajorPowerPowerStance(eliteEntity);
                            }
                        }
                    }.runTask(MetadataHandler.PLUGIN);

                }

            }

        }.runTaskTimerAsynchronously(MetadataHandler.PLUGIN, 0, 5);

    }

    private void rotateItem(Object itemObject, Vector vector, EliteEntity eliteEntity) {

        Item item = (Item) itemObject;

//        if (!item.isValid())
//            return;

        Location currentLocation = item.getLocation().clone();
        Location newLocation = eliteEntity.getLivingEntity().getLocation().clone().add(new Vector(0, 1, 0)).add(vector);

//        if (currentLocation.distanceSquared(newLocation) > Math.pow(3, 2)) {
//            item.teleport(newLocation);
//            item.setVelocity(new Vector(0.01, 0.01, 0.01));
//            return;
//        }

        Vector movementVector = (newLocation.subtract(currentLocation)).toVector();
        movementVector = movementVector.multiply(0.3);

//        if (Math.abs(movementVector.getX()) > 3 || Math.abs(movementVector.getY()) > 3 || Math.abs(movementVector.getZ()) > 3) {
//            item.teleport(newLocation);
//        } else {
        item.setVelocity(movementVector);
//        }

    }

    private void rotateParticle(Object particleObject, Vector vector, EliteEntity eliteEntity) {
        Particle particle = (Particle) particleObject;
        if (eliteEntity.isValid())
            eliteEntity.getLivingEntity().getWorld().spawnParticle(
                    particle, eliteEntity.getLivingEntity().getLocation().add(0, 1, 0).add(vector),
                    1, 0, 0, 0, 0.01);
    }

}
