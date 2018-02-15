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

package com.magmaguy.elitemobs.mobmerger;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobscanner.MobScanner;
import com.magmaguy.elitemobs.mobscanner.ValidAgressiveMobFilter;
import com.magmaguy.elitemobs.mobscanner.ValidPassiveMobFilter;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Created by MagmaGuy on 15/07/2017.
 */
public class MergeHandler implements Listener {

    @EventHandler
    public void onDamageMerge(EntityDamageEvent event) {

        validateEntityType(event.getEntity());

    }

    @EventHandler
    public void onOtherEntityDamageMerge(EntityDamageByEntityEvent event) {

        validateEntityType(event.getEntity());
        validateEntityType(event.getDamager());

    }

    @EventHandler
    public void onSpawnMerge(EntitySpawnEvent event) {

        validateEntityType(event.getEntity());

    }

    @EventHandler
    public void onDetectMerge(EntityTargetEvent event) {

        validateEntityType(event.getEntity());

    }

    private void validateEntityType(Entity eventEntity) {

        if (!ConfigValues.defaultConfig.getBoolean("Valid worlds." + eventEntity.getWorld().getName().toString())) {
            return;
        }

        if (!eventEntity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) && !ConfigValues.defaultConfig.getBoolean(DefaultConfig.STACK_AGGRESSIVE_SPAWNER_MOBS)) {
            return;
        }

        MobScanner mobScanner = new MobScanner();

        if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(eventEntity) && ConfigValues.defaultConfig.getBoolean("Allow aggressive EliteMobs") &&
                ConfigValues.defaultConfig.getBoolean(DefaultConfig.AGGRESSIVE_MOB_STACKING) && !eventEntity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

            if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.AGGRESSIVE_MOB_STACKING) && !eventEntity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                mobScanner.scanValidAggressiveLivingEntity(eventEntity);

            } else if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.AGGRESSIVE_MOB_STACKING) && ConfigValues.defaultConfig.getBoolean(DefaultConfig.STACK_AGGRESSIVE_NATURAL_MOBS) &&
                    eventEntity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                mobScanner.scanValidAggressiveLivingEntity(eventEntity);

            }

        } else if (ValidPassiveMobFilter.ValidPassiveMobFilter(eventEntity) && ConfigValues.defaultConfig.getBoolean("Allow Passive EliteMobs")) {

            mobScanner.scanValidPassiveLivingEntity(eventEntity);

        }

    }

}
