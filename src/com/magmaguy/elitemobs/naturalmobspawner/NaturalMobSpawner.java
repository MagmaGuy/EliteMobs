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

package com.magmaguy.elitemobs.naturalmobspawner;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

import static org.bukkit.Material.*;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalMobSpawner implements Listener {

    private EliteMobs plugin;
    private int range = 50;


    public NaturalMobSpawner(Plugin plugin) {

        this.plugin = (EliteMobs) plugin;

    }

    public void naturalMobProcessor (Entity entity) {

        Random random = new Random();

        List<Entity> scanEntity = entity.getNearbyEntities(range, range, range);

        int amountOfPlayersTogether = 0;

        for (Entity scannedEntity : scanEntity) {

            amountOfPlayersTogether++;

            if (scannedEntity instanceof Player) //vanishnopacket support
            {

                Player player = (Player) scannedEntity;
                int armorRating = 0;

                armorRating = armorRatingHandler(player, armorRating);

                int potionEffectRating = player.getActivePotionEffects().size();

                int EliteMobRating = 0;
                EliteMobRating = EliteMobRating(player, EliteMobRating, range);

                int threathLevel = 0;
                threathLevel = threatLevelCalculator(armorRating, potionEffectRating, EliteMobRating);

                int EliteMobLevel = levelCalculator(threathLevel);

                Damageable damageableMob = (Damageable) entity;

                if (threathLevel == 0) {

                    return;

                }

                MetadataHandler metadataHandler = new MetadataHandler(plugin);

                //Just set up the metadata, scanner will pick it up and apply the correct stats
                if (amountOfPlayersTogether == 1) {

                    entity.setMetadata(metadataHandler.eliteMobMD, new FixedMetadataValue(plugin, EliteMobLevel));

                } else if (amountOfPlayersTogether > 1 && random.nextDouble() < 20) {

                    entity.setMetadata(metadataHandler.eliteMobMD, new FixedMetadataValue(plugin, EliteMobLevel));

                }

            }

        }

    }


    private int armorRatingHandler(Player player, int armorRating) {

        armorRating = 0;

        if (player.getEquipment().getHelmet() != null) {

            Material helmetMaterial = player.getEquipment().getHelmet().getType();

            if (helmetMaterial == LEATHER_HELMET) {

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

        if (player.getEquipment().getChestplate() != null) {

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

        if (player.getEquipment().getLeggings() != null) {

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

        if (player.getEquipment().getBoots() != null) {

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


    private int EliteMobRating(Player player, int EliteMobRating, double range) {

        for (Entity nearPlayer : player.getNearbyEntities(range, range, range)) {

            MetadataHandler metadataHandler = new MetadataHandler(plugin);

            if (nearPlayer.hasMetadata(metadataHandler.passiveEliteMobMD)) {

                EliteMobRating++;

                if (EliteMobRating > 10) {

                    return EliteMobRating;

                }

            }

        }

        return EliteMobRating;

    }


    private int threatLevelCalculator(int armorRating, int potionEffectRating, int EliteMobRating) {

        int threatLevel = armorRating / 2 + potionEffectRating + EliteMobRating;

        return threatLevel;

    }


    private int levelCalculator(int threatLevel) {

        return threatLevel;

    }

}
