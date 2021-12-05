package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AntiExploitMessage;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EliteEntityTracker;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.meta.MajorPower;
import com.magmaguy.elitemobs.powers.meta.MinorPower;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
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

public class EliteEntity implements SimplePersistentEntityInterface {

    protected final HashMap<Player, Double> damagers = new HashMap<>();
    protected final UUID eliteUUID = UUID.randomUUID();
    /*
    Store all powers in one set, makes no sense to access it in individual sets.
    The reason they are split up in the first place is to add them in a certain ratio
    Once added, they can just be stored in a pool
     */
    @Getter
    protected HashSet<ElitePower> elitePowers = new HashSet<>();
    //coming soon - decoupling aggro from damage to allow for tanking mechanics
    protected HashMap<Player, Double> aggro = new HashMap<>();
    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    protected LivingEntity livingEntity;
    //LivingEntity gets removed as soon as it dies, unsyncedLivingEntity only ever overwrites when a new living entity is created.
    protected LivingEntity unsyncedLivingEntity;
    @Getter
    @Setter
    protected int level;
    @Getter
    protected double maxHealth;
    @Getter
    protected String name;
    @Getter
    protected int minorPowerCount = 0;
    @Getter
    protected int majorPowerCount = 0;
    @Getter
    @Setter
    protected boolean minorVisualEffect = false;
    @Getter
    @Setter
    protected boolean majorVisualEffect = false;
    @Getter
    @Setter
    protected boolean visualEffectObfuscated = true;
    @Getter
    protected boolean isNaturalEntity;
    protected EntityType entityType;
    @Getter
    protected Boolean isPersistent = false;
    @Getter
    @Setter
    protected boolean vanillaLoot = true;
    @Getter
    protected boolean eliteLoot = true;
    @Getter
    protected CreatureSpawnEvent.SpawnReason spawnReason;
    @Getter
    @Setter
    protected double healthMultiplier = 1.0;
    @Getter
    @Setter
    protected double damageMultiplier = 1.0;
    protected double defaultMaxHealth;
    @Getter
    @Setter
    protected boolean inCooldown = false;
    @Getter
    protected boolean triggeredAntiExploit = false;
    protected int antiExploitPoints = 0;
    @Getter
    protected boolean inAntiExploitCooldown = false;
    @Getter
    @Setter
    protected boolean inCombat = false;
    @Getter
    protected boolean inCombatGracePeriod = false;
    @Setter
    protected EliteEntity summoningEntity;
    protected List<CustomBossEntity> globalReinforcementEntities = new ArrayList<>();
    protected List<CustomBossEntity> eliteReinforcementEntities = new ArrayList<>();
    //currently used to store ender crystals for the dragon boss fight
    protected List<Entity> nonEliteReinforcementEntities = new ArrayList<>();
    protected boolean bypassesProtections = false;
    private double health;
    @Getter
    @Setter
    private boolean dying = false;

