package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.ArmorDefenseCalculator;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.*;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.DivineShieldSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PhalanxSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ParrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
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
    @Getter
    private boolean playerBlocking;

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

    /**
     * Gets the attacker entity from this event.
     * Handles both direct melee and projectile attacks.
     *
     * @return The living entity that caused the damage, or null if not applicable
     */
    public LivingEntity getAttacker() {
        if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
            return eliteEntity.getLivingEntity();
        }
        return null;
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

        // Custom armor skill handling - process skills with custom trigger methods
        for (String skillId : activeSkillIds) {
            SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
            if (skill == null || !skill.isEnabled()) continue;
            if (!skill.meetsLevelRequirement(skillLevel)) continue;

            // Evasion - chance to completely dodge
            if (skill instanceof EvasionSkill evasion) {
                if (evasion.tryEvade(player, this)) {
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                    setCancelled(true);
                    return true;
                }
                continue; // Skip generic processing for this skill
            }

            // Retaliation - chance to reflect damage back
            if (skill instanceof RetaliationSkill retaliation) {
                LivingEntity attacker = getAttacker();
                if (attacker != null) {
                    retaliation.onDamageTaken(player, attacker, getDamage());
                }
                continue;
            }

            // Fortify - stacking damage reduction (always applies, action bar handled internally)
            if (skill instanceof FortifySkill fortify) {
                double modifiedDamage = fortify.modifyIncomingDamage(player, getDamage());
                setDamage(modifiedDamage);
                skill.incrementProcCount(player);
                continue;
            }

            // ReactiveShielding - check trigger + apply shield reduction
            if (skill instanceof ReactiveShieldingSkill reactiveShielding) {
                // Check if this hit should trigger the shield
                double damagePercent = getDamage() / player.getMaxHealth();
                reactiveShielding.checkTrigger(player, damagePercent);
                // Apply shield reduction if active (might have been activated by this hit or a previous one)
                double modifiedDamage = reactiveShielding.modifyIncomingDamage(player, getDamage());
                if (modifiedDamage != getDamage()) {
                    setDamage(modifiedDamage);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                continue;
            }

            // AdrenalineSurge - buffs when health drops below threshold
            if (skill instanceof AdrenalineSurgeSkill adrenaline) {
                double newHealthPercent = (player.getHealth() - getDamage()) / player.getMaxHealth();
                adrenaline.checkTrigger(player, newHealthPercent);
                continue;
            }

            // SecondWind - heal when health drops below threshold
            if (skill instanceof SecondWindSkill secondWind) {
                double newHealthPercent = (player.getHealth() - getDamage()) / player.getMaxHealth();
                secondWind.checkTrigger(player, newHealthPercent);
                continue;
            }

            // LastStand - prevent fatal damage
            if (skill instanceof LastStandSkill lastStand) {
                boolean fatal = player.getHealth() - getDamage() <= 0;
                if (fatal) {
                    if (lastStand.preventDeath(player, getDamage())) {
                        skill.incrementProcCount(player);
                        SkillBonus.sendSkillActionBar(player, skill);
                        setCancelled(true);
                        return true;
                    }
                }
                continue;
            }

            // IronStance - damage reduction when standing still (custom movement check)
            if (skill instanceof IronStanceSkill ironStance) {
                double modifiedDamage = ironStance.modifyIncomingDamage(player, getDamage(), this);
                if (modifiedDamage != getDamage()) {
                    setDamage(modifiedDamage);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                continue;
            }

            // Grit - scaling damage reduction based on health (custom health-based scaling)
            if (skill instanceof GritSkill grit) {
                double modifiedDamage = grit.modifyIncomingDamage(player, getDamage(), this);
                if (modifiedDamage != getDamage()) {
                    setDamage(modifiedDamage);
                    skill.incrementProcCount(player);
                    SkillBonus.sendSkillActionBar(player, skill);
                }
                continue;
            }

            // For any remaining skills (PASSIVE like BattleHardened),
            // use the generic handler
            DefensiveSkillResult result = processDefensiveSkill(skill, skillLevel);
            if (result.negatesDamage) {
                setCancelled(true);
                return true;
            }
            if (result.multiplier != 1.0) {
                setDamage(getDamage() * result.multiplier);
            }
        }

        // Check weapon-type defensive skills (Parry - sword blocking)
        double parryDamage = ParrySkill.applyParryReduction(player, this, getDamage());
        if (parryDamage != getDamage()) {
            setDamage(parryDamage);
            SkillBonus parrySkill = SkillBonusRegistry.getSkillById(ParrySkill.SKILL_ID);
            if (parrySkill != null) SkillBonus.sendSkillActionBar(player, parrySkill);
        }

        // Phalanx - frontal damage reduction when holding spear
        double phalanxDamage = PhalanxSkill.applyFrontalReduction(player, this, getDamage());
        if (phalanxDamage != getDamage()) {
            setDamage(phalanxDamage);
        }

        // Check death prevention skills from weapon types
        if (player.getHealth() - getDamage() <= 0) {
            if (DeathsEmbraceSkill.preventDeath(player)) {
                setCancelled(true);
                return true;
            }
            if (DivineShieldSkill.preventDeath(player, getDamage())) {
                setCancelled(true);
                return true;
            }
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
                        SkillBonus.sendSkillActionBar(player, skill);
                        yield new DefensiveSkillResult(1.0 - conditionalSkill.getConditionalBonus(skillLevel), false);
                    }
                }
                yield new DefensiveSkillResult(1.0, false);
            }
            case STACKING -> {
                if (skill instanceof StackingSkill stackingSkill) {
                    int stacks = stackingSkill.getCurrentStacks(player);
                    stackingSkill.addStack(player);
                    skill.incrementProcCount(player); // Track activation
                    SkillBonus.sendStackingSkillActionBar(player, skill, stacks + 1, stackingSkill.getMaxStacks());
                    yield new DefensiveSkillResult(1.0 - (stacks * stackingSkill.getBonusPerStack(skillLevel)), false);
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
                            SkillBonus.sendSkillActionBar(player, skill);
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
                        SkillBonus.sendSkillActionBar(player, skill);
                        yield new DefensiveSkillResult(1.0 - skill.getBonusValue(skillLevel), false);
                    }
                }
                yield new DefensiveSkillResult(1.0, false);
            }
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
         * Calculates boss damage to player using the redesigned defensive formula.
         * <p>
         * No pre-compensation. Three multiplicative layers:
         * <ol>
         *   <li><b>Base damage</b> = playerMaxHP / {@link LevelScaling#TARGET_HITS_TO_KILL_PLAYER}</li>
         *   <li><b>Skill adjustment</b> = 2^((mobLevel - armorSkillLevel) / {@link LevelScaling#SKILL_SCALING_RATE})
         *       — exponential scaling from skill vs mob level difference</li>
         *   <li><b>Gear adjustment</b> = 2.0 * (1 - gearReduction)
         *       — damage-type-aware, from {@link ArmorDefenseCalculator}</li>
         * </ol>
         * <p>
         * Expected outcomes:
         * <ul>
         *   <li>Naked vs same level: ~2.5 hits to kill (gear adjustment = 2.0)</li>
         *   <li>Matching gear + skill vs same level: ~5 hits to kill (gear adjustment = 1.0)</li>
         *   <li>Peak gear vs same level: ~10 hits to kill (gear adjustment = 0.5)</li>
         *   <li>+7.5 levels above skill: damage doubles</li>
         * </ul>
         * Final damage is capped at maxHP - 1 (1-shot protection).
         */
        private static double eliteToPlayerDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
            if (ElitePlayerInventory.getPlayer(player) == null) return 0;

            // 1. Player stats
            long armorSkillXP = PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
            int armorSkillLevel = Math.max(1, SkillXPCalculator.levelFromTotalXP(armorSkillXP));
            double playerMaxHealth = ArmorSkillHealthBonus.getConfiguredMaxHealthForArmorLevel(armorSkillLevel);
            int mobLevel = eliteEntity.getLevel();

            // 2. Base damage (no pre-compensation)
            double baseDamage = playerMaxHealth / LevelScaling.TARGET_HITS_TO_KILL_PLAYER;

            // 3. Skill adjustment (exponential, replaces old level modifier + skill reduction)
            double skillAdjustment = Math.pow(2.0, (mobLevel - armorSkillLevel) / LevelScaling.SKILL_SCALING_RATE);

            // 4. Gear adjustment (damage-type-aware)
            ArmorDefenseCalculator.DamageType damageType = ArmorDefenseCalculator.fromEvent(event);
            double gearScore = ArmorDefenseCalculator.getGearScore(player, damageType);
            double gearAdjustment = ArmorDefenseCalculator.getGearAdjustment(gearScore, mobLevel);

            double scaledDamage = baseDamage * skillAdjustment * gearAdjustment;

            // 5. Distance attenuation for explosions (creeper, ghast)
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

            // 6. Resistance potion effect (percentage-based)
            double potionMultiplier = 1.0;
            if (player.hasPotionEffect(PotionEffectType.RESISTANCE)) {
                int amplifier = player.getPotionEffect(PotionEffectType.RESISTANCE).getAmplifier();
                potionMultiplier = 1.0 - ((amplifier + 1) * MobCombatSettingsConfig.getResistanceDamageMultiplier());
                potionMultiplier = Math.max(0, potionMultiplier);
            }

            // 7. Boss damage multiplier (for custom bosses with increased damage)
            double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();

            // Config multipliers
            double configMultiplier;
            if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
                configMultiplier = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
            else
                configMultiplier = MobCombatSettingsConfig.getDamageToPlayerMultiplier();

            // Calculate final damage
            double finalDamage = Math.max(scaledDamage, 1)
                    * potionMultiplier
                    * customBossDamageMultiplier
                    * specialMultiplier
                    * configMultiplier;

            if (specialMultiplier != 1) specialMultiplier = 1;

            // 8. 1-shot protection
            double actualMaxHealth = AttributeManager.getAttributeBaseValue(player, "generic_max_health");
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
            if (player.hasMetadata("NPC") || ElitePlayerInventory.getPlayer(player) == null) return;

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
            if (player.isBlocking() || (com.magmaguy.elitemobs.testing.CombatSimulator.isTestingActive() && com.magmaguy.elitemobs.testing.CombatSimulator.isBlockingOverride())) {
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

                // Trigger Riposte skill on successful block (melee only)
                RiposteSkill.onPlayerBlock(player);
            }

            //Calculate the damage for the event
            double newDamage = eliteToPlayerDamageFormula(player, eliteEntity, event);
            // Test damage override: bypass defense formula during automated testing
            if (CombatSimulator.isTestingActive() && CombatSimulator.getTestDamageOverride() >= 0) {
                newDamage = CombatSimulator.getTestDamageOverride();
            }
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
            playerDamagedByEliteMobEvent.playerBlocking = blocking;
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
