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

package com.magmaguy.elitemobs.events.mobs.sharedeventproperties;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.events.EventMessage;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class BossMobDeathCountdown {

    public static void startDeathCountdown(LivingEntity entity) {

        new BukkitRunnable() {

            @Override
            public void run() {

                if (entity.isValid() && !entity.isDead()) {

                    entity.remove();
                    String eventMessage = ChatColorConverter.chatColorConverter(ConfigValues.eventsConfig.getString(EventsConfig.EVENT_TIMEOUT_MESSAGE).replace("$bossmob", entity.getCustomName()));
                    EventMessage.sendEventMessage(eventMessage);

                }

            }

        }.runTaskLater(MetadataHandler.PLUGIN, (long) (20 * 60 * ConfigValues.eventsConfig.getDouble(EventsConfig.EVENT_TIMEOUT_TIME)));

    }

}
