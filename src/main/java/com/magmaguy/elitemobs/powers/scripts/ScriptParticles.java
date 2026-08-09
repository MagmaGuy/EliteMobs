package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptParticles {

    private static final Set<String> UNSUPPORTED_PARTICLE_DATA_WARNINGS = ConcurrentHashMap.newKeySet();

    ScriptParticlesBlueprint particlesBlueprint;

    public ScriptParticles(ScriptParticlesBlueprint particlesBlueprint) {
        this.particlesBlueprint = particlesBlueprint;
    }

    public void visualize(ScriptActionData scriptActionData, Location location, ScriptRuntimeOwner runtimeOwner) {
        particlesBlueprint.getParticleBlueprints().forEach(particleBlueprint -> new ScriptParticle(particleBlueprint).visualize(scriptActionData, location, runtimeOwner));
    }

    private class ScriptParticle {

        private final ScriptParticlesBlueprint.ScriptParticleBlueprint particleBlueprint;

        public ScriptParticle(ScriptParticlesBlueprint.ScriptParticleBlueprint scriptParticlesBlueprint) {
            this.particleBlueprint = scriptParticlesBlueprint;
        }

        private void visualize(ScriptActionData scriptActionData, Location location, ScriptRuntimeOwner runtimeOwner) {
            // Skip if world is no longer loaded (e.g., dungeon instance closing)
            if (location.getWorld() == null) return;

            double x = particleBlueprint.getX().getValue();
            double y = particleBlueprint.getY().getValue();
            double z = particleBlueprint.getZ().getValue();
            int amount = particleBlueprint.getAmount().getValue();

            if (particleBlueprint.getRelativeVectorBlueprint() != null) {
                ScriptRelativeVector scriptRelativeVector = null;
                scriptRelativeVector = new ScriptRelativeVector(this.particleBlueprint.getRelativeVectorBlueprint(), runtimeOwner, location);

                Vector movementVector = scriptRelativeVector.getVector(scriptActionData);
                amount = 0;
                x = movementVector.getX();
                y = movementVector.getY();
                z = movementVector.getZ();
            }


            if (particleBlueprint.getParticle().equals(Particle.DUST))
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        particleBlueprint.getSpeed().getValue(),
                        new Particle.DustOptions(
                                Color.fromRGB(
                                        particleBlueprint.getRed().getValue(),
                                        particleBlueprint.getGreen().getValue(),
                                        particleBlueprint.getBlue().getValue()),
                                1));
            else if (particleBlueprint.getParticle().equals(Particle.DUST_COLOR_TRANSITION))
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        (double) particleBlueprint.getSpeed().getValue(),
                        new Particle.DustTransition(
                                Color.fromRGB(
                                        particleBlueprint.getRed().getValue(),
                                        particleBlueprint.getGreen().getValue(),
                                        particleBlueprint.getBlue().getValue()),
                                Color.fromRGB(
                                        particleBlueprint.getToRed().getValue(),
                                        particleBlueprint.getToGreen().getValue(),
                                        particleBlueprint.getToBlue().getValue()),
                                1));
            else if (particleBlueprint.getDataType() == Color.class)
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        (double) particleBlueprint.getSpeed().getValue(),
                        Color.fromRGB(
                                particleBlueprint.getRed().getValue(),
                                particleBlueprint.getGreen().getValue(),
                                particleBlueprint.getBlue().getValue()));
            else {
                // Note: WITCH used to have a special-case branch here that passed the x/y/z offset
                // fields as world coordinates (binding to the double-x,y,z overload), spawning the
                // particles near the world origin. WITCH has a Void data type, so the generic
                // branch below handles it with the correct location/offset semantics.
                Class<?> dataType = particleBlueprint.getDataType();
                if (dataType == Void.class) {
                    location.getWorld().spawnParticle(
                            particleBlueprint.getParticle(),
                            location,
                            amount,
                            x,
                            y,
                            z,
                            (double) particleBlueprint.getSpeed().getValue());
                    return;
                }

                Object data = createParticleData(dataType);
                if (data == null) {
                    warnUnsupportedParticleData(dataType);
                    return;
                }

                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        (double) particleBlueprint.getSpeed().getValue(),
                        data);
            }

        }

        private Object createParticleData(Class<?> dataType) {
            if (BlockData.class.isAssignableFrom(dataType)) return particleBlueprint.getBlockData();

            if (ItemStack.class.isAssignableFrom(dataType)) return particleBlueprint.getItemStack();

            //Not cached: the speed value is randomized per call
            if (dataType == Float.class) return particleBlueprint.getSpeed().getValue();
            if (dataType == Integer.class) return 0;
            return null;
        }

        private void warnUnsupportedParticleData(Class<?> dataType) {
            String warningKey = particleBlueprint.getParticle().name() + ':' + dataType.getName();
            if (!UNSUPPORTED_PARTICLE_DATA_WARNINGS.add(warningKey)) return;
            Logger.warn("Skipping script particle " + particleBlueprint.getParticle().name() +
                    " in script " + particleBlueprint.getScriptName() +
                    " because Bukkit requires unsupported " + dataType.getSimpleName() + " data.");
        }
    }
}
