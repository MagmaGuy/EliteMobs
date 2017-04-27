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

package com.magmaguy.elitemobs.mobscanner;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobcustomizer.*;
import com.magmaguy.elitemobs.mobs.passive.ChickenHandler;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.magmaguy.elitemobs.EliteMobs.worldList;
import static com.magmaguy.elitemobs.mobcustomizer.NameHandler.customAggressiveName;
import static com.magmaguy.elitemobs.mobcustomizer.NameHandler.customPassiveName;

/**
 * Created by MagmaGuy on 07/10/2016.
 */
public class MobScanner implements Listener {

    private EliteMobs plugin;
    private int aggressiveRange = 2;
    private int passiveRange = 15;


    public MobScanner(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    public void scanMobs(int passiveStackAmount) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        for (World world : worldList) {

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext()) {

                Entity entity = iterator.next();

                List aggressiveList = plugin.getConfig().getList("Valid aggressive EliteMobs");

                if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity, aggressiveList)) {

                    //scan for naturally spawned EliteMobs
                    if (entity.hasMetadata(metadataHandler.eliteMobMD) && ((Damageable) entity).getMaxHealth() == DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

                        HealthHandler.naturalAgressiveHealthHandler(entity, entity.getMetadata(metadataHandler.eliteMobMD).get(0).asInt());
                        customAggressiveName(entity, plugin);

                    }

                    //scan for stacked EliteMobs
                    if (plugin.getConfig().getBoolean("Allow aggressive EliteMobs")) {

                        scanValidAggressiveLivingEntity(entity);

                        if (entity.hasMetadata(metadataHandler.eliteMobMD)) {


                            PowerHandler powerHandler = new PowerHandler(plugin);

                            powerHandler.powerHandler(entity);

                            ArmorHandler.ArmorHandler(entity);

                        }

                    }

                }

                //scan for passive mobs
                if (plugin.getConfig().getBoolean("Allow Passive EliteMobs")) {

                    List passiveList = plugin.getConfig().getList("Valid Passive EliteMobs");

                    if (ValidPassiveMobFilter.ValidPassiveMobFilter(entity, passiveList) && !entity.hasMetadata(metadataHandler.passiveEliteMobMD)) {

                        scanValidPassiveLivingEntity(entity);

                    }

                    //spawn chicken eggs, really wish there were an event for this
                    ChickenHandler.superEggs(entity, passiveStackAmount);

                }

                //scan for iron golems with missing metadata
                if (!entity.hasMetadata(metadataHandler.eliteMobMD) && entity instanceof IronGolem &&
                        ((IronGolem) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

                    entity.setMetadata(metadataHandler.eliteMobMD, new FixedMetadataValue(plugin, ScalingFormula.reversePowerFormula(((IronGolem) entity).getMaxHealth(),
                            DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity))));
                    customAggressiveName(entity, plugin);

                }

            }

        }

    }

    public void scanValidAggressiveLivingEntity(Entity entity) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        for (Entity secondEntity : entity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange)) {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !entity.hasMetadata(metadataHandler.forbidden) && !secondEntity.hasMetadata(metadataHandler.forbidden)) {

                //If the sum of both entities is above level 50, don't add both entities together
                if (levelCap(entity, secondEntity)) {

                    if (!entity.hasMetadata(metadataHandler.naturalMob) || !secondEntity.hasMetadata(metadataHandler.naturalMob)) {

                        entity.removeMetadata(metadataHandler.naturalMob, plugin);

                    }

                    //remove duplicate
                    secondEntity.remove();

                    //setup new EliteMob
                    LevelHandler.LevelHandler(entity, secondEntity, plugin);
                    HealthHandler.aggressiveHealthHandler(entity, secondEntity);
                    customAggressiveName(entity, plugin);

                    if (secondEntity.hasMetadata(metadataHandler.eliteMobMD)) {

                        secondEntity.removeMetadata(metadataHandler.eliteMobMD, plugin);

                    }

                    return;

                }

            }

        }

    }

    public void scanValidPassiveLivingEntity(Entity entity) {

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        int passiveStacking = plugin.getConfig().getInt("Passive EliteMob stack amount");

        List<LivingEntity> animalContainer = new ArrayList<>();

        for (Entity secondEntity : entity.getNearbyEntities(passiveRange, passiveRange, passiveRange)) {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !secondEntity.hasMetadata(metadataHandler.passiveEliteMobMD)) {

                animalContainer.add((LivingEntity) secondEntity);

                if (animalContainer.size() == passiveStacking && !entity.hasMetadata(metadataHandler.passiveEliteMobMD)) {

                    Iterator<LivingEntity> animalSlaughterer = animalContainer.iterator();

                    boolean firstAnimal = true;

                    for (LivingEntity livingEntity : animalContainer) {

                        livingEntity.remove();

                    }

                    HealthHandler.passiveHealthHandler(entity, passiveStacking);

                    customPassiveName(entity, plugin);

                    return;

                }

            }

        }

        if (((Damageable) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) &&
                !entity.hasMetadata(metadataHandler.passiveEliteMobMD)) {

            customPassiveName(entity, plugin);

        }

    }


    public boolean levelCap(Entity entity1, Entity entity2) {

        Damageable damageable1 = (Damageable) entity1;
        Damageable damageable2 = (Damageable) entity2;

        MetadataHandler metadataHandler = new MetadataHandler(plugin);

        if (damageable1.hasMetadata(metadataHandler.eliteMobMD) && damageable1.getMetadata(metadataHandler.eliteMobMD).get(0).asInt() >= plugin.getConfig().getInt("Aggressive mob stacking cap")) {

            return false;

        } else if (damageable2.hasMetadata(metadataHandler.eliteMobMD) && damageable2.getMetadata(metadataHandler.eliteMobMD).get(0).asInt() >= plugin.getConfig().getInt("Aggressive mob stacking cap")) {

            return false;

        }

        return true;

    }

}
