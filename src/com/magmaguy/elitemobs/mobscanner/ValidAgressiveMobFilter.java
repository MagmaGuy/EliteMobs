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

package com.magmaguy.elitemobs.mobscanner;

import com.magmaguy.elitemobs.EliteMobs;
import org.bukkit.entity.*;

import java.util.List;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class ValidAgressiveMobFilter{

    private EliteMobs plugin;

        public static boolean ValidAgressiveMobFilter(Entity entity, List list) {
        //TODO: allow individual config deselection of allowed entities
            return entity instanceof Blaze && list.contains("Blaze") ||
                    entity instanceof CaveSpider && list.contains("CaveSpider") ||
                    entity instanceof Creeper && list.contains("Creeper") ||
                    entity instanceof Enderman && list.contains("Enderman") ||
                    entity instanceof Endermite && list.contains("Endermite") ||
                    entity instanceof IronGolem && list.contains("IronGolem") ||
                    entity instanceof PigZombie && list.contains("PigZombie") ||
                    entity instanceof PolarBear && list.contains("PolarBear") ||
                    entity instanceof Silverfish && list.contains("Silverfish") ||
                    entity instanceof Skeleton && list.contains("Skeleton") ||
                    entity instanceof Spider && list.contains("Spider") ||
                    entity instanceof Witch && list.contains("Witch") ||
                    entity instanceof ZombieVillager && list.contains("Zombie") ||
                    entity instanceof Zombie && list.contains("Zombie");

        }

}
