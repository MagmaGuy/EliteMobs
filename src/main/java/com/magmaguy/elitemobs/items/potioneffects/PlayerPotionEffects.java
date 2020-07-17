package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.potioneffects.custom.Harm;
import com.magmaguy.elitemobs.items.potioneffects.custom.Heal;
import com.magmaguy.elitemobs.items.potioneffects.custom.Saturation;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PlayerPotionEffects implements Listener {

    public PlayerPotionEffects() {
        new BukkitRunnable() {
            @Override
            public void run() {
                //scan through what players are wearing
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!playerPotionEffectsHashMap.containsKey(player.getUniqueId()))
                        new PlayerPotionEffectCache(player);
                    playerPotionEffectsHashMap.get(player.getUniqueId()).processInventory(player);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 1);
    }

    private static ArrayList<ElitePotionEffect> checkContinuousPotionEffects(ItemStack itemStack, Player player) {
        ArrayList<ElitePotionEffect> potionEffects = new ArrayList<>();
        if (!ItemTagger.isEliteItem(itemStack)) return potionEffects;
        potionEffects = ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.continuousPotionEffectKey);
        if (potionEffects == null)
            return new ArrayList<>();
        return potionEffects;
    }

    private static final HashMap<UUID, PlayerPotionEffectCache> playerPotionEffectsHashMap = new HashMap<>();

    private class PlayerPotionEffectCache {

        private class ItemEnchantmentsPair {
            ItemStack itemStack;
            ArrayList<ElitePotionEffect> potionEffects;

            public ItemEnchantmentsPair(ItemStack itemStack, ArrayList<ElitePotionEffect> potionEffects) {
                this.itemStack = itemStack;
                this.potionEffects = potionEffects;
            }
        }

        public HashMap<String, ItemEnchantmentsPair> itemEnchantmentsPairHashMap = new HashMap<>();

        public PlayerPotionEffectCache(Player player) {
            itemEnchantmentsPairHashMap.put("helmet", processPair(player.getInventory().getHelmet(), player));
            itemEnchantmentsPairHashMap.put("leggings", processPair(player.getInventory().getLeggings(), player));
            itemEnchantmentsPairHashMap.put("chestplate", processPair(player.getInventory().getChestplate(), player));
            itemEnchantmentsPairHashMap.put("boots", processPair(player.getInventory().getBoots(), player));
            itemEnchantmentsPairHashMap.put("itemInMainHand", processPair(player.getInventory().getItemInMainHand(), player));
            itemEnchantmentsPairHashMap.put("itemInOffHand", processPair(player.getInventory().getItemInOffHand(), player));
            playerPotionEffectsHashMap.put(player.getUniqueId(), this);
        }

        private ItemEnchantmentsPair processPair(ItemStack itemStack, Player player) {
            return new ItemEnchantmentsPair(itemStack, checkContinuousPotionEffects(itemStack, player));
        }

        public void processInventory(Player player) {
            processItem(player.getInventory().getHelmet(), "helmet", player);
            processItem(player.getInventory().getChestplate(), "chestplate", player);
            processItem(player.getInventory().getLeggings(), "leggings", player);
            processItem(player.getInventory().getBoots(), "boots", player);
            processItem(player.getInventory().getItemInMainHand(), "itemInMainHand", player);
            processItem(player.getInventory().getItemInOffHand(), "itemInOffHand", player);
        }

        private void processItem(ItemStack itemStack, String key, Player player) {
            ItemEnchantmentsPair itemEnchantmentsPair = itemEnchantmentsPairHashMap.get(key);
            if (itemStack == null) return;
            if (itemEnchantmentsPair.itemStack == null || !itemEnchantmentsPair.itemStack.isSimilar(itemStack))
                itemEnchantmentsPairHashMap.put(key, processPair(itemStack, player));

            for (ElitePotionEffect elitePotionEffect : itemEnchantmentsPair.potionEffects)
                doContinuousPotionEffect(elitePotionEffect, player);
        }
    }


    private void doContinuousPotionEffect(ElitePotionEffect elitePotionEffect, Player player) {

        //if the player has a higher amplifier potion effect, ignore. If it's the same, reapply to refresh the effect
        if (player.hasPotionEffect(elitePotionEffect.getPotionEffect().getType()) &&
                player.getPotionEffect(elitePotionEffect.getPotionEffect().getType()).getAmplifier() > elitePotionEffect.getPotionEffect().getAmplifier())
            return;

        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HEAL)) {
            Heal.doHeal(player, elitePotionEffect);
            return;
        }

        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SATURATION)) {
            Saturation.doSaturation(player, elitePotionEffect);
            return;
        }

        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HARM)) {
            Harm.doHarm(player, elitePotionEffect);
            return;
        }

        if (player.hasPotionEffect(elitePotionEffect.getPotionEffect().getType()))
            player.removePotionEffect(elitePotionEffect.getPotionEffect().getType());
        player.addPotionEffect(elitePotionEffect.getPotionEffect());
    }


    @EventHandler
    public void onPlayerHitWithPotionEffect(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;

        LivingEntity damager = EntityFinder.getRealDamager(event);
        if (damager == null || !damager.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) damager;

        LivingEntity damagee;
        if (event.getEntity() instanceof LivingEntity)
            damagee = (LivingEntity) event.getEntity();
        else
            return;


        if (player.getInventory().getItemInMainHand() != null)
            checkOnHitPotionEffects(player.getInventory().getItemInMainHand(), player, damagee);


        if (player.getInventory().getItemInOffHand() != null)
            checkOnHitPotionEffects(player.getInventory().getItemInOffHand(), player, damagee);

    }

    private void checkOnHitPotionEffects(ItemStack itemStack, Player player, LivingEntity damagee) {
        if (!ItemTagger.isEliteItem(itemStack)) return;
        if (ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.onHitPotionEffectKey) == null) return;
        for (ElitePotionEffect elitePotionEffect : ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.onHitPotionEffectKey))
            doOnHitPotionEffect(elitePotionEffect, player, damagee);
    }

    private void doOnHitPotionEffect(ElitePotionEffect elitePotionEffect, Player player, LivingEntity damagee) {
        switch (elitePotionEffect.getTarget()) {
            case SELF:

                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HEAL)) {
                    Heal.doHeal(player, elitePotionEffect);
                    break;
                }

                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SATURATION)) {
                    Saturation.doSaturation(player, elitePotionEffect);
                    break;
                }

                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HARM)) {
                    Harm.doHarm(player, elitePotionEffect);
                    return;
                }

                player.addPotionEffect(elitePotionEffect.getPotionEffect());
                break;
            case TARGET:
                damagee.addPotionEffect(elitePotionEffect.getPotionEffect());
                break;
        }

    }

    public static void addOnHitCooldown(HashSet<Player> cooldownList, Player player, long delay) {
        cooldownList.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldownList.remove(player);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, delay);
    }

}
