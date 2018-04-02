/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.events.mobs.sharedeventpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SpiritWalk implements Listener {

    HashMap<LivingEntity, Integer> entityHitCount = new HashMap<>();

    @EventHandler
    public void onBossMobGotHit(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof LivingEntity &&
                (event.getEntity().hasMetadata(MetadataHandler.ZOMBIE_KING) || event.getEntity().hasMetadata(MetadataHandler.TREASURE_GOBLIN))))
            return;

        if (entityHitCount.containsKey(event.getEntity()))
            entityHitCount.put((LivingEntity) event.getEntity(), entityHitCount.get(event.getEntity()) + 1);
        else entityHitCount.put((LivingEntity) event.getEntity(), 1);

        if (entityHitCount.get(event.getEntity()) > 15) {

            initializeSpiritWalk((LivingEntity) event.getEntity());
            entityHitCount.put((LivingEntity) event.getEntity(), 0);
        }

    }

    @EventHandler
    public void onBossMobHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager().hasMetadata(MetadataHandler.ZOMBIE_KING) || event.getEntity().hasMetadata(MetadataHandler.TREASURE_GOBLIN)))
            return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        entityHitCount.put((LivingEntity) event.getDamager(), 0);

    }

    private void initializeSpiritWalk(LivingEntity bossMob) {

        new BukkitRunnable() {

            int counter = 1;

            @Override
            public void run() {

                if (counter > 3) cancel();

                Location bossLocation = bossMob.getLocation().clone();

                for (int i = 0; i < 20; i++) {

                    double randomizedX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5;
                    double randomizedY = ThreadLocalRandom.current().nextDouble() - 0.5;
                    double randomizedZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5;

                    Vector normalizedVector = new Vector(randomizedX, randomizedY, randomizedZ).normalize().multiply(7).multiply(counter);

                    Location newSimulatedLocation = bossLocation.add(normalizedVector).clone();

                    Location newValidLocation = checkLocationValidity(newSimulatedLocation);

                    if (newValidLocation != null) {

                        spiritWalkAnimation(bossMob, bossMob.getLocation(), newValidLocation.add(new Vector(0.5, 1, 0.5)));
//                        bossMob.teleport(newValidLocation.add(new Vector(0, 1, 0)));
                        cancel();
                        break;
                    }

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void spiritWalkAnimation(LivingEntity bossMob, Location entityLocation, Location finalLocation) {

        bossMob.setAI(false);
        bossMob.setInvulnerable(true);
        bossMob.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 10, 1));
        Vector toDestination = finalLocation.clone().subtract(entityLocation.clone()).toVector().normalize().divide(new Vector(2, 2, 2));

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (bossMob.getLocation().clone().distance(finalLocation) < 2 || counter > 20 * 10) {

                    bossMob.teleport(finalLocation);
                    bossMob.setAI(true);
                    bossMob.setInvulnerable(false);
                    bossMob.removePotionEffect(PotionEffectType.GLOWING);
                    cancel();

                }

                bossMob.teleport(bossMob.getLocation().clone().add(toDestination.clone()));

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private Location checkLocationValidity(Location simulatedLocation) {

        if (simulatedLocation.getBlock().getType().equals(Material.AIR)) {

            int counter = 1;

            while (true) {

                if (simulatedLocation.getY() < 1) return null;

                Location blockUnderCurrentBlock = simulatedLocation.clone().subtract(new Vector(0, counter, 0));

                if (blockUnderCurrentBlock.getBlock().getType() != Material.AIR) return blockUnderCurrentBlock;

                if (counter > 10) return null;

                counter++;

            }

        }

        return null;

    }

}
