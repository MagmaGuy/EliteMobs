package com.magmaguy.elitemobs.experimentalcombat.challenges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.arena.ArenaContainer;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.*;

/** Physical trial missiles, shared cast budgets, and impact receipts consumed by authored powers. */
final class TrialProjectiles implements Listener, AutoCloseable {
    record Impact(Location location, boolean hitPlayer, double damage) {
        Impact { location = location.clone(); }
        @Override public Location location() { return location.clone(); }
    }
    private static final class Missile {
        final Projectile entity;
        final double damage, cap;
        final String group;
        final long expires;
        final Set<UUID> penetrable = new HashSet<>();
        List<Location> curve = List.of();
        long curveStart;
        Missile(Projectile entity, double damage, String group, double cap, long expires) {
            this.entity=entity; this.damage=damage; this.group=group; this.cap=cap; this.expires=expires;
        }
    }
    private static final class Cast {
        double spent;
        Impact impact;
        long expires;
    }
    private final CustomBossEntity boss;
    private final Player player;
    private final ArenaContainer bounds;
    private final BooleanSupplier canDamage;
    private final Function<UUID, CustomBossEntity> actor;
    private final Map<UUID, Missile> missiles = new HashMap<>();
    private final Map<UUID, Long> retired = new HashMap<>();
    private final Map<String, Cast> casts = new HashMap<>();
    private long tick;
    private boolean closed;

    TrialProjectiles(CustomBossEntity boss, Player player, ArenaContainer bounds,
                     BooleanSupplier canDamage, Function<UUID, CustomBossEntity> actor) {
        this.boss=boss; this.player=player; this.bounds=bounds; this.canDamage=canDamage; this.actor=actor;
        Bukkit.getPluginManager().registerEvents(this, MetadataHandler.PLUGIN);
    }

    void own(Projectile projectile, double damage, String group, double cap, int lifetime) {
        if (closed || missiles.size() >= 48) { projectile.remove(); return; }
        if (!Double.isFinite(damage) || damage<0 || damage>2 || !Double.isFinite(cap) || cap<0 || cap>3
                || lifetime<1 || lifetime>100 || group.length()>80) throw new IllegalArgumentException("Invalid trial missile budget");
        if (!casts.containsKey(group) && casts.size()>=128) { projectile.remove(); return; }
        if (projectile instanceof AbstractArrow arrow) arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        projectile.setPersistent(false);
        if (projectile instanceof Snowball snowball) snowball.setItem(new org.bukkit.inventory.ItemStack(Material.AMETHYST_SHARD));
        missiles.put(projectile.getUniqueId(), new Missile(projectile, damage, group, cap, tick+lifetime));
        casts.computeIfAbsent(group, ignored -> new Cast()).expires=tick+200;
    }

    Projectile arrow(Location from, Location target, double speed, double damage, String group,
                     double cap, int lifetime, List<CustomBossEntity> penetrate, boolean gravity) {
        if (closed || missiles.size()>=48 || !bounds.contains(from) || !Double.isFinite(speed) || speed<=0 || speed>1.8) return null;
        Vector direction=target.toVector().subtract(from.toVector());
        if (direction.lengthSquared()<.001) return null;
        Arrow arrow=from.getWorld().spawnArrow(from, direction.normalize(), (float)speed, 0);
        arrow.setShooter(boss.getLivingEntity()); arrow.setGravity(gravity);
        own(arrow,damage,group,cap,lifetime);
        Missile missile=missiles.get(arrow.getUniqueId());
        if (missile==null) return null;
        for (CustomBossEntity prop : penetrate) if (prop!=null && prop.exists()) missile.penetrable.add(prop.getLivingEntity().getUniqueId());
        if (missile.penetrable.size()>3) { discard(missile); throw new IllegalArgumentException("At most three penetration targets"); }
        if (!missile.penetrable.isEmpty()) arrow.setPierceLevel(missile.penetrable.size());
        return arrow;
    }

    void curve(List<Location> points, double damage, String group, double cap) {
        if (points.size()<3 || points.size()>40) throw new IllegalArgumentException("Curve must have 3–40 fixed points");
        for (int i=0; i<points.size(); i++) {
            if (!bounds.contains(points.get(i))) return;
            if (i>0 && points.get(i).distanceSquared(points.get(i-1))>1.2*1.2) throw new IllegalArgumentException("Curve segment exceeds physical travel bound");
        }
        Projectile projectile=arrow(points.getFirst(),points.get(1),points.getFirst().distance(points.get(1)),damage,group,cap,points.size()+1,List.of(),false);
        if (projectile==null) return;
        Missile missile=missiles.get(projectile.getUniqueId());
        missile.curve=points.stream().map(Location::clone).toList(); missile.curveStart=tick;
    }

