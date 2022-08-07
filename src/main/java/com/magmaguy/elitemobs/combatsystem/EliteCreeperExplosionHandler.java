package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;

public class EliteCreeperExplosionHandler implements Listener {

    private static final HashSet<Player> explosionPlayers = new HashSet<>();

    public static double getDamageIncreasePercentage(Enchantment enchantment, ItemStack weapon) {
        double maxEnchantmentLevel = EnchantmentsConfig.getEnchantment(enchantment).getMaxLevel();
        double currentEnchantmentLevel = weapon.getEnchantmentLevel(enchantment);
        return currentEnchantmentLevel / maxEnchantmentLevel <= 1 ? currentEnchantmentLevel / maxEnchantmentLevel : 1;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEliteCreeperDetonation(ExplosionPrimeEvent event) {

        if (!(event.getEntity() instanceof Creeper && EntityTracker.isEliteMob(event.getEntity())))
            return;

        /*
        This is necessary because the propagate the same duration as they have, which is nearly infinite
         */
        for (PotionEffect potionEffect : ((Creeper) event.getEntity()).getActivePotionEffects())
            ((Creeper) event.getEntity()).removePotionEffect(potionEffect.getType());

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());

        for (Entity entity : event.getEntity().getNearbyEntities(event.getRadius(), event.getRadius(), event.getRadius()))
            if (entity.getType().equals(EntityType.PLAYER)) {
                Player player = (Player) entity;
                explosionPlayers.add(player);
                ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());

                if (elitePlayerInventory == null) continue;
                double damageReduction = 0;
                for (ItemStack itemStack : player.getInventory().getArmorContents()) {
                    if (itemStack == null) continue;
                    for (Enchantment enchantment : itemStack.getEnchantments().keySet())
                        if (enchantment.getName().equals(Enchantment.PROTECTION_EXPLOSIONS.getName()))
                            damageReduction += getDamageIncreasePercentage(enchantment, itemStack);
                }
                damageReduction += elitePlayerInventory.baseDamageReduction();
                double finalDamage = eliteEntity.getLevel() - damageReduction;
                finalDamage = finalDamage < 0 ? 0 : finalDamage;
                player.damage(finalDamage, eliteEntity.getLivingEntity());
            } else if (entity instanceof LivingEntity)
                ((LivingEntity) entity).damage(eliteEntity.getLevel());

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerDamagedByExplosionEvent(EntityDamageEvent event) {
        if (!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) return;
        Player player = (Player) event.getEntity();
        if (!explosionPlayers.contains(player)) return;
        explosionPlayers.remove(player);
        event.setDamage(0);
    }

}
