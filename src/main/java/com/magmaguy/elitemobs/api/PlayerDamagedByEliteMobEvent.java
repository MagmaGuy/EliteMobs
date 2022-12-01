package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final Player player;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final Projectile projectile;
    private boolean isCancelled = false;

    public PlayerDamagedByEliteMobEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, Projectile projectile) {
        this.entity = event.getEntity();
        this.eliteEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.projectile = projectile;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EliteEntity getEliteMobEntity() {
        return this.eliteEntity;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
        return this.entityDamageByEntityEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
        entityDamageByEntityEvent.setCancelled(b);
    }

    public static class PlayerDamagedByEliteMobEventFilter implements Listener {
        @Getter
        @Setter
        private static boolean bypass = false;
        @Getter
        @Setter
        private static double specialMultiplier = 1;

        private static double eliteToPlayerDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
            double baseDamage = EliteMobProperties.getBaselineDamage(eliteEntity.getLivingEntity().getType(), eliteEntity);
            //Case for creeper and ghast explosions
            if (eliteEntity.getLivingEntity() != null && player.isValid() && player.getLocation().getWorld().equals(eliteEntity.getLivingEntity().getWorld()))
                if (eliteEntity.getLivingEntity().getType().equals(EntityType.CREEPER)) {
                    Creeper creeper = (Creeper) eliteEntity.getLivingEntity();
                    double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                    double distanceAttenuation = 1 - distance / creeper.getExplosionRadius();
                    distanceAttenuation = distanceAttenuation < 0 ? 0 : distanceAttenuation;
                    baseDamage *= distanceAttenuation;
                } else if (eliteEntity.getLivingEntity().getType().equals(EntityType.GHAST) &&
                        event.getDamager().getType().equals(EntityType.FIREBALL)) {
                    double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                    double distanceAttenuation = 1 - distance / ((Fireball) event.getDamager()).getYield();
                    distanceAttenuation = distanceAttenuation < 0 ? 0 : distanceAttenuation;
                    baseDamage *= distanceAttenuation;
                }
            double bonusDamage = eliteEntity.getLevel();

            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            if (elitePlayerInventory == null) return 0;

            double damageReduction = elitePlayerInventory.getEliteDefense(true);
            if (event.getDamager() instanceof AbstractArrow)
                damageReduction += elitePlayerInventory.getEliteProjectileProtection(true);
            if (event.getDamager() instanceof Fireball || event.getDamager() instanceof Creeper) {
                damageReduction += elitePlayerInventory.getEliteBlastProtection(true);
            }

            double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();
            double potionEffectDamageReduction = 0;

            if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE))
                potionEffectDamageReduction = (player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).
                        getAmplifier() + 1) * MobCombatSettingsConfig.getResistanceDamageMultiplier();

            double finalDamage = ((baseDamage + bonusDamage ) * customBossDamageMultiplier * specialMultiplier - damageReduction - potionEffectDamageReduction ) *
                    MobCombatSettingsConfig.getDamageToPlayerMultiplier();

            if (specialMultiplier != 1) specialMultiplier = 1;

            double playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            //Prevent 1-shots and players getting healed from hits
            finalDamage = Math.max(finalDamage, 1);
            finalDamage = Math.min(finalDamage, playerMaxHealth - 1);

            return finalDamage;
        }

        //Remove potion effects of creepers when they blow up because Minecraft passes those effects to players, and they are infinite
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        private void explosionEvent(EntityExplodeEvent event) {
            if (event.getEntity().getType().equals(EntityType.CREEPER) && EntityTracker.isEliteMob(event.getEntity())) {
                //by default minecraft spreads potion effects
                Set<PotionEffect> potionEffects = new HashSet<>(((Creeper) event.getEntity()).getActivePotionEffects());
                potionEffects.forEach(potionEffectType -> ((Creeper) event.getEntity()).removePotionEffect(potionEffectType.getType()));
            }
        }

        @EventHandler
        public void onEliteDamagePlayer(EntityDamageByEntityEvent event) {
            if (event.isCancelled()) {
                bypass = false;
                return;
            }
            if (!(event.getEntity() instanceof Player player)) return;

            //citizens
            if (player.hasMetadata("NPC") || ElitePlayerInventory.getPlayer(player) == null)
                return;

            Projectile projectile = null;

            EliteEntity eliteEntity = null;
            if (event.getDamager() instanceof LivingEntity)
                eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
            else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof LivingEntity) {
                eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity) ((Projectile) event.getDamager()).getShooter());
                projectile = (Projectile) event.getDamager();
            } else if (event.getDamager().getType().equals(EntityType.EVOKER_FANGS))
                if (((EvokerFangs) event.getDamager()).getOwner() != null)
                    eliteEntity = EntityTracker.getEliteMobEntity(((EvokerFangs) event.getDamager()).getOwner());

            if (eliteEntity == null || eliteEntity.getLivingEntity() == null) return;

            //dodge chance
            if (ThreadLocalRandom.current().nextDouble() < GuildRank.dodgeBonusValue(GuildRank.getGuildPrestigeRank(player), GuildRank.getActiveGuildRank(player)) / 100) {
                player.sendTitle(" ", "Dodged!");
                event.setCancelled(true);
                return;
            }

            boolean blocking = false;

            if (player.isBlocking()) {
                blocking = true;
                if (player.getInventory().getItemInOffHand().getType().equals(Material.SHIELD)) {
                    ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                    org.bukkit.inventory.meta.Damageable damageable = (Damageable) itemMeta;

                    if (player.getInventory().getItemInOffHand().getItemMeta().hasEnchant(Enchantment.DURABILITY) &&
                            player.getInventory().getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.DURABILITY) / 20D > ThreadLocalRandom.current().nextDouble())
                        damageable.setDamage(damageable.getDamage() + 5);
                    player.getInventory().getItemInOffHand().setItemMeta(itemMeta);
                    if (Material.SHIELD.getMaxDurability() < damageable.getDamage())
                        player.getInventory().setItemInOffHand(null);
                }

                if (event.getDamager() instanceof Projectile) {
                    bypass = false;
                    event.getDamager().remove();
                    return;
                }
            }

            PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent = new PlayerDamagedByEliteMobEvent(eliteEntity, player, event, projectile);
            if (!playerDamagedByEliteMobEvent.isCancelled)
                new EventCaller(playerDamagedByEliteMobEvent);

            if (playerDamagedByEliteMobEvent.isCancelled()) {
                bypass = false;
                return;
            }
            double newDamage = eliteToPlayerDamageFormula(player, eliteEntity, event);
            //Blocking reduces damage by 80%
            if (blocking)
                newDamage = newDamage - newDamage * MobCombatSettingsConfig.getBlockingDamageReduction();
            //nullify vanilla reductions
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
                if (event.isApplicable(modifier))
                    event.setDamage(modifier, 0);

            if (bypass) {
                //Use raw damage in case of bypass
                newDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
                bypass = false;
            }

            //Set the final damage value
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

            //Deal with the player getting killed todo: this is a bit busted, fix
            if (player.getHealth() - event.getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, eliteEntity.getLivingEntity()));

        }

    }

}
