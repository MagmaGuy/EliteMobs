package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ScriptParticles {

    ScriptParticlesBlueprint particlesBlueprint;

    public ScriptParticles(ScriptParticlesBlueprint particlesBlueprint) {
        this.particlesBlueprint = particlesBlueprint;
    }

    public void visualize(ScriptActionData scriptActionData, Location location, EliteScript eliteScript) {
        particlesBlueprint.getParticleBlueprints().forEach(particleBlueprint -> new ScriptParticle(particleBlueprint).visualize(scriptActionData, location, eliteScript));
    }

    private class ScriptParticle {

        private final ScriptParticlesBlueprint.ScriptParticleBlueprint particleBlueprint;

        public ScriptParticle(ScriptParticlesBlueprint.ScriptParticleBlueprint scriptParticlesBlueprint) {
            this.particleBlueprint = scriptParticlesBlueprint;
        }

        private void visualize(ScriptActionData scriptActionData, Location location, EliteScript eliteScript) {
            double x = particleBlueprint.getX().getValue();
            double y = particleBlueprint.getY().getValue();
            double z = particleBlueprint.getZ().getValue();
            int amount = particleBlueprint.getAmount().getValue();

            if (particleBlueprint.getRelativeVectorBlueprint() != null) {
                ScriptRelativeVector scriptRelativeVector = null;
                scriptRelativeVector = new ScriptRelativeVector(this.particleBlueprint.getRelativeVectorBlueprint(), eliteScript, location);

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
            else if (particleBlueprint.getParticle().equals(Particle.WITCH)
                //|| particleBlueprint.getParticle().equals(Particle.WITCH_AMBIENT) todo: 1.20.6 changed this name
            ) {
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        x,
                        y,
                        z,
                        amount,
                        particleBlueprint.getRed().getValue(),
                        particleBlueprint.getGreen().getValue(),
                        particleBlueprint.getBlue().getValue());
            } else
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        (double) particleBlueprint.getSpeed().getValue());

        }
    }
}
