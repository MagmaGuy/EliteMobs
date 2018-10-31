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

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidMobsConfig;
import com.magmaguy.elitemobs.mobconstructor.*;
import com.magmaguy.elitemobs.mobs.passive.ChickenHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.magmaguy.elitemobs.EliteMobs.validWorldList;
import static com.magmaguy.elitemobs.mobconstructor.NameHandler.customAggressiveName;
import static com.magmaguy.elitemobs.mobconstructor.NameHandler.customPassiveName;

/**
 * Created by MagmaGuy on 07/10/2016.
 */
public class MobScanner implements Listener {

    private Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    private int aggressiveRange = ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.AGGRESSIVE_STACK_RANGE);
    private int passiveRange = ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.PASSIVE_STACK_RANGE);

    public void scanMobs() {

        for (World world : validWorldList) {

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext()) {

                LivingEntity entity = iterator.next();

                if (ValidAggressiveMobFilter.checkValidAggressiveMob(entity)) {

                    //scan for stacked EliteMobs
                    if (ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_AGGRESSIVE_ELITEMOBS) &&
                            ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.AGGRESSIVE_MOB_STACKING) &&
                            !entity.hasMetadata(MetadataHandler.CUSTOM_STACK)) {

                        if (!(entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                                entity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.ELITEMOB_STACKING_CAP))) {

                            if (!EntityTracker.isNaturalEntity(entity) &&
                                    ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_SPAWNER_MOBS)) {

                                scanValidAggressiveLivingEntity(entity);

                            }

                            if (EntityTracker.isNaturalEntity(entity) &&
                                    ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.STACK_AGGRESSIVE_NATURAL_MOBS)) {

                                scanValidAggressiveLivingEntity(entity);

                            }

                        }

                    }

                    if (entity.hasMetadata(MetadataHandler.ELITE_MOB_MD)) {

                        PowerHandler.powerHandler(entity);

                        ArmorHandler.ArmorHandler(entity);

                    }

                }

                //scan for passive mobs
                if (ValidPassiveMobFilter.ValidPassiveMobFilter(entity) && ConfigValues.validMobsConfig.getBoolean(ValidMobsConfig.ALLOW_PASSIVE_SUPERMOBS)) {

                    //scan for passive mobs that might have lost their metadata
                    if (entity.getMaxHealth() == DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) * ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT)) {

                        customPassiveName(entity);

                    }

                    //scan for new passive supermobs
                    if (!EntityTracker.isPassiveMob(entity)) {

                        scanValidPassiveLivingEntity(entity);

                    }

                    //spawn chicken eggs, really wish there were an event for this
                    if (EntityTracker.isPassiveMob(entity) && entity instanceof Chicken) {

                        if (!ChickenHandler.activeChickenList.contains(entity)) {

                            ChickenHandler.activeChickenList.add((Chicken) entity);

                        }

                    }

                }

                //scan for iron golems with missing metadata
                if (entity instanceof IronGolem && !entity.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                        (entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)) {

                    MetadataHandler.registerMetadata(entity, MetadataHandler.ELITE_MOB_MD, HealthHandler.reversePowerFormula(entity.getMaxHealth(),
                            DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity)));
                    customAggressiveName(entity);

                }

            }

        }

    }

    public void scanValidAggressiveLivingEntity(LivingEntity entity) {

        for (Entity secondEntity : entity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange)) {

            if (!(secondEntity instanceof LivingEntity)) continue;

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !entity.hasMetadata(MetadataHandler.CUSTOM_STACK) && !secondEntity.hasMetadata(MetadataHandler.CUSTOM_STACK)) {

                if (levelCap(entity, secondEntity)) {

                    if (!EntityTracker.isNaturalEntity(entity) || !EntityTracker.isNaturalEntity(secondEntity))
                        EntityTracker.isNaturalEntity(entity);

                    //setup new EliteMob
                    LevelHandler.LevelHandler(entity, secondEntity, plugin);
                    HealthHandler.aggressiveHealthHandler(entity, secondEntity);
                    customAggressiveName(entity);

                    //remove duplicate
                    secondEntity.remove();
                    MetadataHandler.fullMetadataFlush(secondEntity);

                    return;

                }

            }

        }

    }

    public void scanValidPassiveLivingEntity(Entity entity) {

        int passiveStacking = ConfigValues.defaultConfig.getInt(DefaultConfig.SUPERMOB_STACK_AMOUNT);

        List<LivingEntity> animalContainer = new ArrayList<>();

        if (((LivingEntity) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser((LivingEntity) entity))
            return;

        for (Entity secondEntity : entity.getNearbyEntities(passiveRange, passiveRange, passiveRange))
            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !EntityTracker.isPassiveMob(secondEntity) &&
                    ((LivingEntity) secondEntity).getMaxHealth() == DefaultMaxHealthGuesser.defaultMaxHealthGuesser((LivingEntity) entity)) {

                animalContainer.add((LivingEntity) secondEntity);

                if (animalContainer.size() == passiveStacking && !EntityTracker.isPassiveMob(entity)) {

                    for (LivingEntity livingEntity : animalContainer) {

                        livingEntity.remove();
                        MetadataHandler.fullMetadataFlush(livingEntity);

                    }

                    HealthHandler.passiveHealthHandler(entity, passiveStacking);
                    customPassiveName(entity);
                    return;

                }

            }

    }


    public boolean levelCap(Entity entity1, Entity entity2) {

        Damageable damageable1 = (Damageable) entity1;
        Damageable damageable2 = (Damageable) entity2;

        if (damageable1.hasMetadata(MetadataHandler.ELITE_MOB_MD) &&
                damageable1.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() >= ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.ELITEMOB_STACKING_CAP))
            return false;

        else return !damageable2.hasMetadata(MetadataHandler.ELITE_MOB_MD) ||
                damageable2.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt() < ConfigValues.mobCombatSettingsConfig.getInt(MobCombatSettingsConfig.ELITEMOB_STACKING_CAP);

    }

}
