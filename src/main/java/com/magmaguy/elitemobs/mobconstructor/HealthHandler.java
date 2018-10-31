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

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class HealthHandler {

    public static void aggressiveHealthHandler(Entity entity, Entity deletedEntity) {

        if (entity.hasMetadata(MetadataHandler.CUSTOM_HEALTH)) return;

        Damageable damageableEntity = ((Damageable) entity);
        Damageable damageableDeleted = ((Damageable) deletedEntity);
        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser((LivingEntity) entity);
        int newEliteMobLevel = entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

        if (entity.hasMetadata(MetadataHandler.DOUBLE_DAMAGE_MD)) {

            newEliteMobLevel = (int) Math.floor(newEliteMobLevel / 2);

            if (newEliteMobLevel < 1)
                newEliteMobLevel = 1;

        }

        if (entity.hasMetadata(MetadataHandler.DOUBLE_HEALTH_MD)) {

            newEliteMobLevel = (int) Math.floor(newEliteMobLevel * 2);

        }

        damageableEntity.setMaxHealth(maxHealthFormula(newEliteMobLevel, defaultMaxHealth));

        if (damageableEntity.getHealth() + damageableDeleted.getHealth() > damageableEntity.getMaxHealth()) {

            damageableEntity.setHealth(damageableEntity.getMaxHealth());

        } else {

            damageableEntity.setHealth(damageableEntity.getHealth() + damageableDeleted.getHealth());

        }

    }

    private static double maxHealthFormula(int mobLevel, double defaultMaxHealth) {

        //Baseline calc is made for zombies, increments health in 10% leaps
        double newMaxHealth = (mobLevel * DamageAdjuster.PER_LEVEL_POWER_INCREASE * defaultMaxHealth + defaultMaxHealth);

        return newMaxHealth;

    }

    public static void passiveHealthHandler(Entity entity, int passiveStacking) {

        ((Damageable) entity).setMaxHealth(((LivingEntity) entity).getMaxHealth() * passiveStacking);
        ((Damageable) entity).setHealth(((LivingEntity) entity).getMaxHealth());

    }

    public static void naturalAggressiveHealthHandler(Entity entity, int eliteMobLevel) {

        Damageable damageable = (Damageable) entity;
        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser((LivingEntity) entity);
        int newEliteMobLevel = eliteMobLevel;

        damageable.setMaxHealth(maxHealthFormula(newEliteMobLevel, defaultMaxHealth));
        damageable.setHealth(damageable.getMaxHealth());

    }

    public static double reversePowerFormula(double currentValue, double baseAmount) {

        return (currentValue - baseAmount) / 2;

    }

}
