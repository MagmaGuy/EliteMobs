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

package com.magmaguy.elitemobs.mobpowers.minorpowers;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EventValidator {

    /*
    This class checks if the event is valid
    It does so by checking if it isn't cancelled, if it contains an elite mob and if that elite mob has the relevant
    power for that event
    As such, the event should be cancelled at a power level when this returns null
     */
    public static EliteMobEntity getEventEliteMob(ElitePower mobPower, EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return null;

        Player player = EntityFinder.findPlayer(event);

        if (player == null) return null;

        LivingEntity livingEntity = EntityFinder.getRealDamager(event);

        if (livingEntity == null) return null;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(livingEntity);

        if (eliteMobEntity == null) return null;

        if (!EntityTracker.hasPower(mobPower, eliteMobEntity)) return null;

        return eliteMobEntity;

    }

    public static EliteMobEntity getEventEliteMob(ElitePower mobPower, EntityTargetLivingEntityEvent event) {

        if (event.isCancelled()) return null;

        Player player = EntityFinder.findPlayer(event);

        if (player == null) return null;

        LivingEntity livingEntity = EntityFinder.getRealDamager(event);

        if (livingEntity == null) return null;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(livingEntity);

        if (eliteMobEntity == null) return null;

        if (!EntityTracker.hasPower(mobPower, eliteMobEntity)) return null;

        return eliteMobEntity;

    }

    public static EliteMobEntity getEventEliteMob(ElitePower mobPower, EntityDamageEvent event) {

        if (event.isCancelled()) return null;

        if (!(event.getEntity() instanceof LivingEntity)) return null;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());

        if (eliteMobEntity == null) return null;

        if (!EntityTracker.hasPower(mobPower, eliteMobEntity)) return null;

        return eliteMobEntity;

    }

}
