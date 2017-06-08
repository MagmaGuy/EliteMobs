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
import com.magmaguy.elitemobs.elitedrops.ItemRankHandler;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalMobSpawner implements Listener {

    private EliteMobs plugin;
    private int range = 100;


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

                if (threathLevel == 0) {

                    return;

                }

                //Just set up the metadata, scanner will pick it up and apply the correct stats
                if (amountOfPlayersTogether == 1) {

                    entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, EliteMobLevel));

                } else if (amountOfPlayersTogether > 1 && random.nextDouble() < 20) {

                    entity.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, EliteMobLevel));

                }

            }

        }

    }


    private int armorRatingHandler(Player player, int armorRating) {

        if (player.getEquipment().getHelmet() != null) {

            ItemStack helmet = player.getEquipment().getHelmet();

            armorRating += itemRankCalculator(helmet);

        }

        if (player.getEquipment().getChestplate() != null) {

            ItemStack chestplate = player.getEquipment().getChestplate();

            armorRating += itemRankCalculator(chestplate);

        }

        if (player.getEquipment().getLeggings() != null) {

            ItemStack leggings = player.getEquipment().getLeggings();

            armorRating += itemRankCalculator(leggings);

        }

        if (player.getEquipment().getBoots() != null) {

            ItemStack boots = player.getEquipment().getBoots();

            armorRating += itemRankCalculator(boots);

        }

//        if (player.getEquipment().getItemInMainHand() != null || !player.getEquipment().getItemInOffHand().getType().equals(Material.AIR)) {
//
//            ItemStack mainHand = player.getEquipment().getItemInMainHand();
//
//            armorRating += itemRankCalculator(mainHand);
//
//        }
//
//        if (player.getEquipment().getItemInOffHand() != null || !player.getEquipment().getItemInOffHand().getType().equals(Material.AIR)) {
//
//            ItemStack offHand = player.getEquipment().getItemInOffHand();
//
//            armorRating += itemRankCalculator(offHand);
//
//        }


        return armorRating;

    }

    private int itemRankCalculator (ItemStack itemStack) {

        Material material = itemStack.getType();
        ItemMeta itemMeta = itemStack.getItemMeta();

        int enchantmentCount = 0;

        if (!itemMeta.getEnchants().isEmpty()) {

            for (Enchantment enchantment : itemMeta.getEnchants().keySet()) {

                enchantmentCount += itemMeta.getEnchantLevel(enchantment);

            }

        }

        int rankLevel = ItemRankHandler.guessItemRank(material, enchantmentCount);

        return rankLevel;

    }


    private int EliteMobRating(Player player, int EliteMobRating, double range) {

        for (Entity nearPlayer : player.getNearbyEntities(range, range, range)) {

            if (nearPlayer.hasMetadata(MetadataHandler.PASSIVE_ELITE_MOB_MD)) {

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
