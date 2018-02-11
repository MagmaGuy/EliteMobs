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

package com.magmaguy.elitemobs.events.mobs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.elitedrops.EliteDropsDropper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class TreasureGoblin implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {

        if (event.getEntity().hasMetadata(MetadataHandler.TREASURE_GOBLIN)) {

            EliteDropsDropper eliteDropsDropper = new EliteDropsDropper();

            Entity entity = event.getEntity();

            for (int i = 0; i < 10; i++) {

                eliteDropsDropper.dropItem(entity);

            }

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                if (((LivingEntity) entity).getKiller() != null) {

                    player.sendMessage(ConfigValues.eventsConfig.getString(EventsConfig.SMALL_TREASURE_GOBLIN_EVENT_PLAYER_END_TEXT).replace("$player", ((LivingEntity) entity).getKiller().getDisplayName()));

                } else {

                    player.sendMessage(ConfigValues.eventsConfig.getString(EventsConfig.SMALL_TREASURE_GOBLIN_EVENT_OTHER_END_TEXT));

                }

            }

        }

    }

}
