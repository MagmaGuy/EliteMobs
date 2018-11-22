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

package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import org.bukkit.Location;

public class SpawnRadiusDifficultyIncrementer {

    public static int distanceFromSpawnLevelIncrease(Location location) {

        double distanceUnit = ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.DISTANCE_TO_INCREMENT);
        double levelToIncrement = ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.LEVEL_TO_INCREMENT);

        Location spawnLocation = location.getWorld().getSpawnLocation();
        Location entityLocation = location;

        double distanceFromSpawn = spawnLocation.distance(entityLocation);

        int levelIncrease = (int) (distanceFromSpawn / distanceUnit * levelToIncrement);

        return levelIncrease;

    }

}
