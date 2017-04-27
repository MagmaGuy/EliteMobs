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

package com.magmaguy.elitemobs.naturalmobspawner;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Random;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobMetadataAssigner implements Listener {

    private EliteMobs plugin;
    private int range = 50;


    public NaturalMobMetadataAssigner(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM) {

            Entity entity = event.getEntity();

            ValidAgressiveMobFilter validAgressiveMobFilter = new ValidAgressiveMobFilter();

            if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity, plugin.getConfig().getList("Valid aggressive EliteMobs"))) {

                Random random = new Random();

                MetadataHandler metadataHandler = new MetadataHandler(plugin);

                entity.setMetadata(metadataHandler.naturalMob, new FixedMetadataValue(plugin, true));

                //20% chance of turning a mob into a EliteMob
                if (random.nextDouble() < plugin.getConfig().getDouble("Percentage (%) of aggressive mobs that get converted to EliteMobs when they spawn") / 100) {

                    NaturalMobSpawner naturalMobSpawner = new NaturalMobSpawner(plugin);
                    naturalMobSpawner.naturalMobProcessor(entity);

                }

            }

        }

    }

}
