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

package com.magmaguy.elitemobs.mobpowers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class LivingEntityFinder {

    private static Player playerFilter(Entity entity) {

        return (entity instanceof Player) ? (Player) entity : null;

    }

    private static LivingEntity eliteMobFilter(Entity entity) {

        if (entity instanceof LivingEntity) return (LivingEntity) entity;
        if (entity instanceof Projectile && ((Projectile) entity).getShooter() != null &&
                ((Projectile) entity).getShooter() instanceof LivingEntity) {

            return (LivingEntity) ((Projectile) entity).getShooter();

        }

        return null;

    }

    public static Player findPlayer(EntityDamageByEntityEvent event) {

        Entity damagee = event.getEntity();

        return playerFilter(damagee);

    }

    public static LivingEntity findEliteMob(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();

        return eliteMobFilter(damager);

    }

    public static Player findPlayer(EntityTargetEvent event) {

        Entity damagee = event.getTarget();

        return playerFilter(damagee);

    }

    public static LivingEntity findEliteMob(EntityTargetEvent event) {

        Entity damager = event.getEntity();

        return eliteMobFilter(damager);

    }

}
