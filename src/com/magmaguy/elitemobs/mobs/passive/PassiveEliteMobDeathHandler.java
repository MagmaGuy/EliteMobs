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

package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

/**
 * Created by MagmaGuy on 03/05/2017.
 */
public class PassiveEliteMobDeathHandler implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

            event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), event.getEntityType());
            event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), event.getEntityType());

            event.getEntity().removeMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD, plugin);

        }

    }

}
