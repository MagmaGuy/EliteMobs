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

package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import com.magmaguy.elitemobs.mobscanner.EliteMobScanner;
import com.magmaguy.elitemobs.mobscanner.SuperMobScanner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Created by MagmaGuy on 15/07/2017.
 */
public class MergeHandler implements Listener {

    @EventHandler
    public void onDamageMerge(EntityDamageEvent event) {

        if (!ConfigValues.validWorldsConfig.getBoolean("Valid worlds." + event.getEntity().getWorld().getName()))
            return;
        validateEntityType(event.getEntity());

    }

    @EventHandler
    public void onSpawnMerge(EntitySpawnEvent event) {

        if (!ConfigValues.validWorldsConfig.getBoolean("Valid worlds." + event.getEntity().getWorld().getName()))
            return;
        validateEntityType(event.getEntity());

    }

    private void validateEntityType(Entity eventEntity) {

        if (eventEntity == null) return;
        if (!(eventEntity instanceof LivingEntity)) return;

        if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.AGGRESSIVE_MOB_STACKING) &&
                EliteMobProperties.isValidEliteMobType(eventEntity))
            EliteMobScanner.scanValidAggressiveLivingEntity((LivingEntity) eventEntity);

        if (ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS) &&
                SuperMobProperties.isValidSuperMobType(eventEntity))
            SuperMobScanner.newSuperMobScan((LivingEntity) eventEntity);

    }

}
