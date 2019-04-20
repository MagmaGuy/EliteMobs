package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobEntity {

    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    private LivingEntity eliteMob;
    private int eliteMobLevel;
    private double eliteMobTier;
    private double maxHealth;
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
    private boolean hasStacking = true;
    private boolean hasCustomArmor = false;
    private boolean hasCustomName = false;
    private boolean hasCustomPowers = false;
    private boolean hasFarAwayUnload = true;
    private boolean hasCustomHealth = false;
    private boolean hasNormalLoot = true;
    private CreatureSpawnEvent.SpawnReason spawnReason;

    /**
     * Check through WorldGuard if the location is valid. Regions flagged with the elitemob-spawning deny tag will cancel
     * the Elite Mob conversion
     *
     * @param location Location to be checked
     * @return Whether the location is valid
     */
    private static boolean validSpawnLocation(Location location) {
        //todo: reenable this for 1.14
        return true;
//        if (!Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled()) return true;
//        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
//        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//        RegionQuery query = container.createQuery();
//        ApplicableRegionSet set = query.getApplicableRegions(wgLocation);
//        return set.testState(null, EliteMobs.ELITEMOBS_SPAWN_FLAG);
    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     *
     * @param livingEntity  Minecraft entity associated to this elite mob
     * @param eliteMobLevel Level of the mob, can be modified during runtime. Dynamically assigned.
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(livingEntity.getLocation())) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
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
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
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

        eliteMob.setCanPickupItems(false);

    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     *
     * @param livingEntity  Minecraft entity associated to this elite mob
     * @param eliteMobLevel Level of the mob, can be modified during runtime. Dynamically assigned.
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel, double currentHealthPercent, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(livingEntity.getLocation())) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
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
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        eliteMob.setHealth(maxHealth * currentHealthPercent);
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

        eliteMob.setCanPickupItems(false);

    }


    /**
     * Spawning method for boss mobs.
     * Assumes custom powers and custom names.
     *
     * @param entityType    type of mob that this entity is slated to become
     * @param location      location at which the elite mob will spawn
     * @param eliteMobLevel boss mob level, should be automatically generated based on the highest player tier online
     * @param name          the name for this boss mob, overrides the usual elite mob name format
     * @see BossMobEntity
     */
    public EliteMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(location)) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = spawnBossMobLivingEntity(entityType, location);
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
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
        setCustomName(name);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
        /*
        Register whether or not the elite mob is natural
         */
        this.isNaturalEntity = EntityTracker.isNaturalEntity(this.eliteMob);
        /*
        These have custom powers
         */
        this.hasCustomPowers = true;
        /*
        Start tracking the entity
         */
