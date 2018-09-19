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

package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobpowers.PowerCooldown;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieBloat extends MajorPowers implements Listener {

    private static Random random = new Random();
    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.ZOMBIE_BLOAT_MD;
    Configuration configuration = ConfigValues.translationConfig;

    @Override
    public void applyPowers(Entity entity) {

        MetadataHandler.registerMetadata(entity, powerMetadata, true);
        MajorPowerPowerStance majorPowerStanceMath = new MajorPowerPowerStance();
        majorPowerStanceMath.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity().hasMetadata(powerMetadata) && event.getEntity() instanceof Zombie)) return;
        if (event.getEntity().hasMetadata(MetadataHandler.ZOMBIE_BLOAT_COOLDOWN)) return;

        /*
        Run random check to see if the power should activate
         */
        if (random.nextDouble() > 0.20) return;

        /*
        Create early warning that entity is about to bloat
         */
        new BukkitRunnable() {

            int timer = 1;
            Zombie eventZombie = (Zombie) event.getEntity();

            @Override
            public void run() {

                if (timer > 40) {

                    bloatEffect(eventZombie);
                    cancel();

                }

                if (timer == 21) {

                    /*
                    Remove AI to prevent entity from moving while effect activates
                    */
                    eventZombie.setAI(false);

                    /*
                    Start power cooldown, 10 seconds
                    */
                    PowerCooldown.startCooldownTimer(eventZombie, MetadataHandler.ZOMBIE_BLOAT_COOLDOWN, 10 * 20);

                }

                if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_WARNING_VISUAL_EFFECTS)) {
                        eventZombie.getWorld().spawnParticle(Particle.TOTEM, new Location(eventZombie.getWorld(),
                                        eventZombie.getLocation().getX(), eventZombie.getLocation().getY() + eventZombie.getHeight(), eventZombie.getLocation().getZ()),
                                20, timer / 24, timer / 9, timer / 24, 0.1);

                }

                timer++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);


    }

    private void bloatEffect(Zombie eventZombie) {
        /*
        Spawn giant for "bloat" effect
         */
        Giant giant = (Giant) eventZombie.getWorld().spawnEntity(eventZombie.getLocation(), EntityType.GIANT);
        giant.setAI(false);

        /*
        Apply knockback to all living entities around the giant except for the original zombie entity
         */
        List<Entity> nearbyEntities = giant.getNearbyEntities(4, 15, 4);
        List<LivingEntity> nearbyValidLivingEntities = new ArrayList<>();

        if (nearbyEntities.size() > 0) {

            for (Entity entity : nearbyEntities) {

                if (entity instanceof LivingEntity && !entity.equals(eventZombie)) {

                    nearbyValidLivingEntities.add((LivingEntity) entity);

                }

            }

        }

        Location entityLocation = eventZombie.getLocation();

        for (LivingEntity livingEntity : nearbyValidLivingEntities) {


            Location livingEntityLocation = livingEntity.getLocation();
            Vector toLivingEntityVector = livingEntityLocation.subtract(entityLocation).toVector();

            /*
            Normalize vector to apply powers uniformly
             */
            Vector normalizedVector = toLivingEntityVector.normalize();
            normalizedVector = normalizedVector.multiply(new Vector(2, 0, 2)).add(new Vector(0, 1, 0));

            try {

                livingEntity.setVelocity(normalizedVector);

            } catch (Exception e) {

                //Entity was too recent to set a velocity?

            }

        }

        livingEntityEffect(nearbyValidLivingEntities);

        /*
        Effect is done, start task to remove giant
         */
        new BukkitRunnable() {

            @Override
            public void run() {

                giant.remove();
                eventZombie.setAI(true);

            }

        }.runTaskLater(MetadataHandler.PLUGIN, 10);

    }

    private void livingEntityEffect(List<LivingEntity> livingEntities) {

        if (livingEntities.size() == 0) return;
        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_WARNING_VISUAL_EFFECTS))
            return;

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (counter > 1.5 * 20) {

                    cancel();

                }

                for (LivingEntity livingEntity : livingEntities) {

                    if (!(livingEntity == null || livingEntity.isDead() || !livingEntity.isValid())) {

                        livingEntity.getWorld().spawnParticle(Particle.CLOUD, new Location(livingEntity.getWorld(),
                                        livingEntity.getLocation().getX(),
                                        livingEntity.getLocation().getY() + livingEntity.getHeight() - 1,
                                        livingEntity.getLocation().getZ()),
                                0, 0, 0, 0);

                    }

                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

}
