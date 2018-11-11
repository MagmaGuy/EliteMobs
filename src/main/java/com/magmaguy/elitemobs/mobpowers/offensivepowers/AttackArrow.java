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

package com.magmaguy.elitemobs.mobpowers.offensivepowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.ProjectileLocationGenerator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackArrow extends MinorPower implements Listener {

    private static HashSet<EliteMobEntity> currentlyFiringEntities = new HashSet<>();

    @Override

    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void targetEvent(EntityTargetLivingEntityEvent event) {
        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;
        if (currentlyFiringEntities.contains(eliteMobEntity)) return;
        repeatingArrowTask(eliteMobEntity);
        currentlyFiringEntities.add(eliteMobEntity);
    }

    private void repeatingArrowTask(EliteMobEntity eliteMobEntity) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!eliteMobEntity.getLivingEntity().isValid() || ((Monster) eliteMobEntity.getLivingEntity()).getTarget() == null) {
                    currentlyFiringEntities.remove(eliteMobEntity);
                    cancel();
                    return;
                }

                for (Entity nearbyEntity : eliteMobEntity.getLivingEntity().getNearbyEntities(20, 20, 20))
                    if (nearbyEntity instanceof Player)
                        if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL))
                            shootArrow(eliteMobEntity.getLivingEntity(), (Player) nearbyEntity);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 8);

    }

    public static Arrow shootArrow(Entity entity, Player player) {

        Location offsetLocation = ProjectileLocationGenerator.generateLocation((LivingEntity) entity, player);
        Arrow repeatingArrow = (Arrow) entity.getWorld().spawnEntity(offsetLocation, EntityType.ARROW);
        Vector targetterToTargetted = player.getEyeLocation().subtract(repeatingArrow.getLocation()).toVector()
                .normalize().multiply(2);

        repeatingArrow.setVelocity(targetterToTargetted);
        repeatingArrow.setShooter((ProjectileSource) entity);

        return repeatingArrow;

    }

}
