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

package com.magmaguy.elitemobs.elitedrops;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.magmaguy.elitemobs.elitedrops.EliteDropsHandler.potionEffectItemList;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PotionEffectApplier {

    public void potionEffectApplier() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.getInventory().getItemInMainHand() != null) {

                itemComparator(player.getInventory().getItemInMainHand(), player);

            }

            if (player.getInventory().getItemInOffHand() != null) {

                itemComparator(player.getInventory().getItemInOffHand(), player);

            }

            if (player.getInventory().getBoots() != null) {

                itemComparator(player.getInventory().getBoots(), player);

            }

            if (player.getInventory().getLeggings() != null) {

                itemComparator(player.getInventory().getLeggings(), player);

            }

            if (player.getInventory().getChestplate() != null) {

                itemComparator(player.getInventory().getChestplate(), player);

            }

            if (player.getInventory().getHelmet() != null) {

                itemComparator(player.getInventory().getHelmet(), player);

            }

        }

    }

    private void itemComparator(ItemStack itemStack, Player player) {

        for (ItemStack itemStackIteration : potionEffectItemList.keySet()) {

            if (itemStackIteration.getType().equals(itemStack.getType()) &&
                    itemStackIteration.getItemMeta().equals(itemStack.getItemMeta())) {

                ItemStack defaultStack = itemStackIteration;
                effectApplier(defaultStack, player);

            }

        }

    }

    private void effectApplier(ItemStack key, Player player) {

        for (PotionEffect potionEffect : potionEffectItemList.get(key)) {

            //night vision getting deleted and put back is extremely jarring, bypass
            if (potionEffect.getType().equals(PotionEffectType.NIGHT_VISION)) {

                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60, 1));

            } else {

                //Bypass due to minecraft not reapplying time correctly
                player.removePotionEffect(potionEffect.getType());

                player.addPotionEffect(potionEffect);

            }

        }

    }

}
