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

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Material.*;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class MobSpawner implements Listener{

    private ActiveStack plugin;

    public MobSpawner(Plugin plugin){

        this.plugin = (ActiveStack) plugin;

    }


    private int range = 50;

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event){

        if (event.getSpawnReason() == NATURAL)
        {

            Entity entity = event.getEntity();

            ValidMobFilter validMobFilter = new ValidMobFilter();

            if(validMobFilter.ValidMobFilter(entity))
            {

                Random random = new Random();

                //20% chance of turning a mob into a supermob
                if (random.nextDouble() < 20)
                {

                    List<Entity> scanEntity = entity.getNearbyEntities(range, range, range);

                    for (Entity scannedEntity : scanEntity)
                    {

                        if (scannedEntity instanceof Player)
                        {

                            Player player = (Player) scannedEntity;
                            int armorRating = 0;

                            armorRating = armorRatingHandler(player, armorRating);

                            float playerXP = player.getExp();
                            int xpRating = (int) playerXP;

                            int potionEffectRating = player.getActivePotionEffects().size();

                            int supermobRating = 0;
                            supermobRating = supermobRating(player, supermobRating,  range);

                            int threathLevel = 0;
                            threathLevel = threatLevelCalculator(armorRating, xpRating, potionEffectRating, supermobRating);

                            int supermobLevel = levelCalculator(threathLevel);

                            Damageable damageableMob = (Damageable) entity;

                            damageableMob.setMaxHealth(damageableMob.getMaxHealth() * supermobLevel);
                            damageableMob.setHealth(damageableMob.getMaxHealth());

                        }

                    }

                }

            }

        }

    }


    private int armorRatingHandler(Player player, int armorRating){

        armorRating = 0;

        if (player.getEquipment().getHelmet() != null)
        {

            Material helmetMaterial = player.getEquipment().getHelmet().getType();

            if (helmetMaterial == LEATHER_HELMET)
            {

                armorRating = armorRating + 1;

            } else if (helmetMaterial == GOLD_HELMET) {

                armorRating = armorRating + 2;

            } else if (helmetMaterial == CHAINMAIL_HELMET) {

                armorRating = armorRating + 2;

            } else if (helmetMaterial == IRON_HELMET) {

                armorRating = armorRating + 2;

            } else if (helmetMaterial == DIAMOND_HELMET) {

                armorRating = armorRating + 3;

            }

        }

        if (player.getEquipment().getChestplate() != null)
        {

            Material chestplateMaterial = player.getEquipment().getChestplate().getType();

            if (chestplateMaterial == LEATHER_CHESTPLATE) {

                armorRating = armorRating + 3;

            } else if (chestplateMaterial == GOLD_CHESTPLATE) {

                armorRating = armorRating + 5;

            } else if (chestplateMaterial == CHAINMAIL_CHESTPLATE) {

                armorRating = armorRating + 5;

            } else if (chestplateMaterial == IRON_CHESTPLATE) {

                armorRating = armorRating + 6;

            } else if (chestplateMaterial == DIAMOND_CHESTPLATE) {

                armorRating = armorRating + 8;

            }

        }

        if (player.getEquipment().getLeggings() != null)
        {

            Material leggingsMaterial = player.getEquipment().getLeggings().getType();

            if (leggingsMaterial == LEATHER_LEGGINGS) {

                armorRating = armorRating + 2;

            } else if (leggingsMaterial == GOLD_LEGGINGS) {

                armorRating = armorRating + 3;

            } else if (leggingsMaterial == CHAINMAIL_LEGGINGS) {

                armorRating = armorRating + 4;

            } else if (leggingsMaterial == IRON_LEGGINGS) {

                armorRating = armorRating + 5;

            } else if (leggingsMaterial == DIAMOND_LEGGINGS) {

                armorRating = armorRating + 6;

            }

        }

        if (player.getEquipment().getBoots() != null)
        {

            Material bootsMaterial = player.getEquipment().getBoots().getType();

            if (bootsMaterial == LEATHER_BOOTS) {

                armorRating = armorRating + 1;

            } else if (bootsMaterial == GOLD_BOOTS) {

                armorRating = armorRating + 1;

            } else if (bootsMaterial == CHAINMAIL_BOOTS) {

                armorRating = armorRating + 1;

            } else if (bootsMaterial == IRON_BOOTS) {

                armorRating = armorRating + 2;

            } else if (bootsMaterial == DIAMOND_BOOTS) {

                armorRating = armorRating + 3;

            }

        }

        return armorRating;

    }


    private int supermobRating(Player player, int supermobRating, double range){

        for (Entity nearPlayer : player.getNearbyEntities(range, range, range))
        {

            if (nearPlayer.hasMetadata("SuperChicken") ||
                    nearPlayer.hasMetadata("SuperCow") ||
                    nearPlayer.hasMetadata("SuperIronGolem") ||
                    nearPlayer.hasMetadata("SuperMushroomCow") ||
                    nearPlayer.hasMetadata("SuperPig") ||
                    nearPlayer.hasMetadata("SuperSheep"))
            {

                supermobRating++;

            }

        }

        return supermobRating;

    }


    private int threatLevelCalculator (int armorRating, int xpRating, int potionEffectRating, int supermobRating) {

        int threatLevel = armorRating + xpRating + (potionEffectRating * 5) + (supermobRating * 5);

        return threatLevel;

    }


    private int levelCalculator (int threatLevel){

        Random random = new Random();

        double supermobLevelPercent = 0.01 + (0.2 - 0.01) * random.nextDouble();

        int supermobLevel = (int) (supermobLevelPercent * threatLevel) + threatLevel;

        if (supermobLevel < 1)
        {

            return 1;

        }

        return supermobLevel;

    }

}
