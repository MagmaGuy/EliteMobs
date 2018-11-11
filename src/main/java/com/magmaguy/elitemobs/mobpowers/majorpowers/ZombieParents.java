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

package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.ReinforcementMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.ChatColorConverter.convert;

/**
 * Created by MagmaGuy on 13/05/2017.
 */
public class ZombieParents extends MajorPower implements Listener {

    public static HashSet<EliteMobEntity> activatedZombies = new HashSet<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onHit(EntityDamageEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;
        if (ThreadLocalRandom.current().nextDouble() > 0.01) return;
        if (activatedZombies.contains(eliteMobEntity)) return;

        Skeleton zombieMom = (Skeleton) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(eliteMobEntity.getLivingEntity().getLocation(), EntityType.SKELETON);
        Skeleton zombieDad = (Skeleton) eliteMobEntity.getLivingEntity().getWorld().spawnEntity(eliteMobEntity.getLivingEntity().getLocation(), EntityType.SKELETON);

        ReinforcementMobEntity reinforcementMom = new ReinforcementMobEntity(zombieMom, eliteMobEntity.getLevel(),
                ConfigValues.translationConfig.getString("ZombieParents.Mom Name"));

        ReinforcementMobEntity reinforcementDad = new ReinforcementMobEntity(zombieDad, eliteMobEntity.getLevel(),
                ConfigValues.translationConfig.getString("ZombieParents.Dad Name"));

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!eliteMobEntity.getLivingEntity().isValid()) {

                    if (zombieDad.isValid()) {

                        nameClearer(reinforcementDad);

                        zombieDad.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage")
                                        .size()))));

                    }

                    if (zombieMom.isValid()) {

                        nameClearer(reinforcementMom);

                        zombieMom.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.DeathMessage")
                                        .size()))));

                    }

                    cancel();

                } else {

                    if (ThreadLocalRandom.current().nextDouble() < 0.5) {

                        nameClearer(eliteMobEntity);

                        eliteMobEntity.getLivingEntity().setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && zombieDad.isValid()) {

                        nameClearer(reinforcementDad);

                        zombieDad.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDadDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieDadDialog")
                                        .size()))));

                    }

                    if (ThreadLocalRandom.current().nextDouble() < 0.5 && zombieMom.isValid()) {

                        nameClearer(reinforcementMom);

                        zombieMom.setCustomName(convert(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieMomDialog").
                                get(ThreadLocalRandom.current().nextInt(ConfigValues.translationConfig.getStringList("ZombieParents.ZombieMomDialog")
                                        .size()))));

                    }

                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 8);

    }

    private void nameClearer(EliteMobEntity eliteMobEntity) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (eliteMobEntity.getLivingEntity().isValid())
                    eliteMobEntity.setName(eliteMobEntity.getName());
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);

    }

}
