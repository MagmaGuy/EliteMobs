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

package com.magmaguy.elitemobs.minorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * Created by MagmaGuy on 06/05/2017.
 */
public class AttackFireball extends MinorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.ATTACK_FIREBALL_MD;

    private int processID;

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void attackFireball (EntityTargetEvent event) {

        if (event.getEntity().hasMetadata(powerMetadata)) {

            Entity targetter = event.getEntity();
            Entity targetted = event.getTarget();

            if (targetted instanceof Player) {

                Entity fireball = targetter.getWorld().spawnEntity(targetter.getLocation().add(0, 3, 0), EntityType.FIREBALL);

                Vector targetterToTargetted = targetted.getLocation().toVector().subtract(fireball.getLocation().toVector());

                double distanceNerf = (targetted.getLocation().distance(targetter.getLocation())) /100;

                fireball.setVelocity(targetterToTargetted.multiply(0.5 - distanceNerf));

                processID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

                    @Override
                    public void run() {

                        if (!targetted.isValid() || !targetter.isValid() || targetted.getLocation().distance(targetter.getLocation()) > 20 ) {

                            Bukkit.getScheduler().cancelTask(processID);
                            return;

                        }

                        Entity repeatingFireball = targetter.getWorld().spawnEntity(targetter.getLocation().add(0, 3, 0), EntityType.FIREBALL);

                        Vector targetterToTargetted = targetted.getLocation().toVector().subtract(repeatingFireball.getLocation().toVector());

                        double distanceNerfRepeating = (targetted.getLocation().distance(targetter.getLocation())) / 100;

                        repeatingFireball.setVelocity(targetterToTargetted.multiply(0.5 - distanceNerfRepeating));

                    }

                },20*8, 20*8 );

            }

        }

    }


}
