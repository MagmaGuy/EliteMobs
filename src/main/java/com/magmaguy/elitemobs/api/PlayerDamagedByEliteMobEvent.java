package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.AttributeManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerDamagedByEliteMobEvent extends EliteDamageEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final EliteEntity eliteEntity;
    private final Player player;
    private final EntityDamageByEntityEvent entityDamageByEntityEvent;
    private final Projectile projectile;

    public PlayerDamagedByEliteMobEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, Projectile projectile, double damage) {
        super(damage, event);
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

    /**
     * Unified method to apply all active ARMOR skill bonuses to this damage event.
     * This is the single entry point for the skill bonus system to modify incoming damage.
     * <p>
     * Processes all active armor skills, applying damage reduction from
     * PASSIVE, CONDITIONAL, COOLDOWN, and PROC skills.
     *
     * @return true if damage was completely negated (e.g., by dodge or death prevention), false otherwise
     */
    public boolean applySkillBonuses() {
        if (player == null || player.hasMetadata("NPC")) return false;
        if (!ElitePlayerInventory.playerInventories.containsKey(player.getUniqueId())) return false;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.ARMOR);
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), SkillType.ARMOR);

        double damageMultiplier = 1.0;

        // Process each active armor skill
        for (String skillId : activeSkillIds) {
            SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
            if (skill == null || !skill.isEnabled()) continue;
            if (!skill.meetsLevelRequirement(skillLevel)) continue;

            DefensiveSkillResult result = processDefensiveSkill(skill, skillLevel);

            // Check for complete damage negation (dodge, death prevention, etc.)
            if (result.negatesDamage) {
                setCancelled(true);
                return true;
            }

            damageMultiplier *= result.multiplier;
        }

        // Apply the combined damage reduction
        if (damageMultiplier != 1.0) {
            setDamage(getDamage() * damageMultiplier);
        }

        return false;
    }

    /**
     * Processes a single defensive skill and returns its result.
     * Tracks proc counts for testing purposes.
     */
    private DefensiveSkillResult processDefensiveSkill(SkillBonus skill, int skillLevel) {
        return switch (skill.getBonusType()) {
            case PASSIVE -> {
                skill.incrementProcCount(player); // Track activation
                yield new DefensiveSkillResult(1.0 - skill.getBonusValue(skillLevel), false);
            }
            case CONDITIONAL -> {
                if (skill instanceof ConditionalSkill conditionalSkill) {
                    if (conditionalSkill.conditionMet(player, this)) {
                        skill.incrementProcCount(player); // Track activation
                        yield new DefensiveSkillResult(1.0 - conditionalSkill.getConditionalBonus(skillLevel), false);
                    }
                }
                yield new DefensiveSkillResult(1.0, false);
            }
            case COOLDOWN -> {
                if (skill instanceof CooldownSkill cooldownSkill) {
                    if (!cooldownSkill.isOnCooldown(player)) {
                        // Check for death prevention skills (like Last Stand)
                        if (player.getHealth() - getDamage() <= 0) {
                            cooldownSkill.onActivate(player, this);
                            cooldownSkill.startCooldown(player, skillLevel);
                            skill.incrementProcCount(player); // Track activation
                            yield new DefensiveSkillResult(1.0, true); // Damage negated
                        }
                    }
                }
                yield new DefensiveSkillResult(1.0, false);
            }
            case PROC -> {
                if (skill instanceof ProcSkill procSkill) {
                    double procChance = procSkill.getProcChance(skillLevel);
                    if (ThreadLocalRandom.current().nextDouble() < procChance) {
                        procSkill.onProc(player, this);
                        skill.incrementProcCount(player); // Track proc
                        yield new DefensiveSkillResult(1.0 - skill.getBonusValue(skillLevel), false);
                    }
                }
                yield new DefensiveSkillResult(1.0, false);
            }
            default -> new DefensiveSkillResult(1.0, false);
        };
    }

    /**
     * Result of processing a defensive skill.
     */
    private record DefensiveSkillResult(double multiplier, boolean negatesDamage) {}

    //Thing that launches the event
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
            double bonusDamage = eliteEntity.getLevel() * .5; //A .5 increase in damage for every level the mob has

            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            if (elitePlayerInventory == null) return 0;

            // Get item's elite defense (represents item level contribution)
            double itemEliteDefense = elitePlayerInventory.getEliteDefense(true);

            // Get player's armor skill level
            long armorSkillXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
            int armorSkillLevel = SkillXPCalculator.levelFromTotalXP(armorSkillXP);

            // Calculate skill defense contribution (similar formula to item defense)
            // Each armor skill level provides 0.25 defense (1/4 because there are 4 armor pieces)
            double skillDefense = armorSkillLevel * 0.25;

            // Apply 50/50 split for damage reduction
            // The 0.5 multiplier on defense is kept for balance (mobs gain 0.5 damage per level)
            double effectiveDefense = ((skillDefense * CombatSystem.SKILL_CONTRIBUTION_RATIO)
                    + (itemEliteDefense * CombatSystem.ITEM_CONTRIBUTION_RATIO)) * 0.5;

            double damageReduction = effectiveDefense;
            if (event.getDamager() instanceof AbstractArrow)
                damageReduction += elitePlayerInventory.getEliteProjectileProtection(true);
            if (event.getDamager() instanceof Fireball || event.getDamager() instanceof Creeper) {
                damageReduction += elitePlayerInventory.getEliteBlastProtection(true);
            }

            double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();
            double potionEffectDamageReduction = 0;

            if (player.hasPotionEffect(PotionEffectType.RESISTANCE))
                potionEffectDamageReduction = (player.getPotionEffect(PotionEffectType.RESISTANCE).
                        getAmplifier() + 1) * MobCombatSettingsConfig.getResistanceDamageMultiplier();

            double finalDamage;
            if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
                finalDamage = Math.max(baseDamage + bonusDamage - damageReduction - potionEffectDamageReduction, 1) *
                        customBossDamageMultiplier * specialMultiplier * MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
            else
                finalDamage = Math.max(baseDamage + bonusDamage - damageReduction - potionEffectDamageReduction, 1) *
                        customBossDamageMultiplier * specialMultiplier * MobCombatSettingsConfig.getDamageToPlayerMultiplier();

            // Level scaling is now built into mob HP (exponential scaling)
            // rather than modifying damage. Mob damage is based on their level directly.

            if (specialMultiplier != 1) specialMultiplier = 1;

            double playerMaxHealth = AttributeManager.getAttributeBaseValue(player,"generic_max_health");

            //Prevent 1-shots
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
                if (!(event.getDamager() instanceof Explosive))
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

            //By this point, it is guaranteed that this kind of damage should have custom EliteMobs behavior

            // Dodge chance removed with guild rank system

            boolean blocking = false;

            //Blocking reduces melee damage and nullifies most ranged damage at the cost of shield durability
            if (player.isBlocking()) {
                blocking = true;
                if (player.getInventory().getItemInOffHand().getType().equals(Material.SHIELD)) {
                    ItemMeta itemMeta = player.getInventory().getItemInOffHand().getItemMeta();
                    org.bukkit.inventory.meta.Damageable damageable = (Damageable) itemMeta;

                    if (player.getInventory().getItemInOffHand().getItemMeta().hasEnchant(Enchantment.UNBREAKING) &&
                            player.getInventory().getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.UNBREAKING) / 20D > ThreadLocalRandom.current().nextDouble())
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

            //Calculate the damage for the event
            double newDamage = eliteToPlayerDamageFormula(player, eliteEntity, event);
            //Blocking reduces damage by 80%
            if (blocking)
                newDamage = newDamage - newDamage * MobCombatSettingsConfig.getBlockingDamageReduction();
            //nullify vanilla reductions
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageByEntityEvent.DamageModifier.values())
                if (event.isApplicable(modifier) && modifier != EntityDamageEvent.DamageModifier.ABSORPTION)
                    event.setDamage(modifier, 0);

            //Check if we should be doing raw damage, which some powers have

            if (bypass) {
                //Use raw damage in case of bypass
                newDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);
                bypass = false;
            }

            //Run the event, see if it will get cancelled or suffer further damage modifications
            PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent = new PlayerDamagedByEliteMobEvent(eliteEntity, player, event, projectile, newDamage);
            new EventCaller(playerDamagedByEliteMobEvent);

            //In case damage got modified along the way
            newDamage = playerDamagedByEliteMobEvent.getDamage();

            if (playerDamagedByEliteMobEvent.isCancelled()) {
                bypass = false;
                return;
            }

            //Set the final damage value
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);

            //Deal with the player getting killed todo: this is a bit busted, fix
            if (player.getHealth() - event.getDamage() <= 0)
                PlayerDeathMessageByEliteMob.addDeadPlayer(player, PlayerDeathMessageByEliteMob.initializeDeathMessage(player, eliteEntity));

        }

    }

}
