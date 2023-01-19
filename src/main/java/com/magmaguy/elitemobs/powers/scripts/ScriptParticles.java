package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.powers.scripts.caching.ScriptParticlesBlueprint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ScriptParticles {

    ScriptParticlesBlueprint particlesBlueprint;

    public ScriptParticles(ScriptParticlesBlueprint particlesBlueprint) {
        this.particlesBlueprint = particlesBlueprint;
    }

    public void visualize(ScriptActionData scriptActionData, Location location) {
        particlesBlueprint.getParticleBlueprints().forEach(particleBlueprint -> new ScriptParticle(particleBlueprint).visualize(scriptActionData, location));
    }

    /*
    todo: this needs a completely different implementation with a standalone target (or something like that)
    private org.bukkit.util.Vector getMovementVector(ScriptActionData scriptActionData, Location location) {
        Location sourceLocation = null;
        for (Shape shape : scriptActionData.getCachedShapes())
            if (scriptActionData.getTargetType().equals(TargetType.ZONE_FULL) && shape.getLocations().contains(location) ||
                    scriptActionData.getTargetType().equals(TargetType.ZONE_BORDER) && shape.getEdgeLocations().contains(location)) {
                sourceLocation = shape.getCenter().clone();
                break;
            }
        if (sourceLocation == null) {
            return new org.bukkit.util.Vector(0, 0, 0);
        }
        return sourceLocation.clone().subtract(location).toVector().normalize();
    }

     */

    private class ScriptParticle {

        private final ScriptParticlesBlueprint.ScriptParticleBlueprint particleBlueprint;

        public ScriptParticle(ScriptParticlesBlueprint.ScriptParticleBlueprint scriptParticlesBlueprint) {
            this.particleBlueprint = scriptParticlesBlueprint;
        }

        private void visualize(ScriptActionData scriptActionData, Location location) {
            double x = particleBlueprint.getX();
            double y = particleBlueprint.getY();
            double z = particleBlueprint.getZ();
            int amount = particleBlueprint.getAmount();
            /*
            if (particleBlueprint.getMoveToTarget() != null) {
                amount = 0;
                Vector movementVector = getMovementVector(scriptActionData, location);
                if (!particleBlueprint.getMoveToTarget()) movementVector.multiply(-1);
                x = movementVector.getX();
                y = movementVector.getY();
                z = movementVector.getZ();
            }

             */
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
            else if (particleBlueprint.getParticle().equals(Particle.SPELL_MOB) ||
                    particleBlueprint.getParticle().equals(Particle.SPELL_MOB_AMBIENT)) {
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
