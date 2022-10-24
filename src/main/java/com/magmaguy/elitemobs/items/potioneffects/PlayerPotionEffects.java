package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.potioneffects.custom.Harm;
import com.magmaguy.elitemobs.items.potioneffects.custom.Heal;
import com.magmaguy.elitemobs.items.potioneffects.custom.Saturation;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

/**
 * Created by MagmaGuy on 14/03/2017.
 */
public class PlayerPotionEffects implements Listener {

    public PlayerPotionEffects() {
        new BukkitRunnable() {
            @Override
            public void run() {
                //scan through what players are wearing
                for (Player player : Bukkit.getOnlinePlayers())
                    if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) != null)
                        for (ElitePotionEffect elitePotionEffect : ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getContinuousPotionEffects(true))
                            doContinuousPotionEffect(elitePotionEffect, player);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
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

    private void doContinuousPotionEffect(ElitePotionEffect elitePotionEffect, Player player) {

        //This one doesn't work
        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.ABSORPTION)) return;
        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HEALTH_BOOST)) return;


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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerHitWithPotionEffect(EntityDamageByEntityEvent event) {
        LivingEntity damager = EntityFinder.getRealDamager(event);
        if (damager == null || !damager.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) damager;

        //citizens
        if (player.hasMetadata("NPC"))
            return;

        if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) == null) return;

        LivingEntity damagee;
        if (event.getEntity() instanceof LivingEntity)
            damagee = (LivingEntity) event.getEntity();
        else
            return;

        for (ElitePotionEffect elitePotionEffect : ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getOnHitPotionEffects(true))
            doOnHitPotionEffect(elitePotionEffect, player, damagee);
    }

    private void doOnHitPotionEffect(ElitePotionEffect elitePotionEffect, Player player, LivingEntity damagee) {
        //This one doesn't work
        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.ABSORPTION)) return;
        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HEALTH_BOOST)) return;
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
                if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.LEVITATION) ||
                        elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SLOW) ||
                        elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.BLINDNESS)) {
                    EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(damagee);
                    if (eliteEntity != null && eliteEntity.getHealthMultiplier() > 1)
                        return;
                    if (damagee.getType().equals(EntityType.PLAYER))
                        return;
                }

                player.addPotionEffect(elitePotionEffect.getPotionEffect());
                break;
            case TARGET:
                damagee.addPotionEffect(elitePotionEffect.getPotionEffect());
                break;
        }

    }

}
