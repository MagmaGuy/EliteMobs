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

package com.magmaguy.magmasmobs.collateralminecraftchanges;

import com.magmaguy.magmasmobs.MagmasMobs;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;


/**
 * Created by MagmaGuy on 20/12/2016.
 */
public class ChunkUnloadMetadataPurge implements Listener {

    private MagmasMobs plugin;

    public ChunkUnloadMetadataPurge(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }


    @EventHandler
    public void onUnload(ChunkUnloadEvent event) {

        for (Entity entity : event.getChunk().getEntities()) {

            if (entity.hasMetadata("MagmasSuperMob")) {

                entity.remove();
                entity.removeMetadata("MagmasSuperMob", plugin);

            }

            if (entity.hasMetadata("VisualEffect")) {

                entity.remove();
                entity.removeMetadata("VisualEffect", plugin);

            }

            if (entity.hasMetadata("CylindricalPowerStance")) {

                entity.remove();
                entity.removeMetadata("CylindricalPowerStance", plugin);

            }

            if (entity.hasMetadata("forbidden")) {

                entity.remove();
                entity.removeMetadata("forbidden", plugin);

            }

            if (entity.hasMetadata("NaturalEntity")) {

                entity.remove();
                entity.removeMetadata("NaturalEntity", plugin);

            }

        }

    }

}
