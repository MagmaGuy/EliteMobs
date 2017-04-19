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

package com.magmaguy.magmasmobs.mobcustomizer;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class LevelHandler {

    public static void LevelHandler (Entity entity, Entity deletedEntity, Plugin plugin) {

        //No previous metadata, assume it's the first level
        if (!entity.hasMetadata("MagmasSuperMob") && !deletedEntity.hasMetadata("MagmasSuperMob")) {

            entity.setMetadata("MagmasSuperMob", new FixedMetadataValue(plugin, 2));

        } else if (entity.hasMetadata("MagmasSuperMob") && !deletedEntity.hasMetadata("MagmasSuperMob")) {

            int finalMetadata = entity.getMetadata("MagmasSuperMob").get(0).asInt() + 1;

            entity.setMetadata("MagmasSuperMob", new FixedMetadataValue(plugin, finalMetadata));

        } else if (!entity.hasMetadata("MagmasSuperMob") && deletedEntity.hasMetadata("MagmasSuperMob")) {

            int finalMetadata = deletedEntity.getMetadata("MagmasSuperMob").get(0).asInt() + 1;

            entity.setMetadata("MagmasSuperMob", new FixedMetadataValue(plugin, finalMetadata));

        } else if (entity.hasMetadata("MagmasSuperMob") && deletedEntity.hasMetadata("MagmasSuperMob")){

            int finalMetadata = entity.getMetadata("MagmasSuperMob").get(0).asInt() +
                    deletedEntity.getMetadata("MagmasSuperMob").get(0).asInt();

            entity.setMetadata("MagmasSuperMob", new FixedMetadataValue(plugin, finalMetadata));

        } else {

            getLogger().info("MagmasMobs - Invalid metadata state - Contact the dev!");

        }

    }

}
