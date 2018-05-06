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

package com.magmaguy.elitemobs.elitedrops.uniqueitempowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.elitedrops.UniqueItemConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class HuntingBow {

    public static ArrayList<LivingEntity> aliveBossMobList = new ArrayList();

    public static void scanForBossMobs() {

        new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {

                    if (validateHuntingBowItem(player)) {

                        validateEntityDistance(player);

                    }

                }

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5 * 20);

    }

    private static boolean validateHuntingBowItem(Player player) {

        return player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().hasItemMeta() &&
                player.getInventory().getItemInMainHand().getItemMeta().hasLore() &&
                player.getInventory().getItemInMainHand().getItemMeta().getLore().equals(UniqueItemConstructor.uniqueItems.get(UniqueItemConstructor.HUNTING_BOW).getItemMeta().getLore());

    }

    private static void validateEntityDistance(Player player) {

        for (LivingEntity livingEntity : aliveBossMobList) {

            if (player.getWorld().equals(livingEntity.getWorld())) {

                if (player.getLocation().distance(livingEntity.getLocation()) < 160) {

                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 1));

                }

            }

        }

    }

}
