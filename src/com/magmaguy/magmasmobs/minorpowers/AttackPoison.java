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

package com.magmaguy.magmasmobs.minorpowers;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.powerstances.MinorPowerPowerStance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 12/12/2016.
 */
public class AttackPoison extends MinorPowers implements Listener {

    private MagmasMobs plugin;

    public AttackPoison(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata("AttackPoison", new FixedMetadataValue(plugin, true));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        if (entity.hasMetadata("AttackPoison")) {

            return true;

        }

        return false;

    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();

        if (damager.hasMetadata("AttackPoison") && damagee instanceof Player) {

            ((Player) damagee).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));

        }

    }

}
