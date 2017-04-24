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

package com.magmaguy.magmasmobs.naturalmobspawner;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.mobscanner.ValidAgressiveMobFilter;
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

    private MagmasMobs plugin;

    public NaturalMobMetadataAssigner(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    private int range = 50;

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason() == NATURAL || event.getSpawnReason() == CUSTOM) {

            Entity entity = event.getEntity();

            ValidAgressiveMobFilter validAgressiveMobFilter = new ValidAgressiveMobFilter();

            if (validAgressiveMobFilter.ValidAgressiveMobFilter(entity, plugin.getConfig().getList("Valid aggressive SuperMobs"))) {

                Random random = new Random();

                entity.setMetadata("NaturalEntity", new FixedMetadataValue(plugin, true));

                //20% chance of turning a mob into a supermob
                if (random.nextDouble() < plugin.getConfig().getDouble("Percentage (%) of aggressive mobs that get converted to SuperMobs when they spawn") / 100) {

                    NaturalMobSpawner naturalMobSpawner = new NaturalMobSpawner(plugin);
                    naturalMobSpawner.naturalMobProcessor(entity);

                }

            }

        }

    }

}
