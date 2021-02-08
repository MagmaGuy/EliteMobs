package com.magmaguy.elitemobs.powers.majorpowers.skeleton;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.powers.MajorPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class SkeletonPillar extends MajorPower implements Listener {

    public SkeletonPillar() {
        super(PowersConfig.getPower("skeleton_pillar.yml"));
    }

    @EventHandler
    public void onHit(EliteMobDamagedByPlayerEvent event) {
        SkeletonPillar skeletonPillar = (SkeletonPillar) event.getEliteMobEntity().getPower(this);
        if (skeletonPillar == null) return;
        if (skeletonPillar.isCooldown()) return;

        /*
        Run random check to see if the power should activate
         */
        if (ThreadLocalRandom.current().nextDouble() > 0.20) return;
        skeletonPillar.doCooldown(20 * 27);

        event.getEliteMobEntity().getLivingEntity().setAI(false);
        playPillarSong(event.getEntity().getLocation());

        Location location1 = event.getEliteMobEntity().getLivingEntity().getLocation().clone()
                .add(locationMover(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), 20, 7));
        Location location2 = event.getEliteMobEntity().getLivingEntity().getLocation().clone()
                .add(locationMover(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), 20, -7));

        new BukkitRunnable() {

            int timer = 1;

            @Override
            public void run() {

                if (timer > 20 * 7 || !event.getEliteMobEntity().getLivingEntity().isValid()) {

                    event.getEliteMobEntity().getLivingEntity().setAI(true);
                    cancel();

                } else if (timer > 20 * 1 && timer < 20 * 7) {

                    pillarEffect(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), timer, 7);
                    pillarEffect(event.getEliteMobEntity().getLivingEntity().getLocation().clone(), timer, -7);

                } else {

                    pillarWarningEffect(location1);
                    pillarWarningEffect(location2);

                }

                timer++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void pillarEffect(Location location, int timer, int offset) {
        location.add(locationMover(location, timer, offset));
        location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 15, 0.1, 5, 0.1, 0.05);
        pillarDamage(location);
    }

    private void pillarWarningEffect(Location location) {
        location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 5, 0.1, 5, 0.1, 0.05);
    }

    private void pillarDamage(Location location) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 2, 5, 2))
            if (entity instanceof LivingEntity && !(entity instanceof Pig || entity instanceof Cow || entity instanceof Chicken ||
                    entity instanceof Wolf || entity instanceof Llama || entity instanceof Ocelot || entity instanceof Horse ||
                    entity instanceof Sheep || entity instanceof Rabbit || entity instanceof Parrot || entity instanceof Villager)) {

                LivingEntity livingEntity = (LivingEntity) entity;

                if (livingEntity.isValid())
                    livingEntity.damage(1);
            }
    }

    private Location locationMover(Location location, int timer, int offset) {
        int numberOfPointsPerRotation = 20 * 3;
        Location newLocation = GenericRotationMatrixMath.applyRotation(0, 1, 0, numberOfPointsPerRotation,
                0, 0, offset, timer).toLocation(location.getWorld());
        return newLocation;
    }

    private Location soundLocation;

    private void playPillarSong(Location location) {
        soundLocation = location;
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                counter++;
                switch (counter) {
                    case 1:
                        playSound(d());
                        break;
                    case 2:
                        playSound(d());
                        break;
                    case 4:
                        playSound(eHigher());
                        break;
                    case 7:
                        playSound(aHigher());
                        break;
                    case 10:
                        playSound(aSharpHigher());
                        break;
                    case 12:
                        playSound(gHigher());
                        break;
                    case 14:
                        playSound(f());
                        break;
                    case 16:
                        playSound(f());
                        break;
                    case 17:
                        playSound(d());
                        break;
                    case 18:
                        playSound(f());
                        break;
                    case 19:
                        playSound(gHigher());
                        break;
                    case 21:
                        playSound(c());
                        break;
                    case 22:
                        playSound(c());
                        break;
                    case 24:
                        playSound(eHigher());
                        break;
                    case 27:
                        playSound(aHigher());
                        break;
                    case 30:
                        playSound(aSharpHigher());
                        break;
                    case 32:
                        playSound(gHigher());
                        break;
                    case 34:
                        playSound(f());
                        break;
                    case 36:
                        playSound(f());
                        break;
                    case 37:
                        playSound(d());
                        break;
                    case 38:
                        playSound(f());
                        break;
                    case 39:
                        playSound(gHigher());
                        break;
                    case 41:
                        playSound(b());
                        break;
                    case 42:
                        playSound(b());
                        break;
                    case 44:
                        playSound(eHigher());
                        break;
                    case 47:
                        playSound(aHigher());
                        break;
                    case 50:
                        playSound(aSharpHigher());
                        break;
                    case 52:
                        playSound(gHigher());
                        break;
                    case 54:
                        playSound(f());
                        break;
                    case 56:
                        playSound(f());
                        break;
                    case 57:
                        playSound(d());
                        break;
                    case 58:
                        playSound(f());
                        break;
                    case 59:
                        playSound(gHigher());
                        break;
                    case 61:
                        playSound(aSharp());
                        break;
                    case 62:
                        playSound(aSharp());
                        break;
                    case 64:
                        playSound(eHigher());
                        break;
                    case 67:
                        playSound(aHigher());
                        break;
                    case 70:
                        playSound(aSharpHigher());
                        break;
                    case 72:
                        playSound(gHigher());
                        break;
                    case 74:
                        playSound(f());
                        break;
                    case 76:
                        playSound(f());
                        break;
                    case 77:
                        playSound(d());
                        break;
                    case 78:
                        playSound(f());
                        break;
                    case 79:
                        playSound(gHigher());
                        break;
                    case 80:
                        cancel();
                        break;
                    default:
                        break;
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 2);
    }

    private void playSound(float pitch) {
        soundLocation.getWorld().playSound(soundLocation, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 2f, pitch);
    }

    private float d() {
        return 0.793701f;
    }

    private float eHigher() {
        return 1.781797f;
    }

    private float aHigher() {
        return 1.189207f;
    }

    private float aSharpHigher() {
        return 1.259921f;
    }

    private float gHigher() {
        return 1.122462f;
    }

    private float f() {
        return 0.943874f;
    }

    private float c() {
        return 0.707107f;
    }

    private float b() {
        return 0.667420f;
    }

    private float aSharp() {
        return 0.629961f;
    }

}
