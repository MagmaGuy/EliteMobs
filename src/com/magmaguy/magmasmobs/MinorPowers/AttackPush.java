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

package com.magmaguy.magmasmobs.MinorPowers;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.PowerStances.ParticleEffects;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class AttackPush extends MinorPowers implements Listener {

    private MagmasMobs plugin;

    public AttackPush(Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }

    @Override
    public void applyPowers(Entity entity)
    {

        entity.setMetadata("AttackPush", new FixedMetadataValue(plugin, true));
        ParticleEffects particleEffects = new ParticleEffects(plugin);
        particleEffects.attackPushEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity)
    {

        if (entity.hasMetadata("AttackPush"))
        {

            return true;

        }

        return false;

    }

    @EventHandler
    public void attackPush (EntityDamageByEntityEvent event)
    {

        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        if (damager.hasMetadata("AttackPush"))
        {

            int pushbackStrength = 2;

            Vector pushbackDirection = damagee.getLocation().toVector().subtract(damager.getLocation().toVector());
            Vector pushbackApplied = pushbackDirection.normalize().multiply(pushbackStrength);

            damagee.setVelocity(pushbackApplied);

        }

    }

}
