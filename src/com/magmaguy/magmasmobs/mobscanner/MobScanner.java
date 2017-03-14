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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
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
public class MobScanner implements Listener {

    private MagmasMobs plugin;

    public MobScanner(Plugin plugin) {

        this.plugin = (MagmasMobs) plugin;

    }

    public static final int maxSuperMobLevel = 50;

    public void scanMobs() {

        for (World world : worldList) {

            Iterator<LivingEntity> iterator = world.getLivingEntities().iterator();

            while (iterator.hasNext()) {

                Entity entity = iterator.next();

                if (ValidAgressiveMobFilter.ValidAgressiveMobFilter(entity)) {

                    scanValidAggressiveLivingEntity(entity);

                    if (entity.hasMetadata("MagmasSuperMob")) {

                        PowerHandler powerHandler = new PowerHandler(plugin);

                        powerHandler.powerHandler(entity);

                        armorHandler(entity);

                    }

                }

                if (ValidPassiveMobFilter.ValidPassiveMobFilter(entity) && !entity.hasMetadata("MagmasPassiveSupermob")) {

                    scanValidPassiveLivingEntity(entity);

                }

                //spawn chicken eggs, really wish there were an event for this
                ChickenHandler.superEggs(entity);

            }

        }

    }


    private int aggressiveRange = 2;

    public void scanValidAggressiveLivingEntity(Entity entity) {

        for (Entity secondEntity : entity.getNearbyEntities(aggressiveRange, aggressiveRange, aggressiveRange)) {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !entity.hasMetadata("forbidden") && !secondEntity.hasMetadata("forbidden")) {

                //If the sum of both entities is above level 50, don't add both entities together
                if (levelCap(entity, secondEntity)) {
                    //remove duplicate
                    secondEntity.remove();
                    if (secondEntity.hasMetadata("MagmasSuperMob")) {

                        secondEntity.removeMetadata("MagmasSuperMob", plugin);

                    }
                    //secondEntity.setMetadata("removed", new FixedMetadataValue(plugin, true)); TODO:Check if this still works

                    //setup new MagmasSuperMob
                    double newMaxHealth = ((Damageable) entity).getMaxHealth() + ((Damageable) secondEntity).getMaxHealth();
                    ((Damageable) entity).setMaxHealth(newMaxHealth);

                    double newHealth = ((Damageable) entity).getHealth() + ((Damageable) secondEntity).getHealth();
                    ((Damageable) entity).setHealth(((Damageable) entity).getMaxHealth());

                    customAggressiveName(entity);

                    return;

                }

            }

        }

        if (((Damageable) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) &&
                !entity.hasMetadata("MagmasSuperMob")) {

            customAggressiveName(entity);

        }

    }

    private int passiveRange = 15;
    private int passiveStacking = 50;

    public void scanValidPassiveLivingEntity(Entity entity) {

        List<LivingEntity> animalContainer = new ArrayList<>();

        for (Entity secondEntity : entity.getNearbyEntities(passiveRange, passiveRange, passiveRange)) {

            if (entity.getType() == secondEntity.getType() && entity.isValid() && secondEntity.isValid()
                    && !secondEntity.hasMetadata("MagmasPassiveSupermob")) {

                animalContainer.add((LivingEntity) secondEntity);

                if (animalContainer.size() == passiveStacking && !entity.hasMetadata("MagmasPassiveSupermob")) {

                    Iterator<LivingEntity> animalSlaughterer = animalContainer.iterator();

                    boolean firstAnimal = true;

                    for (LivingEntity livingEntity : animalContainer) {

                        livingEntity.remove();

                    }

                    ((LivingEntity) entity).setMaxHealth(((LivingEntity) entity).getMaxHealth() * passiveStacking);
                    ((LivingEntity) entity).setHealth(((LivingEntity) entity).getMaxHealth());
                    customPassiveName(entity);

                    return;

                }

            }

        }

        if (((Damageable) entity).getMaxHealth() != DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity) &&
                !entity.hasMetadata("MagmasPassiveSupermob")) {

            customPassiveName(entity);

        }

    }


    public void customAggressiveName(Entity entity) {

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


    public void customPassiveName(Entity entity) {

        switch (entity.getType()) {

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
    public void onSuperMobDamage(EntityDamageEvent event) {

        if (event.getEntity().hasMetadata("MagmasSuperMob")) {

            customAggressiveName(event.getEntity());

        }

    }


    public boolean levelCap(Entity entity1, Entity entity2) {

        Damageable damageable1 = (Damageable) entity1;
        Damageable damageable2 = (Damageable) entity2;

        double defaultMaxHealth = DefaultMaxHealthGuesser.defaultMaxHealthGuesser(entity1);

        if (damageable1.getMaxHealth() + damageable2.getMaxHealth() > defaultMaxHealth * maxSuperMobLevel) {

            return false;

        }

        return true;

    }


    public void armorHandler(Entity entity) {

        if (entity instanceof Zombie || entity instanceof ZombieVillager || entity instanceof PigZombie ||
                entity instanceof Skeleton || entity instanceof WitherSkeleton) {

            int mobLevel = entity.getMetadata("MagmasSuperMob").get(0).asInt();

            if (mobLevel >= 2) {

                ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

            }

            if (mobLevel >= 4) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

            }

            if (mobLevel >= 6) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

            }

            if (mobLevel >= 8) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

            }

            if (mobLevel >= 12) {

                ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET));

            }

            if (mobLevel >= 14) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS));

            }

            if (mobLevel >= 16) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));

            }

            if (mobLevel >= 18) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));

            }

            if (mobLevel >= 22) {

                ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));

            }

            if (mobLevel >= 24) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

            }

            if (mobLevel >= 26) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

            }

            if (mobLevel >= 28) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

            }

            if (mobLevel >= 32) {

                ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

            }

            if (mobLevel >= 34) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

            }

            if (mobLevel >= 36) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

            }

            if (mobLevel >= 38) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

            }

            if (mobLevel >= 42) {

                ((LivingEntity) entity).getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

            }

            if (mobLevel >= 44) {

                ((LivingEntity) entity).getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));

            }

            if (mobLevel >= 46) {

                ((LivingEntity) entity).getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

            }

            if (mobLevel >= 48) {

                ((LivingEntity) entity).getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

            }

        }

    }

}
