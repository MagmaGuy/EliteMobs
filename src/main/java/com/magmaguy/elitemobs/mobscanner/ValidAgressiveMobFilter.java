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
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;

import static com.magmaguy.elitemobs.mobcustomizer.NameHandler.currentVersionUnder;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class ValidAgressiveMobFilter {

    public static boolean checkValidAggressiveMob(Entity entity) {

        Configuration config = ConfigValues.validMobsConfig;

        switch (entity.getType()) {
            case BLAZE:
                return config.getBoolean(ValidMobsConfig.BLAZE);
            case CAVE_SPIDER:
                return config.getBoolean(ValidMobsConfig.CAVE_SPIDER);
            case CREEPER:
                return config.getBoolean(ValidMobsConfig.CREEPER);
            case ENDERMAN:
                return config.getBoolean(ValidMobsConfig.ENDERMAN);
            case IRON_GOLEM:
                return config.getBoolean(ValidMobsConfig.IRON_GOLEM);
            case PIG_ZOMBIE:
                return config.getBoolean(ValidMobsConfig.PIG_ZOMBIE);
            case SILVERFISH:
                return config.getBoolean(ValidMobsConfig.SILVERFISH);
            case SKELETON:
                return config.getBoolean(ValidMobsConfig.SKELETON);
            case WITHER_SKELETON:
                return config.getBoolean(ValidMobsConfig.WITHER_SKELETON);
            case SPIDER:
                return config.getBoolean(ValidMobsConfig.SPIDER);
            case WITCH:
                return config.getBoolean(ValidMobsConfig.WITCH);
            case ZOMBIE:
                return config.getBoolean(ValidMobsConfig.ZOMBIE);
        }

        if (currentVersionUnder(1, 8))
            switch (entity.getType()) {
                case ENDERMITE:
                    return config.getBoolean(ValidMobsConfig.ENDERMITE);
            }

        if (!currentVersionUnder(1, 11))
            switch (entity.getType()) {
                case STRAY:
                    return config.getBoolean(ValidMobsConfig.STRAY);
                case HUSK:
                    return config.getBoolean(ValidMobsConfig.HUSK);
                case VEX:
                    return config.getBoolean(ValidMobsConfig.VEX);
                case POLAR_BEAR:
                    return config.getBoolean(ValidMobsConfig.POLAR_BEAR);
                case VINDICATOR:
                    return config.getBoolean(ValidMobsConfig.VINDICATOR);
            }


        return false;

    }

}
