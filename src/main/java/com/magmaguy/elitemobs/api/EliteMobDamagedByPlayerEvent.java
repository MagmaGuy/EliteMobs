package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.Round;
import com.magmaguy.elitemobs.utils.VersionChecker;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.concurrent.ThreadLocalRandom;

public class EliteMobDamagedByPlayerEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Entity entity;
    @Getter
    private final EliteEntity eliteMobEntity;
    @Getter
    private final Player player;
    @Getter
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    @Getter
    private final double damage;
    @Getter
    private final boolean criticalStrike;
    @Getter
    private final boolean isCustomDamage;
    @Getter
    private final double damageModifier;
    @Getter
    public boolean rangedAttack;
    @Getter
    private boolean isCancelled = false;

    public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity,
                                        Player player,
                                        EntityDamageByEntityEvent event,
                                        double damage,
                                        boolean criticalStrike,
                                        boolean isCustomDamage,
                                        double damageModifier) {
        this.damage = damage;
        this.entity = eliteEntity.getLivingEntity();
        this.eliteMobEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.rangedAttack = event.getDamager() instanceof Projectile;
        this.criticalStrike = criticalStrike;
        this.isCustomDamage = isCustomDamage;
        this.damageModifier = damageModifier;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        entityDamageByEntityEvent.setCancelled(b);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static class EliteMobDamagedByPlayerEventFilter implements Listener {
        public static boolean bypass = false;

        private static double getThornsDamage(Player player) {
            if (!ItemSettingsConfig.isUseEliteEnchantments()) return 0D;
            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            int thornsLevel = 0;
            if (elitePlayerInventory.helmet.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.helmet.thornsLevel - Enchantment.THORNS.getMaxLevel();
            if (elitePlayerInventory.chestplate.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.chestplate.thornsLevel - Enchantment.THORNS.getMaxLevel();
            if (elitePlayerInventory.leggings.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.helmet.thornsLevel - Enchantment.THORNS.getMaxLevel();
            if (elitePlayerInventory.boots.thornsLevel > Enchantment.THORNS.getMaxLevel())
                thornsLevel += elitePlayerInventory.boots.thornsLevel - Enchantment.THORNS.getMaxLevel();
            return thornsLevel * 2.5D;
        }

        /**
         * Gets the amount of damage dealt by EliteMobs-specific features
         *
         * @param player Damager
         * @return Bonus damage applied
         */
        private static double getEliteMeleeDamage(Player player, LivingEntity livingEntity) {
            if (player.getInventory().getItemInMainHand().getType().equals(Material.BOW) ||
                    player.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW))
                return 0.0;
            double eliteDamage = ElitePlayerInventory.getPlayer(player).getEliteDamage(true);
            double bonusEliteDamage = secondaryEnchantmentDamageIncrease(player, livingEntity);
            return (eliteDamage + bonusEliteDamage) * player.getAttackCooldown();
        }

        private static double getEliteRangedDamage(Projectile arrow) {
            //note: the arrow velocity amplitude at full load is about 2.8
            double arrowSpeedMultiplier = Math.sqrt(Math.pow(arrow.getVelocity().getX(), 2D) + Math.pow(arrow.getVelocity().getY(), 2D) + Math.pow(arrow.getVelocity().getZ(), 2D));
            arrowSpeedMultiplier /= 4.0D;
            double arrowDamage = EliteItemManager.getArrowEliteDamage(arrow);
            return arrowSpeedMultiplier * arrowDamage;
        }

        private static double getCustomDamageModifier(EliteEntity eliteEntity, Player player) {
            if (!(eliteEntity instanceof CustomBossEntity)) return 1;
            //This doesn't really take into account people switching their weapon out on ranged attacks. That's probably fine.
            return ((CustomBossEntity) eliteEntity).getDamageModifier(player.getInventory().getItemInMainHand().getType());
        }

        private static double secondaryEnchantmentDamageIncrease(Player player, LivingEntity livingEntity) {
            if (ItemSettingsConfig.isUseEliteEnchantments()) return 0D;
            if (livingEntity instanceof Spider || livingEntity instanceof Silverfish) {
                int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageArthropodsLevel(player.getInventory().getItemInMainHand(), false);
                level -= Enchantment.DAMAGE_ARTHROPODS.getMaxLevel();
                if (level < 1) return 0D;
                return level * 2.5D;
            }
            if (livingEntity instanceof Zombie ||
                    livingEntity instanceof Skeleton ||
                    livingEntity instanceof Wither ||
                    livingEntity instanceof SkeletonHorse ||
                    livingEntity instanceof ZombieHorse ||
                    !VersionChecker.serverVersionOlderThan(16, 0)
                            && livingEntity.getType().equals(EntityType.ZOMBIFIED_PIGLIN)) {
                int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageUndeadLevel(player.getInventory().getItemInMainHand(), false);
                level -= Enchantment.DAMAGE_UNDEAD.getMaxLevel();
                if (level < 1) return 0D;
                return level * 2.5D;
            }
            return 0;
        }

        private static boolean isCriticalHit(Player player) {
            double criticalStrike = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getCritChance(false);
            criticalStrike += (GuildRank.critBonusValue(GuildRank.getGuildPrestigeRank(player), GuildRank.getActiveGuildRank(player)) / 100);
            return ThreadLocalRandom.current().nextDouble() < criticalStrike;
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerShootArrow(ProjectileLaunchEvent event) {
            if (!(event.getEntity().getShooter() instanceof Player)) return;
            EliteItemManager.tagArrow(event.getEntity());
        }

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobAttack(EntityDamageByEntityEvent event) {
            if (event.getEntity().getType().equals(EntityType.ENDER_DRAGON) && ((EnderDragon) event.getEntity()).getPhase().equals(EnderDragon.Phase.DYING))
                return;
            LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
            if (livingEntity == null) return;
            if (!livingEntity.getType().equals(EntityType.PLAYER)) return;
            Player player = (Player) livingEntity;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            //Living entity is sometimes null when the damage is dealt to an already dead entity - might happen with mcmmo due to DOTs and stuff
            if (eliteEntity == null || !eliteEntity.isValid()) return;

            /*
            From this point on, the damage is confirmed to be processed by EliteMobs
             */

            //nullify vanilla reductions, this is needed because boss armor is just cosmetic
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
                if (event.isApplicable(modifier) && modifier != EntityDamageEvent.DamageModifier.BASE)
                    event.setDamage(modifier, 0);

            //If the damage wasn't caused by an elite item, just allow the event to go as raw
            double damage = event.getDamage();
            double eliteDamage = 0;
            //Sometimes players are "fake" due to npc plugins
            boolean validPlayer = !player.hasMetadata("NPC") && ElitePlayerInventory.getPlayer(player) != null;

            if (validPlayer && event.getCause().equals(EntityDamageEvent.DamageCause.THORNS))
                //Thorns are their own kind of damage
                eliteDamage = getThornsDamage(player);
            else if (validPlayer &&
                    (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) ||
                            event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) &&
                                    EliteItemManager.isEliteMobsItem(player.getInventory().getItemInMainHand())))
                eliteDamage = getEliteMeleeDamage(player, livingEntity);
            else if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                //Scan arrow for arrow damage
                eliteDamage = getEliteRangedDamage((Projectile) event.getDamager());

            double damageModifier = getCustomDamageModifier(eliteEntity, player);

            damage = Round.twoDecimalPlaces((damage + eliteDamage) * damageModifier * MobCombatSettingsConfig.getDamageToEliteMultiplier());

            boolean criticalHit = false;

            if (validPlayer) {
                isCriticalHit(player);
                if (criticalHit) damage *= 1.5;
            }

            EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent = new EliteMobDamagedByPlayerEvent(
                    eliteEntity,
                    player,
                    event,
                    damage,
                    criticalHit,
                    bypass,
                    damageModifier);

            new EventCaller(eliteMobDamagedByPlayerEvent);

            if (eliteMobDamagedByPlayerEvent.isCancelled) {
                event.setCancelled(true);
                return;
            }

            if (validPlayer) {
                //Time to deal custom damage!
                eliteEntity.addDamager(player, damage);
            }

            //Dragons need special handling due to their custom deaths
            if (eliteEntity.getLivingEntity() != null &&
                    eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON) &&
                    eliteEntity.getLivingEntity().getHealth() - damage < 1) {
                if (eliteEntity.isDying()) return;
                damage = 0;
                event.setCancelled(true);
                ((EnderDragon) eliteEntity.getLivingEntity()).setPhase(EnderDragon.Phase.DYING);
                eliteEntity.setDying(true);
                //remove the dragon after it is done with the light show, this death doesn't show up on events
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> new EventCaller(new EliteMobDeathEvent(eliteEntity)), 200);
            }

            event.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);

            eliteEntity.syncPluginHealth(((LivingEntity) event.getEntity()).getHealth());

            //No antiexploit checks for dungeons
            if (!(EliteMobs.worldGuardIsEnabled &&
                    !WorldGuardFlagChecker.checkFlag(eliteEntity.getLivingEntity().getLocation(), WorldGuardCompatibility.getEliteMobsAntiExploitFlag())) &&
                    event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) &&
                    !eliteEntity.isInAntiExploitCooldown())
                Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerAntiExploitEvent(eliteEntity, eliteMobDamagedByPlayerEvent));
        }

    }

}
