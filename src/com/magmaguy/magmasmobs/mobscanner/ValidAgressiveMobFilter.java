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

package com.magmaguy.magmasmobs.mobscanner;

import org.bukkit.entity.*;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class ValidAgressiveMobFilter {

    public static boolean ValidAgressiveMobFilter(Entity entity){

        if(entity instanceof Zombie ||
                entity instanceof Skeleton ||
                entity instanceof PigZombie ||
                entity instanceof Creeper ||
                entity instanceof Spider ||
                entity instanceof Enderman ||
                entity instanceof CaveSpider ||
                entity instanceof Silverfish ||
                entity instanceof Blaze ||
                entity instanceof Witch ||
                entity instanceof Endermite ||
                entity instanceof PolarBear ||
                entity instanceof ZombieVillager)
        {

            return true;

        }

        return false;

    }

}
