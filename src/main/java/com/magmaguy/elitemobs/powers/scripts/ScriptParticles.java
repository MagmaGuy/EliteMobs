package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ScriptParticles {

    //private final List<ScriptParticle> scriptParticles = new ArrayList<>();
    ScriptParticlesBlueprint particlesBlueprint;

    public ScriptParticles(ScriptParticlesBlueprint particlesBlueprint) {
        this.particlesBlueprint = particlesBlueprint;
        // particlesBlueprint.getParticleBlueprints().forEach(iteratedParticlesBlueprint -> scriptParticles.add(new ScriptParticle(iteratedParticlesBlueprint)));
    }

    public void visualize(Location location) {
        particlesBlueprint.getParticleBlueprints().forEach(particleBlueprint -> new ScriptParticle(particleBlueprint).visualize(location));
        //scriptParticles.forEach(scriptParticle -> scriptParticle.visualize(location));
    }

    private class ScriptParticle {

        private final ScriptParticlesBlueprint.ScriptParticleBlueprint particleBlueprint;

        public ScriptParticle(ScriptParticlesBlueprint.ScriptParticleBlueprint scriptParticlesBlueprint) {
            this.particleBlueprint = scriptParticlesBlueprint;
        }

        private void visualize(Location location) {
            if (particleBlueprint.getParticle().equals(Particle.REDSTONE))
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location, particleBlueprint.getAmount(),
                        particleBlueprint.getX(),
                        particleBlueprint.getY(),
                        particleBlueprint.getZ(),
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
                        particleBlueprint.getAmount(),
                        particleBlueprint.getX(),
                        particleBlueprint.getY(),
                        particleBlueprint.getZ(),
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
            else if (particleBlueprint.getParticle().equals(Particle.SPELL_MOB) ||
                    particleBlueprint.getParticle().equals(Particle.SPELL_MOB_AMBIENT)) {
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        particleBlueprint.getAmount(),
                        particleBlueprint.getRed(),
                        particleBlueprint.getGreen(),
                        particleBlueprint.getBlue());
            } else
                location.getWorld().spawnParticle(
                        particleBlueprint.getParticle(),
                        location,
                        particleBlueprint.getAmount(),
                        particleBlueprint.getX(),
                        particleBlueprint.getY(),
                        particleBlueprint.getZ(),
                        particleBlueprint.getSpeed());

        }
    }
}
