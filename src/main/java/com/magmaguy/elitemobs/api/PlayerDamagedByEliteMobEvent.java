package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
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

        /**
         * Calculates boss damage to player using armor-skill-based scaling.
         * <p>
         * The goal is for properly geared players to survive ~5 normal hits from
         * same-level content, and ~2 hits from content 10 levels above them.
         * This scales with player's armor skill level since that determines their
         * max health (via ArmorSkillHealthBonus).
         * <p>
         * Formula:
         * 1. Player max health = 20 + (armorLevel - 1) * 2
         * 2. Target damage per hit = playerMaxHealth / TARGET_HITS_TO_KILL_PLAYER
         * 3. Raw damage = targetDamage / EXPECTED_GEAR_DAMAGE_MULTIPLIER (pre-compensate for gear reduction)
         * 4. Level modifier = 2^((mobLevel - armorLevel) / LEVELS_PER_BOSS_DAMAGE_DOUBLE)
         * 5. Final damage = rawDamage * levelModifier * otherMultipliers
         * <p>
         * Expected outcomes with full gear/skills:
         * <ul>
         *   <li>Same level: ~5 hits to kill</li>
         *   <li>+5 levels: ~3 hits to kill</li>
         *   <li>+10 levels: ~2 hits to kill</li>
         * </ul>
         */
        private static double eliteToPlayerDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            if (elitePlayerInventory == null) return 0;

            // Get player's armor skill level (determines their max health and defensive scaling)
            long armorSkillXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
            int armorSkillLevel = Math.max(1, SkillXPCalculator.levelFromTotalXP(armorSkillXP));

            // Calculate player's max health from armor skill level
            // This matches the ArmorSkillHealthBonus formula: 20 base + (level - 1) * 2
            double playerMaxHealth = 20.0 + Math.max(0, armorSkillLevel - 1) * 2.0;

            // Calculate target damage per hit (what we want player to RECEIVE after all reductions)
            double targetDamagePerHit = playerMaxHealth / LevelScaling.TARGET_HITS_TO_KILL_PLAYER;

            // Pre-compensate for expected gear and skill damage reductions
            // Armor provides ~50% reduction, defense skills provide ~50% reduction
            // Combined: player receives 25% of raw damage, so we multiply by 4 to compensate
            double baseDamagePerHit = targetDamagePerHit / LevelScaling.EXPECTED_GEAR_DAMAGE_MULTIPLIER;

            // Apply level scaling based on mob level vs player armor level
            // Uses separate scaling constant for boss damage (softer than player damage scaling)
            // +10 levels = ~2.5x damage, resulting in ~2 hits to kill
            int mobLevel = eliteEntity.getLevel();
            double levelModifier = Math.pow(LevelScaling.SCALING_BASE,
                    (mobLevel - armorSkillLevel) / LevelScaling.LEVELS_PER_BOSS_DAMAGE_DOUBLE);

            // Clamp the modifier to prevent extreme cases
            levelModifier = LevelScaling.clampModifier(levelModifier);

            // Calculate level-scaled base damage
            double scaledDamage = baseDamagePerHit * levelModifier;

            // Apply distance attenuation for explosions (creeper, ghast)
            if (eliteEntity.getLivingEntity() != null && player.isValid() &&
                    player.getLocation().getWorld().equals(eliteEntity.getLivingEntity().getWorld())) {
                if (eliteEntity.getLivingEntity().getType().equals(EntityType.CREEPER)) {
                    Creeper creeper = (Creeper) eliteEntity.getLivingEntity();
                    double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                    double distanceAttenuation = Math.max(0, 1 - distance / creeper.getExplosionRadius());
                    scaledDamage *= distanceAttenuation;
                } else if (eliteEntity.getLivingEntity().getType().equals(EntityType.GHAST) &&
                        event.getDamager().getType().equals(EntityType.FIREBALL)) {
                    double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                    double distanceAttenuation = Math.max(0, 1 - distance / ((Fireball) event.getDamager()).getYield());
                    scaledDamage *= distanceAttenuation;
                }
            }

            // Apply protection enchantments as flat damage reduction
            double damageReduction = 0;
            if (event.getDamager() instanceof AbstractArrow)
                damageReduction += elitePlayerInventory.getEliteProjectileProtection(true);
            if (event.getDamager() instanceof Fireball || event.getDamager() instanceof Creeper)
                damageReduction += elitePlayerInventory.getEliteBlastProtection(true);

            // Apply resistance potion effect (percentage-based)
            double potionMultiplier = 1.0;
            if (player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                int amplifier = player.getPotionEffect(PotionEffectType.RESISTANCE).getAmplifier();
                potionMultiplier = 1.0 - ((amplifier + 1) * MobCombatSettingsConfig.getResistanceDamageMultiplier());
                potionMultiplier = Math.max(0, potionMultiplier);
            }

            // Apply boss damage multiplier (for custom bosses with increased damage)
            double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();

            // Apply config multipliers
            double configMultiplier;
            if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
                configMultiplier = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
            else
                configMultiplier = MobCombatSettingsConfig.getDamageToPlayerMultiplier();

            // Calculate final damage
            double finalDamage = Math.max(scaledDamage - damageReduction, 1)
                    * potionMultiplier
                    * customBossDamageMultiplier
                    * specialMultiplier
                    * configMultiplier;

            if (specialMultiplier != 1) specialMultiplier = 1;

            // Get actual max health (may differ from calculated due to other effects)
            double actualMaxHealth = AttributeManager.getAttributeBaseValue(player, "generic_max_health");

            // Prevent 1-shots
            finalDamage = Math.min(finalDamage, actualMaxHealth - 1);

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
