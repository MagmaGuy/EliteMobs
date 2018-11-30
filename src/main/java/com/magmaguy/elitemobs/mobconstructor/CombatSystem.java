package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.items.ObfuscatedSignatureLoreData;
import com.magmaguy.elitemobs.items.itemconstructor.LoreGenerator;
import com.magmaguy.elitemobs.utils.EntityFinder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class CombatSystem implements Listener {

    public static final double PER_LEVEL_POWER_INCREASE = 0.1;
    public static final double TARGET_HITS_TO_KILL = ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.TARGET_HITS_TO_KILL);
    public static final double BASE_DAMAGE_DEALT_TO_PLAYERS = ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.BASE_DAMAGE_DEALT_TO_PLAYER);
    //    public static final double TO_PLAYER_DAMAGE_TIER_HANDICAP = 0.75;
    public static final double TO_ELITE_DAMAGE_TIER_HANDICAP = 0.33;

    public static final double DIAMOND_TIER_LEVEL = 3;
    public static final double IRON_TIER_LEVEL = 2;
    public static final double STONE_CHAIN_TIER_LEVEL = 1;
    public static final double GOLD_WOOD_LEATHER_TIER_LEVEL = 0;

    //TODO: Handle thorns damage and potion effects

    //this event deals with elite mobs damaging players
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageHandler(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        LivingEntity damager = EntityFinder.getRealDamager(event);
        if (damager == null) return;

        if (!EntityTracker.isEliteMob(event.getDamager()))
            return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(damager);
        if (eliteMobEntity == null) return;

        //From this point on, the damage event is fully altered by Elite Mobs

        //Get rid of all vanilla armor reduction
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.isApplicable(modifier))
                event.setDamage(modifier, 0);

        Player player = (Player) event.getEntity();

        //Determine tiers
        double eliteTier = MobTierFinder.findMobTier(eliteMobEntity);
        double playerTier = ItemTierFinder.findArmorSetTier(player);

        double newDamage = eliteToPlayerDamageFormula(eliteTier, playerTier, player, event);

        //Prevent untouchable armor and 1-shots

        if (newDamage < 1) newDamage = 1;
        if (newDamage > 19) newDamage = 19;

        //Set the final damage value
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        //Deal with the player getting killed
        if (player.getHealth() - event.getDamage() <= 0)
            PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.intializeDeathMessage(player, damager));

    }

    private double eliteToPlayerDamageFormula(double eliteTier, double playerTier, Player player, EntityDamageByEntityEvent event) {

        double tierDifference = eliteTier - playerTier;
        tierDifference = (Math.abs(tierDifference) < 2) ? tierDifference : tierDifference > 0 ? 2 : -2;

        /*
        Apply secondary enchantment damage reduction
         */
        double newBaseDamage = BASE_DAMAGE_DEALT_TO_PLAYERS - secondaryEnchantmentDamageReduction(player, event);

        return newBaseDamage + newBaseDamage * (tierDifference);

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

    /*
    Deal with elite creeper explosions (visually)
    Damage is dealt by eliteMobDamageHandler
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEliteCreeperDetonation(ExplosionPrimeEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Creeper && EntityTracker.isEliteMob(event.getEntity())))
            return;

        /*
        This is necessary because the propagate the same duration as they have, which is nearly infinite
         */
        for (PotionEffect potionEffect : ((Creeper) event.getEntity()).getActivePotionEffects())
            ((Creeper) event.getEntity()).removePotionEffect(potionEffect.getType());

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());

        int mobLevel = eliteMobEntity.getLevel() < 1 ? 1 : eliteMobEntity.getLevel();

        float newExplosionRange = (float) (event.getRadius() + Math.ceil(0.01 * mobLevel * event.getRadius() *
                ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.ELITE_CREEPER_EXPLOSION_MULTIPLIER)));

        if (newExplosionRange > 20)
            newExplosionRange = 20;


        event.setRadius(newExplosionRange);

    }

    /*
    Elite Mobs have purely cosmetic armor. It doesn't do any actual damage reduction, as that would seriously complicate
    the underlying math for dealing damage for no real reason.
     */

    /*
    This deals with elite mobs being damaged by non-player entities
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageGeneric(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!EntityTracker.isEliteMob(event.getEntity())) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM))
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, event.getDamage());

    }

    /*
    This deals with players hitting the elite mob
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageByPlayer(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        LivingEntity damager = EntityFinder.getRealDamager(event);
        if (damager == null) return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(event.getEntity());
        if (eliteMobEntity == null) return;

        //From this point on, the event damage is handled by Elite Mobs

        /*
        Case in which the player is not the entity dealing damage, just deal raw damage
         */
        if (!damager.getType().equals(EntityType.PLAYER) && EntityTracker.isEliteMob(damager)) {

            event.setDamage(event.getDamage());
            return;

        }

        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.isApplicable(modifier))
                event.setDamage(modifier, 0);

        /*
        Case in which a player has hit the Elite Mob
         */

        if (!damager.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) damager;

        double playerTier;
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().equals(Material.BOW) && event.getDamager() instanceof Player)
            playerTier = 0;
        else
            playerTier = ItemTierFinder.findBattleTier(player.getInventory().getItemInMainHand());
        double eliteTier = MobTierFinder.findMobTier(eliteMobEntity);
        double maxHealth = eliteMobEntity.getMaxHealth();

        double newDamage = playerToEliteDamageFormula(eliteTier, playerTier, maxHealth, player, eliteMobEntity.getLivingEntity());

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

    }

    private double playerToEliteDamageFormula(double eliteTier, double playerTier, double maxHealth, Player player, LivingEntity eliteMob) {

        double tierDifference = playerTier - eliteTier;

        /*
        This caps the tier difference between mobs and players to prevent insurmountable boss fights
         */
        tierDifference = Math.abs(tierDifference) > 3 ? (tierDifference > 0 ? 3 : -3) : tierDifference;

        /*
        This applies secondary enchantments, that is, it applies enchantments that only affect specific mob types
        such as smite which only works with undead mobs
         */
        double bonusSecondaryEnchantmentDamage = secondaryEnchantmentDamageIncrease(player.getInventory().getItemInMainHand(), eliteMob);
        double newTargetHitsToKill = TARGET_HITS_TO_KILL - bonusSecondaryEnchantmentDamage;

        double finalDamage = getCooledAttackStrength(player) * (maxHealth / newTargetHitsToKill +
                maxHealth / newTargetHitsToKill * (tierDifference) * TO_ELITE_DAMAGE_TIER_HANDICAP);

        /*
        Make sure that players are dealing at least 1 damage as to not create unkillable bosses
         */
        return finalDamage < 1 ? 1 : finalDamage;

    }

    /*
    This deals with cooldown damage reduction based on the default cooldown property of minecraft and a custom
    cooldown penalty on top of it (just linear)
     */
    private float getCooledAttackStrength(Player player) {

        if (!playerHitCooldownHashMap.containsKey(player)) return 1;

        float swingDelay = clock - playerHitCooldownHashMap.get(player);
        float cooldownPeriod = getCooldownPeriod(player);

        if (swingDelay > cooldownPeriod) return 1;

        return swingDelay / cooldownPeriod;

    }

    /*
    This part is pretty much a copy of how Minecraft does the cooldown check
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


    /*
    This deals with damage increases specific to specific mob types
    It returns a percentage where 0 is no relevant enchantment present and 1 is maximum potential level reached for
    relevant enchantment
     */
    private double secondaryEnchantmentDamageIncrease(ItemStack weapon, LivingEntity eliteMob) {

        if (ObfuscatedSignatureLoreData.obfuscatedSignatureDetector(weapon)) {
            String deobfuscatedString = weapon.getItemMeta().getLore().get(0).replace("§", "");
            if (deobfuscatedString.contains(LoreGenerator.OBFUSCATED_ENCHANTMENTS)) {
                if (eliteMob instanceof Spider || eliteMob instanceof Silverfish)
                    return getDamageIncreasePercentage(Enchantment.DAMAGE_ARTHROPODS, findObfuscatedMainEnchantment(deobfuscatedString, Enchantment.DAMAGE_ARTHROPODS));

                if (eliteMob instanceof Zombie || eliteMob instanceof Skeleton)
                    return getDamageIncreasePercentage(Enchantment.DAMAGE_ARTHROPODS, findObfuscatedMainEnchantment(deobfuscatedString, Enchantment.DAMAGE_UNDEAD));
            }
        }

        for (Enchantment enchantment : weapon.getEnchantments().keySet()) {

            if (enchantment.getName().equals(Enchantment.DAMAGE_ARTHROPODS.getName()) && (eliteMob instanceof Spider || eliteMob instanceof Silverfish))
                return getDamageIncreasePercentage(enchantment, weapon);

            if (enchantment.getName().equals(Enchantment.DAMAGE_UNDEAD.getName()) && (eliteMob instanceof Zombie || eliteMob instanceof Skeleton))
                return getDamageIncreasePercentage(enchantment, weapon);

        }

        return 0;

    }

    private static int findObfuscatedMainEnchantment(String deobfuscatedLore, Enchantment enchantment) {
        for (String string : deobfuscatedLore.split(","))
            if (string.contains(enchantment.getName()))
                return Integer.parseInt(string.split(":")[1]);

        return 0;
    }

    private double getDamageIncreasePercentage(Enchantment enchantment, ItemStack weapon) {

        double maxEnchantmentLevel = getMaxEnchantmentLevel(enchantment);
        double currentEnchantmentLevel = weapon.getEnchantmentLevel(enchantment);

        return currentEnchantmentLevel / maxEnchantmentLevel <= 1 ? currentEnchantmentLevel / maxEnchantmentLevel : 1;

    }

    private double getDamageIncreasePercentage(Enchantment enchantment, double level) {

        double maxEnchantmentLevel = getMaxEnchantmentLevel(enchantment);

        return level / maxEnchantmentLevel <= 1 ? level / maxEnchantmentLevel : 1;

    }

    private int getMaxEnchantmentLevel(Enchantment enchantment) {
        return ConfigValues.itemsProceduralSettingsConfig.getInt("Valid Enchantments." + enchantment.getName() + ".Max Level");
    }

    private HashMap<Player, Integer> playerHitCooldownHashMap = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (!(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
                event.getAction().equals(Action.PHYSICAL))) return;

        playerHitCooldownHashMap.put(event.getPlayer(), clock);

    }

}
