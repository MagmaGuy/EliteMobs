package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.ProjectileTagger;
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
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobHandler implements Listener {

    public static boolean bypass = false;

    public static double getDamageIncreasePercentage(Enchantment enchantment, ItemStack weapon) {
        double maxEnchantmentLevel = EnchantmentsConfig.getEnchantment(enchantment).getMaxLevel();
        double currentEnchantmentLevel = weapon.getEnchantmentLevel(enchantment);
        return currentEnchantmentLevel / maxEnchantmentLevel <= 1 ? currentEnchantmentLevel / maxEnchantmentLevel : 1;
    }

    /**
     * EliteMobs -> player damage handler.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageHandler(PlayerDamagedByEliteMobEvent event) {
        if (!event.getEliteMobEntity().isValid()) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntityDamageByEntityEvent().isCancelled()) {
            if (bypass)
                bypass = false;
            return;
        }

        //From this point on, the damage event is fully altered by Elite Mobs

        Player player = (Player) event.getEntity();
        if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) == null) return;

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

        //if the projectile deals custom damage
        if (event.getProjectile() != null && ProjectileTagger.projectileHasCustomDamage(event.getProjectile())) {
            double damage = ProjectileTagger.getProjectileCustomDamage(event.getProjectile());
            if (damage < 0) return;
            //Set the final damage value
            event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, damage);

            //Deal with the player getting killed
            if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));
            return;
        }

        //Determine tiers
        double eliteTier = MobTierCalculator.findMobTier(event.getEliteMobEntity());
        double playerTier = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getArmorTier(true);

        double newDamage = eliteToPlayerDamageFormula(eliteTier, playerTier, player, event.getEliteMobEntity(), event.getEntityDamageByEntityEvent());


        //nullify vanilla reductions
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
            if (event.getEntityDamageByEntityEvent().isApplicable(modifier))
                event.getEntityDamageByEntityEvent().setDamage(modifier, 0);

        //Set the final damage value
        event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        //Deal with the player getting killed
        if (player.getHealth() - event.getEntityDamageByEntityEvent().getDamage() <= 0)
            PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, event.getEliteMobEntity().getLivingEntity()));

    }

    private double eliteToPlayerDamageFormula(double eliteTier, double playerTier, Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {

        /*
        Note: all baseline damage gets divided by 2 because this damage level expects players to be wearing armor.
        This method applies armor reduction and essentially nullifies any protection the armor gives.
        This means that the baseline damage divided by 2 is the amount of damage a mob will deal to a player in a
        balanced setting.
        Regional bosses have normalized damage, so they use stats similar to a zombie's. Everything else uses their own
        stats, which can make them vary pretty significantly in difficulty, which is pretty much intended as per Minecraft's
        own system.
         */
        double baseDamage = EliteMobProperties.getBaselineDamage(eliteEntity.getLivingEntity().getType(), eliteEntity) / 2D;
        double bonusDamage = eliteTier;
        double damageReduction = playerTier;
        double secondaryDamageReduction = secondaryEnchantmentDamageReduction(player, event);
        double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();
        double potionEffectDamageReduction = 0;
        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
            potionEffectDamageReduction = (player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() + 1) * MobCombatSettingsConfig.getResistanceDamageMultiplier();

        double finalDamage = (baseDamage * customBossDamageMultiplier + bonusDamage - damageReduction - secondaryDamageReduction - potionEffectDamageReduction) *
                MobCombatSettingsConfig.getDamageToPlayerMultiplier();

        double playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        //Prevent 1-shots and players getting healed from hits
        finalDamage = finalDamage < 1 ? 1 : finalDamage > playerMaxHealth ? playerMaxHealth - 1 : finalDamage;

        return finalDamage;

    }

    private double secondaryEnchantmentDamageReduction(Player player, EntityDamageByEntityEvent event) {

        double totalReductionLevel = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack == null || itemStack.getItemMeta() == null) continue;
            if (event.getDamager() instanceof Projectile)
                totalReductionLevel += ItemTagger.getEnchantment(itemStack.getItemMeta(), Enchantment.PROTECTION_ENVIRONMENTAL.getKey());
        }

        totalReductionLevel = totalReductionLevel / 4 / 10;

        return totalReductionLevel;

    }

}
