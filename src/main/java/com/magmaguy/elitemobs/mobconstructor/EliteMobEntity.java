package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.*;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.MajorPower;
import com.magmaguy.elitemobs.powers.MinorPower;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobEntity {

    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    private LivingEntity livingEntity;
    private int eliteLevel;
    private double eliteTier;
    private double maxHealth;
    private double health;
    private String name;
    /*
    Store all powers in one set, makes no sense to access it in individual sets.
    The reason they are split up in the first place is to add them in a certain ratio
    Once added, they can just be stored in a pool
     */
    private HashSet<ElitePower> powers = new HashSet<>();
    private int minorPowerCount = 0;
    private int majorPowerCount = 0;
    private boolean hasMinorVisualEffect = false;
    private boolean hasMajorVisualEffect = false;
    private boolean hasVisualEffectObfuscated = true;
    private boolean isNaturalEntity;
    /*
    This just defines default behavior
     */
    private boolean hasCustomPowers = false;
    private boolean isPersistent = true;
    private boolean hasVanillaLoot = true;
    private boolean hasEliteLoot = true;
    private CreatureSpawnEvent.SpawnReason spawnReason;

    private HashSet<Player> damagers = new HashSet<>();

    private double healthMultiplier = 1.0;
    private double damageMultiplier = 1.0;
    private double defaultMaxHealth;

    private boolean isCooldown = false;

    /**
     * Check through WorldGuard if the location is valid. Regions flagged with the elitemob-spawning deny tag will cancel
     * the Elite Mob conversion
     *
     * @param location Location to be checked
     * @return Whether the location is valid
     */
    private static boolean validSpawnLocation(Location location) {
        if (!EliteMobs.WORLDGUARD_IS_ENABLED)
            return true;
        if (!Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled()) return true;
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
        return set.testState(null, (StateFlag) WorldGuardCompatibility.getEliteMobsSpawnFlag());
    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     *
     * @param livingEntity Minecraft entity associated to this elite mob
     * @param eliteLevel   Level of the mob, can be modified during runtime. Dynamically assigned.
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteLevel, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(livingEntity.getLocation())) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.livingEntity = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteLevel(eliteLevel);
        eliteTier = MobTierCalculator.findMobTier(eliteLevel);
        /*
        Sets the spawn reason
         */
        setSpawnReason(spawnReason);
        /*
        Start tracking the entity
         */
        if (!EntityTracker.registerEliteMob(this)) return;
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        /*
        Handle name, variable as per stacking rules
         */
        setName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth();
        /*
        Set the armor
         */
        setArmor();
        /*
        Register whether or not the elite mob is natural
         */
        this.isNaturalEntity = EntityTracker.isNaturalEntity(livingEntity);
        /*
        Set the power list
         */
        randomizePowers(eliteMobProperties);

        this.livingEntity.setCanPickupItems(false);

    }

    /**
     * Spawning method for boss mobs.
     * Assumes custom powers and custom names.
     *
     * @param entityType type of mob that this entity is slated to become
     * @param location   location at which the elite mob will spawn
     * @param eliteLevel boss mob level, should be automatically generated based on the highest player tier online
     * @param name       the name for this boss mob, overrides the usual elite mob name format
     * @see com.magmaguy.elitemobs.custombosses.CustomBossEntity
     */
    public EliteMobEntity(EntityType entityType, Location location, int eliteLevel, String name, HashSet<ElitePower> mobPowers, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(location)) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.livingEntity = spawnBossMobLivingEntity(entityType, location);
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteLevel(eliteLevel);
        eliteTier = MobTierCalculator.findMobTier(eliteLevel);
        /*
        Sets the spawn reason
         */
        setSpawnReason(spawnReason);
        /*
        Start tracking the entity
         */
        if (!EntityTracker.registerEliteMob(this)) return;
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(entityType);
        /*
        Handle name, variable as per stacking rules
         */
        setName(name);
        /*
        Handle health, max is variable as per stacking rules
         */
        setMaxHealth();
        /*
        Register whether or not the elite mob is natural
         */
        this.isNaturalEntity = EntityTracker.isNaturalEntity(this.livingEntity);
        /*
        These have custom powers
         */
        this.hasCustomPowers = true;
        this.powers = mobPowers;
        for (ElitePower elitePower : powers) {
            elitePower.applyPowers(livingEntity);
            if (elitePower instanceof MajorPower)
                this.majorPowerCount++;
            if (elitePower instanceof MinorPower)
                this.minorPowerCount++;
        }

        livingEntity.setCanPickupItems(false);

    }

    /**
     * Constructor for Elite Mobs spawned via command
     *
     * @param entityType Type of entity to be spawned
     * @param location   Location at which the entity will spawn
     * @param eliteLevel Level of the Elite Mob
     * @param mobPowers  HashSet of ElitePower that the entity will have (can be empty)
     */
    public EliteMobEntity(EntityType entityType, Location location, int eliteLevel, HashSet<ElitePower> mobPowers, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(location)) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.livingEntity = spawnBossMobLivingEntity(entityType, location);
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteLevel(eliteLevel);
        eliteTier = MobTierCalculator.findMobTier(eliteLevel);
        /*
        Sets the spawn reason
         */
        setSpawnReason(spawnReason);
        /*
        Start tracking the entity
         */
        if (!EntityTracker.registerEliteMob(this)) return;
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(entityType);
        /*
        Handle name, variable as per stacking rules
         */
        setName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth();
        /*
        Set the armor
         */
        setArmor();
        /*
        Register whether or not the elite mob is natural
        All mobs spawned via commands are considered natural
         */
        this.isNaturalEntity = true;
        /*
        Set the power list
         */
        if (!mobPowers.isEmpty()) {
            this.powers = mobPowers;
            for (ElitePower elitePower : powers) {
                elitePower.applyPowers(livingEntity);
                if (elitePower instanceof MajorPower)
                    this.majorPowerCount++;
                if (elitePower instanceof MinorPower)
                    this.minorPowerCount++;
            }
            MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(this);
            MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance(this);
        } else {
            randomizePowers(eliteMobProperties);
        }

        livingEntity.setCanPickupItems(false);

    }

    /**
     * This avoids accidentally assigning an elite mob to an entity spawned specifically to be a boss mob or reinforcement
     */
    private static LivingEntity spawnBossMobLivingEntity(EntityType entityType, Location location) {
        NaturalMobSpawnEventHandler.setIgnoreMob(true);
        return (LivingEntity) location.getWorld().spawnEntity(location, entityType);
    }

    /**
     * Sets the display name to be used by this Elite Mob
     *
     * @param eliteMobProperties EliteMobProperties from where the display name will be obtained
     */
    private void setName(EliteMobProperties eliteMobProperties) {
        this.name = ChatColorConverter.convert(
                eliteMobProperties.getName().replace(
                        "$level", eliteLevel + ""));
        livingEntity.setCustomName(this.name);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            livingEntity.setCustomNameVisible(true);
    }

    /**
     * Sets the display name to be used by this Elite Mob
     *
     * @param name String which defines the display name
     */
    public void setName(String name) {
        this.name = ChatColorConverter.convert(name);
        this.getLivingEntity().setCustomName(this.name);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            livingEntity.setCustomNameVisible(true);
    }

    /**
     * Sets the level of the Elite Mob. Values below 1 default to 1
     *
     * @param newLevel Level of the Elite Mob
     */
    private void setEliteLevel(int newLevel) {
        if (newLevel < 1)
            newLevel = 1;
        this.eliteLevel = newLevel;
    }

    /**
     * Sets the max health of the Elite Mob. This is calculated based on the Elite Mob level. Maxes out at 2000 due to
     * Minecraft restrictions
     */
    private void setMaxHealth() {
        this.defaultMaxHealth = EliteMobProperties.getPluginData(this.getLivingEntity().getType()).getDefaultMaxHealth();
        this.maxHealth = (eliteLevel * CombatSystem.PER_LEVEL_POWER_INCREASE * this.defaultMaxHealth + this.defaultMaxHealth);
        this.health = this.maxHealth;
        double vanillaValue = maxHealth > 2048 ? 2048 : maxHealth;
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(vanillaValue);
        livingEntity.setHealth(vanillaValue);
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getHealth() {
        return this.health;
    }

    /**
     * Sets the armor of the EliteMob. The equipment progresses with every passing Elite Mob tier and dynamically adjusts
     * itself to the maximum tier set.
     */
    private void setArmor() {

        if (VersionChecker.currentVersionIsUnder(12, 2)) return;
        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_ARMOR)) return;

        livingEntity.getEquipment().setItemInMainHandDropChance(0);
        livingEntity.getEquipment().setHelmetDropChance(0);
        livingEntity.getEquipment().setChestplateDropChance(0);
        livingEntity.getEquipment().setLeggingsDropChance(0);
        livingEntity.getEquipment().setBootsDropChance(0);

        if (!(livingEntity instanceof Zombie || livingEntity instanceof PigZombie ||
                livingEntity instanceof Skeleton || livingEntity instanceof WitherSkeleton)) return;

        livingEntity.getEquipment().setBoots(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (eliteLevel >= 12)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (eliteLevel >= 14)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        if (eliteLevel >= 16)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

        if (eliteLevel >= 18)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        if (eliteLevel >= 20)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));

        if (eliteLevel >= 22)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        if (eliteLevel >= 24)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

        if (eliteLevel >= 26)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        if (eliteLevel >= 28)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

        if (eliteLevel >= 30)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

        if (eliteLevel >= 32)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

        if (eliteLevel >= 34)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

        if (eliteLevel >= 36)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        if (eliteLevel >= 38)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));

        if (eliteLevel >= 40)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

        if (eliteLevel >= 42)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

    }

    /**
     * Randomizes the powers that the EliteMob has. Determines which powers will be picked by randomizing the powers
     * from pools associated to the mob type and conditioned by config settings
     *
     * @param eliteMobProperties Properties of the Elite Mob Type
     */
    private void randomizePowers(EliteMobProperties eliteMobProperties) {

        if (hasCustomPowers) return;
        if (eliteTier < 1) return;

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (eliteTier >= 1) availableDefensivePowers = 1;
        if (eliteTier >= 2) availableOffensivePowers = 1;
        if (eliteTier >= 3) availableMiscellaneousPowers = 1;
        if (eliteTier >= 4) availableMajorPowers = 1;
        if (eliteTier >= 5) availableDefensivePowers = 2;
        if (eliteTier >= 6) availableOffensivePowers = 2;
        if (eliteTier >= 7) availableMiscellaneousPowers = 2;
        if (eliteTier >= 8) availableMajorPowers = 2;

        //apply defensive powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidDefensivePowers().clone(), availableDefensivePowers);

        //apply offensive powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidOffensivePowers().clone(), availableOffensivePowers);

        //apply miscellaneous powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidMiscellaneousPowers().clone(), availableMiscellaneousPowers);

        //apply major powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidMajorPowers().clone(), availableMajorPowers);

        MinorPowerPowerStance minorPowerStanceMath = new MinorPowerPowerStance(this);
        MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance(this);

    }

    /**
     * Applies the power to the Elite Mob based on a HashSet of powers
     *
     * @param elitePowers          ElitePower HashSet from which the powers will be randomized
     * @param availablePowerAmount Amount of powers to pick
     */
    private void applyPowers(HashSet<ElitePower> elitePowers, int availablePowerAmount) {

        if (availablePowerAmount < 1) return;

        ArrayList<ElitePower> localPowers = new ArrayList<>(elitePowers);

        for (ElitePower mobPower : this.powers)
            localPowers.remove(mobPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localPowers.size() < 1)
                break;
            else {
                ElitePower selectedPower = localPowers.get(ThreadLocalRandom.current().nextInt(localPowers.size()));
                this.powers.add(selectedPower);
                selectedPower.applyPowers(this.livingEntity);
                localPowers.remove(selectedPower);
                if (selectedPower instanceof MajorPower)
                    this.majorPowerCount++;
                if (selectedPower instanceof MinorPower)
                    this.minorPowerCount++;
            }

    }

    /**
     * Applies a HashSet of ElitePower to an Elite Mob
     *
     * @param elitePowers HashSet of Elite Powers to be applied
     */
    public void setCustomPowers(HashSet<ElitePower> elitePowers) {

        this.powers = elitePowers;
        for (ElitePower elitePower : elitePowers) {
            elitePower.applyPowers(this.livingEntity);
            if (elitePower instanceof MinorPower)
                this.minorPowerCount++;
            if (elitePower instanceof MajorPower)
                this.majorPowerCount++;
        }

        if (this.minorPowerCount > 0) {
            MinorPowerPowerStance minorPowerStanceMath = new MinorPowerPowerStance(this);
        }

        if (this.majorPowerCount > 0) {
            MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance(this);
        }

    }

    /**
     * Returns the living EliteMob
     *
     * @return LivingEntity associated to the Elite Mob
     */
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * Sets the living EliteMob
     */
    public void setLivingEntity(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    /**
     * Returns the level of the Elite Mob
     *
     * @return Level of the Elite Mob
     */
    public int getLevel() {
        return eliteLevel;
    }

    public double getTier() {
        return eliteTier;
    }

    /**
     * Checks if an EliteMobs has that MobPower
     *
     * @param mobPower MobPower to be checked
     * @return If the EliteMob has this MobPower
     */
    public boolean hasPower(ElitePower mobPower) {
        for (ElitePower elitePower : powers)
            if (elitePower.getClass().equals(mobPower.getClass()))
                return true;
        return false;
    }

    /**
     * Returns how many minor powers this Elite Mob has
     *
     * @return How many minor powers this Elite Mob has
     */
    public int getMinorPowerCount() {
        return this.minorPowerCount;
    }

    /**
     * Returns how many major powers this Elite Mob has
     *
     * @return How many major powers this Elite Mob has
     */
    public int getMajorPowerCount() {
        return this.majorPowerCount;
    }

    /**
     * Returns the MobPowers that this Elite Mob has
     *
     * @return MobPowers that this Elite Mob has
     */
    public HashSet<ElitePower> getPowers() {
        return powers;
    }

    public ElitePower getPower(ElitePower elitePower) {
        for (ElitePower iteratedPower : getPowers())
            if (iteratedPower.getClass().equals(elitePower.getClass()))
                return iteratedPower;
        return null;
    }

    /**
     * Returns the maximum health that this Elite Mob has
     *
     * @return Maximum health that this Elite Mob has
     */
    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Returns the name of the Elite Mob
     *
     * @return Name of the Elite Mob
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns whether the Elite Mob has minor visual effects
     *
     * @return Whether the Elite Mob has minor visual effects
     */
    public boolean hasMinorVisualEffect() {
        return this.hasMinorVisualEffect;
    }

    /**
     * Sets whether the Elite Mob has a minor visual effect
     *
     * @param bool Whether the Elite Mob has a minor visual effect
     */
    public void setHasMinorVisualEffect(boolean bool) {
        this.hasMinorVisualEffect = bool;
    }

    /**
     * Returns whether the Elite Mob has a major visual effect
     *
     * @return Whether the Elite Mob has a major visual effect
     */
    public boolean hasMajorVisualEffect() {
        return this.hasMajorVisualEffect;
    }

    /**
     * Sets whether the Elite Mob has a major visual effect
     *
     * @param bool Whether the Elite Mob has a major visual effect
     */
    public void setHasMajorVisualEffect(boolean bool) {
        this.hasMajorVisualEffect = bool;
    }

    /**
     * Returns whether the Elite Mob is a natural entity. Only natural entities can drop special plugin loot. Additionally,
     * based on the settings, only natural Elite Mobs tend to only have visual effects if they are natural.
     *
     * @return Whether the Elite Mob is a natural entity.
     */
    public boolean isNaturalEntity() {
        return this.isNaturalEntity;
    }

    /**
     * Returns whether the Elite Mob will unload when far away.
     *
     * @return Whether the Elite Mob will unload when far away.
     */
    public boolean getPersistent() {
        return this.isPersistent;
    }

    /**
     * Sets whether the Elite Mob will persist through chunk unload. Inverts the bool method for the livingEntity#setRemoveWhenFarAway()
     *
     * @param bool Whether the Elite Mob will unload when far away.
     */
    public void setPersistent(boolean bool) {
        this.isPersistent = bool;
        this.getLivingEntity().setRemoveWhenFarAway(!bool);
    }

    /**
     * Returns whether the Elite Mob can drop special loot. This only affects its eligibility and does not necessarily
     * mean it will drop special loot.
     *
     * @return Whether the Elite Mob can drop special loot.
     */
    public boolean getHasSpecialLoot() {
        return this.hasEliteLoot;
    }

    /**
     * Sets whether the Elite Mob can drop special loot. This only affects its eligibility and does not necessarily
     * mean it will drop special loot.
     *
     * @param bool Whether the Elite Mob can drop special loot.
     */
    public void setHasSpecialLoot(boolean bool) {
        this.isNaturalEntity = bool;
        this.hasEliteLoot = bool;
        this.hasVanillaLoot = bool;
    }

    /**
     * Sets whether the Elite Mob has obfuscate visual effects
     *
     * @param bool Whether the Elite Mob has visual effects
     */
    public void setHasVisualEffectObfuscated(boolean bool) {
        this.hasVisualEffectObfuscated = bool;
    }

    /**
     * Returns whether the Elite Mob has obfuscated visual effects
     *
     * @return Whether the Elite Mob has obfuscated visual effects
     */
    public boolean getHasVisualEffectObfuscated() {
        return this.hasVisualEffectObfuscated;
    }

    /**
     * Gets the spawn reason for the LivingEntity. Used for the API.
     *
     * @return Spawn reason for the LivingEntity.
     */
    public CreatureSpawnEvent.SpawnReason getSpawnReason() {
        return this.spawnReason;
    }

    /**
     * Sets the spawn reason for the Living Entity. Used for the API.
     *
     * @param spawnReason Spawn reason for the Living Entity.
     */
    public void setSpawnReason(CreatureSpawnEvent.SpawnReason spawnReason) {
        this.spawnReason = spawnReason;
    }

    public HashSet<Player> getDamagers() {
        return this.damagers;
    }

    public void addDamager(Player player) {
        this.damagers.add(player);
    }

    public boolean hasDamagers() {
        return !damagers.isEmpty();
    }

    public double getHealthMultiplier() {
        return this.healthMultiplier;
    }

    public void setHealthMultiplier(double healthMultiplier) {
        this.healthMultiplier = healthMultiplier;
    }

    public double getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public double getDefaultMaxHealth() {
        return this.defaultMaxHealth;
    }

    public boolean isCooldown() {
        return this.isCooldown;
    }

    private void setCooldown(boolean isCooldown) {
        this.isCooldown = isCooldown;
    }

    public void doCooldown() {
        setCooldown(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 15);
    }

    public void remove() {
        this.getLivingEntity().remove();
        EntityTracker.unregisterEliteMob(this);
    }

    public void setHasVanillaLoot(boolean hasVanillaLoot) {
        this.hasVanillaLoot = hasVanillaLoot;
    }

}
