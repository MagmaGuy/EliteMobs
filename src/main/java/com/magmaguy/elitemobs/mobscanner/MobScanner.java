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
import com.magmaguy.elitemobs.config.ConfigValues;
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

        for (World world : worldList) {

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext()) {

                Entity entity = iterator.next();

                if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) {

                    //scan for naturally/command/plugin spawned EliteMobs
                    if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() != 1
                            && ((Damageable) entity).getMaxHealth() == DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

                        HealthHandler.naturalAgressiveHealthHandler(entity, entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt());
                        customAggressiveName(entity);
                        PowerHandler.powerHandler(entity);
                        ArmorHandler.ArmorHandler(entity);

                    }

                    //scan for stacked EliteMobs
                    if (ConfigValues.defaultConfig.getBoolean("Allow aggressive EliteMobs") &&
                            ConfigValues.defaultConfig.getBoolean("Aggressive mob stacking") &&
                            !entity.hasMetadata(MetadataHandler.FORBIDDEN_MD)) {

                        if (!(entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                                entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= ConfigValues.defaultConfig.getInt("Aggressive mob stacking cap")) &&
                                !entity.hasMetadata(MetadataHandler.FORBIDDEN_MD)) {

                            if(!entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                                    ConfigValues.defaultConfig.getBoolean("Stack aggressive spawner mobs")) {

                                scanValidAggressiveLivingEntity(entity);

                            }

                            if(entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) &&
                                    ConfigValues.defaultConfig.getBoolean("Stack aggressive natural mobs")) {

                                scanValidAggressiveLivingEntity(entity);

                            }

                        }

                        if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

                            PowerHandler.powerHandler(entity);

                            ArmorHandler.ArmorHandler(entity);

                        }

                    }

                }

                //scan for passive mobs
                if (ConfigValues.defaultConfig.getBoolean("Allow Passive EliteMobs")) {

                    //scan for passive mobs that might have lost their metadata
                    if (ValidPassiveMobFilter.ValidPassiveMobFilter(entity)
                            && ((LivingEntity) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)
                            &&((LivingEntity) entity).getMaxHealth() == DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) * ConfigValues.defaultConfig.getInt("Passive EliteMob stack amount")) {

                        customPassiveName(entity, plugin);

                    }

                    //scan for new passive supermobs
                    if (ValidPassiveMobFilter.ValidPassiveMobFilter(entity) && !entity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

                        scanValidPassiveLivingEntity(entity);

                    }

                    //spawn chicken eggs, really wish there were an event for this
                    ChickenHandler.superEggs(entity, passiveStackAmount);

                }

                //scan for iron golems with missing metadata
                if (!entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) && entity instanceof IronGolem &&
                        ((IronGolem) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

                    entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, ScalingFormula.reversePowerFormula(((IronGolem) entity).getMaxHealth(),
                            DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity))));
                    customAggressiveName(entity);

                }

            }

        }

    }

    public void scanValidAggressiveLivingEntity(Entity entity) {

        for (Entity secondEntity : entity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange)) {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !entity.hasMetadata(MetadataHandler.FORBIDDEN_MD) && !secondEntity.hasMetadata(MetadataHandler.FORBIDDEN_MD)) {

                //If the sum of both entities is above level 50, don't add both entities together
                if (levelCap(entity, secondEntity)) {

                    if (!entity.hasMetadata(MetadataHandler.NATURAL_MOB_MD) || !secondEntity.hasMetadata(MetadataHandler.NATURAL_MOB_MD)) {

                        entity.removeMetadata(MetadataHandler.NATURAL_MOB_MD, plugin);

                    }

                    //remove duplicate
                    secondEntity.remove();

                    //setup new EliteMob
                    LevelHandler.LevelHandler(entity, secondEntity, plugin);
                    HealthHandler.aggressiveHealthHandler(entity, secondEntity);
                    customAggressiveName(entity);

                    if (secondEntity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

                        secondEntity.removeMetadata(MetadataHandler.ELITE_MOB_MD, plugin);

                    }

                    return;

                }

            }

        }

    }

    public void scanValidPassiveLivingEntity(Entity entity) {

        int passiveStacking = ConfigValues.defaultConfig.getInt("Passive EliteMob stack amount");

        List<LivingEntity> animalContainer = new ArrayList<>();

        if (((LivingEntity) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

            return;

        }

        for (Entity secondEntity : entity.getNearbyEntities(passiveRange, passiveRange, passiveRange)) {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !secondEntity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD) &&
                    ((LivingEntity) secondEntity).getMaxHealth() == DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

                animalContainer.add((LivingEntity) secondEntity);

                if (animalContainer.size() == passiveStacking && !entity.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

                    for (LivingEntity livingEntity : animalContainer) {

                        livingEntity.remove();

                    }

                    HealthHandler.passiveHealthHandler(entity, passiveStacking);

                    customPassiveName(entity, plugin);

                    return;

                }

            }

        }

    }


    public boolean levelCap(Entity entity1, Entity entity2) {

        Damageable damageable1 = (Damageable) entity1;
        Damageable damageable2 = (Damageable) entity2;

        if (damageable1.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                damageable1.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= ConfigValues.defaultConfig.getInt("Aggressive mob stacking cap")) {

            return false;

        } else if (damageable2.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                damageable2.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= ConfigValues.defaultConfig.getInt("Aggressive mob stacking cap")) {

            return false;

        }

        return true;

    }

}
