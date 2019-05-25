package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.ItemsCustomLootSettingsConfig;
import com.magmaguy.elitemobs.items.itemconstructor.LoreGenerator;
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

    private void itemPotionEffectCheck(ItemStack itemStack, Player player, boolean event) {

        itemPotionEffectCheck(itemStack, player, event, null);

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

    public List<String> loreDeobfuscator(ItemStack itemStack) {

        List<String> lore = itemStack.getItemMeta().getLore();
        List<String> deobfuscatedPotionEffect = new ArrayList<>();

        if (lore.isEmpty()) return deobfuscatedPotionEffect;

        String deobfuscatedString = lore.get(0).replace("§", "");

        if (!deobfuscatedString.contains(LoreGenerator.OBFUSCATED_POTIONS)) return deobfuscatedPotionEffect;

        for (PotionEffectType potionEffectType : PotionEffectType.values())
            if (potionEffectType != null)
                for (String spaceSeparation : deobfuscatedString.split(",")) {
                    //individual potion effects are separated at this level
                    List<String> amplifierSeparation = Arrays.asList(spaceSeparation.split(":"));

                    if (amplifierSeparation.size() > 1 && amplifierSeparation.get(0).equals(potionEffectType.getName()))
                        deobfuscatedPotionEffect.add(spaceSeparation);

                }


        return deobfuscatedPotionEffect;

    }

    private HashMap<PotionEffect, List<String>> potionEffectCreator(List<String> deobfuscatedLore, boolean event) {

        HashMap<PotionEffect, List<String>> potionEffects = new HashMap<>();


        for (String string : deobfuscatedLore) {

            int substringIndex = 0;
            PotionEffectType potionEffectType = null;
            int potionEffectMagnitude = 0;
            String victim;
            List<String> tags = new ArrayList<>();


            for (String substring : string.split(":")) {

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
        if (potionEffectType.equals(PotionEffectType.SATURATION)) {
            return new PotionEffect(potionEffectType, 1, potionEffectMagnitude);
        }
        return new PotionEffect(potionEffectType, 5 * 20, potionEffectMagnitude);

    }

    private PotionEffect eventPotionEffect(PotionEffectType potionEffectType, int potionEffectMagnitude) {

        return new PotionEffect(potionEffectType, potionEffectDuration(potionEffectType), potionEffectMagnitude);

    }

    private int potionEffectDuration(PotionEffectType potionEffectType) {

        if (ConfigValues.itemsCustomLootSettingsConfig.contains(ItemsCustomLootSettingsConfig.DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + potionEffectType.getName())) {

            return 20 * ConfigValues.itemsCustomLootSettingsConfig.getInt(ItemsCustomLootSettingsConfig.DEFAULT_POTION_EFFECT_DURATION_NODE_PREFIX + potionEffectType.getName());

        } else {

            return 1;

        }

    }

    private void continuousPotionEffectApplier(HashMap<PotionEffect, List<String>> potionEffects, Player
            player) {

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

    private void eventPotionEffectApplier(HashMap<PotionEffect, List<String>> potionEffects, Player
            player, LivingEntity livingEntity) {

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
