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

package com.magmaguy.magmasmobs.mobspawner;

import com.magmaguy.magmasmobs.MagmasMobs;
import com.magmaguy.magmasmobs.mobscanner.ValidAgressiveMobFilter;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

import static org.bukkit.Material.*;
import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class MobSpawner implements Listener{

    private MagmasMobs plugin;

    public MobSpawner(Plugin plugin){

        this.plugin = (MagmasMobs) plugin;

    }


    private int range = 50;

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event){

        if (event.getSpawnReason() == NATURAL)
        {

            Entity entity = event.getEntity();

            ValidAgressiveMobFilter validAgressiveMobFilter = new ValidAgressiveMobFilter();

            if(validAgressiveMobFilter.ValidAgressiveMobFilter(entity))
            {

                Random random = new Random();

                //20% chance of turning a mob into a supermob
                if (random.nextDouble() < 0.20)
                {

                    entity.setMetadata("NaturalEntity", new FixedMetadataValue(plugin, true));

                    List<Entity> scanEntity = entity.getNearbyEntities(range, range, range);

                    int amountOfPlayersTogether = 0;

                    for (Entity scannedEntity : scanEntity)
                    {

                        amountOfPlayersTogether++;

                        if (scannedEntity instanceof Player
                                && scannedEntity.hasMetadata("vanished")
                                && !scannedEntity.getMetadata("vanished").get(0).asBoolean()) //vanishnopacket support
                        {

                            Player player = (Player) scannedEntity;
                            int armorRating = 0;

                            armorRating = armorRatingHandler(player, armorRating);

                            int potionEffectRating = player.getActivePotionEffects().size();

                            int supermobRating = 0;
                            supermobRating = supermobRating(player, supermobRating,  range);

                            int threathLevel = 0;
                            threathLevel = threatLevelCalculator(armorRating, potionEffectRating, supermobRating);

                            int supermobLevel = levelCalculator(threathLevel);

                            Damageable damageableMob = (Damageable) entity;

                            if (threathLevel == 0)
                            {

                                return;

                            }

                            if (amountOfPlayersTogether == 1)
                            {

                                damageableMob.setMaxHealth(damageableMob.getMaxHealth() * supermobLevel);
                                damageableMob.setHealth(damageableMob.getMaxHealth());

                            } else if (amountOfPlayersTogether > 1 && random.nextDouble() < 20) {

                                damageableMob.setMaxHealth(damageableMob.getMaxHealth() * supermobLevel);
                                damageableMob.setHealth(damageableMob.getMaxHealth());

                            }

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

                if (supermobRating > 10)
                {

                    return supermobRating;

                }

            }

        }

        return supermobRating;

    }


    private int threatLevelCalculator (int armorRating, int potionEffectRating, int supermobRating) {

        int threatLevel = armorRating / 2 + potionEffectRating + supermobRating;

        return threatLevel;

    }


    private int levelCalculator (int threatLevel){

        return threatLevel;

    }

}