    double spent(String group) { Cast cast=casts.get(group); return cast==null ? 0 : cast.spent; }
    long count(String group) { return missiles.values().stream().filter(missile -> missile.group.equals(group)).count(); }
    Impact consume(String group) { Cast cast=casts.get(group); if (cast==null) return null; Impact result=cast.impact; cast.impact=null; return result; }
    void forget(String group) { if (count(group)==0) casts.remove(group); }
    void clear(String group) {
        for (Missile missile : List.copyOf(missiles.values())) if (missile.group.equals(group)) discard(missile);
    }

    void tick(long now) {
        tick=now;
        retired.values().removeIf(expiry -> now>=expiry);
        casts.entrySet().removeIf(entry -> now>=entry.getValue().expires);
        for (Missile missile : List.copyOf(missiles.values())) {
            Projectile projectile=missile.entity;
            if (!projectile.isValid() || now>=missile.expires || !bounds.contains(projectile.getLocation())) {
                receipt(missile,projectile.getLocation(),false,0); discard(missile); continue;
            }
            if (!missile.curve.isEmpty()) {
                int next=(int)(now-missile.curveStart)+1;
                if (next>=missile.curve.size()) { receipt(missile,projectile.getLocation(),false,0); discard(missile); continue; }
                Vector velocity=missile.curve.get(next).toVector().subtract(projectile.getLocation().toVector());
                if (velocity.lengthSquared()>1.8*1.8) { discard(missile); continue; }
                // Native arrow movement performs terrain/entity collision on each segment; no teleport or retargeting.
                projectile.setVelocity(velocity);
            }
            if (now%2==0 && projectile instanceof Snowball)
                player.spawnParticle(Particle.DUST,projectile.getLocation(),2,.03,.03,.03,0,new Particle.DustOptions(Color.fromRGB(160,120,255),.8F));
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void nativeDamage(EntityDamageByEntityEvent event) {
        UUID id=event.getDamager().getUniqueId();
        if (missiles.containsKey(id) || retired.containsKey(id)) event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void hit(ProjectileHitEvent event) {
        Missile missile=missiles.get(event.getEntity().getUniqueId()); if (missile==null) return;
        event.setCancelled(true);
        if (event.getHitEntity()!=null && missile.penetrable.remove(event.getHitEntity().getUniqueId())) {
            CustomBossEntity prop=actor.apply(event.getHitEntity().getUniqueId());
            if (prop!=null && prop.exists()) prop.remove(RemovalReason.EFFECT_TIMEOUT);
            player.spawnParticle(Particle.CLOUD,event.getHitEntity().getLocation().add(0,1,0),5,.2,.3,.2,0);
            return;
        }
        Location point=impactLocation(event);
        boolean playerHit=event.getHitEntity()==player;
        double damage=0;
        if (playerHit && !closed && boss.exists() && canDamage.getAsBoolean()) {
            Cast cast=casts.computeIfAbsent(missile.group,ignored -> new Cast());
            double proposed=Math.min(missile.damage,Math.max(0,missile.cap-cast.spent));
            double before=player.getHealth()+player.getAbsorptionAmount();
            if (proposed>0) CombatDamageContext.runEliteToPlayerMultiplier(proposed,() -> player.damage(1,boss.getLivingEntity()));
            if (player.getHealth()+player.getAbsorptionAmount()<before) { damage=proposed; cast.spent+=damage; }
        }
        receipt(missile,point,playerHit,damage); discard(missile);
    }

    private void receipt(Missile missile,Location point,boolean playerHit,double damage) {
        Cast cast=casts.get(missile.group); if (cast==null) return;
        // A successful contact remains observable even if another arrow in the fan misses later.
        if (cast.impact==null || playerHit) cast.impact=new Impact(point,playerHit,damage);
    }
    private Location impactLocation(ProjectileHitEvent event) {
        Location origin=event.getEntity().getLocation();
        Vector velocity=event.getEntity().getVelocity();
        if (velocity.lengthSquared()<.0001) return origin;
        double reach=velocity.length()+2;
        Vector direction=velocity.clone().normalize();
        org.bukkit.util.RayTraceResult hit=null;
        if (event.getHitBlock()!=null) hit=event.getHitBlock().rayTrace(origin,direction,reach,FluidCollisionMode.NEVER);
        else if (event.getHitEntity()!=null) hit=event.getHitEntity().getBoundingBox().rayTrace(origin.toVector(),direction,reach);
        return hit==null ? origin : hit.getHitPosition().toLocation(origin.getWorld());
    }
    private void discard(Missile missile) {
        missiles.remove(missile.entity.getUniqueId()); retired.put(missile.entity.getUniqueId(),tick+2);
        EntityTracker.unregisterProjectileEntity(missile.entity); missile.entity.remove();
    }
    @Override public void close() {
        if (closed) return; closed=true; HandlerList.unregisterAll(this);
        for (Missile missile : List.copyOf(missiles.values())) discard(missile);
        missiles.clear(); casts.clear(); retired.clear();
    }
}
