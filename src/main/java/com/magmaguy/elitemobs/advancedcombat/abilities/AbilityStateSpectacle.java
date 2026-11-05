package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Visual and sound moments for stateful mechanics. Presentation only; never changes gameplay. */
final class AbilityStateSpectacle {
    /** Ticks the ancestral spirit hovers beside its ally before it casts the echoed heal. */
    static final int SPIRIT_GATHER_TICKS = 26;
    private static final int SPIRIT_LINGER_TICKS = 14;
    private static final Particle.DustOptions GHOST_GLOW =
            new Particle.DustOptions(Color.fromRGB(235, 250, 255), 1.1F);
    private static final Set<Entity> SPIRITS = new HashSet<>();

    private AbilityStateSpectacle() {
    }

    /**
     * Calls an ancestral spirit, an Allay, to the healed ally's side. It follows the ally while it
     * gathers, sends a beam of light into them exactly when {@code onCast} runs, lingers a moment
     * and dissolves. The spirit has no AI, can't be hurt or handed items and never saves with the
     * chunk.
     */
    static void ancestralEcho(Plugin plugin, Player target, Runnable onCast) {
        World world = target.getWorld();
        Location start = spiritSpot(target, 0);
        Allay spirit = world.spawn(start, Allay.class, entity -> {
            EntityTracker.registerVisualEffects(entity);
            entity.setPersistent(false);
            entity.setAI(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setCollidable(false);
            entity.setCanPickupItems(false);
            entity.setGlowing(true);
        });
        SPIRITS.add(spirit);
        world.spawnParticle(Particle.SOUL, start.clone().add(0D, .3D, 0D), 14, .3D, .3D, .3D, .03D);
        world.playSound(start, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, .9F, .6F);
        world.playSound(start, Sound.BLOCK_BEACON_AMBIENT, .7F, 1.8F);
        new BukkitRunnable() {
            private int tick;
            private boolean cast;

            @Override
            public void run() {
                if (!target.isOnline() || target.isDead() || !spirit.isValid()
                        || !spirit.getWorld().equals(target.getWorld())) {
                    if (!cast) onCast.run();
                    dismiss(spirit);
                    cancel();
                    return;
                }
                Location spot = spiritSpot(target, tick);
                spirit.teleport(spot);
                if (tick < SPIRIT_GATHER_TICKS) {
                    if (tick % 3 == 0)
                        spirit.getWorld().spawnParticle(Particle.END_ROD, spot.clone().add(0D, .3D, 0D),
                                1, .15D, .15D, .15D, .01D);
                } else if (tick == SPIRIT_GATHER_TICKS) {
                    cast = true;
                    beam(spot.clone().add(0D, .35D, 0D),
                            target.getLocation().add(0D, target.getHeight() * .6D, 0D));
                    World current = spirit.getWorld();
                    current.playSound(spot, Sound.ENTITY_ALLAY_ITEM_THROWN, 1F, 1.3F);
                    current.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1F, 1.2F);
                    current.playSound(target.getLocation(), Sound.ENTITY_ALLAY_ITEM_GIVEN, .8F, 1.1F);
                    onCast.run();
                } else if (tick > SPIRIT_GATHER_TICKS + SPIRIT_LINGER_TICKS) {
                    spirit.getWorld().spawnParticle(Particle.SOUL, spot.clone().add(0D, .3D, 0D),
                            10, .25D, .3D, .25D, .02D);
                    spirit.getWorld().playSound(spot, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, .6F, 1.6F);
                    dismiss(spirit);
                    cancel();
                    return;
                }
                tick++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /** Beside and slightly ahead of the ally at shoulder height, facing them, with a gentle bob. */
    private static Location spiritSpot(Player target, int tick) {
        Location anchor = target.getLocation();
        Vector facing = anchor.getDirection().setY(0);
        if (facing.lengthSquared() < 1.0E-6D) facing = new Vector(0, 0, 1);
        facing.normalize();
        Vector side = new Vector(-facing.getZ(), 0, facing.getX());
        Location spot = anchor.clone().add(side.multiply(1.1D)).add(facing.clone().multiply(.3D))
                .add(0D, 1.1D + Math.sin(tick * .25D) * .08D, 0D);
        Vector toward = anchor.toVector().subtract(spot.toVector()).setY(0);
        spot.setDirection(toward.lengthSquared() < 1.0E-6D ? facing : toward);
        spot.setPitch(0F);
        return spot;
    }

    static boolean isSpirit(Entity entity) {
        return SPIRITS.contains(entity);
    }

    /** Removes every spirit still on screen; the runtime calls this when it closes. */
    static void dismissAll() {
        for (Entity spirit : List.copyOf(SPIRITS)) dismiss(spirit);
    }

    private static void dismiss(Entity spirit) {
        SPIRITS.remove(spirit);
        if (spirit.isValid()) spirit.remove();
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
