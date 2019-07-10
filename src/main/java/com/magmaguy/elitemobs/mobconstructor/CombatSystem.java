package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTierFinder;
import com.magmaguy.elitemobs.items.MobTierCalculator;
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
import java.util.HashSet;

public class CombatSystem implements Listener {

    public static final double PER_LEVEL_POWER_INCREASE = 0.1;
    public static final double BASE_DAMAGE_DEALT_TO_PLAYERS = ConfigValues.mobCombatSettingsConfig.getDouble(MobCombatSettingsConfig.BASE_DAMAGE_DEALT_TO_PLAYER);
    public static final double TO_ELITE_DAMAGE_TIER_HANDICAP = 0.5;

    public static final double DIAMOND_TIER_LEVEL = 3;
    public static final double IRON_TIER_LEVEL = 2;
    public static final double STONE_CHAIN_TIER_LEVEL = 1;
    public static final double GOLD_WOOD_LEATHER_TIER_LEVEL = 0;

    private static HashSet<LivingEntity> customDamageEntity = new HashSet<>();

    public static HashSet<LivingEntity> getCustomDamageEntities() {
        return customDamageEntity;
    }

    public static boolean isCustomDamageEntity(LivingEntity livingEntity) {
        return customDamageEntity.contains(livingEntity);
    }

    public static void addCustomEntity(LivingEntity livingEntity) {
        customDamageEntity.add(livingEntity);
    }

    public static void removeCustomEntity(LivingEntity livingEntity) {
        customDamageEntity.remove(livingEntity);
    }

    //TODO: Handle thorns damage and potion effects

    /**
     * EliteMobs -> player damage handler.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamageHandler(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        LivingEntity damager = EntityFinder.getRealDamager(event);
        if (damager == null) return;

        EliteMobEntity eliteMobEntity = EntityTracker.getEliteMobEntity(damager);
        if (eliteMobEntity == null) return;

        //From this point on, the damage event is fully altered by Elite Mobs

        double rawDamage = event.getDamage();

        //Get rid of all vanilla armor reduction
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.isApplicable(modifier))
                event.setDamage(modifier, 0);

        Player player = (Player) event.getEntity();

        //if the damage source is custom , the damage is final
        if (isCustomDamageEntity(eliteMobEntity.getLivingEntity())) {
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, rawDamage);
            //Deal with the player getting killed
            if (player.getHealth() - event.getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, damager));
            removeCustomEntity(eliteMobEntity.getLivingEntity());
            return;
        }

        //Determine tiers
        double eliteTier = MobTierCalculator.findMobTier(eliteMobEntity);
        double playerTier = ItemTierFinder.findArmorSetTier(player);

        double newDamage = eliteToPlayerDamageFormula(eliteTier, playerTier, player, eliteMobEntity, event);

        //Prevent untouchable armor and 1-shots

        if (newDamage < 1) newDamage = 1;
        if (newDamage > 19) newDamage = 19;

        //Set the final damage value
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        //Deal with the player getting killed
        if (player.getHealth() - event.getDamage() <= 0)
            PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, damager));

    }

    /**
     * Calculates EliteMobs -> player damage taking into account tier difference. This has minimum and maximum values,
     * and is calculated based on the difference between the player's armor tier and the mob's tier.
     *
     * @param eliteTier  Tier of the Elite Mob
     * @param playerTier Player combat tier
     * @param player     Player involved in combat
     * @param event      EntityDamageEvent
     * @return Final value for the damage
     */
    private double eliteToPlayerDamageFormula(double eliteTier, double playerTier, Player player, EliteMobEntity eliteMobEntity, EntityDamageByEntityEvent event) {

        double tierDifference = eliteTier - playerTier;
        tierDifference = (Math.abs(tierDifference) < 2) ? tierDifference : tierDifference > 0 ? 2 : -2;

        /*
        Apply secondary enchantment damage reduction
         */
        double newBaseDamage = BASE_DAMAGE_DEALT_TO_PLAYERS - secondaryEnchantmentDamageReduction(player, event);

        return (newBaseDamage + newBaseDamage * (tierDifference)) * eliteMobEntity.getDamageMultiplier();

    }

