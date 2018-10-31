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

package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.events.mobs.TreasureGoblin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class SmallTreasureGoblin implements Listener {

    public static boolean entityQueued = false;

    public static void initializeEvent() {
        entityQueued = true;
    }

    public static void initalizeEvent(LivingEntity treasureGoblin) {

        TreasureGoblin.createGoblin(treasureGoblin);

        entityQueued = false;

    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.isCancelled()) return;
        if (!EventLauncher.verifyPlayerCount()) return;

        if (EliteMobs.validWorldList.contains(event.getEntity().getWorld())) {

            if (entityQueued && (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) ||
                    event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) &&
                    event.getEntity() instanceof Zombie) {

                initalizeEvent(event.getEntity());

            }

        }

    }


}
