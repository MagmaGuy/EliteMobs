/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.mobcustomizer;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.mobcustomizer.displays.DamageDisplay;
import com.magmaguy.elitemobs.mobcustomizer.displays.HealthDisplay;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageAdjuster implements Listener {


    /*
    The math presented here for damage is based on server with hard difficulty.
    More specifically, it's based around creating a fair, scalable challenge around Zombies.
    With each level, mobs gain 10% more power (2 health and 0.45 damage) (based on their base attributes, not based on each subsequent level).
    The item tiers are based around swords.
    It should take ~10 hits to kill an Elite Mob.
    It should take ~5 hits to die from an Elite Mob.
     */

    /*
    Goal hits to kill: 10
    NOTE: since the system doesn't take into account the base health of the mob, mobs take a few more hits than what is
    set here to kill
     */

    public static final double PER_LEVEL_POWER_INCREASE = 0.1;
    public static final double HITS_TO_KILL = 10;
    public static final double BASE_DAMAGE = 4.5;
    public static final double BASE_HEALTH_INCREASE = 20 * PER_LEVEL_POWER_INCREASE;

    /*
    Determine vanilla weapon damage
     */

    public static final double DIAMOND_TIER_DAMAGE = 7;
    public static final double IRON_TIER_DAMAGE = 6;
    public static final double STONE_CHAIN_TIER_DAMAGE = 5;
    public static final double GOLD_WOOD_LEATHER_TIER_DAMAGE = 4;

    /*
    Find how much damage gets dealt in the goal HTK (HITS TO KILL)
    Since raw damage is dealt to mobs, this means that this is also mob health
     */

    public static final double HTK_DIAMOND = HITS_TO_KILL * DIAMOND_TIER_DAMAGE;
    public static final double HTK_IRON = HITS_TO_KILL * IRON_TIER_DAMAGE;
    public static final double HTK_STONE_CHAIN = HITS_TO_KILL * STONE_CHAIN_TIER_DAMAGE;
    public static final double HTK_GOLD_WOOD_LEATHER = HITS_TO_KILL * GOLD_WOOD_LEATHER_TIER_DAMAGE;

    /*
    Determine appropriate mob level for tier knowing each level increases mob health by 10% (2) and the previously
    calculated mob health ignoring base health for scaling's sake (only accounts for the 2% increase over time)
     */

    public static final double DIAMOND_TIER_LEVEL = HTK_DIAMOND / BASE_HEALTH_INCREASE;
    public static final double IRON_TIER_LEVEL = HTK_IRON / BASE_HEALTH_INCREASE;
    public static final double STONE_CHAIN_TIER_LEVEL = HTK_STONE_CHAIN / BASE_HEALTH_INCREASE;
    public static final double GOLD_WOOD_LEATHER_TIER_LEVEL = HTK_GOLD_WOOD_LEATHER / BASE_HEALTH_INCREASE;

    /*
    Determine the necessary damage reduction from armor in order to scale to mob damage, knowing that mob damage should
    always aim to nullify armor (since default damage is 4.5 which is close to the desired value)
    R = reduction    x = mob level
    R(4.5+0.45x) = 4.5 <=> R = 4.5/(4.5+0.45x)
    0.45 is picked as it is 10% of 4.5 which is the default damage of a zombie in hard mode
     */

    public static final double DIAMOND_SET_REDUCTION = 1 - BASE_DAMAGE / (BASE_DAMAGE + BASE_DAMAGE * PER_LEVEL_POWER_INCREASE * DIAMOND_TIER_LEVEL);
    public static final double IRON_SET_REDUCTION = 1 - BASE_DAMAGE / (BASE_DAMAGE + BASE_DAMAGE * PER_LEVEL_POWER_INCREASE * IRON_TIER_LEVEL);
    public static final double STONE_CHAIN_SET_REDUCTION = 1 - BASE_DAMAGE / (BASE_DAMAGE + BASE_DAMAGE * PER_LEVEL_POWER_INCREASE * STONE_CHAIN_TIER_LEVEL);
    public static final double GOLD_WOOD_LEATHER_SET_REDUCTION = 1 - BASE_DAMAGE / (BASE_DAMAGE + BASE_DAMAGE * PER_LEVEL_POWER_INCREASE * GOLD_WOOD_LEATHER_TIER_LEVEL);

    /*
    Determine damage reduction per armor slot for 4 armor slots
     */

    public static final double DIAMOND_REDUCTION = DIAMOND_SET_REDUCTION / 4;
    public static final double IRON_REDUCTION = IRON_SET_REDUCTION / 4;
    public static final double STONE_CHAIN_REDUCTION = STONE_CHAIN_SET_REDUCTION / 4;
    public static final double GOLD_WOOD_LEATHER_REDUCTION = GOLD_WOOD_LEATHER_SET_REDUCTION / 4;

    /*
    Determine level increase resulting from a potion effect or enchantment blocking 1 damage
    To calculate this, assume that a full set of armor gets a +1 enchantment, resulting in +4 defense
    Knowing the damage increase per level is 0.45
     */

    public static final double DEFENSIVE_ENCHANTMENT_OR_POTION_EFFECT_LEVEL_INCREASE = 4 / (BASE_DAMAGE * PER_LEVEL_POWER_INCREASE);

    /*
    Determine how much damage 1 level of an enchantment or potion effect should do taking into account that it is a part
    of a full set of armor where everything is presumed to have 1 level of enchantments
    Remember that all damage is raw and that it should always roughly take the same amount of hits to kill an elite mob
     */

    public static final double TIER_DAMAGE_INCREASE = (DEFENSIVE_ENCHANTMENT_OR_POTION_EFFECT_LEVEL_INCREASE * BASE_HEALTH_INCREASE) / HITS_TO_KILL;

    /*
    Determine the threat increase from 1 level of potion effects or enchantments. Since these values are bundled into sets,
    the level increase from 4 defensive enchantment or potion effect levels should be the base for threat calculation.
    Since weapons are taken into account and 1 level of offensive enchants is supposed to cover for the level increase of
    4 protection enchantments, this means the threat calc needs to get distributed over 5 items (from the original 4).
     */

    public static final double ENCHANTMENT_OR_POTION_EFFECT_THREAT_INCREMENTER = DEFENSIVE_ENCHANTMENT_OR_POTION_EFFECT_LEVEL_INCREASE * 4 / 5;


    //this event deals with elite mobs damaging players
    @EventHandler(priority = EventPriority.LOWEST)
    public void eliteMobDamageHandler(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof LivingEntity && !event.getDamager().hasMetadata(MetadataHandler.ELITE_MOB_MD))
            return;
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity &&
                !((LivingEntity) ((Projectile) event.getDamager()).getShooter()).hasMetadata(MetadataHandler.ELITE_MOB_MD))
            return;
        if (event.getDamager() instanceof Projectile && !(((Projectile) event.getDamager()).getShooter() instanceof LivingEntity))
            return;

        LivingEntity livingEntity = null;

        if (event.getDamager() instanceof LivingEntity) livingEntity = (LivingEntity) event.getDamager();
        else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
            livingEntity = (LivingEntity) ((Projectile) event.getDamager()).getShooter();

        if (livingEntity == null) return;

        int mobLevel = livingEntity.getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

        Player player = (Player) event.getEntity();

        double newDamage = newAdjustedDamageToPlayer(event.getDamage(), mobLevel, player);


        if (event.getDamager() instanceof Arrow) {
            newDamage -= arrowEnchantmentReduction(player);
        } else if (event.getDamager() instanceof Fireball || event.getDamager() instanceof Creeper) {
            newDamage -= explosionEnchantmentReduction(player);
        }

        //this checks for simple protection, which always applies
        newDamage -= enchantmentReduction(player);


        newDamage -= potionEffectReduction(player);

        //Prevent untouchable armor and 1-shots
        if (newDamage < 1) newDamage = 1;
        if (newDamage > 19) newDamage = 19;

        if (player.getHealth() - newDamage < 0) {

            player.setHealth(0);

        } else {

            player.setHealth(player.getHealth() - newDamage);

        }

        event.setDamage(0);

    }

    //Deal with creeper explosions
    @EventHandler
    public void onEliteCreeperDetonation(ExplosionPrimeEvent event) {

        if (!(event.getEntity() instanceof Creeper && event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)))
            return;

        for (PotionEffect potionEffect : ((Creeper) event.getEntity()).getActivePotionEffects()) {

            ((Creeper) event.getEntity()).removePotionEffect(potionEffect.getType());

        }

        int mobLevel = event.getEntity().getMetadata(MetadataHandler.ELITE_MOB_MD).get(0).asInt();

        if (mobLevel <= 1) {

            return;

        }

        float newExplosionRange = (float) (event.getRadius() + Math.ceil(0.1 * mobLevel * event.getRadius() * ConfigValues.defaultConfig.getDouble(DefaultConfig.ELITE_CREEPER_EXPLOSION_MULTIPLIER)));

        if (newExplosionRange > Integer.MAX_VALUE) {

            newExplosionRange = Integer.MAX_VALUE;

        }

        int convertedExplosionRange = (int) newExplosionRange;

        event.setRadius(convertedExplosionRange);

    }


    private double newAdjustedDamageToPlayer(double rawDamage, int mobLevel, Player player) {

        double newRawDamage = rawDamage + rawDamage * 0.1 * mobLevel;

        double postArmorReductionDamage = newRawDamage - totalArmorReduction(player) * newRawDamage;

        return postArmorReductionDamage;

    }

    private double totalArmorReduction(Player player) {

        double armorReduction = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {

            if (itemStack != null && itemStack.getType() != Material.AIR) {

                armorReduction += materialGrabber(itemStack);

            }

        }

        return armorReduction;

    }

    private double materialGrabber(ItemStack itemStack) {

        return materialRating(itemStack.getType());

    }

    private double materialRating(Material material) {

        switch (material) {

            case DIAMOND_HELMET:
                return DIAMOND_REDUCTION;
            case DIAMOND_CHESTPLATE:
                return DIAMOND_REDUCTION;
            case DIAMOND_LEGGINGS:
                return DIAMOND_REDUCTION;
            case DIAMOND_BOOTS:
                return DIAMOND_REDUCTION;
            case IRON_HELMET:
                return IRON_REDUCTION;
            case IRON_CHESTPLATE:
                return IRON_REDUCTION;
            case IRON_LEGGINGS:
                return IRON_REDUCTION;
            case IRON_BOOTS:
                return IRON_REDUCTION;
            case CHAINMAIL_HELMET:
                return STONE_CHAIN_REDUCTION;
            case CHAINMAIL_CHESTPLATE:
                return STONE_CHAIN_REDUCTION;
            case CHAINMAIL_LEGGINGS:
                return STONE_CHAIN_REDUCTION;
            case CHAINMAIL_BOOTS:
                return STONE_CHAIN_REDUCTION;
            case GOLD_HELMET:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case GOLD_CHESTPLATE:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case GOLD_LEGGINGS:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case GOLD_BOOTS:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case LEATHER_HELMET:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case LEATHER_CHESTPLATE:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case LEATHER_LEGGINGS:
                return GOLD_WOOD_LEATHER_REDUCTION;
            case LEATHER_BOOTS:
                return GOLD_WOOD_LEATHER_REDUCTION;

        }

        return 0;

    }

    /*
    For the purposes of Elite Mobs, every enchantment reduces damage by 1
    This isn't done through percentual scaling because not doing so allows for nearly infinite progression
     */

    private int enchantmentReduction(Player player) {

        int reducedDamage = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {

            if (itemStack != null && !(itemStack.getType().equals(Material.AIR))) {

                if (itemStack.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {

                    reducedDamage += itemStack.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);

                }

            }

        }

        return reducedDamage;

    }

    private int arrowEnchantmentReduction(Player player) {

        int reducedDamage = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {

            if (itemStack != null && !(itemStack.getType().equals(Material.AIR))) {

                if (itemStack.containsEnchantment(Enchantment.PROTECTION_PROJECTILE)) {

                    reducedDamage += itemStack.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);

                }

            }

        }

        return reducedDamage;

    }

    private int explosionEnchantmentReduction(Player player) {

        int reducedDamage = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {

            if (itemStack != null && !(itemStack.getType().equals(Material.AIR))) {

                if (itemStack.containsEnchantment(Enchantment.PROTECTION_EXPLOSIONS)) {

                    reducedDamage += itemStack.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);

                }

            }

        }

        return reducedDamage;

    }

    /*
    The damage resistance potion effect will only reduce damage by 1.
     */

    private int potionEffectReduction(Player player) {

        int reducedDamage = 0;

        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {

            reducedDamage = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() + 1;

        }

        return reducedDamage;

    }

    /*
    Elite Mobs have purely cosmetic armor. It doesn't do any actual damage reduction, as that would seriously complicate
    the underlying math for dealing damage for no real reason.
     */

    /*
    This deals with elite mobs being damaged by non-player entities
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void eliteMobDamageGeneric(EntityDamageEvent event) {

        if (event.isCancelled()) return;
        if (!event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;

        LivingEntity livingEntity = (LivingEntity) event.getEntity();

        if (livingEntity.getHealth() - event.getDamage() < 0) {

            livingEntity.setHealth(0);

        } else {

            livingEntity.setHealth(livingEntity.getHealth() - event.getDamage());

        }

        event.setDamage(0);

    }


    /*
    This deals with players hitting the elite mob
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void eliteMobDamageByPlayer(EntityDamageByEntityEvent event) {

        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player || !(event.getEntity() instanceof LivingEntity) ||
                !event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD)) return;

        LivingEntity damagingLivingEntity = null;
        LivingEntity damagedLivingEntity = (LivingEntity) event.getEntity();

        if (event.getDamager() instanceof LivingEntity) damagingLivingEntity = (LivingEntity) event.getDamager();
        else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity)
            damagingLivingEntity = (LivingEntity) ((Projectile) event.getDamager()).getShooter();

        if (!(damagingLivingEntity instanceof Player)) {

            if (damagedLivingEntity.getHealth() - event.getDamage() < 0) {

                damagedLivingEntity.setHealth(0);

            } else {

                damagedLivingEntity.setHealth(damagedLivingEntity.getHealth() - event.getDamage());

            }

        } else {

            //added damage done by players is fixed, not percentual
            double newDamage = event.getDamage();
            int damageIncrease = 0;

            Player player = (Player) damagingLivingEntity;

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {

                if (mainHand != null && mainHand.getType().equals(Material.BOW) && offHand != null && offHand.getType().equals(Material.BOW)) {

                    damageIncrease += addedBowEnchantmentComparator(Enchantment.ARROW_DAMAGE, mainHand);

                } else if (mainHand != null && mainHand.getType().equals(Material.BOW)) {

                    damageIncrease += addedBowEnchantmentComparator(Enchantment.ARROW_DAMAGE, mainHand);

                } else if (offHand != null && offHand.getType().equals(Material.BOW)) {

                    damageIncrease += addedBowEnchantmentComparator(Enchantment.ARROW_DAMAGE, offHand);

                }

            } else {

                //Minecraft can only do main hand damage from this point on
                if (mainHand != null) {

                    damageIncrease += addedOtherItemEnchantmentComparator(mainHand, damagedLivingEntity);

                }

            }

            damageIncrease += addedPotionEffect(player);

            newDamage += damageIncrease * TIER_DAMAGE_INCREASE;

            if (damagedLivingEntity.getHealth() - newDamage < 0) {

                damagedLivingEntity.setHealth(0);

            } else {

                damagedLivingEntity.setHealth(damagedLivingEntity.getHealth() - newDamage);

            }

            DamageDisplay.displayDamage(damagedLivingEntity, newDamage);
            HealthDisplay.displayHealth(damagedLivingEntity);

        }

        event.setDamage(0);

    }

    private int addedBowEnchantmentComparator(Enchantment enchantment, ItemStack itemStack) {

        int addedDamage = 0;

        if (itemStack.containsEnchantment(enchantment)) {

            addedDamage += itemStack.getEnchantmentLevel(enchantment);

        }

        return addedDamage;

    }

    private int addedOtherItemEnchantmentComparator(ItemStack itemStack, LivingEntity livingEntity) {

        int addedDamage = 0;

        if (itemStack.containsEnchantment(Enchantment.DAMAGE_ALL)) {

            addedDamage += itemStack.getEnchantmentLevel(Enchantment.DAMAGE_ALL);

        }

        if (livingEntity instanceof Spider) {

            if (itemStack.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) {

                addedDamage += itemStack.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);

            }

        }

        if (livingEntity instanceof Zombie || livingEntity instanceof Skeleton || livingEntity instanceof Wither || livingEntity instanceof SkeletonHorse) {

            if (itemStack.containsEnchantment(Enchantment.DAMAGE_UNDEAD)) {

                addedDamage += itemStack.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);

            }

        }

        return addedDamage;

    }

    private int addedPotionEffect(Player player) {

        int addedDamage = 0;

        if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {

            addedDamage += player.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() + 1;

        }

        return addedDamage;

    }


}
