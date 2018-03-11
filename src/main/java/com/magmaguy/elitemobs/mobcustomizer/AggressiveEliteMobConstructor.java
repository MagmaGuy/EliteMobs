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

package com.magmaguy.elitemobs.mobcustomizer;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.Entity;

import static com.magmaguy.elitemobs.mobcustomizer.NameHandler.customAggressiveName;

/**
 * Created by MagmaGuy on 15/07/2017.
 */
public class AggressiveEliteMobConstructor {

    public static void constructAggressiveEliteMob(Entity entity) {

        if (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() < 1) {

            entity.remove();
            return;

        }

        HealthHandler.naturalAgressiveHealthHandler(entity, entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt());
        customAggressiveName(entity);
        PowerHandler.powerHandler(entity);
        ArmorHandler.ArmorHandler(entity);

    }

}
