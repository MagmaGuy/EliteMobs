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

import java.util.HashSet;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PotionEffectApplier implements Listener {

    public static void potionEffectApplier() {
        new BukkitRunnable() {
            @Override
            public void run() {
                //scan through what players are wearing
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkContinuousPotionEffects(player.getInventory().getItemInMainHand(), player);
                    checkContinuousPotionEffects(player.getInventory().getItemInOffHand(), player);
                    checkContinuousPotionEffects(player.getInventory().getBoots(), player);
                    checkContinuousPotionEffects(player.getInventory().getLeggings(), player);
                    checkContinuousPotionEffects(player.getInventory().getChestplate(), player);
                    checkContinuousPotionEffects(player.getInventory().getHelmet(), player);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20 * 1);
    }

    private static void checkContinuousPotionEffects(ItemStack itemStack, Player player) {
        if (!ItemTagger.isEliteItem(itemStack)) return;
        if (ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.continuousPotionEffectKey) == null)
            return;
        for (ElitePotionEffect elitePotionEffect : ItemTagger.getPotionEffects(itemStack.getItemMeta(), ItemTagger.continuousPotionEffectKey))
            doContinuousPotionEffect(elitePotionEffect, player);
    }

    private static void doContinuousPotionEffect(ElitePotionEffect elitePotionEffect, Player player) {

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
