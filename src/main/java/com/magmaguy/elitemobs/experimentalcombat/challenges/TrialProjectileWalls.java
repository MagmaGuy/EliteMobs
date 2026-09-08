package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.freeminecraftmodels.api.magic.MagicProjectileTravelEvent;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.*;

/** Finite spectral panels: projectile collision only, with open edges and no block or walking collision. */
final class TrialProjectileWalls implements Listener, AutoCloseable {
    private record Panel(Location center, Vector normal, double halfWidth, double height, long expires) {
        Location intersection(Location from, Location to) {
            if (from.getWorld() != center.getWorld() || to.getWorld() != center.getWorld()) return null;
            Vector segment = to.toVector().subtract(from.toVector());
            double denominator = segment.dot(normal);
            if (Math.abs(denominator) < 1e-7) return null;
            double t = center.toVector().subtract(from.toVector()).dot(normal) / denominator;
            if (t < 0 || t > 1) return null;
            Location point = from.clone().add(segment.multiply(t));
            Vector relative = point.toVector().subtract(center.toVector());
            double side = relative.getX() * normal.getZ() - relative.getZ() * normal.getX();
            return Math.abs(side) <= halfWidth && relative.getY() >= 0 && relative.getY() <= height ? point : null;
        }
    }

    private final Player challenger;
    private final Map<String, Panel> panels = new LinkedHashMap<>();
    private final Map<UUID, Location> previous = new HashMap<>();
    private final Set<UUID> absorbed = new HashSet<>();
    private final Listener magicListener;

    TrialProjectileWalls(Player challenger) {
        this.challenger = challenger;
        if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
            if (MagicWeaponAPI.capabilityVersion() < 5)
                throw new IllegalStateException("Spectral trial walls require the current FreeMinecraftModels build (magic API 5)");
            magicListener = new MagicListener();
            Bukkit.getPluginManager().registerEvents(magicListener, MetadataHandler.PLUGIN);
        } else magicListener = null;
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
    }

    void add(String id, Location center, Location facing, double width, int duration, long now) {
        if (panels.size() >= 3 && !panels.containsKey(id)) throw new IllegalStateException("Too many trial shield panels");
        if (!Double.isFinite(width) || width < 1 || width > 8 || duration < 1 || duration > 240)
            throw new IllegalArgumentException("Invalid spectral panel size or duration");
        Vector normal = facing.toVector().subtract(center.toVector()).setY(0);
        if (normal.lengthSquared() < .001) return;
        panels.put(id, new Panel(center.clone(), normal.normalize(), width / 2, 3.2, now + duration));
    }

    void remove(String id) { panels.remove(id); }

    void tick(long now) {
        panels.values().removeIf(panel -> now >= panel.expires);
        if (panels.isEmpty()) { previous.clear(); absorbed.clear(); return; }
        Set<UUID> seen = new HashSet<>();
        for (Panel panel : panels.values()) {
            if (now % 4 == 0) draw(panel);
            for (var entity : panel.center.getWorld().getNearbyEntities(panel.center, panel.halfWidth + 5, 6, panel.halfWidth + 5)) {
                if (!(entity instanceof Projectile projectile) || projectile.getShooter() != challenger || !seen.add(entity.getUniqueId())) continue;
                Location current = projectile.getLocation();
                Location old = previous.put(projectile.getUniqueId(), current.clone());
                if ((old != null && blocks(old, current)) || blocks(current, current.clone().add(projectile.getVelocity()))) {
                    absorbed.add(projectile.getUniqueId());
                    projectile.remove();
                }
            }
        }
        previous.keySet().retainAll(seen);
        absorbed.retainAll(seen);
    }

    private boolean blocks(Location from, Location to) {
        for (Panel panel : panels.values()) {
            Location hit = panel.intersection(from, to);
            if (hit == null) continue;
            Vector direction = hit.toVector().subtract(from.toVector());
            double distance = direction.length();
            if (distance > .001) {
                // Terrain or an intervening body still wins if it is physically closer than the panel.
                var nearer = from.getWorld().rayTrace(from, direction.normalize(), distance, FluidCollisionMode.NEVER,
                        true, 0, entity -> entity instanceof LivingEntity && entity != challenger);
                if (nearer != null && nearer.getHitPosition().distance(from.toVector()) < distance - .02) continue;
            }
            challenger.spawnParticle(Particle.END_ROD, hit, 5, .1, .1, .1, .01);
            hit.getWorld().playSound(hit, Sound.ITEM_SHIELD_BLOCK, .5f, 1.3f);
            return true;
        }
        return false;
    }

    private void draw(Panel panel) {
        Vector side = new Vector(panel.normal.getZ(), 0, -panel.normal.getX());
        var dust = new Particle.DustOptions(Color.fromRGB(240, 220, 140), .8f);
        for (int i = 0; i <= 12; i++) {
            double across = -panel.halfWidth + 2 * panel.halfWidth * i / 12;
            for (double y : new double[]{0, panel.height})
                challenger.spawnParticle(Particle.DUST, panel.center.clone().add(side.clone().multiply(across)).add(0, y, 0), 1, 0, 0, 0, 0, dust);
        }
        for (int i = 1; i < 8; i++) for (double across : new double[]{-panel.halfWidth, 0, panel.halfWidth})
            challenger.spawnParticle(Particle.DUST, panel.center.clone().add(side.clone().multiply(across)).add(0, panel.height * i / 8, 0), 1, 0, 0, 0, 0, dust);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void impact(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() != challenger) return;
        Location current = projectile.getLocation();
        Location old = previous.getOrDefault(projectile.getUniqueId(), current.clone().subtract(projectile.getVelocity()));
        if (absorbed.contains(projectile.getUniqueId()) || blocks(old, current)) {
            event.setCancelled(true);
            absorbed.add(projectile.getUniqueId());
            projectile.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void damage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile) || projectile.getShooter() != challenger) return;
        Location old = previous.getOrDefault(projectile.getUniqueId(), projectile.getLocation().subtract(projectile.getVelocity()));
        if (absorbed.contains(projectile.getUniqueId()) || blocks(old, event.getEntity().getLocation().add(0, 1, 0))) {
            event.setCancelled(true);
            projectile.remove();
        }
    }

    private final class MagicListener implements Listener {
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void magicTravel(MagicProjectileTravelEvent event) {
            if (event.getShooter() == challenger && blocks(event.getFrom(), event.getTo())) event.setCancelled(true);
        }
    }

    @Override public void close() {
        HandlerList.unregisterAll(this);
        if (magicListener != null) HandlerList.unregisterAll(magicListener);
        panels.clear(); previous.clear(); absorbed.clear();
    }
}
