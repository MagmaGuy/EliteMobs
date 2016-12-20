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

package com.magmaguy.magmasmobs.mobscanner;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.mobs.passive.ChickenHandler;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.magmaguy.magmasmobs.MagmasMobs.worldList;
import static org.bukkit.Bukkit.getLogger;

/**
 * Created by MagmaGuy on 07/10/2016.
 */
public class MobScanner implements Listener{

    private MagmasMobs plugin;

    public MobScanner(Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }

    public static final int maxSuperMobLevel = 50;

    public void scanMobs(){

        for (World world : worldList)
        {

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext())
            {

                Entity entity = iterator.next();

                if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity))
                {


                    if (scanValidAgressiveLivingEntity(entity))
                    {

                        //return;

                    }

                    if(entity.hasMetadata("MagmasSuperMob"))
                    {

                        PowerHandler powerHandler = new PowerHandler(plugin);

                        powerHandler.powerHandler(entity);

                    }

                }

                if (ValidPassiveMobFilter.ValidPassiveMobFilter(entity) && !entity.hasMetadata("MagmasPassiveSupermob"))
                {

                    if (scanValidPassiveLivingEntity(entity))
                    {

                        //return;

                    }

                }

                //spawn chicken eggs, really wish there were an event for this
                ChickenHandler.superEggs(entity);

            }

        }

    }


    private int aggressiveRange = 2;

    public boolean scanValidAgressiveLivingEntity(Entity entity){

        for (Entity secondEntity : entity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange))
        {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !entity.hasMetadata("forbidden") && !secondEntity.hasMetadata("forbidden"))
            {

                //If the sum of both entities is above level 50, don't add both entities together
                if (levelCap(entity, secondEntity))
                {
                    //remove duplicate
                    secondEntity.remove();
                    if (secondEntity.hasMetadata("MagmasSuperMob"))
                    {

                        secondEntity.removeMetadata("MagmasSuperMob", plugin);

                    }
                    //secondEntity.setMetadata("removed", new FixedMetadataValue(plugin, true)); TODO:Check if this still works

                    //setup new MagmasSuperMob
                    double newMaxHealth = ((Damageable) entity).getMaxHealth() + ((Damageable) secondEntity).getMaxHealth();
                    ((Damageable) entity).setMaxHealth(newMaxHealth);

                    double newHealth = ((Damageable) entity).getHealth() + ((Damageable) secondEntity).getHealth();
                    ((Damageable) entity).setHealth(((Damageable) entity).getMaxHealth());

                    customAggressiveName(entity);

                    return true;

                }

            }

        }

        if (((Damageable)entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) &&
                !entity.hasMetadata("MagmasSuperMob"))
        {

            customAggressiveName(entity);

        }

        return false;

    }

    private int passiveRange = 15;
    private int passiveStacking = 50;

    public boolean scanValidPassiveLivingEntity(Entity entity){

        List<LivingEntity> animalContainer = new ArrayList<>();

        for (Entity secondEntity : entity.getNearbyEntities(passiveRange, passiveRange, passiveRange))
        {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !secondEntity.hasMetadata("MagmasPassiveSupermob"))
            {

                animalContainer.add((LivingEntity) secondEntity);

                if (animalContainer.size() == passiveStacking && !entity.hasMetadata("MagmasPassiveSupermob"))
                {

                    Iterator<LivingEntity> animalSlaughterer = animalContainer.iterator();

                    boolean firstAnimal = true;

                    for (LivingEntity livingEntity: animalContainer)
                    {

                        livingEntity.remove();

                    }

                    ((LivingEntity)entity).setMaxHealth(((LivingEntity)entity).getMaxHealth() * passiveStacking);
                    ((LivingEntity)entity).setHealth(((LivingEntity)entity).getMaxHealth());
                    customPassiveName(entity);

                    return true;

                }

            }

        }

        if (((Damageable)entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) &&
                !entity.hasMetadata("MagmasPassiveSupermob"))
        {

            customPassiveName(entity);

        }

        return false;

    }


    public void customAggressiveName(Entity entity){

        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity);

        int mobLevel = (int) Math.ceil(((Damageable) entity).getMaxHealth() / defaultMaxHealth);

        switch (entity.getType()) {
            case ZOMBIE:
                entity.setCustomName("Level " + mobLevel + " Zombie");
                break;
            case HUSK:
                entity.setCustomName("Level " + mobLevel + " Husk");
                break;
            case ZOMBIE_VILLAGER:
                entity.setCustomName("Level " + mobLevel + " Zombie Villager");
                break;
            case SKELETON:
                entity.setCustomName("Level " + mobLevel + " Skeleton");
                break;
            case WITHER_SKELETON:
                entity.setCustomName("Level " + mobLevel + " Wither Skeleton");
                break;
            case STRAY:
                entity.setCustomName("Level " + mobLevel + " Stray");
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
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected aggressive boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setCustomNameVisible(true);

        entity.setMetadata("MagmasSuperMob", new FixedMetadataValue(plugin, mobLevel));

    }


    public void customPassiveName(Entity entity)
    {

        switch (entity.getType())
        {

            case CHICKEN:
                entity.setCustomName("Super Chicken");
                break;
            case COW:
                entity.setCustomName("Super Cow");
                break;
            case IRON_GOLEM:
                entity.setCustomName("Super Iron Golem");
                break;
            case MUSHROOM_COW:
                entity.setCustomName("Super Mooshroom");
                break;
            case PIG:
                entity.setCustomName("Super Pig");
                break;
            case SHEEP:
                entity.setCustomName("Super Sheep");
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected passive boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

        entity.setCustomNameVisible(true);

        entity.setMetadata("MagmasPassiveSupermob", new FixedMetadataValue(plugin, true));

    }

    @EventHandler
    public void onSuperMobDamage(EntityDamageEvent event)
    {

        if(event.getEntity().hasMetadata("MagmasSuperMob"))
        {

            customAggressiveName(event.getEntity());

        }

    }


    public boolean levelCap(Entity entity1, Entity entity2)
    {

        Damageable damageable1 = (Damageable) entity1;
        Damageable damageable2 = (Damageable) entity2;

        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity1);

        if (damageable1.getMaxHealth() + damageable2.getMaxHealth() > defaultMaxHealth * maxSuperMobLevel)
        {

            return false;

        }

        return true;

    }

}