    /**
     * Functions as a placeholder for {@link CustomBossEntity} that haven't been initialized yet. Uses the builder pattern
     * in order to further initialize values at an arbitrary point in the future.
     * <p>
     * Uses:
     * - {@link CustomEvent} queueing through the {@link CustomSpawn} system
     * <p>
     * {@link EliteEntity} constructed this way must at some point correctly invoke {@link CustomBossEntity#setSpawnLocation(Location)}
     * for the spawn method.
     */
    public EliteEntity() {
    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     */
    public EliteEntity(LivingEntity livingEntity,
                       int level,
                       CreatureSpawnEvent.SpawnReason spawnReason) {
        setLevel(level);
        setLivingEntity(livingEntity, spawnReason);
        //Get correct instance of plugin data, necessary for settings names and health among other things
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        setName(eliteMobProperties);
        setArmor();
        setMaxHealth();
        randomizePowers(eliteMobProperties);
    }

    public boolean getBypassesProtections() {
        return bypassesProtections;
    }

    public void setBypassesProtections(boolean bypassesProtections) {
        this.bypassesProtections = bypassesProtections;
    }

    /**
     * Elite UUID. This UUID is guaranteed to be final for the Elite Entity, and is not in sync with the {@link LivingEntity} {@link UUID} .
     * <p>
     * Noteworthy uses: {@link EliteEntity} will survive chunk reloads, and in the case of {@link com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity} will survive deaths for the entirety of runtime.
     *
     * @return The final UUID value of this {@link EliteEntity}
     */
    public UUID getEliteUUID() {
        return eliteUUID;
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

    public HashMap<Player, Double> getDamagers() {
        return damagers;
    }

    public boolean isCustomBossEntity() {
        return this instanceof CustomBossEntity;
    }

    /**
     * Returns the {@link LivingEntity} currently being used by the {@link EliteEntity}. Returns null if none is currently active, even
     * if one was active previously.
     *
     * @return Currently active {@link LivingEntity}
     * @see EliteEntity#getUnsyncedLivingEntity() if you want a version that does get nulled once the {@link LivingEntity} stops being valid
     */
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * This {@link LivingEntity} represents the previous entity spawned by EliteMobs. It is only overwritten when a new one
     * is generated, whereas {@link EliteEntity#getLivingEntity()} returns null as soon as the {@link LivingEntity} stops being alive
     * for safety reasons.
     * <p>
     * This is the method used when operations need to be run on dead instances, such as in {@link com.magmaguy.elitemobs.api.EliteMobDeathEvent}.
     *
     * @return The latest {@link LivingEntity} generated for this {@link EliteEntity}
     */
    public LivingEntity getUnsyncedLivingEntity() {
        return unsyncedLivingEntity;
    }

    public void setLivingEntity(LivingEntity livingEntity, CreatureSpawnEvent.SpawnReason spawnReason) {
        if (this.livingEntity != null) EntityTracker.unregister(this.livingEntity.getUniqueId(), RemovalReason.ENTITY_REPLACEMENT);
        if (livingEntity == null) return;
        this.livingEntity = livingEntity;
        this.unsyncedLivingEntity = livingEntity;
        this.entityType = livingEntity.getType();

        this.livingEntity.setCanPickupItems(false);
        livingEntity.getEquipment().setItemInMainHandDropChance(0);
        livingEntity.getEquipment().setItemInOffHandDropChance(0);
        livingEntity.getEquipment().setHelmetDropChance(0);
        livingEntity.getEquipment().setChestplateDropChance(0);
        livingEntity.getEquipment().setLeggingsDropChance(0);
        livingEntity.getEquipment().setBootsDropChance(0);

        if (livingEntity.getType().equals(EntityType.RABBIT))
            ((Rabbit) livingEntity).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);

        if (entityType.equals(EntityType.WOLF)) {
            Wolf wolf = (Wolf) livingEntity;
            wolf.setAngry(true);
            wolf.setBreed(false);
        }

        if (entityType.equals(EntityType.ENDER_DRAGON))
            ((EnderDragon) livingEntity).getBossBar().setTitle(getName());

        if (!VersionChecker.serverVersionOlderThan(15, 0))
            if (livingEntity instanceof Bee)
                ((Bee) livingEntity).setCannotEnterHiveTicks(Integer.MAX_VALUE);

        this.spawnReason = spawnReason;

        //This sets whether the entity gets despawned when beyond a certain distance from the player, should only happen
        //for entities which aren't pseudo-persistent
        this.getLivingEntity().setRemoveWhenFarAway(!isPersistent);
        this.getLivingEntity().setPersistent(false);

        setMaxHealth();

        if (getName() == null)
            setName(EliteMobProperties.getPluginData(entityType));

        this.name = livingEntity.getCustomName();

        EntityTracker.registerEliteMob(this);
    }

    public void setNameVisible(boolean isVisible) {
        //Check if the boss is already dead
        if (livingEntity == null) return;
        livingEntity.setCustomNameVisible(isVisible);
        if (isCustomBossEntity())
            DisguiseEntity.setDisguiseNameVisibility(isVisible, livingEntity);
    }

    private void setMaxHealth() {
        this.defaultMaxHealth = EliteMobProperties.getPluginData(this.getLivingEntity().getType()).getDefaultMaxHealth();
        this.maxHealth = (level * CombatSystem.TARGET_HITS_TO_KILL + this.defaultMaxHealth) * healthMultiplier;
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        livingEntity.setHealth(maxHealth);
        this.health = maxHealth;
    }

    public void resetMaxHealth() {
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        livingEntity.setHealth(maxHealth);
        this.health = maxHealth;
    }

    /**
     * Health is cached by EliteMobs for when health needs to be displayed when the {@link LivingEntity} isn't valid, but
     * return the field from the {@link LivingEntity} when available
     *
     * @return Boss health
     */
    public double getHealth() {
        if (livingEntity != null)
            return livingEntity.getHealth();
        else
            return this.health;
    }

    public void setHealth(double health) {
        this.health =  Math.min(health, this.maxHealth);
        livingEntity.setHealth(this.health);
    }

    public void syncPluginHealth(double health) {
        this.health = health;
    }

    public void heal(double healAmount){
        EliteMobHealEvent eliteMobHealEvent = new EliteMobHealEvent(this, healAmount);
        new EventCaller(eliteMobHealEvent);
        if (eliteMobHealEvent.isCancelled()) return;
        setHealth(health + healAmount);
    }

    public double damage(double damage) {
        health = Math.max(0, livingEntity.getHealth() - damage);
        livingEntity.setHealth(health);
        return damage;
    }

    public void fullHeal() {
        EliteMobHealEvent eliteMobHealEvent = new EliteMobHealEvent(this, true);
        new EventCaller(eliteMobHealEvent);
        if (eliteMobHealEvent.isCancelled()) return;
        setHealth(this.maxHealth);
        this.health = maxHealth;
        damagers.clear();
    }

    public void combatEnd() {
    }

    private void setArmor() {
        if (!MobCombatSettingsConfig.doEliteArmor) return;

        if (!(livingEntity instanceof Zombie || livingEntity instanceof PigZombie ||
                livingEntity instanceof Skeleton || livingEntity instanceof WitherSkeleton)) return;

        livingEntity.getEquipment().setBoots(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (level >= 5)
            if (MobCombatSettingsConfig.doEliteHelmets)
                livingEntity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (level >= 10) livingEntity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        if (level >= 15) livingEntity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        if (level >= 20) livingEntity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        if (level >= 25) if (MobCombatSettingsConfig.doEliteHelmets)
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        if (level >= 30) livingEntity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        if (level >= 35) livingEntity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        if (level >= 40) livingEntity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        if (level >= 45) if (MobCombatSettingsConfig.doEliteHelmets)
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        if (level >= 50) livingEntity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
        if (level >= 55) livingEntity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        if (level >= 60) livingEntity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        if (level >= 65) livingEntity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        if (level >= 70) if (MobCombatSettingsConfig.doEliteHelmets)
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        if (level >= 75) livingEntity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        if (level >= 80) livingEntity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

        if (livingEntity.getEquipment().getHelmet() != null) {
            ItemMeta helmetMeta = livingEntity.getEquipment().getHelmet().getItemMeta();
            if (helmetMeta != null) {
                helmetMeta.setUnbreakable(true);
                livingEntity.getEquipment().getHelmet().setItemMeta(helmetMeta);
            }
        }
    }

    public void randomizePowers(EliteMobProperties eliteMobProperties) {

        if (level < 1) return;

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (level >= 10) availableDefensivePowers = 1;
        if (level >= 20) availableOffensivePowers = 1;
        if (level >= 30) availableMiscellaneousPowers = 1;
        if (level >= 40) availableMajorPowers = 1;
        if (level >= 50) availableDefensivePowers = 2;
        if (level >= 60) availableOffensivePowers = 2;
        if (level >= 70) availableMiscellaneousPowers = 2;
        if (level >= 80) availableMajorPowers = 2;

        //apply defensive powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidDefensivePowers().clone(), availableDefensivePowers);

        //apply offensive powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidOffensivePowers().clone(), availableOffensivePowers);

        //apply miscellaneous powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidMiscellaneousPowers().clone(), availableMiscellaneousPowers);

        //apply major powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidMajorPowers().clone(), availableMajorPowers);

        new MinorPowerPowerStance(this);
        new MajorPowerPowerStance(this);

    }

    public void applyPowers(HashSet<ElitePower> elitePowers, int availablePowerAmount) {
        for (Iterator<ElitePower> elitePowerIterator = elitePowers.iterator(); elitePowerIterator.hasNext(); ) {
            ElitePower elitePower = elitePowerIterator.next();
            if (!PowersConfig.getPower(elitePower.getFileName()).isEnabled())
                elitePowerIterator.remove();
        }

        if (availablePowerAmount < 1) return;

        ArrayList<ElitePower> localPowers = new ArrayList<>(elitePowers);

        for (ElitePower mobPower : this.elitePowers)
            localPowers.remove(mobPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localPowers.size() < 1)
                break;
            else {
                ElitePower selectedPower = localPowers.get(ThreadLocalRandom.current().nextInt(localPowers.size()));
                try {
                    this.elitePowers.add(selectedPower.getClass().newInstance());
                    selectedPower.applyPowers(this.livingEntity);
                    localPowers.remove(selectedPower);
                    if (selectedPower instanceof MajorPower)
                        this.majorPowerCount++;
                    if (selectedPower instanceof MinorPower)
                        this.minorPowerCount++;
                } catch (Exception ex) {
                    new WarningMessage("Failed to instance new power!");
                }
            }

    }

    public void applyPowers(HashSet<ElitePower> elitePowers) {
        this.elitePowers = elitePowers;
        for (ElitePower elitePower : elitePowers)
            elitePower.applyPowers(livingEntity);
    }

    public boolean hasPower(ElitePower mobPower) {
        for (ElitePower elitePower : elitePowers)
            if (elitePower.getClass().equals(mobPower.getClass()))
                return true;
        return false;
    }

    public boolean hasPower(PowersConfigFields powersConfigFields) {
        for (ElitePower elitePower : elitePowers)
            if (elitePower.getPowersConfigFields().equals(powersConfigFields))
                return true;
        return false;
    }

    public ElitePower getPower(PowersConfigFields powersConfigFields) {
        for (ElitePower elitePower : elitePowers)
            if (elitePower.getPowersConfigFields().equals(powersConfigFields))
                return elitePower;
        return null;
    }

    public ElitePower getPower(ElitePower elitePower) {
        for (ElitePower iteratedPower : getElitePowers())
            if (iteratedPower.getClass().equals(elitePower.getClass()))
                return iteratedPower;
        return null;
    }

    public ElitePower getPower(String elitePower) {
        for (ElitePower iteratedPower : getElitePowers())
            if (iteratedPower.getFileName().equals(elitePower))
                return iteratedPower;
        return null;
    }

    private void setMaxHealth(double healthMultiplier) {
        this.defaultMaxHealth = EliteMobProperties.getPluginData(this.getLivingEntity().getType()).getDefaultMaxHealth();
        this.maxHealth = (level * CombatSystem.TARGET_HITS_TO_KILL + this.defaultMaxHealth) * healthMultiplier;
        //7 is the base damage of a diamond sword
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        livingEntity.setHealth(maxHealth);
        this.health = maxHealth;
    }

    public void setName(EliteMobProperties eliteMobProperties) {
        this.name = ChatColorConverter.convert(
                eliteMobProperties.getName().replace(
                        "$level", level + ""));
        livingEntity.setCustomName(this.name);
        livingEntity.setCustomNameVisible(DefaultConfig.alwaysShowNametags);
    }

    public void setName(String name, boolean applyToLivingEntity) {
        this.name = name;
        //This is necessary for the custom boss mega consumer
        if (applyToLivingEntity)
            livingEntity.setCustomName(name);
    }

    public void setPersistent(boolean bool) {
        this.isPersistent = bool;
        if (livingEntity != null)
            livingEntity.setRemoveWhenFarAway(!isPersistent);
    }

    public void setHasSpecialLoot(boolean bool) {
        this.isNaturalEntity = bool;
        this.eliteLoot = bool;
        this.vanillaLoot = bool;
    }

    public void doCooldown() {
        setInCooldown(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setInCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 15);
    }

    public void doGlobalPowerCooldown(int ticks) {
        setInCooldown(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setInCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    public void setTriggeredAntiExploit(boolean triggeredAntiExploit) {
        this.triggeredAntiExploit = triggeredAntiExploit;
        if (triggeredAntiExploit) {
            this.eliteLoot = false;
            this.vanillaLoot = false;
        }
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

    public void setInAntiExploitCooldown() {
        this.inAntiExploitCooldown = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                inAntiExploitCooldown = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20);
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

    public void addGlobalReinforcement(CustomBossEntity customBossEntity) {
        this.globalReinforcementEntities.add(customBossEntity);
    }

    public void addReinforcement(CustomBossEntity customBossEntity) {
        this.eliteReinforcementEntities.add(customBossEntity);
    }

    public void addReinforcement(Entity entity) {
        this.nonEliteReinforcementEntities.add(entity);
    }

    /**
     * Returns true if the {@link LivingEntity} is exists and loaded.
     *
     * @return Whether the EliteEntity is currently valid.
     */
    public boolean isValid() {
        if (livingEntity == null) return false;
        return livingEntity.isValid();
    }

    public boolean exists() {
        if (livingEntity == null) return false;
        return !livingEntity.isDead();
    }

    public Location getLocation(){
        if (livingEntity != null) return livingEntity.getLocation();
        return null;
    }

    public void  remove(RemovalReason removalReason) {
        //This prevents the entity tracker from running this code twice when removing due to specific reasons
        if (livingEntity != null){
            EliteEntityTracker.eliteMobEntities.remove(livingEntity.getUniqueId());
            EliteEntityTracker.trackedEntities.remove(livingEntity.getUniqueId());
        }
        if (livingEntity != null && !removalReason.equals(RemovalReason.DEATH))
            livingEntity.remove();
        if (livingEntity instanceof EnderDragon && removalReason.equals(RemovalReason.DEATH)) {
            ((EnderDragon) livingEntity).setPhase(EnderDragon.Phase.DYING);
            ((EnderDragon) livingEntity).getDragonBattle().generateEndPortal(false);
        }
        this.livingEntity = null;
    }

    public void removeReinforcement(CustomBossEntity customBossEntity) {
        eliteReinforcementEntities.remove(customBossEntity);
    }

    public int getGlobalReinforcementsCount() {
        return this.globalReinforcementEntities.size();
    }

    /**
     * This method is implemented by the {@link SimplePersistentEntityInterface} to conform to the pattern but is only ever used in overrides by extended classes.
     */
    @Override
    public void chunkLoad() {
        remove(RemovalReason.CHUNK_UNLOAD);
    }

    /**
     * Chunk unloading always removes Elite Entities as if they are Custom Bosses they have their own handler with an override.
     */
    @Override
    public void chunkUnload() {
        remove(RemovalReason.CHUNK_UNLOAD);
    }

    @Override
    public void worldLoad() {
    }

    @Override
    public void worldUnload() {
        remove(RemovalReason.WORLD_UNLOAD);
    }
}
