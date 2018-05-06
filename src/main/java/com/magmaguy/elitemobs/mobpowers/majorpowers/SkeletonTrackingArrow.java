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
import com.magmaguy.elitemobs.mobpowers.minorpowers.AttackArrow;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SkeletonTrackingArrow extends MajorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.SKELETON_TRACKING_ARROW_MD;

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(plugin, true));
        MajorPowerPowerStance majorPowerStanceMath = new MajorPowerPowerStance();
        majorPowerStanceMath.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void eliteMobAggro(EntityTargetLivingEntityEvent event) {

        if (!(event.getEntity() instanceof LivingEntity && event.getTarget() instanceof LivingEntity)) return;

        if (!event.getEntity().hasMetadata(powerMetadata) && event.getEntity() instanceof Skeleton) return;

        if (!(event.getTarget() instanceof Player)) return;

        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        Player player = (Player) event.getTarget();

        if (livingEntity.hasMetadata(MetadataHandler.TRACKING_ARROWS_ACTIVE)) return;

        startFiringTrackingArrows(livingEntity, player);

    }

    private static void startFiringTrackingArrows(LivingEntity skeleton, Player player) {

        //fire arrows every 10 seconds
        new BukkitRunnable() {

            @Override
            public void run() {

                if (skeleton.isValid() && !skeleton.isDead() && player.isValid() && !player.isDead() &&
                        skeleton.getWorld().equals(player.getWorld()) && skeleton.getLocation().distance(player.getLocation()) < 16) {

                    Arrow eventArrow = AttackArrow.fireArrow(skeleton, player);
                    eventArrow.setVelocity(eventArrow.getVelocity().multiply(0.1));
                    trackingArrowLoop(player, eventArrow);
                    eventArrow.setGravity(false);

                } else {

                    skeleton.removeMetadata(MetadataHandler.TRACKING_ARROWS_ACTIVE, MetadataHandler.PLUGIN);
                    cancel();

                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 10 * 20, 10 * 20);

    }

    private static void trackingArrowLoop(Player player, Arrow arrow) {

        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {

                if (player.isValid() && !player.isDead() && arrow.isValid() && arrow.getWorld().equals(player.getWorld())) {

                    if (counter % 10 == 0) {

                        arrow.setVelocity(arrow.getVelocity().add(arrowAdjustmentVector(arrow, player)));

                    }

                    arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 10, 0.01, 0.01, 0.01, 0.01);

                } else {

                    cancel();

                }

                if (counter > 20 * 60) {
                    arrow.setGravity(true);
                    cancel();
                }

                counter++;

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private static Vector arrowAdjustmentVector(Arrow arrow, Player player) {

        return player.getEyeLocation().subtract(arrow.getLocation()).toVector()
                .normalize().multiply(0.1);

    }

}
