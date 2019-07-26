package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

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

        if (player.isBlocking())
            return;

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

        //Prevent untouchable armor and 1-shots
        newDamage = newDamage < 1 ? 1 : newDamage;
        newDamage = newDamage > 19 ? 19 : newDamage;

        //Set the final damage value
        event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        //Deal with the player getting killed
        if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
            PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));

    }

    private double eliteToPlayerDamageFormula(double eliteTier, double playerTier, Player player, EliteMobEntity eliteMobEntity, EntityDamageByEntityEvent event) {

        double tierDifference = eliteTier - playerTier;

        tierDifference = (Math.abs(tierDifference) < 2) ? tierDifference : tierDifference > 0 ? 2 : -2;

        /*
        Apply secondary enchantment damage reduction
         */
        double newBaseDamage = 4 - secondaryEnchantmentDamageReduction(player, event);

        return ((newBaseDamage + newBaseDamage * tierDifference) * eliteMobEntity.getDamageMultiplier()) * MobCombatSettingsConfig.damageToPlayerMultiplier;

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
        totalReductionLevel = totalReductionLevel > 1 ? 1 : totalReductionLevel;

        return totalReductionLevel;

    }

    private double getDamageIncreasePercentage(Enchantment enchantment, ItemStack weapon) {
        double maxEnchantmentLevel = EnchantmentsConfig.getEnchantment(enchantment).getMaxLevel();
        double currentEnchantmentLevel = weapon.getEnchantmentLevel(enchantment);
        return currentEnchantmentLevel / maxEnchantmentLevel <= 1 ? currentEnchantmentLevel / maxEnchantmentLevel : 1;
    }

}