//        EntityTracker.registerEliteMob(this);

        eliteMob.setCanPickupItems(false);

        this.setHasStacking(false);
        this.setHasCustomArmor(true);

    }

    /**
     * Constructor for Elite Mobs spawned via command
     *
     * @param entityType    Type of entity to be spawned
     * @param location      Location at which the entity will spawn
     * @param eliteMobLevel Level of the Elite Mob
     * @param mobPowers     HashSet of ElitePower that the entity will have (can be empty)
     */
    public EliteMobEntity(EntityType entityType, Location location, int eliteMobLevel, HashSet<ElitePower> mobPowers, CreatureSpawnEvent.SpawnReason spawnReason) {

        /*
        Run a WorldGuard check to see if the entity is allowed to get converted at this location
         */
        if (!validSpawnLocation(location)) return;

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = spawnBossMobLivingEntity(entityType, location);
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
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
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
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
                elitePower.applyPowers(eliteMob);
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

        eliteMob.setCanPickupItems(false);

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
    private void setCustomName(EliteMobProperties eliteMobProperties) {
        this.name = ChatColorConverter.convert(
                eliteMobProperties.getName().replace(
                        "$level", eliteMobLevel + ""));
        eliteMob.setCustomName(this.name);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            eliteMob.setCustomNameVisible(true);
    }

    /**
     * Sets the display name to be used by this Elite Mob
     *
     * @param name String which defines the display name
     */
    private void setCustomName(String name) {
        this.name = ChatColorConverter.convert(name);
        this.getLivingEntity().setCustomName(this.name);
        this.hasCustomName = true;
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            eliteMob.setCustomNameVisible(true);
    }

    /**
     * Sets the level of the Elite Mob. Values below 1 default to 1
     *
     * @param newLevel Level of the Elite Mob
     */
    private void setEliteMobLevel(int newLevel) {
        if (newLevel < 1)
            newLevel = 1;
        this.eliteMobLevel = newLevel;
    }

    /**
     * Sets the max health of the Elite Mob. This is calculated based on the Elite Mob level. Maxes out at 2000 due to
     * Minecraft restrictions
     *
     * @param eliteMobProperties EliteMobProperties from where the default max health of the mob will be obtained
     */
    private void setMaxHealth(EliteMobProperties eliteMobProperties) {
        double defaultMaxHealth = eliteMobProperties.getDefaultMaxHealth();
        this.maxHealth = (eliteMobLevel * CombatSystem.PER_LEVEL_POWER_INCREASE * defaultMaxHealth + defaultMaxHealth);
        eliteMob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    /**
     * Sets the health of the Elite Mob. This is the same value as the max health. Caps out at 2000.
     */
    private void setHealth() {
        eliteMob.setHealth(maxHealth = (maxHealth > 2000) ? 2000 : maxHealth);
    }

    /**
     * Sets the health of the Elite Mob. This is a percentage of the maximum health.
     *
     * @param healthPercentage Percentage of the maximum health to be set
     */
    private void setHealth(double healthPercentage) {
        eliteMob.setHealth(this.maxHealth * healthPercentage);
    }


    /**
     * Sets the armor of the EliteMob. The equipment progresses with every passing Elite Mob tier and dynamically adjusts
     * itself to the maximum tier set.
     */
    private void setArmor() {

        if (VersionChecker.currentVersionIsUnder(12, 2)) return;
        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_ARMOR)) return;

        eliteMob.getEquipment().setItemInMainHandDropChance(0);
        eliteMob.getEquipment().setHelmetDropChance(0);
        eliteMob.getEquipment().setChestplateDropChance(0);
        eliteMob.getEquipment().setLeggingsDropChance(0);
        eliteMob.getEquipment().setBootsDropChance(0);

        if (hasCustomArmor) return;

        if (!(eliteMob instanceof Zombie || eliteMob instanceof PigZombie ||
                eliteMob instanceof Skeleton || eliteMob instanceof WitherSkeleton)) return;

        eliteMob.getEquipment().setBoots(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setLeggings(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setChestplate(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (eliteMobLevel >= 12)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (eliteMobLevel >= 14)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        if (eliteMobLevel >= 16)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

        if (eliteMobLevel >= 18)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        if (eliteMobLevel >= 20)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));

        if (eliteMobLevel >= 22)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        if (eliteMobLevel >= 24)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

        if (eliteMobLevel >= 26)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        if (eliteMobLevel >= 28)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

        if (eliteMobLevel >= 30)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

        if (eliteMobLevel >= 32)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

        if (eliteMobLevel >= 34)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

        if (eliteMobLevel >= 36)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        if (eliteMobLevel >= 38)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));

        if (eliteMobLevel >= 40)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

        if (eliteMobLevel >= 42)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

    }

    /**
     * Randomizes the powers that the EliteMob has. Determines which powers will be picked by randomizing the powers
     * from pools associated to the mob type and conditioned by config settings
     *
     * @param eliteMobProperties Properties of the Elite Mob Type
     */
    private void randomizePowers(EliteMobProperties eliteMobProperties) {

        if (hasCustomPowers) return;
        if (eliteMobTier < 1) return;

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (eliteMobTier >= 1) availableDefensivePowers = 1;
        if (eliteMobTier >= 2) availableOffensivePowers = 1;
        if (eliteMobTier >= 3) availableMiscellaneousPowers = 1;
        if (eliteMobTier >= 4) availableMajorPowers = 1;
        if (eliteMobTier >= 5) availableDefensivePowers = 2;
        if (eliteMobTier >= 6) availableOffensivePowers = 2;
        if (eliteMobTier >= 7) availableMiscellaneousPowers = 2;
        if (eliteMobTier >= 8) availableMajorPowers = 2;

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
                selectedPower.applyPowers(this.eliteMob);
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
            elitePower.applyPowers(this.eliteMob);
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
        return eliteMob;
    }

    /**
     * Returns the level of the Elite Mob
     *
     * @return Level of the Elite Mob
     */
    public int getLevel() {
        return eliteMobLevel;
    }

    /**
     * Checks if an EliteMobs has that MobPower
     *
     * @param mobPower MobPower to be checked
     * @return If the EliteMob has this MobPower
     */
    public boolean hasPower(ElitePower mobPower) {
        for (ElitePower elitePower : powers)
            if (elitePower.getClass().getTypeName().equals(mobPower.getClass().getTypeName()))
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

    /**
     * Returns the maximum health that this Elite Mob has
     *
     * @return Maximum health that this Elite Mob has
     */
    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Sets whether this Elite Mob can stack with other Elite Mobs or Entities of the the same Type
     *
     * @param bool Whether the Elite Mob can stack
     */
    public void setHasStacking(boolean bool) {
        this.hasStacking = bool;
    }

    /**
     * Returns if this Elite Mob has custom armor
     *
     * @return Whether Elite Mob has custom armor
     */
    public boolean getHasCustomArmor() {
        return this.hasCustomArmor;
    }

    /**
     * Sets if this Elite Mob will wear custom armor. Will then only be applied by other methods.
     *
     * @param bool whether this Elite Mob will wear custom armor
     */
    public void setHasCustomArmor(boolean bool) {
        this.hasCustomArmor = bool;
    }

    /**
     * Returns whether the Elite Mob has custom ElitePower
     *
     * @return Whether the Elite Mob has custom ElitePower
     */
    public boolean getHasCustomPowers() {
        return this.hasCustomPowers;
    }

    /**
     * Sets if the Elite Mob will have custom ElitePower
     *
     * @param bool Whether the Elite Mob will have custom EltiePower
     */
    public void setHasCustomPowers(boolean bool) {
        this.hasCustomPowers = bool;
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
     * Sets whether the ELite Mob is a natural entity. Only natural entities can drop special plugin loot. Additionally,
     * based on the settings, only natural Elite Mobs tend to only have visual effects if they are natural.
     *
     * @param bool Whether the Elite Mob is a natural entity.
     */
    public void setIsNaturalEntity(Boolean bool) {
        this.isNaturalEntity = bool;
        this.hasNormalLoot = bool;
    }

    /**
     * Returns whether the Elite Mob can stack.
     *
     * @return Whether the Elite Mob can stack.
     */
    public boolean canStack() {
        return this.hasStacking;
    }

    /**
     * Sets the name of the Elite Mob.
     *
     * @param name Name of the Elite Mob.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether the Elite Mob will unload when far away.
     *
     * @return Whether the Elite Mob will unload when far away.
     */
    public boolean getHasFarAwayUnload() {
        return this.hasFarAwayUnload;
    }

    /**
     * Sets whether the Elite Mob will unload when far away.
     *
     * @param bool Whether the Elite Mob will unload when far away.
     */
    public void setHasFarAwayUnload(boolean bool) {
        this.hasFarAwayUnload = bool;
    }

    /**
     * Returns whether the Elite Mob can drop special loot. This only affects its eligibility and does not necessarily
     * mean it will drop special loot.
     *
     * @return Whether the Elite Mob can drop special loot.
     */
    public boolean getHasSpecialLoot() {
        return this.hasNormalLoot;
    }

    /**
     * Sets whether the ELite Mob can drop special loot. This only affects its eligibility and does not necessarily
     * mean it will drop special loot.
     *
     * @param bool Whether the Elite Mob can drop special loot.
     */
    public void setHasSpecialLoot(boolean bool) {
        this.isNaturalEntity = bool;
        this.hasNormalLoot = bool;
    }

    /**
     * Returns whether the Elite Mob has a special health value. Default values are simply multiplied from the default
     * health.
     *
     * @return Whether the Elite Mob has a special health value.
     */
    public boolean getHasCustomHealth() {
        return this.hasCustomHealth;
    }

    /**
     * Sets whether the Elite Mob has a special health value. Default health values are simply multiplied from the default
     * health.
     *
     * @param bool Whether the Elite Mob has a special health value.
     */
    public void setHasCustomHealth(boolean bool) {
        this.hasCustomHealth = bool;
    }

    /**
     * Returns whether the Elite Mob has a custom name
     *
     * @return Whether the Elite Mob has a custom name
     */
    public boolean getHasCustomName() {
        return this.hasCustomName;
    }

    /**
     * Sets whether the Elite Mob has a custom name
     *
     * @param bool Whether the Elite Mob has a custom name
     */
    public void setHasCustomName(boolean bool) {
        this.hasCustomName = bool;
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

}
