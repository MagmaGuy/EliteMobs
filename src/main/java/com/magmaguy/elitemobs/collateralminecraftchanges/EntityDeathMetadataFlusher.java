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

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by MagmaGuy on 14/07/2017.
 */
public class EntityDeathMetadataFlusher implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathFlushMetadata(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        for (String metadata : MetadataHandler.metadataList()) {

            if (entity.hasMetadata(metadata)) {

                new BukkitRunnable() {

                    @Override
                    public void run() {

                        MetadataHandler.fullMetadataFlush(entity);

                    }

                }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 10);

                return;
            }

        }

    }


}