    /**
     * Calculates damage reduction from secondary enchantments
     *
     * @param player Player for whom the reduction will run
     * @param event  Damage event
     * @return
     */
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

    /**
     * Deals with the block damage from creeper explosions (but not the damage from these explosions)
     *
     * @param event Explosion event
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

    /**
     * Damage dealt to EliteMobs by entities other than Players.
     *
     * @param event Damage event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void eliteMobDamagedGeneric(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!EntityTracker.isEliteMob(event.getEntity())) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM))
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, event.getDamage());

    }


    /**
     * Player -> EliteMobs damage. Ignores vanilla armor that Elite Mobs are wearing as that is purely cosmetic.
     *
     * @param event
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

        double rawDamage = event.getDamage();

        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            if (event.isApplicable(modifier))
                event.setDamage(modifier, 0);

        /*
        Case in which a player has hit the Elite Mob
         */

        if (!damager.getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) damager;
        eliteMobEntity.addDamager(player);

        //if the damage source is custom , the damage is final
        if (isCustomDamageEntity(eliteMobEntity.getLivingEntity())) {
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, rawDamage);
            //Deal with the player getting killed
            if (player.getHealth() - rawDamage <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, damager));
            removeCustomEntity(eliteMobEntity.getLivingEntity());
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
                player.getInventory().getItemInMainHand().getType().equals(Material.BOW) && event.getDamager() instanceof Player)
            playerTier = 0;
        else
            playerTier = ItemTierFinder.findBattleTier(player.getInventory().getItemInMainHand());
        double eliteTier = MobTierCalculator.findMobTier(eliteMobEntity);
        double maxHealth = eliteMobEntity.getLivingEntity().getMaxHealth();

        double newDamage = playerToEliteDamageFormula(eliteTier, playerTier, maxHealth, player, eliteMobEntity);

        if (event.getDamager() instanceof Arrow) {
            double arrowSpeedMultiplier = Math.sqrt(Math.pow(event.getDamager().getVelocity().getX(), 2) +
                    Math.pow(event.getDamager().getVelocity().getY(), 2) +
                    Math.pow(event.getDamager().getVelocity().getZ(), 2)) / 5;
            arrowSpeedMultiplier = (arrowSpeedMultiplier < 1) ? arrowSpeedMultiplier : 1;
            newDamage *= arrowSpeedMultiplier;
        }

        event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

        /*
        This is a bit of a dirty hack, I may want to tighten it up later
         */
        //adjust current plugin health
        eliteMobEntity.setHealth(eliteMobEntity.getMaxHealth() * eliteMobEntity.getLivingEntity().getHealth() / eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        double damagePercentOfHealth = newDamage / eliteMobEntity.getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double pluginDamage = damagePercentOfHealth * eliteMobEntity.getMaxHealth();
        eliteMobEntity.setHealth(eliteMobEntity.getHealth() - pluginDamage);

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
        double maxTierDifference = 1;
        tierDifference = Math.abs(tierDifference) > maxTierDifference ? (tierDifference > 0 ? maxTierDifference : -maxTierDifference) : tierDifference;

        /*
        This applies secondary enchantments, that is, it applies enchantments that only affect specific mob types
        such as smite which only works with undead mobs
         */
        double bonusSecondaryEnchantmentDamage = secondaryEnchantmentDamageIncrease(player.getInventory().getItemInMainHand(), eliteMob.getLivingEntity());
        double newTargetHitsToKill = (eliteMob.getDefaultMaxHealth() / 2) - bonusSecondaryEnchantmentDamage;

        double finalDamage = getCooledAttackStrength(player) * (maxHealth / newTargetHitsToKill +
                maxHealth / newTargetHitsToKill * (tierDifference) * TO_ELITE_DAMAGE_TIER_HANDICAP);

        //Apply health multiplier
        finalDamage /= eliteMob.getHealthMultiplier();

        /*
        Make sure that players are dealing at least 1 damage as to not create unkillable bosses
         */
        return finalDamage < 1 ? 1 : finalDamage;

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

    /**
     * Gets the obfuscated main enchant in an item's lore
     *
     * @param deobfuscatedLore
     * @param enchantment
     * @return
     */
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
