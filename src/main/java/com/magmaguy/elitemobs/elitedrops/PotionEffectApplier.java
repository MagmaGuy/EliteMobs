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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PotionEffectApplier implements Listener {

    public static final String TARGET = "TARGET";
    public static final String SELF = "SELF";
    public static final String CONTINUOUS = "CONTINUOUS";
    public static final String ONHIT = "ONHIT";
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

    public void potionEffectApplier() {

        //scan through what players are wearing
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (player.getInventory().getItemInMainHand() != null) {

                itemPotionEffectCheck(player.getInventory().getItemInMainHand(), player, false);

            }

            if (player.getInventory().getItemInOffHand() != null) {

                itemPotionEffectCheck(player.getInventory().getItemInOffHand(), player, false);

            }

            if (player.getInventory().getBoots() != null) {

                itemPotionEffectCheck(player.getInventory().getBoots(), player, false);

            }

            if (player.getInventory().getLeggings() != null) {

                itemPotionEffectCheck(player.getInventory().getLeggings(), player, false);

            }

            if (player.getInventory().getChestplate() != null) {

                itemPotionEffectCheck(player.getInventory().getChestplate(), player, false);

            }

            if (player.getInventory().getHelmet() != null) {

                itemPotionEffectCheck(player.getInventory().getHelmet(), player, false);

            }

        }

    }

    private void itemPotionEffectCheck(ItemStack itemStack, Player player, boolean event, LivingEntity livingEntity) {

        if (itemStack.getItemMeta() == null || itemStack.getItemMeta().getLore() == null) return;

        List<String> deobfuscatedLore = loreDeobfuscator(itemStack);

        if (deobfuscatedLore.size() > 0) {

            HashMap<PotionEffect, List<String>> potionEffects = potionEffectCreator(deobfuscatedLore, event);

            if (!event) {

                continuousPotionEffectApplier(potionEffects, player);

            }

            if (event) {

                eventPotionEffectApplier(potionEffects, player, livingEntity);

            }

        }

    }

    private void itemPotionEffectCheck(ItemStack itemStack, Player player, boolean event) {

        itemPotionEffectCheck(itemStack, player, event, null);

    }

    public List<String> loreDeobfuscator(ItemStack itemStack) {

        List<String> lore = itemStack.getItemMeta().getLore();
        List<String> deobfuscatedString = new ArrayList<>();

        for (String string : lore) {

            if (string.contains("§")) {

                string = string.replaceAll("§", "");

                for (PotionEffectType potionEffectType : PotionEffectType.values()) {

                    if (potionEffectType != null) {

                        for (String spaceSeparation : string.split(" ")) {

                            //individual potion effects are separated at this level
                            boolean validPotionEffect = false;

                            for (String commaSeparation : spaceSeparation.split(",")) {

                                if (commaSeparation.equals(potionEffectType.getName()) && spaceSeparation.contains(",")) {

                                    validPotionEffect = true;

                                }

                            }

                            if (validPotionEffect) {

                                deobfuscatedString.add(spaceSeparation);

                            }

                        }

                    }

                }

            }

        }

        return deobfuscatedString;

    }

    private HashMap<PotionEffect, List<String>> potionEffectCreator(List<String> deobfuscatedLore, boolean event) {

        HashMap<PotionEffect, List<String>> potionEffects = new HashMap<>();


        for (String string : deobfuscatedLore) {

            int substringIndex = 0;
            PotionEffectType potionEffectType = null;
            int potionEffectMagnitude = 0;
            String victim;
            List<String> tags = new ArrayList<>();


            for (String substring : string.split(",")) {

                if (substringIndex == 0) {

                    potionEffectType = PotionEffectType.getByName(substring);

                }

                if (substringIndex == 1) {

                    potionEffectMagnitude = Integer.parseInt(substring);

                }

                if (substringIndex == 2) {

                    victim = substring;
                    if (victim.equalsIgnoreCase(TARGET) || victim.equalsIgnoreCase(SELF)) {

                        tags.add(victim);

                    } else {

                        Bukkit.getLogger().info("[EliteMobs] Error with ItemsCustomLootList.yml: was expecting '" + TARGET + "' or '"
                                + SELF + "' values but got '" + victim + "' instead.");

                    }

                }

                if (substringIndex == 3) {

                    if (substring.equalsIgnoreCase(CONTINUOUS)) {

                        tags.add(substring);

                    } else if (substring.equalsIgnoreCase(ONHIT)) {

                        tags.add(substring);

                    } else {

                        Bukkit.getLogger().info("[EliteMobs] Error with ItemsCustomLootList.yml: was expecting '" + CONTINUOUS +
                                "' or '" + ONHIT + "' value but got '" + substring + "' instead.");

                    }

                }

                substringIndex++;

            }

            if (potionEffectType != null && potionEffectMagnitude > 0) {

                PotionEffect potionEffect;

                if (!event) potionEffect = continuousPotionEffect(potionEffectType, potionEffectMagnitude - 1);
                else potionEffect = eventPotionEffect(potionEffectType, potionEffectMagnitude - 1);

                potionEffects.put(potionEffect, tags);

            } else {

                Bukkit.getLogger().info("[EliteMobs] There seems to be something very wrong with one of your potion effects.");
                Bukkit.getLogger().info("[EliteMobs] An item has an invalid potion effect or potion effect level.");
                Bukkit.getLogger().info("[EliteMobs] Problematic entry has the follow settings: " + string);

            }

        }


        return potionEffects;

    }

    private PotionEffect continuousPotionEffect(PotionEffectType potionEffectType, int potionEffectMagnitude) {

        if (potionEffectType.equals(PotionEffectType.NIGHT_VISION)) {
            return new PotionEffect(potionEffectType, 15 * 20, potionEffectMagnitude);
        }
        return new PotionEffect(potionEffectType, 2 * 20, potionEffectMagnitude);

    }

    private PotionEffect eventPotionEffect(PotionEffectType potionEffectType, int potionEffectMagnitude) {
        //TODO: put real settings in
        return new PotionEffect(potionEffectType, 5 * 20, potionEffectMagnitude);

    }

    private void continuousPotionEffectApplier(HashMap<PotionEffect, List<String>> potionEffects, Player player) {

        for (PotionEffect potionEffect : potionEffects.keySet()) {

            if (potionEffects.get(potionEffect).size() > 0) {

                if (potionEffects.get(potionEffect).get(0).equalsIgnoreCase(SELF)) {

                    if ((!offensivePotionEffects.contains(potionEffect.getType()) && potionEffects.get(potionEffect).size() == 1) ||
                            (!offensivePotionEffects.contains(potionEffect.getType()) && potionEffects.get(potionEffect).get(1).equalsIgnoreCase(CONTINUOUS))) {

                        player.removePotionEffect(potionEffect.getType());
                        player.addPotionEffect(potionEffect);

                    }

                    if (offensivePotionEffects.contains(potionEffect.getType()) && potionEffects.get(potionEffect).size() > 1) {

                        if (potionEffects.get(potionEffect).get(1).equalsIgnoreCase(CONTINUOUS)) {

                            player.removePotionEffect(potionEffect.getType());
                            player.addPotionEffect(potionEffect);

                        }

                    }


                } else if (potionEffects.get(potionEffect).get(0).equalsIgnoreCase(TARGET)) {

                    //this doesn't do anything since this will only continuously apply to the player.

                }

            } else {

                //legacy config settings where there are no tags
                if (!offensivePotionEffects.contains(potionEffect.getType())) {

                    player.removePotionEffect(potionEffect.getType());
                    player.addPotionEffect(potionEffect);

                }

            }

        }

    }

    private void eventPotionEffectApplier(HashMap<PotionEffect, List<String>> potionEffects, Player player, LivingEntity livingEntity) {

        for (PotionEffect potionEffect : potionEffects.keySet()) {

            if (potionEffects.get(potionEffect).size() > 0) {

                if (potionEffects.get(potionEffect).get(0).equalsIgnoreCase(SELF)) {

                    player.removePotionEffect(potionEffect.getType());
                    player.addPotionEffect(potionEffect);

                    if (potionEffects.get(potionEffect).size() > 1) {

                        if (potionEffects.get(potionEffect).get(1).equalsIgnoreCase(CONTINUOUS)) {

                            player.removePotionEffect(potionEffect.getType());

                        }

                    }


                } else if (potionEffects.get(potionEffect).get(0).equalsIgnoreCase(TARGET)) {

                    livingEntity.removePotionEffect(potionEffect.getType());
                    livingEntity.addPotionEffect(potionEffect);

                }

            } else {

                //legacy config settings where there are no tags
                if (offensivePotionEffects.contains(potionEffect.getType())) {

                    livingEntity.removePotionEffect(potionEffect.getType());
                    livingEntity.addPotionEffect(potionEffect);

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

        if (player.getInventory().getItemInMainHand() != null) {

            itemPotionEffectCheck(player.getInventory().getItemInMainHand(), player, true, damagee);

        }

        if (player.getInventory().getItemInOffHand() != null) {

            itemPotionEffectCheck(player.getInventory().getItemInOffHand(), player, true, damagee);

        }


    }

}
