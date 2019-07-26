package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

import static com.magmaguy.elitemobs.combatsystem.CombatSystem.isCustomDamageEntity;
import static com.magmaguy.elitemobs.combatsystem.CombatSystem.removeCustomDamageEntity;

public class EliteMobDamagedByPlayerHandler implements Listener {

    /**
     * Player -> EliteMobs damage. Ignores vanilla armor that Elite Mobs are wearing as that is purely cosmetic.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageByPlayer(EliteMobDamagedByPlayerEvent event) {

        if (event.isCancelled()) return;
        LivingEntity damager = EntityFinder.getRealDamager(event.getEntityDamageByEntityEvent());
        if (damager == null) return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        //From this point on, the event damage is handled by Elite Mobs

        /*
        Case in which the player is not the entity dealing damage, just deal raw damage
         */
        if (!damager.getType().equals(EntityType.PLAYER) && EntityTracker.isEliteMob(damager)) {
            event.getEntityDamageByEntityEvent().setDamage(event.getEntityDamageByEntityEvent().getDamage());
            return;
        }

        double rawDamage = event.getEntityDamageByEntityEvent().getDamage();

        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.getEntityDamageByEntityEvent().isApplicable(modifier))
                event.getEntityDamageByEntityEvent().setDamage(modifier, 0);

        /*
        Case in which a player has hit the Elite Mob
         */

        if (!damager.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) damager;

        //if the damage source is custom , the damage is final
        if (isCustomDamageEntity(eliteMobEntity.getLivingEntity())) {
            event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, rawDamage);
            //Deal with the player getting killed
            if (player.getHealth() - rawDamage <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, damager));
            removeCustomDamageEntity(eliteMobEntity.getLivingEntity());
            /*
        This is a bit of a dirty hack, I may want to tighten it up later
         */
            //adjust current plugin health
            eliteMobEntity.setHealth(eliteMobEntity.getMaxHealth() * eliteMobEntity.getLivingEntity().getHealth() / eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            double damagePercentOfHealth = rawDamage / eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double pluginDamage = damagePercentOfHealth * eliteMobEntity.getMaxHealth();
            eliteMobEntity.setHealth(eliteMobEntity.getHealth() - pluginDamage);
            return;
        }

        double playerTier;
        if (player.getInventory().getItemInMainHand() == null ||
                player.getInventory().getItemInMainHand().getType().equals(Material.BOW) && event.getEntityDamageByEntityEvent().getDamager() instanceof Player)
            playerTier = 0;
        else
            playerTier = ItemTierFinder.findBattleTier(player.getInventory().getItemInMainHand());
        double eliteTier = MobTierCalculator.findMobTier(eliteMobEntity);
        double maxHealth = eliteMobEntity.getLivingEntity().getMaxHealth();

        double newDamage = playerToEliteDamageFormula(eliteTier, playerTier, maxHealth, player, eliteMobEntity);

        if (event.getEntityDamageByEntityEvent().getDamager() instanceof Arrow) {
            double arrowSpeedMultiplier = Math.sqrt(Math.pow(event.getEntityDamageByEntityEvent().getDamager().getVelocity().getX(), 2) +
                    Math.pow(event.getEntityDamageByEntityEvent().getDamager().getVelocity().getY(), 2) +
                    Math.pow(event.getEntityDamageByEntityEvent().getDamager().getVelocity().getZ(), 2)) / 5;
            arrowSpeedMultiplier = (arrowSpeedMultiplier < 1) ? arrowSpeedMultiplier : 1;
            newDamage *= arrowSpeedMultiplier;
        }

        event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        /*
        This is a bit of a dirty hack, I may want to tighten it up later
         */
        //adjust current plugin health
        eliteMobEntity.setHealth(eliteMobEntity.getMaxHealth() * eliteMobEntity.getLivingEntity().getHealth() / eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        double damagePercentOfHealth = newDamage / eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double pluginDamage = damagePercentOfHealth * eliteMobEntity.getMaxHealth();
        eliteMobEntity.setHealth(eliteMobEntity.getHealth() - pluginDamage);
        eliteMobEntity.addDamager(player, pluginDamage);

    }

    /**
     * Calculates Player -> EliteMobs damage.
     *
     * @param eliteTier  Tier of the EliteMob
     * @param playerTier Tier of the Player's weapon
     * @param maxHealth  EliteMob's max health
     * @param player     Player object
     * @param eliteMob   EliteMob object
     * @return
     */
    private double playerToEliteDamageFormula(double eliteTier, double playerTier, double maxHealth, Player player, EliteMobEntity eliteMob) {

        double tierDifference = playerTier - eliteTier;

        /*
        This caps the tier difference between mobs and players to prevent insurmountable boss fights
         */
        double maxTierDifference = 3;
        tierDifference = Math.abs(tierDifference) > maxTierDifference ? (tierDifference > 0 ? maxTierDifference : -maxTierDifference) : tierDifference;

        /*
        This applies secondary enchantments, that is, it applies enchantments that only affect specific mob types
        such as smite which only works with undead mobs
         */
        double bonusSecondaryEnchantmentDamage = secondaryEnchantmentDamageIncrease(player.getInventory().getItemInMainHand(), eliteMob.getLivingEntity());
        double newTargetHitsToKill = (eliteMob.getDefaultMaxHealth() / 2) - bonusSecondaryEnchantmentDamage; //10 for zombies

        double finalDamage = getCooledAttackStrength(player) * (maxHealth / newTargetHitsToKill + maxHealth / newTargetHitsToKill * tierDifference * 0.2);

        //Apply health multiplier
        finalDamage /= eliteMob.getHealthMultiplier();

        //apply config multiplier
        finalDamage *= MobCombatSettingsConfig.damageToEliteMultiplier;

        finalDamage = finalDamage < 1 ? 1 : finalDamage;
        return finalDamage;

    }

    /**
     * Calculates the cooldown debuff following vanilla Minecraft rules and then linearly applies a debuff to attacks
     *
     * @param player Player in cooldown
     * @return Debuff multiplier
     */
    private float getCooledAttackStrength(Player player) {
        if (!playerHitCooldownHashMap.containsKey(player)) return 1;
        float swingDelay = clock - playerHitCooldownHashMap.get(player);
        float cooldownPeriod = getCooldownPeriod(player);
        if (swingDelay > cooldownPeriod) return 1;
        return swingDelay / cooldownPeriod;
    }

    /**
     * Calculates the cooldown following vanilla rules
     *
     * @param player
     * @return
     */
    private float getCooldownPeriod(Player player) {
        return (float) (1.0D / player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getValue() * 20.0D);
    }

    private static int clock = 0;

    public static void launchInternalClock() {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (clock == Integer.MAX_VALUE) clock = 0;
                clock++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 1, 1);
    }


    /**
     * Calculates the Player -> EliteMobs damage increase from secondary enchantments. Adds to the total as a percentage
     * of the maximum health.
     *
     * @param weapon   Weapon used by the player
     * @param eliteMob EliteMob instance
     * @return Value to be added
     */
    private double secondaryEnchantmentDamageIncrease(ItemStack weapon, LivingEntity eliteMob) {

        if (eliteMob instanceof Spider || eliteMob instanceof Silverfish)
            return getDamageIncreasePercentage(Enchantment.DAMAGE_ARTHROPODS, ItemTagger.getEnchantment(weapon.getItemMeta(), Enchantment.DAMAGE_ARTHROPODS.getKey()));
        if (eliteMob instanceof Zombie || eliteMob instanceof Skeleton)
            return getDamageIncreasePercentage(Enchantment.DAMAGE_UNDEAD, ItemTagger.getEnchantment(weapon.getItemMeta(), Enchantment.DAMAGE_UNDEAD.getKey()));

        return 0;

    }


    private double getDamageIncreasePercentage(Enchantment enchantment, int level) {
        double maxEnchantmentLevel = EnchantmentsConfig.getEnchantment(enchantment).getMaxLevel();
        return level / maxEnchantmentLevel <= 1 ? level / maxEnchantmentLevel : 1;
    }

    private HashMap<Player, Integer> playerHitCooldownHashMap = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
                event.getAction().equals(Action.PHYSICAL))) return;
        playerHitCooldownHashMap.put(event.getPlayer(), clock);

    }


}
