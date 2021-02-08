package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AntiExploitMessage;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.entitytracker.EliteEntityTracker;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.MajorPower;
import com.magmaguy.elitemobs.powers.MinorPower;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobEntity {

    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    private LivingEntity livingEntity;
    private int eliteLevel;
    private double eliteTier;
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
    private EntityType entityType;
    /*
    This just defines default behavior
     */
    private boolean hasCustomPowers = false;
    private Boolean isPersistent = false;
    private boolean hasVanillaLoot = true;
    private boolean hasEliteLoot = true;
    private CreatureSpawnEvent.SpawnReason spawnReason;

    private final HashMap<Player, Double> damagers = new HashMap<>();

    private double healthMultiplier = 1.0;
    private double damageMultiplier = 1.0;
    private double defaultMaxHealth;

    private boolean isCooldown = false;

    private boolean triggeredAntiExploit = false;
    private int antiExploitPoints = 0;
    private boolean inAntiExploitCooldown = false;

    private boolean isRegionalBoss = false;
    private boolean inCombat = false;
    private boolean inCombatGracePeriod = false;

    public UUID uuid;

    public CustomBossEntity customBossEntity;
    public RegionalBossEntity regionalBossEntity;
    public PhaseBossEntity phaseBossEntity;

    /**
     * Check through WorldGuard if the location is valid. Regions flagged with the elitemob-spawning deny tag will cancel
     * the Elite Mob conversion
     */
    public static boolean validSpawnLocation(Location location) {
        if (!EliteMobs.worldguardIsEnabled)
            return true;
        if (!Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled()) return true;
        return WorldGuardFlagChecker.checkFlag(location, WorldGuardCompatibility.getEliteMobsSpawnFlag());
    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     */
    public EliteMobEntity(LivingEntity livingEntity,
                          int eliteLevel,
                          CreatureSpawnEvent.SpawnReason spawnReason) {
        if (!initializeBasicValues(livingEntity, eliteLevel, spawnReason, false)) return;

        //Get correct instance of plugin data, necessary for settings names and health among other things
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);

        //Handle name, variable as per stacking rules
        setName(eliteMobProperties);

        //Set the armor
        setArmor();

        //Set the power list
        randomizePowers(eliteMobProperties);
    }

    /**
     * Spawning method for boss mobs / regional bosses.
     * Assumes custom powers and custom names.
     */
    public EliteMobEntity(LivingEntity livingEntity,
                          int eliteLevel,
                          String name,
                          HashSet<ElitePower> mobPowers,
                          CreatureSpawnEvent.SpawnReason spawnReason,
                          Boolean isPersistent) {
        if (!initializeBasicValues(livingEntity, eliteLevel, spawnReason, isPersistent)) return;
        initializeCustomBossEliteEntity(livingEntity, eliteLevel, name, mobPowers, spawnReason);
        //These have custom powers
        applyCustomPowers(mobPowers);
    }

    /**
     * For phase bosses
     */
    public EliteMobEntity(LivingEntity livingEntity,
                          int eliteLevel,
                          String name,
                          HashSet<ElitePower> mobPowers,
                          CreatureSpawnEvent.SpawnReason spawnReason,
                          double healthPercentage,
                          Boolean isPersistent) {
        if (!initializeBasicValues(livingEntity, eliteLevel, spawnReason, isPersistent)) return;
        initializeCustomBossEliteEntity(livingEntity, eliteLevel, name, mobPowers, spawnReason);
        this.setHealth(healthPercentage * maxHealth);
        //These have custom powers
        applyCustomPowers(mobPowers);
    }

    /**
     * Constructor for Elite Mobs spawned via command
     */
    public EliteMobEntity(LivingEntity livingEntity,
                          int eliteLevel,
                          HashSet<ElitePower> mobPowers,
                          CreatureSpawnEvent.SpawnReason spawnReason) {

        if (!initializeBasicValues(livingEntity, eliteLevel, spawnReason, false)) return;

        //Get correct instance of plugin data, necessary for settings names and health among other things
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity.getType());

        //Handle name, variable as per stacking rules
        setName(eliteMobProperties);

        //Set the armor
        setArmor();

        //Register whether or not the elite mob is natural
        //All mobs spawned via commands are considered natural
        this.isNaturalEntity = true;

        //Set the power list
        if (!mobPowers.isEmpty()) {
            this.powers = mobPowers;
            for (ElitePower elitePower : powers) {
                elitePower.applyPowers(livingEntity);
                if (elitePower instanceof MajorPower)
                    this.majorPowerCount++;
                if (elitePower instanceof MinorPower)
                    this.minorPowerCount++;
            }
            new MinorPowerPowerStance(this);
            new MajorPowerPowerStance(this);
        } else
            randomizePowers(eliteMobProperties);

    }

    /**
     * Constructor for Custom Bosses
     */
    private void initializeCustomBossEliteEntity(LivingEntity livingEntity,
                                                 int eliteLevel,
                                                 String name,
                                                 HashSet<ElitePower> mobPowers,
                                                 CreatureSpawnEvent.SpawnReason spawnReason) {

        if (!initializeBasicValues(livingEntity, eliteLevel, spawnReason, isPersistent)) return;

        this.name = name;

        //Stop creation if the creation was cancelled in the spawn event
        //if (!EntityTracker.registerEliteMob(this)) return;


        //Handle name, variable as per stacking rules
        setName(name);

        //These have custom powers
        applyCustomPowers(mobPowers);

    }

    private boolean initializeBasicValues(LivingEntity livingEntity,
                                          int eliteLevel,
                                          CreatureSpawnEvent.SpawnReason spawnReason,
                                          boolean isPersistent) {
        //Run a WorldGuard check to see if the entity is allowed to get converted at this location
        if (!validSpawnLocation(livingEntity.getLocation())) return false;

        //Register living entity to keep track of which entity this object is tied to
        this.livingEntity = livingEntity;
        this.entityType = livingEntity.getType();

        setPersistent(isPersistent);

        //Register UUID to be used in trackers
        this.uuid = livingEntity.getUniqueId();

        //Sets the spawn reason
        this.spawnReason = spawnReason;

        //Register level, this is variable as per stacking rules
        this.eliteLevel = eliteLevel;
        this.eliteTier = MobTierCalculator.findMobTier(eliteLevel);

        this.livingEntity.setCanPickupItems(false);
        livingEntity.getEquipment().setItemInMainHandDropChance(0);
        livingEntity.getEquipment().setItemInOffHandDropChance(0);
        livingEntity.getEquipment().setHelmetDropChance(0);
        livingEntity.getEquipment().setChestplateDropChance(0);
        livingEntity.getEquipment().setLeggingsDropChance(0);
        livingEntity.getEquipment().setBootsDropChance(0);

        if (!VersionChecker.currentVersionIsUnder(1, 15))
            if (livingEntity instanceof Bee)
                ((Bee) livingEntity).setCannotEnterHiveTicks(Integer.MAX_VALUE);

        setMaxHealth();

        if (entityType.equals(EntityType.WOLF)) {
            Wolf wolf = (Wolf) livingEntity;
            wolf.setAngry(true);
            wolf.setBreed(false);
        }

        this.getLivingEntity().setRemoveWhenFarAway(!isPersistent);

        //Stop creation if the creation was cancelled in the spawn event
        return EntityTracker.registerEliteMob(this);
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
        livingEntity.setCustomNameVisible(DefaultConfig.alwaysShowNametags);
    }

    /**
     * Sets the display name to be used by this Elite Mob
     *
     * @param name String which defines the display name
     */
    public void setName(String name) {
        String parsedName = name.replace("$level", this.eliteLevel + "")
                .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + this.eliteLevel + "&2]&f"))
                .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + this.eliteLevel + "&6〗&f"))
                .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + this.eliteLevel + "&4』&f"))
                .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + this.eliteLevel + "&8〕&f")
                .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + this.eliteLevel + "&4」&f"));
        this.name = ChatColorConverter.convert(parsedName);
        this.getLivingEntity().setCustomName(this.name);
        livingEntity.setCustomNameVisible(DefaultConfig.alwaysShowNametags);
        if (customBossEntity != null)
            DisguiseEntity.setDisguiseNameVisibility(DefaultConfig.alwaysShowNametags, livingEntity);
    }

    public void setNameVisible(boolean isVisible) {
        livingEntity.setCustomNameVisible(isVisible);
        if (customBossEntity != null)
            DisguiseEntity.setDisguiseNameVisibility(isVisible, livingEntity);
    }

    /**
     * Sets the max health of the Elite Mob. This is calculated based on the Elite Mob level. Maxes out at 2000 due to
     * Minecraft restrictions
     */
    private void setMaxHealth() {
        this.defaultMaxHealth = EliteMobProperties.getPluginData(this.getLivingEntity().getType()).getDefaultMaxHealth();
        this.maxHealth = (eliteTier * CombatSystem.TARGET_HITS_TO_KILL + this.defaultMaxHealth);
        //7 is the base damage of a diamond sword
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        livingEntity.setHealth(maxHealth);
    }

    public void resetMaxHealth() {
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        livingEntity.setHealth(maxHealth);
    }

    private void setMaxHealth(double healthMultiplier) {
        this.defaultMaxHealth = EliteMobProperties.getPluginData(this.getLivingEntity().getType()).getDefaultMaxHealth();
        this.maxHealth = (eliteTier * CombatSystem.TARGET_HITS_TO_KILL + this.defaultMaxHealth) * healthMultiplier;
        //7 is the base damage of a diamond sword
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        livingEntity.setHealth(maxHealth);
    }

    public void setHealth(double health) {
        livingEntity.setHealth(health);
    }

    public double getHealth() {
        return livingEntity.getHealth();
    }

    public double damage(double damage) {
        double health = Math.max(0, livingEntity.getHealth() - damage);
        livingEntity.setHealth(health);
        return damage;
    }

    public void fullHeal() {
        if (customBossEntity != null)
            if (customBossEntity.phaseBossEntity != null)
                customBossEntity.phaseBossEntity.fullHeal(this);
        setHealth(this.maxHealth);
        damagers.clear();
    }

    /**
     * Sets the armor of the EliteMob. The equipment progresses with every passing Elite Mob tier and dynamically adjusts
     * itself to the maximum tier set.
     */
    private void setArmor() {

        if (!MobCombatSettingsConfig.doEliteArmor) return;

        if (!(livingEntity instanceof Zombie || livingEntity instanceof PigZombie ||
                livingEntity instanceof Skeleton || livingEntity instanceof WitherSkeleton)) return;

        livingEntity.getEquipment().setBoots(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (eliteLevel >= 5)
            if (MobCombatSettingsConfig.doEliteHelmets)
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (eliteLevel >= 10)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        if (eliteLevel >= 15)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

        if (eliteLevel >= 20)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        if (eliteLevel >= 25)
            if (MobCombatSettingsConfig.doEliteHelmets)
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));

        if (eliteLevel >= 30)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        if (eliteLevel >= 35)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

        if (eliteLevel >= 40)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        if (eliteLevel >= 45)
            if (MobCombatSettingsConfig.doEliteHelmets)
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

        if (eliteLevel >= 50)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

        if (eliteLevel >= 55)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

        if (eliteLevel >= 60)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

        if (eliteLevel >= 65)
            livingEntity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        if (eliteLevel >= 70)
            if (MobCombatSettingsConfig.doEliteHelmets)
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));

        if (eliteLevel >= 75)
            livingEntity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

        if (eliteLevel >= 80)
            livingEntity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

        if (livingEntity.getEquipment().getHelmet() != null) {
            ItemMeta helmetMeta = livingEntity.getEquipment().getHelmet().getItemMeta();
            if (helmetMeta != null) {
                helmetMeta.setUnbreakable(true);
                livingEntity.getEquipment().getHelmet().setItemMeta(helmetMeta);
            }
        }
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

        if (eliteTier >= 10) availableDefensivePowers = 1;
        if (eliteTier >= 20) availableOffensivePowers = 1;
        if (eliteTier >= 30) availableMiscellaneousPowers = 1;
        if (eliteTier >= 40) availableMajorPowers = 1;
        if (eliteTier >= 50) availableDefensivePowers = 2;
        if (eliteTier >= 60) availableOffensivePowers = 2;
        if (eliteTier >= 70) availableMiscellaneousPowers = 2;
        if (eliteTier >= 80) availableMajorPowers = 2;

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

    private void applyCustomPowers(HashSet<ElitePower> elitePowers) {
        this.hasCustomPowers = true;
        this.powers = elitePowers;
        for (ElitePower elitePower : powers) {
            elitePower.applyPowers(livingEntity);
            if (elitePower instanceof MajorPower)
                this.majorPowerCount++;
            if (elitePower instanceof MinorPower)
                this.minorPowerCount++;
        }
    }

    /**
     * Applies the power to the Elite Mob based on a HashSet of powers
     *
     * @param elitePowers          ElitePower HashSet from which the powers will be randomized
     * @param availablePowerAmount Amount of powers to pick
     */
    private void applyPowers(HashSet<ElitePower> elitePowers, int availablePowerAmount) {
        //TODO: this may not be amazingly efficient
        for (Iterator<ElitePower> elitePowerIterator = elitePowers.iterator(); elitePowerIterator.hasNext(); ) {
            ElitePower elitePower = elitePowerIterator.next();
            if (!PowersConfig.getPower(elitePower.getFileName()).isEnabled())
                elitePowerIterator.remove();
        }

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
     * Used by the Custom Boss Entity to respawn entities that are known to be supposed to be alive but whose LivingEntity
     * has been nullified for whatever reason.
     *
     * @param location
     */
    public void setNewLivingEntity(Location location) {
        WorldGuardSpawnEventBypasser.forceSpawn();
        this.livingEntity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        this.uuid = livingEntity.getUniqueId();
        livingEntity.setRemoveWhenFarAway(!isPersistent);
        new EliteEntityTracker(this, isPersistent);
        if (customBossEntity != null)
            customBossEntity.silentCustomBossInitialization();
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

    public ElitePower getPower(String elitePower) {
        for (ElitePower iteratedPower : getPowers())
            if (iteratedPower.getFileName().equals(elitePower))
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
    public void setPersistent(Boolean bool) {
        if (bool != null) {
            this.isPersistent = bool;
        } else {
            this.isPersistent = false;
        }
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

    public HashMap<Player, Double> getDamagers() {
        return this.damagers;
    }

    public void addDamager(Player player, double damage) {
        if (!damagers.isEmpty())
            for (Player iteratedPlayer : damagers.keySet())
                if (iteratedPlayer.getUniqueId().equals(player.getUniqueId())) {
                    this.damagers.put(iteratedPlayer, this.damagers.get(iteratedPlayer) + damage);
                    return;
                }
        this.damagers.put(player, damage);
    }

    public void addDamagers(HashMap<Player, Double> newDamagers) {
        this.damagers.putAll(newDamagers);
    }

    public boolean hasDamagers() {
        return !damagers.isEmpty();
    }

    public void setHealthMultiplier(double healthMultiplier) {
        this.healthMultiplier = healthMultiplier;
        this.setMaxHealth(healthMultiplier);
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

    public void remove(boolean removeEntity) {
        if (removeEntity)
            this.getLivingEntity().remove();
    }

    public void remove(RemovalReason removalReason) {
        if (removalReason.equals(RemovalReason.SHUTDOWN)) {
            remove(true);
        }
        if (removalReason.equals(RemovalReason.CHUNK_UNLOAD))
            if (isPersistent)
                return;
        if (livingEntity != null) {
            livingEntity.removeMetadata(MetadataHandler.ELITE_MOB_METADATA, MetadataHandler.PLUGIN);
            livingEntity.remove();
        }
    }

    public void setHasVanillaLoot(boolean hasVanillaLoot) {
        this.hasVanillaLoot = hasVanillaLoot;
    }

    public boolean hasVanillaLoot() {
        return this.hasVanillaLoot;
    }

    public void setTriggeredAntiExploit(boolean triggeredAntiExploit) {
        this.triggeredAntiExploit = triggeredAntiExploit;
        if (triggeredAntiExploit) {
            this.hasEliteLoot = false;
            this.hasVanillaLoot = false;
        }
    }

    public boolean getTriggeredAntiExploit() {
        return this.triggeredAntiExploit;
    }

    public void incrementAntiExploit(int value, String cause) {
        antiExploitPoints += value;
        if (antiExploitPoints > AntiExploitConfig.antiExploitThreshold) {
            setTriggeredAntiExploit(true);
            AntiExploitMessage.sendWarning(livingEntity, cause);
        }
    }

    public void decrementAntiExploit(int value) {
        antiExploitPoints -= value;
    }

    public boolean isInAntiExploitCooldown() {
        return this.inAntiExploitCooldown;
    }

    public void setInAntiExploitCooldown() {
        this.inAntiExploitCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                inAntiExploitCooldown = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20);
    }

    public boolean isRegionalBoss() {
        return this.isRegionalBoss;
    }

    public void setIsRegionalBoss(boolean isRegionalBoss) {
        this.isRegionalBoss = isRegionalBoss;
    }

    public void setIsInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public boolean isInCombat() {
        return this.inCombat;
    }

    public void setCombatGracePeriod(int delayInTicks) {
        this.inCombatGracePeriod = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                inCombatGracePeriod = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, delayInTicks);
    }

    public boolean isInCombatGracePeriod() {
        return this.inCombatGracePeriod;
    }
}
