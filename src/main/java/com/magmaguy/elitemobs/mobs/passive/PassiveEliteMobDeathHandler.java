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

package com.magmaguy.elitemobs.mobs.passive;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.SuperMobConstructor;
import com.magmaguy.elitemobs.mobconstructor.mobdata.passivemobs.SuperMobProperties;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;

/**
 * Created by MagmaGuy on 03/05/2017.
 */
public class PassiveEliteMobDeathHandler implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(EntityDeathEvent event) {

        if (EntityTracker.isSuperMob(event.getEntity())) {

            event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), event.getEntityType());
            event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), event.getEntityType());

        }

    }

    public static class SuperMobScanner {

        private static final int passiveRange = MobCombatSettingsConfig.superMobsStackRange;

        public static void scanSuperMobs() {

            for (World world : validWorldList) {

                if (world.getLivingEntities().isEmpty()) continue;

                Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

                while (iterator.hasNext()) {

                    LivingEntity livingEntity = iterator.next();

                    if (!SuperMobProperties.isValidSuperMobType(livingEntity)) continue;

                            /*
                            Re-register lost passive mob
                            */
                    checkLostSuperMob(livingEntity);

                            /*
                           Check passive mobs to register new super mobs
                           */
                    newSuperMobScan(livingEntity);

                }

            }

        }

        private static void checkLostSuperMob(LivingEntity livingEntity) {

            if (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() !=
                    SuperMobProperties.getSuperMobMaxHealth(livingEntity))
                return;

            if (!EntityTracker.isSuperMob(livingEntity))
                EntityTracker.registerSuperMob(livingEntity);

        }

        public static void newSuperMobScan(LivingEntity livingEntity) {

            if (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() ==
                    SuperMobProperties.getSuperMobMaxHealth(livingEntity))
                return;

            if (ThreadLocalRandom.current().nextDouble() < 1 / DefaultConfig.superMobStackAmount)
                return;

            ArrayList<LivingEntity> livingEntities = new ArrayList<>();

            for (Entity entity : livingEntity.getNearbyEntities(passiveRange, passiveRange, passiveRange)) {

                if (!entity.getType().equals(livingEntity.getType())) continue;
                if (EntityTracker.isSuperMob(entity)) continue;
                livingEntities.add((LivingEntity) entity);
                if (livingEntities.size() >= DefaultConfig.superMobStackAmount)
                    break;

            }

            if (livingEntities.size() < DefaultConfig.superMobStackAmount)
                return;

            SuperMobConstructor.constructSuperMob(livingEntity);
            for (Entity entity : livingEntities) {
                entity.remove();
            }

        }

    }
}
