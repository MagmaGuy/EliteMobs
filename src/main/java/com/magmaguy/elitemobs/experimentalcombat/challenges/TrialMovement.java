package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/** Read-only collision planning for trial actors; never changes the arena or its blocks. */
final class TrialMovement {
    private final ArenaContainer bounds;
    private final Player challenger;

    TrialMovement(ArenaContainer bounds, Player challenger) {
        this.bounds = bounds;
        this.challenger = challenger;
    }

    boolean standing(Entity actor, Location at) {
        if (!clear(actor, at)) return false;
        Material floor = at.clone().subtract(0, .1, 0).getBlock().getType();
        return floor.isSolid() && floor != Material.MAGMA_BLOCK && floor != Material.CACTUS
                && floor != Material.CAMPFIRE && floor != Material.SOUL_CAMPFIRE;
    }

    boolean clear(Entity actor, Location at) {
        if (at == null || !bounds.contains(at) || !at.getWorld().isChunkLoaded(at.getBlockX() >> 4, at.getBlockZ() >> 4))
            return false;
        BoundingBox box = actor.getBoundingBox().clone().shift(at.toVector().subtract(actor.getLocation().toVector()));
        box.expand(-.001);
        if (!bounds.contains(new Location(at.getWorld(), box.getMinX(), box.getMinY(), box.getMinZ()))
                || !bounds.contains(new Location(at.getWorld(), box.getMaxX(), box.getMaxY(), box.getMaxZ()))) return false;
        for (int x = (int) Math.floor(box.getMinX()); x <= Math.floor(box.getMaxX()); x++)
            for (int y = (int) Math.floor(box.getMinY()); y <= Math.floor(box.getMaxY()); y++)
                for (int z = (int) Math.floor(box.getMinZ()); z <= Math.floor(box.getMaxZ()); z++) {
                    if (!at.getWorld().isChunkLoaded(x >> 4, z >> 4)) return false;
                    var block = at.getWorld().getBlockAt(x, y, z);
                    if (block.isLiquid() || block.getType() == Material.FIRE || block.getType() == Material.SOUL_FIRE)
                        return false;
                    for (BoundingBox shape : block.getCollisionShape().getBoundingBoxes())
                        if (box.overlaps(shape.clone().shift(x, y, z))) return false;
                }
        return true;
    }

    boolean straight(Entity actor, Location destination, boolean grounded, boolean avoidPlayer) {
        Location origin = actor.getLocation();
        if (destination == null || origin.getWorld() != destination.getWorld()
                || origin.distanceSquared(destination) > 20 * 20 || !standing(actor, destination)) return false;
        Vector delta = destination.toVector().subtract(origin.toVector());
        int steps = Math.max(1, (int) Math.ceil(delta.length() / .125));
        for (int i = 1; i <= steps; i++) {
            Location point = origin.clone().add(delta.clone().multiply((double) i / steps));
            if (!(grounded ? standing(actor, point) : clear(actor, point))) return false;
            if (avoidPlayer && actor.getBoundingBox().clone().shift(point.toVector().subtract(origin.toVector()))
                    .overlaps(challenger.getBoundingBox().clone().expand(.2))) return false;
        }
        return true;
    }

    /** A real ballistic launch, preflighted along its arc. Bukkit resolves the resulting movement. */
    boolean leap(Entity actor, Location destination, int ticks) {
        if (ticks < 10 || ticks > 40 || destination == null || !standing(actor, destination)) return false;
        Location origin = actor.getLocation();
        if (origin.getWorld() != destination.getWorld() || origin.distanceSquared(destination) > 12 * 12) return false;
        double drag = .98;
        double sum = (1 - Math.pow(drag, ticks)) / (1 - drag);
        double horizontalSum = (1 - Math.pow(.91, ticks)) / (1 - .91);
        Vector delta = destination.toVector().subtract(origin.toVector());
        Vector velocity = new Vector(delta.getX() / horizontalSum,
                (delta.getY() + .08 * drag / (1 - drag) * (ticks - sum)) / sum, delta.getZ() / horizontalSum);
        Location previous = origin.clone();
        double vertical = velocity.getY();
        double horizontalDrag = 1;
        for (int i = 0; i < ticks; i++) {
            Location next = previous.clone().add(velocity.getX() * horizontalDrag, vertical,
                    velocity.getZ() * horizontalDrag);
            Vector step = next.toVector().subtract(previous.toVector());
            int samples = Math.max(1, (int) Math.ceil(step.length() / .125));
            for (int sample = 1; sample <= samples; sample++)
                if (!clear(actor, previous.clone().add(step.clone().multiply((double) sample / samples)))) return false;
            vertical = (vertical - .08) * drag;
            horizontalDrag *= .91;
            previous = next;
        }
        actor.setVelocity(velocity);
        return true;
    }
}
