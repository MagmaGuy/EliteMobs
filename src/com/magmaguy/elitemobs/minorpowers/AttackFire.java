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

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * Created by MagmaGuy on 28/04/2017.
 */
public class AttackFire extends MinorPowers implements Listener {

    private EliteMobs plugin;

    public AttackFire(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @Override
    public void applyPowers(Entity entity) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);
        entity.setMetadata(metadataHandler.attackFireMD, new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {
        MetadataHandler metadataHandler = new MetadataHandler(plugin);
        return entity.hasMetadata(metadataHandler.attackFireMD);
    }

    @EventHandler
    public void attackFire(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (damager.hasMetadata(metadataHandler.attackFireMD)) {

            damagee.setFireTicks(40);

        }

        if (damager instanceof Projectile) {

            if (ProjectileMetadataDetector.projectileMetadataDetector((Projectile) damager, metadataHandler.attackFireMD)) {

                damagee.setFireTicks(40);

            }

        }

    }

}
