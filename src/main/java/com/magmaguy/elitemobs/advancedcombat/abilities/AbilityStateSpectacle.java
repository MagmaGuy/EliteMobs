package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Visual and sound moments for stateful mechanics. Presentation only; never changes gameplay. */
final class AbilityStateSpectacle {
    /** Ticks the ancestral ghost takes to fade in before it casts. */
    static final int GHOST_FADE_IN_TICKS = 10;
    private static final int GHOST_FADE_OUT_TICKS = 8;
    private static final Particle.DustOptions GHOST_BODY =
            new Particle.DustOptions(Color.fromRGB(176, 232, 255), .85F);
    private static final Particle.DustOptions GHOST_GLOW =
            new Particle.DustOptions(Color.fromRGB(235, 250, 255), 1.1F);

    private AbilityStateSpectacle() {
    }

    /**
     * Raises a translucent ancestor beside the healed ally. It fades in, lifts its arms and sends
     * a beam of light into the ally exactly when {@code onCast} runs, then dissolves.
     */
    static void ancestralEcho(Plugin plugin, Player target, Runnable onCast) {
        Location anchor = target.getLocation();
        Vector facing = anchor.getDirection().setY(0);
        if (facing.lengthSquared() < 1.0E-6D) facing = new Vector(0, 0, 1);
        facing.normalize();
        Vector side = new Vector(-facing.getZ(), 0, facing.getX());
        Location ghost = anchor.clone().add(side.multiply(1.4D)).add(facing.clone().multiply(-.4D));
        Vector towardTarget = anchor.toVector().subtract(ghost.toVector()).setY(0);
        if (towardTarget.lengthSquared() < 1.0E-6D) towardTarget = facing.clone();
        Vector forward = towardTarget.normalize();
        if (ghost.getWorld() == null) {
            onCast.run();
            return;
        }
        ghost.getWorld().playSound(ghost, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, .8F, .6F);
        ghost.getWorld().playSound(ghost, Sound.BLOCK_BEACON_AMBIENT, .7F, 1.8F);
        new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!target.isOnline() || target.isDead() || ghost.getWorld() == null) {
                    if (tick <= GHOST_FADE_IN_TICKS) onCast.run();
                    cancel();
                    return;
                }
                if (tick < GHOST_FADE_IN_TICKS) {
                    silhouette(ghost, forward, false, (tick + 1D) / GHOST_FADE_IN_TICKS);
                } else if (tick == GHOST_FADE_IN_TICKS) {
                    silhouette(ghost, forward, true, 1D);
                    beam(ghost.clone().add(forward.clone().multiply(.35D)).add(0D, 1.75D, 0D),
                            target.getLocation().add(0D, target.getHeight() * .6D, 0D));
                    World world = ghost.getWorld();
                    world.playSound(ghost, Sound.ENTITY_EVOKER_CAST_SPELL, .8F, 1.5F);
                    world.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1F, 1.2F);
                    world.playSound(target.getLocation(), Sound.ENTITY_ALLAY_ITEM_GIVEN, .8F, 1.1F);
                    onCast.run();
                } else if (tick <= GHOST_FADE_IN_TICKS + GHOST_FADE_OUT_TICKS) {
                    double fade = 1D - (tick - GHOST_FADE_IN_TICKS) / (double) (GHOST_FADE_OUT_TICKS + 1);
                    silhouette(ghost.clone().add(0D, (1D - fade) * .5D, 0D), forward, true, fade);
                } else {
                    ghost.getWorld().spawnParticle(Particle.SOUL, ghost.clone().add(0D, 1.2D, 0D),
                            8, .25D, .5D, .25D, .02D);
                    cancel();
                    return;
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Layered sound and light when a fatal hit is refused. */
    static void deathRefused(Plugin plugin, Player target) {
        Location center = target.getLocation().add(0D, target.getHeight() * .55D, 0D);
        World world = center.getWorld();
        if (world == null) return;
        world.playSound(center, Sound.ENTITY_WARDEN_HEARTBEAT, 1.2F, .8F);
        world.playSound(center, Sound.BLOCK_ANVIL_LAND, .6F, .5F);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, center, 70, .5D, .9D, .5D, .45D);
        new BukkitRunnable() {
            private int step;

            @Override
            public void run() {
                if (!target.isOnline()) {
                    cancel();
                    return;
                }
                Location now = target.getLocation().add(0D, target.getHeight() * .55D, 0D);
                switch (step++) {
                    case 0 -> {
                        world.playSound(now, Sound.ITEM_TOTEM_USE, .7F, .85F);
                        world.playSound(now, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, .8F, .9F);
                    }
                    case 1 -> {
                        world.playSound(now, Sound.ENTITY_WARDEN_HEARTBEAT, 1.2F, 1.05F);
                        ring(now.clone().add(0D, -.5D, 0D), 1.6D, Particle.END_ROD, 28);
                    }
                    default -> {
                        world.playSound(now, Sound.BLOCK_BEACON_ACTIVATE, .8F, 1.4F);
                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, now, 30, .3D, .6D, .3D, .2D);
                        ring(now.clone().add(0D, -.5D, 0D), 2.4D, Particle.END_ROD, 36);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 4L, 4L);
    }

    private static void silhouette(Location feet, Vector forward, boolean armsRaised, double density) {
        World world = feet.getWorld();
        if (world == null) return;
        Vector side = new Vector(-forward.getZ(), 0, forward.getX());
        // Head ring
        for (int index = 0; index < 10; index++) {
            if (Math.random() > density) continue;
            double angle = Math.PI * 2D * index / 10D;
            Location point = feet.clone().add(0D, 1.72D, 0D)
                    .add(side.clone().multiply(Math.cos(angle) * .22D))
                    .add(0D, Math.sin(angle) * .22D, 0D);
            world.spawnParticle(Particle.DUST, point, 1, 0D, 0D, 0D, 0D, GHOST_GLOW);
        }
        // Torso and legs
        line(world, feet.clone().add(0D, 1.45D, 0D), feet.clone().add(0D, .8D, 0D), 6, density, GHOST_BODY);
        line(world, feet.clone().add(0D, .8D, 0D),
                feet.clone().add(side.clone().multiply(.18D)).add(0D, .05D, 0D), 5, density, GHOST_BODY);
        line(world, feet.clone().add(0D, .8D, 0D),
                feet.clone().add(side.clone().multiply(-.18D)).add(0D, .05D, 0D), 5, density, GHOST_BODY);
        // Arms: hanging while it gathers, raised toward the ally when it casts.
        Location shoulders = feet.clone().add(0D, 1.4D, 0D);
        for (int sign = -1; sign <= 1; sign += 2) {
            Location shoulder = shoulders.clone().add(side.clone().multiply(.28D * sign));
            Location hand = armsRaised
                    ? shoulder.clone().add(forward.clone().multiply(.45D)).add(0D, .45D, 0D)
                    .add(side.clone().multiply(-.1D * sign))
                    : shoulder.clone().add(side.clone().multiply(.08D * sign)).add(0D, -.6D, 0D);
            line(world, shoulder, hand, 4, density, GHOST_BODY);
        }
        if (Math.random() < density * .5D)
            world.spawnParticle(Particle.SOUL, feet.clone().add(0D, 1D, 0D), 1, .2D, .5D, .2D, .01D);
    }

    private static void line(World world, Location from, Location to, int points, double density,
                             Particle.DustOptions dust) {
        Vector step = to.toVector().subtract(from.toVector()).multiply(1D / Math.max(1, points));
        for (int index = 0; index <= points; index++) {
            if (Math.random() > density) continue;
            world.spawnParticle(Particle.DUST, from.clone().add(step.clone().multiply(index)),
                    1, 0D, 0D, 0D, 0D, dust);
        }
    }

    private static void beam(Location from, Location to) {
        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) return;
        Vector path = to.toVector().subtract(from.toVector());
        double length = path.length();
        if (length < 1.0E-6D) return;
        path.multiply(1D / length);
        for (double travelled = 0D; travelled <= length; travelled += .25D) {
            Location point = from.clone().add(path.clone().multiply(travelled));
            world.spawnParticle(Particle.END_ROD, point, 1, 0D, 0D, 0D, 0D);
            world.spawnParticle(Particle.DUST, point, 1, .03D, .03D, .03D, 0D, GHOST_GLOW);
        }
        world.spawnParticle(Particle.SOUL, to, 10, .3D, .4D, .3D, .03D);
    }

    private static void ring(Location center, double radius, Particle particle, int points) {
        World world = center.getWorld();
        if (world == null) return;
        for (int index = 0; index < points; index++) {
            double angle = Math.PI * 2D * index / points;
            world.spawnParticle(particle, center.getX() + Math.cos(angle) * radius, center.getY(),
                    center.getZ() + Math.sin(angle) * radius, 1, 0D, 0D, 0D, .01D);
        }
    }
}
