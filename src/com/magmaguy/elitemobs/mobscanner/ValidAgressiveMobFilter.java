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
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.*;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class ValidAgressiveMobFilter{

    private EliteMobs plugin;

        public static boolean ValidAgressiveMobFilter(Entity entity) {

            Configuration config = ConfigValues.defaultConfig;

            return entity instanceof Blaze && config.getBoolean("Valid aggressive EliteMobs.Blaze") ||
                    entity instanceof CaveSpider && config.getBoolean("Valid aggressive EliteMobs.CaveSpider") ||
                    entity instanceof Creeper && config.getBoolean("Valid aggressive EliteMobs.Creeper") ||
                    entity instanceof Enderman && config.getBoolean("Valid aggressive EliteMobs.Enderman") ||
                    entity instanceof Endermite && config.getBoolean("Valid aggressive EliteMobs.Endermite") ||
                    entity instanceof IronGolem && config.getBoolean("Valid aggressive EliteMobs.IronGolem") ||
                    entity instanceof PigZombie && config.getBoolean("Valid aggressive EliteMobs.PigZombie") ||
                    entity instanceof PolarBear && config.getBoolean("Valid aggressive EliteMobs.PolarBear") ||
                    entity instanceof Silverfish && config.getBoolean("Valid aggressive EliteMobs.Silverfish") ||
                    entity instanceof Skeleton && config.getBoolean("Valid aggressive EliteMobs.Skeleton") ||
                    entity instanceof Spider && config.getBoolean("Valid aggressive EliteMobs.Spider") ||
                    entity instanceof Witch && config.getBoolean("Valid aggressive EliteMobs.Witch") ||
                    entity instanceof ZombieVillager && config.getBoolean("Valid aggressive EliteMobs.Zombie") || //TODO: seperate zombievillager from zombie
                    entity instanceof Zombie && config.getBoolean("Valid aggressive EliteMobs.Zombie");

        }

}
