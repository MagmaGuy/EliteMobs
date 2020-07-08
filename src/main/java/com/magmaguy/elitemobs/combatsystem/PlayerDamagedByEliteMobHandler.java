package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.combatsystem.CombatSystem.isCustomDamageEntity;
import static com.magmaguy.elitemobs.combatsystem.CombatSystem.removeCustomDamageEntity;

public class PlayerDamagedByEliteMobHandler implements Listener {

    /**
     * EliteMobs -> player damage handler.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageHandler(PlayerDamagedByEliteMobEvent event) {

        if (event.getEntityDamageByEntityEvent().isCancelled()) return;

        //From this point on, the damage event is fully altered by Elite Mobs

        Player player = (Player) event.getEntity();

        if (player.isBlocking()) {
            if (player.getInventory().getItemInOffHand().getType().equals(Material.SHIELD)) {
                Damageable damageable = (Damageable) player.getInventory().getItemInOffHand().getItemMeta();

                if (player.getInventory().getItemInOffHand().getItemMeta().hasEnchant(Enchantment.DURABILITY))
                    if (player.getInventory().getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.DURABILITY) / 20D < ThreadLocalRandom.current().nextDouble())
                        damageable.setDamage(damageable.getDamage() + 5);
                    else
                        damageable.setDamage(damageable.getDamage() + 5);
                if (Material.SHIELD.getMaxDurability() < damageable.getDamage())
                    player.getInventory().setItemInOffHand(null);
            }
            return;
        }

        double rawDamage = event.getEntityDamageByEntityEvent().getDamage();

        //Get rid of all vanilla armor reduction
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.getEntityDamageByEntityEvent().isApplicable(modifier))
                event.getEntityDamageByEntityEvent().setDamage(modifier, 0);

        //if the damage source is custom , the damage is final
        if (isCustomDamageEntity(event.getEliteMobEntity().getLivingEntity())) {
            event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, rawDamage);
            //Deal with the player getting killed
            if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));
            removeCustomDamageEntity(event.getEliteMobEntity().getLivingEntity());
            return;
        }

        //Determine tiers
        double eliteTier = MobTierCalculator.findMobTier(event.getEliteMobEntity());
        double playerTier = ItemTierFinder.findArmorSetTier(player);

        double newDamage = eliteToPlayerDamageFormula(eliteTier, playerTier, player, event.getEliteMobEntity(), event.getEntityDamageByEntityEvent());

        //Set the final damage value
        event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        //Deal with the player getting killed
        if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
            PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));

    }

    private double eliteToPlayerDamageFormula(double eliteTier, double playerTier, Player player, EliteMobEntity eliteMobEntity, EntityDamageByEntityEvent event) {

        double baseDamage = 5;
        double bonusDamage = eliteTier;
        double damageReduction = playerTier;
        double secondaryDamageReduction = secondaryEnchantmentDamageReduction(player, event);
        double customBossDamageMultiplier = eliteMobEntity.getDamageMultiplier();

        double finalDamage = (baseDamage + bonusDamage - damageReduction - secondaryDamageReduction) *
                MobCombatSettingsConfig.damageToPlayerMultiplier * customBossDamageMultiplier;

        //Prevent 1-shots and players getting healed from hits
        finalDamage = finalDamage < 1 ? 1 : finalDamage > 15 ? 15 : finalDamage;

        return finalDamage;

    }

    private double secondaryEnchantmentDamageReduction(Player player, EntityDamageByEntityEvent event) {

        double totalReductionLevel = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack == null) continue;
            for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
                if (enchantment.getName().equals(Enchantment.PROTECTION_PROJECTILE.getName()) && event.getDamager() instanceof Projectile)
                    totalReductionLevel += getDamageIncreasePercentage(enchantment, itemStack);
                if (enchantment.getName().equals(Enchantment.PROTECTION_EXPLOSIONS.getName()) && event.getCause().equals(EntityDamageByEntityEvent.DamageCause.ENTITY_EXPLOSION))
                    totalReductionLevel += getDamageIncreasePercentage(enchantment, itemStack);
            }
        }

        totalReductionLevel = totalReductionLevel / 4;

        return totalReductionLevel;

    }

    private double getDamageIncreasePercentage(Enchantment enchantment, ItemStack weapon) {
        double maxEnchantmentLevel = EnchantmentsConfig.getEnchantment(enchantment).getMaxLevel();
        double currentEnchantmentLevel = weapon.getEnchantmentLevel(enchantment);
        return currentEnchantmentLevel / maxEnchantmentLevel <= 1 ? currentEnchantmentLevel / maxEnchantmentLevel : 1;
    }

}
