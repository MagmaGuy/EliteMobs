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
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.mobcustomizer.AggressiveEliteMobConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class SmallTreasureGoblin implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);

    public static boolean entityQueued = false;

    public static void initializeEvent() {
        entityQueued = true;
    }

    public void createGoblin(Entity entity) {

        //Give custom setup to entity
        entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, 200));
        entity.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(plugin, true));
        entity.setMetadata(MetadataHandler.CUSTOM_NAME, new FixedMetadataValue(plugin, true));
        entity.setMetadata(MetadataHandler.CUSTOM_ARMOR, new FixedMetadataValue(plugin, true));
//        entity.setMetadata(MetadataHandler.FORBIDDEN_MD, new FixedMetadataValue(plugin, true));
        entity.setMetadata(MetadataHandler.NATURAL_MOB_MD, new FixedMetadataValue(plugin, true));
        AggressiveEliteMobConstructor.constructAggressiveEliteMob(entity);

        ((Zombie) entity).setBaby(true);
        ((Zombie) entity).setRemoveWhenFarAway(false);

        entity.setCustomName("Treasure Goblin");
        entity.setCustomNameVisible(true);

        entity.setMetadata(MetadataHandler.TREASURE_GOBLIN, new FixedMetadataValue(plugin, true));

        String coordinates = entity.getLocation().getBlockX() + ", " + entity.getLocation().getBlockY() + ", " + entity.getLocation().getBlockZ();

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {

            String sendString = ConfigValues.eventsConfig.getString(EventsConfig.SMALL_TREASURE_GOBLIN_EVENT_ANNOUNCEMENT_TEXT).replace("$location", coordinates);

            player.sendMessage(sendString);

        }

        entityQueued = false;

    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {

        if (EliteMobs.worldList.contains(event.getEntity().getWorld())) {

            if (entityQueued && (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) ||
                    event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM)) &&
                    event.getEntity() instanceof Zombie) {

                createGoblin(event.getEntity());

            }

        }

    }


}
