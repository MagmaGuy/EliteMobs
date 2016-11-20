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

package com.magmaguy.activestack;

import com.magmaguy.activestack.MinorPowers.*;
import com.magmaguy.activestack.PowerStances.ParticleEffects;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static com.magmaguy.activestack.ActiveStack.worldList;
import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 07/10/2016.
 */
public class MobScanner implements Listener{

    private ActiveStack plugin;

    public MobScanner(Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }

    public static final int maxSuperMobLevel = 50;


    public void scanMobs(){

        for (World world : worldList)
        {

            for (Entity entity : world.getEntities())
            {

                ValidMobFilter validMobFilter = new ValidMobFilter();

                if (validMobFilter.ValidMobFilter(entity))
                {

                    if (!entity.hasMetadata("removed"))
                    {

                        if (scanValidEntity(entity))
                        {

                            return;

                        }

                    }

                    if(entity.hasMetadata("SuperMob"))
                    {

                        powerHandler(entity);

                    }

                }

            }

        }

    }


    private int range = 2;

    public boolean scanValidEntity(Entity entity){

        for (Entity secondEntity : entity.getNearbyEntities(range, range, range))
        {

            if (entity.getType() == secondEntity.getType() && !entity.hasMetadata("removed")
                    && !entity.hasMetadata("forbidden") && !secondEntity.hasMetadata("forbidden"))
            {

                //If the sum of both entities is above level 50, don't add both entities together
                if (levelCap(entity, secondEntity))
                {

                    //remove duplicate
                    secondEntity.remove();
                    secondEntity.setMetadata("removed", new FixedMetadataValue(plugin, true));

                    //setup new supermob
                    double newHealth = ((Damageable) entity).getHealth() + ((Damageable) secondEntity).getHealth();

                    ((Damageable) entity).setMaxHealth(newHealth);
                    ((Damageable) entity).setHealth(((Damageable) entity).getMaxHealth());

                    customName(entity);
                    entity.setCustomNameVisible(true);

                    return true;

                }

            }

        }

        return false;

    }


    public void customName(Entity entity){

        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity);

        int mobLevel = (int) Math.ceil(((Damageable) entity).getMaxHealth() / defaultMaxHealth);

        switch (entity.getType()) {
            case ZOMBIE:
                entity.setCustomName("Level " + mobLevel + " Zombie");
                break;
            case ZOMBIE_VILLAGER:
                entity.setCustomName("Level " + mobLevel + " Zombie Villager");
                break;
            case SKELETON:
                entity.setCustomName("Level " + mobLevel + " Skeleton");
                break;
            case PIG_ZOMBIE:
                entity.setCustomName("Level " + mobLevel + " Zombie Pigman");
                break;
            case CREEPER:
                entity.setCustomName("Level " + mobLevel + " Creeper");
                break;
            case SPIDER:
                entity.setCustomName("Level " + mobLevel + " Spider");
                break;
            case ENDERMAN:
                entity.setCustomName("Level " + mobLevel + " Enderman");
                break;
            case CAVE_SPIDER:
                entity.setCustomName("Level " + mobLevel + " Cave Spider");
                break;
            case SILVERFISH:
                entity.setCustomName("Level " + mobLevel + " Silverfish");
                break;
            case BLAZE:
                entity.setCustomName("Level " + mobLevel + " Blaze");
                break;
            case WITCH:
                entity.setCustomName("Level " + mobLevel + " Witch");
                break;
            case ENDERMITE:
                entity.setCustomName("Level " + mobLevel + " Endermite");
                break;
            case POLAR_BEAR:
                entity.setCustomName("Level " + mobLevel + " Polar Bear");
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevel));


    }

    @EventHandler
    public void onSuperMobDamage(EntityDamageEvent event){

        if(event.getEntity().hasMetadata("SuperMob"))
        {

            customName(event.getEntity());

        }

    }


    public boolean levelCap(Entity entity1, Entity entity2){

        Damageable damageable1 = (Damageable) entity1;
        Damageable damageable2 = (Damageable) entity2;

        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity1);

        if (damageable1.getMaxHealth() + damageable2.getMaxHealth() > defaultMaxHealth * maxSuperMobLevel)
        {

            return false;

        }

        return true;

    }


    public void powerHandler(Entity entity)
    {

        int availableMinorPowers = 0;
        int availableMajorPowers = 0;

        if (entity.hasMetadata("SuperMob"))
        {

            if (entity.getMetadata("SuperMob").get(0).asInt() >= 5)
            {

            int superMobLevel = entity.getMetadata("SuperMob").get(0).asInt();

            availableMinorPowers = (superMobLevel - 5) / 10 + 1;
            availableMajorPowers = superMobLevel / 10;

            }

        }

        if (availableMinorPowers >= 1)
        {

            int currentMinorPowerAmount = 0;

            ArrayList <MinorPowers> minorPowerArray = new ArrayList();
            minorPowerArray.add(new InvulnerabilityFire(plugin));
            minorPowerArray.add(new InvulnerabilityArrow(plugin));
            minorPowerArray.add(new AttackGravity(plugin));
            minorPowerArray.add(new AttackPush(plugin));
            minorPowerArray.add(new MovementSpeed(plugin));

            if (entity.hasMetadata("MinorPowerAmount"))
            {

                currentMinorPowerAmount = entity.getMetadata("MinorPowerAmount").get(0).asInt();

                Iterator<MinorPowers> iterator = minorPowerArray.iterator();

                while(iterator.hasNext())
                {

                    MinorPowers minorPower = iterator.next();

                    if (minorPower.existingPowers(entity))
                    {

                        iterator.remove();

                    }

                }

            }

            int missingMinorPowerAmount = availableMinorPowers - currentMinorPowerAmount;

            if (missingMinorPowerAmount > 0 && minorPowerArray.size() > 0)
            {

                for (int i = 0; i < missingMinorPowerAmount; i++)
                {

                    if (minorPowerArray.size() > 0)
                    {

                        Random random = new Random();
                        int randomizer = random.nextInt(minorPowerArray.size());
                        MinorPowers selectedMinorPower = minorPowerArray.get(randomizer);
                        minorPowerArray.remove(minorPowerArray.get(randomizer));
                        //weird shit's going on here, read debug code

                        getLogger().info("added " + selectedMinorPower);

                        if (entity.hasMetadata("MinorPowerAmount"))
                        {

                            int oldMinorPowerAmount = entity.getMetadata("MinorPowerAmount").get(0).asInt();
                            int newMinorPowerAmount = oldMinorPowerAmount + 1;

                            entity.setMetadata("MinorPowerAmount", new FixedMetadataValue(plugin, newMinorPowerAmount));

                        } else {

                            entity.setMetadata("MinorPowerAmount", new FixedMetadataValue(plugin, 1));

                        }

                        selectedMinorPower.applyPowers(entity);

                    }

                }

            }

        }

        if (availableMajorPowers >= 1)
        {

            //Todo: Add major powers

        }

    }

}
