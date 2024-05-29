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
            double x = particleBlueprint.getX();
            double y = particleBlueprint.getY();
            double z = particleBlueprint.getZ();
            int amount = particleBlueprint.getAmount();

            if (particleBlueprint.getRelativeVectorBlueprint() != null) {
                ScriptRelativeVector scriptRelativeVector = null;
                scriptRelativeVector = new ScriptRelativeVector(this.particleBlueprint.getRelativeVectorBlueprint(), eliteScript, location);

                    Vector movementVector = scriptRelativeVector.getVector(scriptActionData);
                    amount = 0;
                    x = movementVector.getX();
                    y = movementVector.getY();
                    z = movementVector.getZ();
            }


            if (particleBlueprint.getParticle().equals(Particle.REDSTONE))
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        particleBlueprint.getSpeed(),
                        new Particle.DustOptions(
                                Color.fromRGB(
                                        particleBlueprint.getRed(),
                                        particleBlueprint.getGreen(),
                                        particleBlueprint.getBlue()),
                                1));
            else if (particleBlueprint.getParticle().equals(Particle.DUST_COLOR_TRANSITION))
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        particleBlueprint.getSpeed(),
                        new Particle.DustTransition(
                                Color.fromRGB(
                                        particleBlueprint.getRed(),
                                        particleBlueprint.getGreen(),
                                        particleBlueprint.getBlue()),
                                Color.fromRGB(
                                        particleBlueprint.getToRed(),
                                        particleBlueprint.getToGreen(),
                                        particleBlueprint.getToBlue()),
                                1));
            else if (particleBlueprint.getParticle().equals(Particle.SPELL_MOB)
                    //|| particleBlueprint.getParticle().equals(Particle.SPELL_MOB_AMBIENT) todo: 1.20.6 changed this name
            ) {
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        x,
                        y,
                        z,
                        amount,
                        particleBlueprint.getRed(),
                        particleBlueprint.getGreen(),
                        particleBlueprint.getBlue());
            } else
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        amount,
                        x,
                        y,
                        z,
                        particleBlueprint.getSpeed());

        }
    }
}
