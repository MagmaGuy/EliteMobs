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

package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class OptionalNameTagAssigner {

    private static final int RANGE = ConfigValues.defaultConfig.getInt(DefaultConfig.NAME_TAG_RANGE);

    public static void entityScanner() {

        for (World world : EliteMobs.worldList) {

            for (Player player : world.getPlayers()) {

                for (Entity entity : player.getNearbyEntities(RANGE, RANGE, RANGE)) {

                    if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

                        entity.setCustomNameVisible(true);

                    }

                }

            }

        }

    }

}
