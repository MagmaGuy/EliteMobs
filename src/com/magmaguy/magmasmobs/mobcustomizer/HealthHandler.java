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

package com.magmaguy.magmasmobs.mobcustomizer;

import com.magmaguy.magmasmobs.mobscanner.DefaultMaxHealthGuesser;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class HealthHandler {

    public static void aggressiveHealthHandler(Entity entity, Entity deletedEntity) {

        Damageable damageableEntity = ((Damageable) entity);
        Damageable damageableDeleted = ((Damageable) deletedEntity);

        damageableEntity.setMaxHealth(PowerFormula.PowerFormula (DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity), entity.getMetadata("MagmasSuperMob").get(0).asInt()));

        if (entity.hasMetadata("MagmasSuperMob") && !deletedEntity.hasMetadata("MagmasSuperMob")) {

            double adjustedAddedHealth = damageableEntity.getHealth() + PowerFormula.PowerFormula(DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity),
                    entity.getMetadata("MagmasSuperMob").get(0).asInt()) - PowerFormula.PowerFormula
                    (DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity), entity.getMetadata("MagmasSuperMob").get(0).asInt() - 1) -
                    (damageableDeleted.getMaxHealth() - damageableDeleted.getHealth());

            damageableEntity.setHealth(adjustedAddedHealth);

            getLogger().info("added health " + adjustedAddedHealth);

        } else if (entity.hasMetadata("MagmasSuperMob") && deletedEntity.hasMetadata("MagmasSuperMob")) {

            if (damageableEntity.getHealth() + damageableDeleted.getHealth() > damageableEntity.getMaxHealth()) {

                damageableEntity.setHealth(damageableEntity.getMaxHealth());

            } else {

                damageableEntity.setHealth(damageableEntity.getHealth() + ((Damageable) deletedEntity).getHealth());

            }

        }

    }

    public static void passiveHealthHandler (Entity entity, int passiveStacking) {

        ((Damageable) entity).setMaxHealth(((LivingEntity) entity).getMaxHealth() * passiveStacking);

        ((Damageable) entity).setHealth(((LivingEntity) entity).getMaxHealth());

    }

}
