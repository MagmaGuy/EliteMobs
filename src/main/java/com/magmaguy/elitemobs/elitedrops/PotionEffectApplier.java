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
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.magmaguy.elitemobs.elitedrops.EliteDropsHandler.*;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PotionEffectApplier implements Listener {

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

        List<String> parsedItemLore = loreStripper(itemStack);

        for (ItemStack itemStackIteration : potionEffectItemList.keySet()) {

            if (itemStackIteration.getType().equals(itemStack.getType()) &&
                    ChatColor.stripColor(itemStackIteration.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName())) &&
                    loreStripper(itemStackIteration).equals(parsedItemLore)) {

                effectApplier(itemStackIteration, player);

            }

        }

    }

    private List<String> loreStripper(ItemStack itemStack) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> rawLore = new ArrayList<>();

        if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore()) {

            return rawLore;

        }

        rawLore = itemMeta.getLore();

        List<String> strippedLore = new ArrayList<>();

        for (String string : rawLore) {

            String strippedLine = ChatColor.stripColor(string);

            strippedLore.add(strippedLine);

        }

        return strippedLore;

    }

    List<PotionEffectType> offensivePotionEffects = new ArrayList<>(Arrays.asList(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION,
            PotionEffectType.GLOWING,
            PotionEffectType.HARM,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    ));

    private void effectApplier(ItemStack key, Player player) {

        for (PotionEffect potionEffect : potionEffectItemList.get(key)) {

            if (!offensivePotionEffects.contains(potionEffect.getType()) &&
                    (invertedCombatItemComparator(key) == null || !invertedCombatItemComparator(key).contains(potionEffect)) ||
                    offensivePotionEffects.contains(potionEffect.getType()) && invertedContinuousItemComparator(key) != null &&
                            invertedContinuousItemComparator(key).contains(potionEffect) &&
                            invertedCombatItemComparator(key) != null && invertedCombatItemComparator(key).contains(potionEffect)) {

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

    @EventHandler
    public void onPlayerHitWithPotionEffect(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;

        Player player;

        //deal with bows
        if (event.getDamager() instanceof Arrow) {

            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player) {

                player = (Player) arrow.getShooter();

            } else {

                return;

            }

        } else if (event.getDamager() instanceof Player) {

            player = (Player) event.getDamager();

        } else {

            return;

        }

        LivingEntity damagee;

        if (event.getEntity() instanceof LivingEntity) {

            damagee = (LivingEntity) event.getEntity();

        } else {

            return;

        }

        ItemStack mainHand = combatItemComparator(player.getEquipment().getItemInMainHand());
        ItemStack offHand = combatItemComparator(player.getEquipment().getItemInOffHand());

        //todo: check if there's a way to tell which hand dealt the damage
        if (mainHand != null) {

            for (PotionEffect potionEffect : potionEffectItemList.get(mainHand)) {


                //todo: find appropriate duration
                PotionEffect potionEffect1 = new PotionEffect(potionEffect.getType(), 20 * 5, potionEffect.getAmplifier());

                if ((offensivePotionEffects.contains(potionEffect.getType()) && invertedCombatItemComparator(mainHand) != null && invertedCombatItemComparator(mainHand).contains(potionEffect)) ||
                        (!offensivePotionEffects.contains(potionEffect.getType()) &&
                                (invertedCombatItemComparator(mainHand) == null || invertedCombatItemComparator(mainHand) != null && !invertedCombatItemComparator(mainHand).contains(potionEffect)))) {

                    player.addPotionEffect(potionEffect1);

                } else if ((!offensivePotionEffects.contains(potionEffect.getType()) && invertedCombatItemComparator(mainHand) != null && invertedCombatItemComparator(mainHand).contains(potionEffect)) ||
                        (offensivePotionEffects.contains(potionEffect.getType()) &&
                                (invertedCombatItemComparator(mainHand) == null || invertedCombatItemComparator(mainHand) != null && !invertedCombatItemComparator(mainHand).contains(potionEffect)))) {

                    damagee.addPotionEffect(potionEffect1);

                }


            }

        }

        if (offHand != null) {

            for (PotionEffect potionEffect : potionEffectItemList.get(offHand)) {


                //todo: find appropriate duration
                PotionEffect potionEffect1 = new PotionEffect(potionEffect.getType(), 20 * 5, potionEffect.getAmplifier());

                if (invertedCombatItemComparator(offHand) != null && invertedCombatItemComparator(offHand).contains(potionEffect)) {

                    player.addPotionEffect(potionEffect1);

                } else if ((!offensivePotionEffects.contains(potionEffect.getType()) && invertedCombatItemComparator(offHand) != null && invertedCombatItemComparator(offHand).contains(potionEffect)) ||
                        (offensivePotionEffects.contains(potionEffect.getType()) &&
                                (invertedCombatItemComparator(offHand) == null || invertedCombatItemComparator(offHand) != null && !invertedCombatItemComparator(offHand).contains(potionEffect)))) {

                    damagee.addPotionEffect(potionEffect1);

                }


            }

        }

    }

    private ItemStack combatItemComparator(ItemStack itemStack) {

        List<String> parsedItemLore = loreStripper(itemStack);

        for (ItemStack itemStackIteration : potionEffectItemList.keySet()) {

            if (itemStackIteration.getType().equals(itemStack.getType()) &&
                    ChatColor.stripColor(itemStackIteration.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName())) &&
                    loreStripper(itemStackIteration).equals(parsedItemLore)) {

                return itemStackIteration;

            }

        }

        return null;

    }

    private List<PotionEffect> invertedCombatItemComparator(ItemStack itemStack) {

        List<String> parsedItemLore = loreStripper(itemStack);

        for (ItemStack itemStackIteration : itemsWithInvertedPotionEffects.keySet()) {

            if (itemStackIteration.getType().equals(itemStack.getType()) &&
                    ChatColor.stripColor(itemStackIteration.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName())) &&
                    loreStripper(itemStackIteration).equals(parsedItemLore)) {

                return itemsWithInvertedPotionEffects.get(itemStackIteration);

            }

        }

        return null;

    }

    private List<PotionEffect> invertedContinuousItemComparator(ItemStack itemStack) {

        List<String> parsedItemLore = loreStripper(itemStack);

        for (ItemStack itemStackIteration : itemsWithContinuousInvertedPotionEffects.keySet()) {

            if (itemStackIteration.getType().equals(itemStack.getType()) &&
                    ChatColor.stripColor(itemStackIteration.getItemMeta().getDisplayName()).equals(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName())) &&
                    loreStripper(itemStackIteration).equals(parsedItemLore)) {

                return itemsWithContinuousInvertedPotionEffects.get(itemStackIteration);

            }

        }

        return null;

    }


}
