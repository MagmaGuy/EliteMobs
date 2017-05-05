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

import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.*;

/**
 * Created by MagmaGuy on 19/12/2016.
 */
public class ValidPassiveMobFilter {

    public static boolean ValidPassiveMobFilter(Entity entity) {

        Configuration config = ConfigValues.defaultConfig;

        return entity instanceof Chicken && config.getBoolean("Valid passive EliteMobs.Chicken")||
                entity instanceof Cow && config.getBoolean("Valid passive EliteMobs.Cow") ||
                entity instanceof MushroomCow && config.getBoolean("Valid passive EliteMobs.MushroomCow") ||
                entity instanceof Pig && config.getBoolean("Valid passive EliteMobs.Pig") ||
                entity instanceof Sheep && config.getBoolean("Valid passive EliteMobs.Sheep");

    }

}
