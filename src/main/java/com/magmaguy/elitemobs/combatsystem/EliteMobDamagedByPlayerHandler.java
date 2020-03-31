package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.displays.DamageDisplay;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.customenchantments.CriticalStrikesEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import org.bukkit.NamespacedKey;
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
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.combatsystem.CombatSystem.isCustomDamageEntity;
import static com.magmaguy.elitemobs.combatsystem.CombatSystem.removeCustomDamageEntity;

public class EliteMobDamagedByPlayerHandler implements Listener {

    public static boolean display = false;
    public static int damage = 0;

    /**
     * Player -> EliteMobs damage. Ignores vanilla armor that Elite Mobs are wearing as that is purely cosmetic.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageByPlayer(EliteMobDamagedByPlayerEvent event) {

        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (player == null) return;

        EliteMobEntity eliteMobEntity = event.getEliteMobEntity();
        if (eliteMobEntity == null) return;

        //From this point on, the event damage is handled by Elite Mobs

        //nullify vanilla reductions
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.getEntityDamageByEntityEvent().isApplicable(modifier))
                event.getEntityDamageByEntityEvent().setDamage(modifier, 0);

        double rawDamage = event.getEntityDamageByEntityEvent().getDamage();

        //if the damage source is custom , the damage is final
        if (isCustomDamageEntity(eliteMobEntity.getLivingEntity())) {
            event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, rawDamage);
            //Deal with the mob getting killed
            if (player.getHealth() - rawDamage <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, player));
            removeCustomDamageEntity(eliteMobEntity.getLivingEntity());
            eliteMobEntity.damage(rawDamage);
            return;
        }

        double playerWeaponTier = ItemTierFinder.findBattleTier(player.getInventory().getItemInMainHand());
        double newDamage = finalDamageCalculator(playerWeaponTier, player, eliteMobEntity);

        if (event.getEntityDamageByEntityEvent().getDamager() instanceof Arrow) {
            double arrowSpeedMultiplier = Math.sqrt(Math.pow(event.getEntityDamageByEntityEvent().getDamager().getVelocity().getX(), 2) +
                    Math.pow(event.getEntityDamageByEntityEvent().getDamager().getVelocity().getY(), 2) +
                    Math.pow(event.getEntityDamageByEntityEvent().getDamager().getVelocity().getZ(), 2)) / 5;
            arrowSpeedMultiplier = (arrowSpeedMultiplier < 1) ? arrowSpeedMultiplier : 1;
            newDamage *= arrowSpeedMultiplier;
        }

        if (isCriticalHit(player)) {
            newDamage += newDamage * 0.5;
            DamageDisplay.isCriticalHit = true;
        }

        display = true;
        damage = (int) newDamage;
        double eventDamage = 0;
        if (damage <= 7)
            eventDamage = damage;
        else
            eventDamage = 7;

        event.getEntityDamageByEntityEvent().setDamage(EntityDamageEvent.DamageModifier.BASE, eventDamage);
        if (eliteMobEntity.getHealth() - newDamage < 0)
            newDamage = eliteMobEntity.getHealth();
        eliteMobEntity.damage(newDamage);
        eliteMobEntity.addDamager(player, newDamage);
        playerHitCooldownHashMap.put(event.getPlayer(), clock);

    }

    /**
     * Calculates Player -> EliteMobs damage.
     *
     * @param player Player object
     * @return
     */
    private double finalDamageCalculator(double playerWeaponTier, Player player, EliteMobEntity eliteMobEntity) {

        double finalDamage = getCooledAttackStrength(player) *
                (playerWeaponTier + secondaryEnchantmentDamageIncrease(player.getInventory().getItemInMainHand(), eliteMobEntity.getLivingEntity())) *
                MobCombatSettingsConfig.damageToEliteMultiplier;

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
            return ItemTagger.getEnchantment(weapon.getItemMeta(), Enchantment.DAMAGE_ARTHROPODS.getKey());
        if (eliteMob instanceof Zombie || eliteMob instanceof Skeleton)
            return ItemTagger.getEnchantment(weapon.getItemMeta(), Enchantment.DAMAGE_UNDEAD.getKey());

        return 0;

    }

    private boolean isCriticalHit(Player player) {
        int criticalStrike = ItemTagger.getEnchantment(player.getInventory().getItemInMainHand().getItemMeta(), new NamespacedKey(MetadataHandler.PLUGIN, CriticalStrikesEnchantment.key));
        return ThreadLocalRandom.current().nextDouble() < criticalStrike / 10.0;
    }

    private HashMap<Player, Integer> playerHitCooldownHashMap = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
                event.getAction().equals(Action.PHYSICAL))) return;
        playerHitCooldownHashMap.put(event.getPlayer(), clock);

    }

}
