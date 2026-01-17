package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.entitytracker.CustomProjectileData;
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
import com.magmaguy.elitemobs.skills.bonuses.interfaces.*;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.magmacore.util.Round;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobDamagedByPlayerEvent extends EliteDamageEvent {

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
    private final boolean criticalStrike;
    @Getter
    private final boolean isCustomDamage;
    @Getter
    private final double damageModifier;
    @Getter
    public boolean rangedAttack;

    /**
     * Event fired when an elite is damaged by a player.
     *
     * @param eliteEntity    Elite damaged.
     * @param player         Player acting as the damged.
     * @param event          Original Minecraft damage event.
     * @param damage         Damage. Can be modifed!
     * @param criticalStrike Whether the strike is a critical strike.
     * @param isCustomDamage Whether the amount of damage is custom, meaning it should apply with no damage reduction of any kind, including armor!
     * @param damageModifier Damage modifiers that the boss may have to reduce incoming damage.
     */
    public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, double damage, boolean criticalStrike, boolean isCustomDamage, double damageModifier) {
        super(damage, event);
        this.entity = eliteEntity.getLivingEntity();
        this.eliteMobEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = event;
        this.rangedAttack = event != null && event.getDamager() instanceof Projectile;
        this.criticalStrike = criticalStrike;
        this.isCustomDamage = isCustomDamage;
        this.damageModifier = damageModifier;
    }

    /**
     * Constructor for testing purposes that allows explicit ranged attack flag.
     * Use this when simulating attacks without an actual EntityDamageByEntityEvent.
     *
     * @param eliteEntity    Elite damaged.
     * @param player         Player acting as the damager.
     * @param damage         Base damage amount.
     * @param isRangedAttack Whether this is a ranged attack.
     */
    public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, double damage, boolean isRangedAttack) {
        super(damage, null);
        this.entity = eliteEntity.getLivingEntity();
        this.eliteMobEntity = eliteEntity;
        this.player = player;
        this.entityDamageByEntityEvent = null;
        this.rangedAttack = isRangedAttack;
        this.criticalStrike = false;
        this.isCustomDamage = false;
        this.damageModifier = 1.0;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Unified method to apply all active skill bonuses to this damage event.
     * This is the single entry point for the skill bonus system to modify offensive damage.
     * <p>
     * Processes all active weapon skills for the player's current weapon type,
     * applying damage multipliers from PASSIVE, CONDITIONAL, STACKING, and PROC skills.
     */
    public void applySkillBonuses() {
        if (player == null || player.hasMetadata("NPC")) return;
        if (!ElitePlayerInventory.playerInventories.containsKey(player.getUniqueId())) return;

        // Determine weapon type from main hand
        SkillType weaponSkillType = getWeaponSkillType(player);
        if (weaponSkillType == null) return;

        int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, weaponSkillType);
        List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), weaponSkillType);

        double damageMultiplier = 1.0;
        StringBuilder debugLog = new StringBuilder();
        debugLog.append("[SkillBonuses] ");

        // Process each active skill for this weapon type
        for (String skillId : activeSkillIds) {
            SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
            if (skill == null || !skill.isEnabled()) continue;
            if (!skill.meetsLevelRequirement(skillLevel)) continue;

            // Skip skills that don't affect damage
            if (!skill.affectsDamage()) {
                continue;
            }

            double skillMultiplier = processOffensiveSkill(skill, skillLevel);
            if (skillMultiplier != 1.0) {
                debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", skillMultiplier)).append(" ");
            }
            damageMultiplier *= skillMultiplier;
        }

        // Apply the combined damage multiplier
        if (damageMultiplier != 1.0) {
            double oldDamage = getDamage();
            setDamage(oldDamage * damageMultiplier);
            debugLog.append("| Total=").append(String.format("%.2fx", damageMultiplier))
                    .append(" | Damage: ").append(String.format("%.1f", oldDamage))
                    .append(" -> ").append(String.format("%.1f", getDamage()));
            DebugMessage.log(player, debugLog.toString());
        }
    }

    /**
     * Processes a single offensive skill and returns its damage multiplier contribution.
     * Tracks proc counts for testing purposes.
     */
    private double processOffensiveSkill(SkillBonus skill, int skillLevel) {
        return switch (skill.getBonusType()) {
            case PASSIVE -> {
                skill.incrementProcCount(player); // Track activation
                yield 1.0 + skill.getBonusValue(skillLevel);
            }
            case CONDITIONAL -> {
                if (skill instanceof ConditionalSkill conditionalSkill) {
                    if (conditionalSkill.conditionMet(player, this)) {
                        skill.incrementProcCount(player); // Track activation
                        yield 1.0 + conditionalSkill.getConditionalBonus(skillLevel);
                    }
                }
                yield 1.0;
            }
            case STACKING -> {
                if (skill instanceof StackingSkill stackingSkill) {
                    int stacks = stackingSkill.getCurrentStacks(player);
                    stackingSkill.addStack(player); // Add stack for this hit
                    skill.incrementProcCount(player); // Track activation
                    yield 1.0 + (stacks * stackingSkill.getBonusPerStack(skillLevel));
                }
                yield 1.0;
            }
            case PROC -> {
                if (skill instanceof ProcSkill procSkill) {
                    double procChance = procSkill.getProcChance(skillLevel);
                    if (ThreadLocalRandom.current().nextDouble() < procChance) {
                        procSkill.onProc(player, this);
                        skill.incrementProcCount(player); // Track proc
                        yield 1.0 + skill.getBonusValue(skillLevel);
                    }
                }
                yield 1.0;
            }
            case COOLDOWN -> {
                if (skill instanceof CooldownSkill cooldownSkill) {
                    if (!cooldownSkill.isOnCooldown(player)) {
                        cooldownSkill.onActivate(player, this);
                        cooldownSkill.startCooldown(player, skillLevel);
                        skill.incrementProcCount(player); // Track activation
                        yield 1.0 + skill.getBonusValue(skillLevel);
                    }
                }
                yield 1.0;
            }
            case TOGGLE -> {
                if (skill instanceof ToggleSkill toggleSkill) {
                    if (toggleSkill.isToggled(player)) {
                        skill.incrementProcCount(player); // Track activation while toggled
                        yield 1.0 + toggleSkill.getPositiveBonus(skillLevel);
                    }
                }
                yield 1.0;
            }
        };
    }

    /**
     * Determines the weapon skill type based on the player's main hand item.
     */
    private static SkillType getWeaponSkillType(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR) return null;

        Material type = mainHand.getType();
        String typeName = type.name();

        if (typeName.endsWith("_SWORD")) return SkillType.SWORDS;
        if (typeName.endsWith("_AXE")) return SkillType.AXES;
        if (type == Material.BOW) return SkillType.BOWS;
        if (type == Material.CROSSBOW) return SkillType.CROSSBOWS;
        if (type == Material.TRIDENT) return SkillType.TRIDENTS;
        if (typeName.endsWith("_HOE")) return SkillType.HOES;

        return null;
    }

    //The thing that calls the event
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
         * Gets the amount of damage dealt by EliteMobs-specific features.
         * Uses 50/50 split: half from player skill level, half from item level.
         * Both scale exponentially with level to match mob HP scaling.
         *
         * @param player Damager
         * @return Bonus damage applied
         */
        private static double getEliteMeleeDamage(Player player, LivingEntity livingEntity) {
            if (player.getInventory().getItemInMainHand().getType().equals(Material.BOW) || player.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW))
                return 0.0;

            // Get player's skill level for the weapon type
            SkillType weaponSkillType = getWeaponSkillType(player);
            int playerSkillLevel = 1;
            if (weaponSkillType != null) {
                long skillXP = PlayerData.getSkillXP(player.getUniqueId(), weaponSkillType);
                playerSkillLevel = SkillXPCalculator.levelFromTotalXP(skillXP);
            }

            // Get item level
            int itemLevel = (int) EliteItemManager.getItemLevel(player.getInventory().getItemInMainHand());

            // Calculate effective level using 50/50 split
            // This is the average of skill level and item level
            int effectiveLevel = (int) ((playerSkillLevel * CombatSystem.SKILL_CONTRIBUTION_RATIO)
                    + (itemLevel * CombatSystem.ITEM_CONTRIBUTION_RATIO));

            // Use exponential damage scaling (matches mob HP scaling)
            // This ensures same-level combat feels consistent at all levels
            double attackSpeed = EliteItemManager.getAttackSpeed(player.getInventory().getItemInMainHand());
            double effectiveEliteDamage = LevelScaling.calculatePlayerDamage(effectiveLevel, attackSpeed);

            double bonusEliteDamage = secondaryEnchantmentDamageIncrease(player, livingEntity);

            // Populate breakdown if tracking is active
            DamageBreakdown breakdown = DamageBreakdown.getActiveBreakdown(player);
            if (breakdown != null) {
                breakdown.setSkillDamage(playerSkillLevel); // Now represents level, not raw damage
                breakdown.setItemDamage(itemLevel); // Now represents level, not raw damage
                breakdown.setPlayerSkillLevel(playerSkillLevel);
                breakdown.setItemLevel(itemLevel);
                breakdown.setEnchantmentDamage(bonusEliteDamage);
                breakdown.setAttackCooldownMultiplier(player.getAttackCooldown());
                breakdown.setWeaponType(player.getInventory().getItemInMainHand().getType().name());
                breakdown.setRangedAttack(false);
            }

            // Log melee damage components when debug mode is enabled
            double finalEliteDamage = (effectiveEliteDamage + bonusEliteDamage) * player.getAttackCooldown();
            DebugMessage.log(player, "[MeleeDmg] SkillLv=" + playerSkillLevel +
                    " ItemLv=" + itemLevel +
                    " EffectiveLv=" + effectiveLevel +
                    " BaseDmg=" + String.format("%.1f", effectiveEliteDamage) +
                    " Cooldown=" + String.format("%.2f", player.getAttackCooldown()) +
                    " Final=" + String.format("%.1f", finalEliteDamage));

            return finalEliteDamage;
        }

        /**
         * Gets the amount of ranged damage dealt by EliteMobs-specific features.
         * Uses 50/50 split: half from player skill level, half from item level.
         * Both scale exponentially with level to match mob HP scaling.
         */
        private static double getEliteRangedDamage(Projectile arrow) {
            //note: the arrow velocity amplitude at full load is about 2.8
            double arrowSpeedMultiplier = Math.sqrt(Math.pow(arrow.getVelocity().getX(), 2D) + Math.pow(arrow.getVelocity().getY(), 2D) + Math.pow(arrow.getVelocity().getZ(), 2D));
            arrowSpeedMultiplier /= 4.0D;

            // Get player's skill level for the ranged weapon
            if (arrow.getShooter() instanceof Player player && !player.hasMetadata("NPC")) {
                Material weaponType = player.getInventory().getItemInMainHand().getType();
                SkillType skillType = (weaponType == Material.BOW) ? SkillType.BOWS :
                        (weaponType == Material.CROSSBOW) ? SkillType.CROSSBOWS : null;

                if (skillType != null) {
                    long skillXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
                    int playerSkillLevel = SkillXPCalculator.levelFromTotalXP(skillXP);

                    // Get item level
                    int itemLevel = (int) EliteItemManager.getItemLevel(player.getInventory().getItemInMainHand());

                    // Calculate effective level using 50/50 split
                    int effectiveLevel = (int) ((playerSkillLevel * CombatSystem.SKILL_CONTRIBUTION_RATIO)
                            + (itemLevel * CombatSystem.ITEM_CONTRIBUTION_RATIO));

                    // Use exponential damage scaling (matches mob HP scaling)
                    // For ranged, we use base DPS (no attack speed division)
                    double effectiveDamage = LevelScaling.calculatePlayerDPS(effectiveLevel);

                    // Populate breakdown if tracking is active
                    DamageBreakdown breakdown = DamageBreakdown.getActiveBreakdown(player);
                    if (breakdown != null) {
                        breakdown.setSkillDamage(playerSkillLevel);
                        breakdown.setItemDamage(itemLevel);
                        breakdown.setPlayerSkillLevel(playerSkillLevel);
                        breakdown.setItemLevel(itemLevel);
                        breakdown.setAttackCooldownMultiplier(arrowSpeedMultiplier);
                        breakdown.setWeaponType(weaponType.name());
                        breakdown.setRangedAttack(true);
                    }

                    return arrowSpeedMultiplier * effectiveDamage;
                }
            }

            // Fallback for non-player shooters or unrecognized weapons - use level 1 damage
            return arrowSpeedMultiplier * LevelScaling.calculatePlayerDPS(1);
        }

        private static double getCustomDamageModifier(EliteEntity eliteEntity, Material itemStackType) {
            if (!(eliteEntity instanceof CustomBossEntity)) return 1;
            //This doesn't really take into account people switching their weapon out on ranged attacks. That's probably fine.
            return ((CustomBossEntity) eliteEntity).getDamageModifier(itemStackType);
        }

        private static double secondaryEnchantmentDamageIncrease(Player player, LivingEntity livingEntity) {
            if (ItemSettingsConfig.isUseEliteEnchantments()) return 0D;
            if (livingEntity instanceof Spider || livingEntity instanceof Silverfish) {
                int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageArthropodsLevel(player.getInventory().getItemInMainHand(), false);
                level -= Enchantment.BANE_OF_ARTHROPODS.getMaxLevel();
                if (level < 1) return 0D;
                return level * 2.5D;
            }
            if (livingEntity instanceof Zombie || livingEntity instanceof Skeleton || livingEntity instanceof Wither || livingEntity instanceof SkeletonHorse || livingEntity instanceof ZombieHorse || livingEntity.getType().equals(EntityType.ZOMBIFIED_PIGLIN)) {
                int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).mainhand.getDamageUndeadLevel(player.getInventory().getItemInMainHand(), false);
                level -= Enchantment.SMITE.getMaxLevel();
                if (level < 1) return 0D;
                return level * 2.5D;
            }
            return 0;
        }

        private static boolean isCriticalHit(Player player) {
            double criticalStrike = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getCritChance(false);
            // Note: Guild rank crit bonus removed - skill bonuses will be added in the future
            return ThreadLocalRandom.current().nextDouble() < criticalStrike;
        }

        /**
         * Gets the player's effective offensive level for level scaling calculations.
         * This is based on the player's weapon skill level for the weapon they are currently using.
         *
         * @param player The player to get the offensive level for
         * @return The player's offensive level (weapon skill level, or 1 if no valid weapon)
         */
        private static int getPlayerOffensiveLevel(Player player) {
            SkillType weaponSkillType = getWeaponSkillType(player);
            if (weaponSkillType == null) return 1;
            long skillXP = PlayerData.getSkillXP(player.getUniqueId(), weaponSkillType);
            return Math.max(1, SkillXPCalculator.levelFromTotalXP(skillXP));
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerShootArrow(ProjectileLaunchEvent event) {
            if (!(event.getEntity().getShooter() instanceof Player)) return;
            EliteItemManager.tagArrow(event.getEntity());
        }

        @EventHandler(ignoreCancelled = true)
        public void onEliteMobAttacked(EntityDamageByEntityEvent event) {
            if (event.getEntity().getType().equals(EntityType.ENDER_DRAGON) && ((EnderDragon) event.getEntity()).getPhase().equals(EnderDragon.Phase.DYING))
                return;
            LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
            if (livingEntity == null) return;
            if (!livingEntity.getType().equals(EntityType.PLAYER)) return;
            Player player = (Player) livingEntity;
            EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
            //Living entity is sometimes null when the damage is dealt to an already dead entity - might happen with mcmmo due to DOTs and stuff
            if (eliteEntity == null || !eliteEntity.isValid()) return;
            //There's at least 1 gun plugin that makes players the projectile themselves.
            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) && !(event.getDamager() instanceof Projectile))
                return;

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

            // Check if breakdown tracking is active for this player
            DamageBreakdown breakdown = validPlayer ? DamageBreakdown.getActiveBreakdown(player) : null;

            if (validPlayer && event.getCause().equals(EntityDamageEvent.DamageCause.THORNS)) {
                //Thorns are their own kind of damage
                eliteDamage = getThornsDamage(player);
                if (breakdown != null) {
                    breakdown.setThornsDamage(eliteDamage);
                }
            } else if (validPlayer && (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) && EliteItemManager.isEliteMobsItem(player.getInventory().getItemInMainHand())))
                eliteDamage = getEliteMeleeDamage(player, livingEntity);
            else if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                //Scan arrow for arrow damage
                eliteDamage = getEliteRangedDamage((Projectile) event.getDamager());

            double damageModifier = 1;
            if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE))
                if (CustomProjectileData.getCustomProjectileDataHashMap().get((Projectile) event.getDamager()) == null)
                    damageModifier = getCustomDamageModifier(eliteEntity, null);
                else
                    damageModifier = getCustomDamageModifier(eliteEntity, CustomProjectileData.getCustomProjectileDataHashMap().get(event.getDamager()).getProjectileShooterMaterial());
            else getCustomDamageModifier(eliteEntity, player.getInventory().getItemInMainHand().getType());

            double combatMultiplier;
            if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
                combatMultiplier = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
            else
                combatMultiplier = MobCombatSettingsConfig.getDamageToEliteMultiplier();

            damage = Round.twoDecimalPlaces((damage + eliteDamage) * damageModifier * combatMultiplier);

            // Populate breakdown with vanilla damage and multipliers
            if (breakdown != null) {
                breakdown.setVanillaDamage(event.getDamage());
                breakdown.setDamageModifier(damageModifier);
                breakdown.setCombatMultiplier(combatMultiplier);
                breakdown.setEliteLevel(eliteEntity.getLevel());
            }

            // Level scaling is now built into mob HP (exponential scaling)
            // rather than modifying damage dealt. This feels better for players.
            if (validPlayer) {
                int playerLevel = getPlayerOffensiveLevel(player);
                int eliteLevel = eliteEntity.getLevel();

                if (breakdown != null) {
                    breakdown.setPlayerLevel(playerLevel);
                    breakdown.setLevelScalingModifier(1.0); // No longer modifying damage
                }

                // Debug logging for combat balance tuning
                DebugMessage.log(player, "[Combat] Player Lv" + playerLevel + " vs Elite Lv" + eliteLevel +
                        " | Damage: " + String.format("%.1f", damage) +
                        " | Elite HP: " + String.format("%.1f", eliteEntity.getHealth()));
            }

            boolean criticalHit = false;

            if (validPlayer) {
                criticalHit = isCriticalHit(player);
                if (criticalHit) {
                    damage *= 1.5;
                    if (breakdown != null) {
                        breakdown.setCriticalHit(true);
                        breakdown.setCritMultiplier(1.5);
                    }
                }
            }

            // Finalize breakdown computation
            if (breakdown != null) {
                breakdown.compute();
            }

            EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent = new EliteMobDamagedByPlayerEvent(eliteEntity, player, event, damage, criticalHit, bypass, damageModifier);

            new EventCaller(eliteMobDamagedByPlayerEvent);

            if (eliteMobDamagedByPlayerEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            //In case things got modified along the way
            damage = eliteMobDamagedByPlayerEvent.getDamage();

            if (validPlayer) {
                //Time to deal custom damage!
                eliteEntity.addDamager(player, damage);
            }

            //Dragons need special handling due to their custom deaths
            if (eliteEntity.getLivingEntity() != null && eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON) && eliteEntity.getLivingEntity().getHealth() - damage < 1) {
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


            runAntiexploit(eliteEntity, event, eliteMobDamagedByPlayerEvent);
        }

        private void runAntiexploit(EliteEntity eliteEntity, EntityDamageByEntityEvent event, EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
            if (EliteMobsWorld.isEliteMobsWorld(event.getDamager().getWorld().getUID())) return;
            if (EliteMobs.worldGuardIsEnabled) {
                Boolean regionQuery = WorldGuardFlagChecker.checkNullableFlag(eliteEntity.getLocation(), WorldGuardCompatibility.getELITEMOBS_ANTIEXPLOIT());
                if (regionQuery != null && !regionQuery) return;
            }
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                    event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK &&
                    event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE)
                return;

            if (eliteEntity.isInAntiExploitCooldown() ||
                    eliteEntity.getLivingEntity() == null) return;
            Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerAntiExploitEvent(eliteEntity, eliteMobDamagedByPlayerEvent));
        }
    }

}
