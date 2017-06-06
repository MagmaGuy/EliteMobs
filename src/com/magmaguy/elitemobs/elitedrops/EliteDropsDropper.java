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

package com.magmaguy.elitemobs.elitedrops;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.Random;

/**
 * Created by MagmaGuy on 04/06/2017.
 */
public class EliteDropsDropper implements Listener{

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        Entity entity = event.getEntity();

        Random random = new Random();

        if (entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() > 4) {


            if (random.nextDouble() < ConfigValues.defaultConfig.getDouble("Aggressive EliteMobs flat loot drop rate %") / 100) {

                int randomDrop = random.nextInt(EliteDropsHandler.lootList.size());

                entity.getWorld().dropItem(entity.getLocation(), EliteDropsHandler.lootList.get(randomDrop));

            }

            //double drops
            if (ConfigValues.defaultConfig.getBoolean("Aggressive EliteMobs can drop additional loot with drop % based on EliteMob level (higher is more likely)") &&
                    random.nextDouble() < (entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() / 100)) {

                int randomDrop = random.nextInt(EliteDropsHandler.lootList.size());

                entity.getWorld().dropItem(entity.getLocation(), EliteDropsHandler.lootList.get(randomDrop));

            }

            entity.removeMetadata(MetadataHandler.ELITE_MOB_MD, plugin);

            if (entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                entity.removeMetadata(MetadataHandler.NATURAL_MOB_MD, plugin);

            }

        }

    }

}
