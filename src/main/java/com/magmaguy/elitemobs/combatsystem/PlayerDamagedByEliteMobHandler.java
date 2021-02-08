package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobHandler implements Listener {

    public static boolean bypass = false;

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
                ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                Damageable damageable = (Damageable) itemMeta;

                if (player.getInventory().getItemInOffHand().getItemMeta().hasEnchant(Enchantment.DURABILITY))
                    if (player.getInventory().getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.DURABILITY) / 20D > ThreadLocalRandom.current().nextDouble())
                        damageable.setDamage(damageable.getDamage() + 5);
                player.getInventory().getItemInOffHand().setItemMeta(itemMeta);
                if (Material.SHIELD.getMaxDurability() < damageable.getDamage())
                    player.getInventory().setItemInOffHand(null);
            }

            if (event.getEntityDamageByEntityEvent().getDamager() instanceof Projectile)
                event.getEntityDamageByEntityEvent().getDamager().remove();

            return;
        }

        //if the damage source is custom , the damage is final
        if (bypass) {
            bypass = false;
            //Deal with the player getting killed
            if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));
            return;
        }

        //Determine tiers
        double eliteTier = MobTierCalculator.findMobTier(event.getEliteMobEntity());
        double playerTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getArmorTier(true);

        double newDamage = eliteToPlayerDamageFormula(eliteTier, playerTier, player, event.getEliteMobEntity(), event.getEntityDamageByEntityEvent());

        //Set the final damage value
        event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        //Deal with the player getting killed
        if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
            PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));

    }

    private double eliteToPlayerDamageFormula(double eliteTier, double playerTier, Player player, EliteMobEntity eliteMobEntity, EntityDamageByEntityEvent event) {

        double baseDamage = EliteMobProperties.getPluginData(eliteMobEntity.getLivingEntity().getType()).baseDamage;
        double bonusDamage = eliteTier;
        double damageReduction = playerTier;
        double secondaryDamageReduction = secondaryEnchantmentDamageReduction(player, event);
        double customBossDamageMultiplier = eliteMobEntity.getDamageMultiplier();

        double finalDamage = (baseDamage + bonusDamage - damageReduction - secondaryDamageReduction) *
                MobCombatSettingsConfig.damageToPlayerMultiplier * customBossDamageMultiplier;

        double playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        //Prevent 1-shots and players getting healed from hits
        finalDamage = finalDamage < 1 ? 1 : finalDamage > playerMaxHealth ? playerMaxHealth - 1 : finalDamage;

        return finalDamage;

    }

    private double secondaryEnchantmentDamageReduction(Player player, EntityDamageByEntityEvent event) {

        double totalReductionLevel = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack == null) continue;
            for (Enchantment enchantment : itemStack.getEnchantments().keySet())
                if (enchantment.getName().equals(Enchantment.PROTECTION_PROJECTILE.getName()) && event.getDamager() instanceof Projectile)
                    totalReductionLevel += getDamageIncreasePercentage(enchantment, itemStack);
        }

        totalReductionLevel = totalReductionLevel / 4;

        return totalReductionLevel;

    }

    public static double getDamageIncreasePercentage(Enchantment enchantment, ItemStack weapon) {
        double maxEnchantmentLevel = EnchantmentsConfig.getEnchantment(enchantment).getMaxLevel();
        double currentEnchantmentLevel = weapon.getEnchantmentLevel(enchantment);
        return currentEnchantmentLevel / maxEnchantmentLevel <= 1 ? currentEnchantmentLevel / maxEnchantmentLevel : 1;
    }

}
