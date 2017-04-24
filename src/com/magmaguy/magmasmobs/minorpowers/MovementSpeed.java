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
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by MagmaGuy on 05/11/2016.
 */
public class MovementSpeed extends MinorPowers {

    private MagmasMobs plugin;

    public MovementSpeed(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata("MovementSpeed", new FixedMetadataValue(plugin, true));
        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999 * 20, 2));
        MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(plugin);
        minorPowerPowerStance.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        if (entity.hasMetadata("MovementSpeed")) {

            return true;

        }

        return false;

    }

}