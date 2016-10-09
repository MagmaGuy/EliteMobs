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

import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

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


    public void scanMobs(){

        for (World world : worldList)
        {

            for (Entity entity : world.getEntities())
            {

                if(entity instanceof Zombie || entity instanceof Skeleton || entity instanceof PigZombie ||
                        entity instanceof Creeper || entity instanceof Spider || entity instanceof Enderman ||
                        entity instanceof CaveSpider || entity instanceof Silverfish || entity instanceof Blaze ||
                        entity instanceof Witch || entity instanceof Endermite || entity instanceof PolarBear)
                {

                    if (!entity.hasMetadata("removed"))
                    {

                        if (scanValidEntity(entity))
                        {

                            return;

                        }

                    }

                }

            }

        }

    }


    private int range = 2;

    public boolean scanValidEntity(Entity entity){

        for (Entity secondEntity : entity.getNearbyEntities(range, range, range))
        {

            if (entity.getType() == secondEntity.getType() && !entity.hasMetadata("removed"))
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

        return false;

    }


    public static final int zombieHealth = 20;
    public static final int skeletonHealth = 20;
    public static final int pigZombieHealth = 20;
    public static final int creeperHealth = 20;
    public static final int spiderHealth = 16;
    public static final int endermanHealth = 40;
    public static final int caveSpiderHealth = 12;
    public static final int silverfishHealth = 8;
    public static final int blazeHealth = 20;
    public static final int witchHealth = 26;
    public static final int endermiteHealth = 8;
    public static final int polarBearHealth = 30;

    public void customName(Entity entity){

        switch (entity.getType())
        {
            case ZOMBIE:
                int mobLevelZombie = (int) Math.ceil(((Zombie)entity).getHealth() / zombieHealth);
                entity.setCustomName("Level " + mobLevelZombie + " Zombie");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelZombie));
                break;
            case SKELETON:
                int mobLevelSkeleton = (int) Math.ceil(((Skeleton)entity).getHealth() / skeletonHealth);
                entity.setCustomName("Level " + mobLevelSkeleton + " Skeleton");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelSkeleton));
                break;
            case PIG_ZOMBIE:
                int mobLevelPigZombie = (int) Math.ceil(((PigZombie)entity).getHealth() / pigZombieHealth);
                entity.setCustomName("Level " + mobLevelPigZombie + " Zombie Pigman");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelPigZombie));
                break;
            case CREEPER:
                int mobLevelCreeper = (int) Math.ceil(((Creeper)entity).getHealth() / creeperHealth);
                entity.setCustomName("Level " + mobLevelCreeper + " Creeper");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelCreeper));
                break;
            case SPIDER:
                int mobLevelSpider = (int) Math.ceil(((Spider)entity).getHealth() / spiderHealth);
                entity.setCustomName("Level " + mobLevelSpider + " Spider");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelSpider));
                break;
            case ENDERMAN:
                int mobLevelEnderman = (int) Math.ceil(((Enderman)entity).getHealth() / endermanHealth);
                entity.setCustomName("Level " + mobLevelEnderman + " Enderman");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelEnderman));
                break;
            case CAVE_SPIDER:
                int mobLevelCaveSpider = (int) Math.ceil(((CaveSpider)entity).getHealth() / caveSpiderHealth);
                entity.setCustomName("Level " + mobLevelCaveSpider + " Cave Spider");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelCaveSpider));
                break;
            case SILVERFISH:
                int mobLevelSilverfish = (int) Math.ceil(((Silverfish)entity).getHealth() / silverfishHealth);
                entity.setCustomName("Level " + mobLevelSilverfish + " Silverfish");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelSilverfish));
                break;
            case BLAZE:
                int mobLevelBlaze = (int) Math.ceil(((Blaze)entity).getHealth() / blazeHealth);
                entity.setCustomName("Level " + mobLevelBlaze + " Blaze");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelBlaze));
                break;
            case WITCH:
                int mobLevelWitch = (int) Math.ceil(((Witch)entity).getHealth() / witchHealth);
                entity.setCustomName("Level " + mobLevelWitch + " Witch");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelWitch));
                break;
            case ENDERMITE:
                int mobLevelEndermite = (int) Math.ceil(((Endermite)entity).getHealth() / endermiteHealth);
                entity.setCustomName("Level " + mobLevelEndermite + " Endermite");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelEndermite));
                break;
            case POLAR_BEAR:
                int mobLevelPolarBear = (int) Math.ceil(((PolarBear)entity).getHealth() / polarBearHealth);
                entity.setCustomName("Level " + mobLevelPolarBear + " Polar Bear");
                entity.setMetadata("SuperMob", new FixedMetadataValue(plugin, mobLevelPolarBear));
                break;
            default:
                getLogger().info("Error: Couldn't assign custom mob name due to unexpected boss mob (talk to the dev!)");
                getLogger().info("Missing mob type: " + entity.getType());
                break;
        }

    }

    @EventHandler
    public void onSuperMobDamage(EntityDamageEvent event){

        if(event.getEntity().hasMetadata("SuperMob"))
        {

            customName(event.getEntity());

        }

    }

}
