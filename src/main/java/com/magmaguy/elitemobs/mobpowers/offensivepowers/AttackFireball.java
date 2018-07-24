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
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPowers;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackFireball extends MinorPowers {

    String powerMetadata = MetadataHandler.ATTACK_FIREBALL_MD;

    @Override
    public void applyPowers(Entity entity) {

        MetadataHandler.registerMetadata(entity, powerMetadata, true);
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance();
        minorPowerPowerStance.itemEffect(entity);
        repeatingFireballTask(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    private void repeatingFireballTask(Entity entity) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!entity.isValid() || entity.isDead()) {

                    cancel();
                    return;

                }

                for (Entity nearbyEntity : entity.getNearbyEntities(20, 20, 20))
                    if (nearbyEntity instanceof Player)
                        if (((Player) nearbyEntity).getGameMode().equals(GameMode.ADVENTURE) ||
                                ((Player) nearbyEntity).getGameMode().equals(GameMode.SURVIVAL))
                            shootFireball(entity, (Player) nearbyEntity);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20 * 8);

    }

    private static void shootFireball(Entity entity, Player player) {

        Fireball repeatingFireball = (Fireball) entity.getWorld().spawnEntity(entity.getLocation().add(0, 3, 0), EntityType.FIREBALL);

        Vector targetterToTargetted = player.getLocation().toVector().subtract(repeatingFireball.getLocation().toVector()).normalize();

        repeatingFireball.setVelocity(targetterToTargetted);
        repeatingFireball.setYield(1F);
        repeatingFireball.setIsIncendiary(true);
        repeatingFireball.setShooter((ProjectileSource) entity);

    }

}
