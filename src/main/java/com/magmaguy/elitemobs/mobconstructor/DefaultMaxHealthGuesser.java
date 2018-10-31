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

package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

/**
 * Created by MagmaGuy on 02/11/2016.
 */
public class DefaultMaxHealthGuesser {

    /*
    Todo: find a better solution for this
    It's really not a great solution but it will do for now
     */

    public static double defaultMaxHealthGuesser(LivingEntity livingEntity) {

        LivingEntity livingEntity1 = (LivingEntity) livingEntity.getWorld().spawnEntity(livingEntity.getLocation(), livingEntity.getType());
        double maxHealth = livingEntity1.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        livingEntity1.remove();

        return maxHealth;

    }

}
