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

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobscanner.MobScanner;
import com.magmaguy.elitemobs.mobscanner.ValidAggressiveMobFilter;
import com.magmaguy.elitemobs.mobscanner.ValidPassiveMobFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

        if (eventEntity == null || !(eventEntity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) eventEntity;

        if (!EliteMobs.validWorldList.contains(eventEntity.getWorld()))
            return;

        if (!EntityTracker.isNaturalEntity(livingEntity) && !ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_SPAWNER_MOBS))
            return;

        MobScanner mobScanner = new MobScanner();

        if (ValidAggressiveMobFilter.checkValidAggressiveMob(eventEntity) && ConfigValues.defaultConfig.getBoolean("Allow aggressive EliteMobs") &&
                ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.AGGRESSIVE_MOB_STACKING) && !EntityTracker.isNaturalEntity(livingEntity)) {

            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.AGGRESSIVE_MOB_STACKING) && !EntityTracker.isNaturalEntity(livingEntity)) {

                mobScanner.scanValidAggressiveLivingEntity(livingEntity);

            } else if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.AGGRESSIVE_MOB_STACKING) && ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_NATURAL_MOBS) &&
                    EntityTracker.isNaturalEntity(livingEntity)) {

                mobScanner.scanValidAggressiveLivingEntity(livingEntity);

            }

        } else if (ValidPassiveMobFilter.ValidPassiveMobFilter(eventEntity) && ConfigValues.defaultConfig.getBoolean("Allow Passive EliteMobs"))
            mobScanner.scanValidPassiveLivingEntity(eventEntity);

    }

}
