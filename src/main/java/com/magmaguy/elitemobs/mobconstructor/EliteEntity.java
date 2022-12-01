package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.collateralminecraftchanges.KeepNeutralsAngry;
import com.magmaguy.elitemobs.combatsystem.CombatSystem;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AntiExploitMessage;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
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

public class EliteEntity {

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
    @Setter
    protected boolean eliteLoot = true;
    @Getter
    @Setter
    protected boolean randomLoot = true;
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
    protected Double health = null;
    @Getter
    @Setter
    private boolean dying = false;
    @Getter
    @Setter
    private boolean healing = false;
    @Getter
    @Setter
    protected Location spawnLocation;
    //Used for custom arbitrary tags from elite scripts
    private HashSet<String> customMetadata = new HashSet<>();

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
     * Noteworthy uses: {@link EliteEntity} will survive chunk reloads, and in the case of {@link RegionalBossEntity} will survive deaths for the entirety of runtime.
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
                    this.aggro.put(iteratedPlayer, this.aggro.get(iteratedPlayer) + damage *
                            ElitePlayerInventory.getPlayer(player).getLoudStrikesBonusMultiplier(false));
                    return;
                }
        this.damagers.put(player, damage);
        this.aggro.put(player, damage *
                ElitePlayerInventory.getPlayer(player).getLoudStrikesBonusMultiplier(false));
    }

    public boolean hasDamagers() {
        return !damagers.isEmpty();
    }

    public Map<Player, Double> getDamagers() {
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
        if (livingEntity == null) return;
        if (!(this instanceof CustomBossEntity))
            this.spawnLocation = livingEntity.getLocation().clone();
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

        if (livingEntity.getType().equals(EntityType.RABBIT)) {
            ((Rabbit) livingEntity).setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
        }

        if (entityType.equals(EntityType.WOLF)) {
            Wolf wolf = (Wolf) livingEntity;
            wolf.setAngry(true);
            wolf.setBreed(false);
            KeepNeutralsAngry.showMeYouWarFace(this);
        }

        if (entityType.equals(EntityType.POLAR_BEAR)) {
            KeepNeutralsAngry.showMeYouWarFace(this);
        }

        if (entityType.equals(EntityType.ENDER_DRAGON))
            if (((EnderDragon) livingEntity).getBossBar() != null)
                ((EnderDragon) livingEntity).getBossBar().setTitle(getName());

        if (entityType.equals(EntityType.LLAMA)) {
            KeepNeutralsAngry.showMeYouWarFace(this);
        }

        //todo: this should become configurable real soon for the primis gladius event
        if (entityType.equals(EntityType.IRON_GOLEM) && this instanceof CustomBossEntity)
            KeepNeutralsAngry.showMeYouWarFace(this);

        if (!VersionChecker.serverVersionOlderThan(17, 0) && entityType.equals(EntityType.GOAT)) {
            ((Goat) livingEntity).setScreaming(true);
        }

        if (!VersionChecker.serverVersionOlderThan(15, 0) && livingEntity instanceof Bee) {
            KeepNeutralsAngry.showMeYouWarFace(this);
            ((Bee) livingEntity).setCannotEnterHiveTicks(Integer.MAX_VALUE);
        }
        this.spawnReason = spawnReason;

        //This sets whether the entity gets despawned when beyond a certain distance from the player, should only happen
        //for entities which aren't pseudo-persistent
        this.getLivingEntity().setRemoveWhenFarAway(!isPersistent);
        this.getLivingEntity().setPersistent(false);

        setMaxHealth();

        if (getName() == null)
            setName(EliteMobProperties.getPluginData(entityType));

        this.name = livingEntity.getCustomName();

        PersistentTagger.tagElite(livingEntity, eliteUUID);
        EntityTracker.registerEliteMob(this);
    }

    public void setNameVisible(boolean isVisible) {
        //Check if the boss is already dead
        if (livingEntity == null) return;
        livingEntity.setCustomNameVisible(isVisible);
    }

    public void setMaxHealth() {
        if (EliteMobProperties.getPluginData(entityType) != null)
            this.defaultMaxHealth = EliteMobProperties.getPluginData(entityType).getDefaultMaxHealth();
        else this.defaultMaxHealth = 20;
        this.maxHealth = (level * CombatSystem.TARGET_HITS_TO_KILL_MINUS_ONE + this.defaultMaxHealth) * healthMultiplier;
        if (livingEntity != null) livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        if (health == null) {
            if (livingEntity != null) livingEntity.setHealth(maxHealth);
            this.health = maxHealth;
        }
        //This is useful for phase boss entities that spawn in unloaded chunks and shouldn't full heal between phases, like in dungeons
        else if (livingEntity != null)
            livingEntity.setHealth(Math.min(health, livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    public void setNormalizedMaxHealth() {
        this.defaultMaxHealth = MobCombatSettingsConfig.getNormalizedBaselineHealth();
        this.maxHealth = (level * CombatSystem.TARGET_HITS_TO_KILL_MINUS_ONE + this.defaultMaxHealth) * healthMultiplier;
        if (livingEntity != null) {
            livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            livingEntity.setHealth(maxHealth);
        }
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
        else if (this.health != null)
            return this.health;
        else {
            setMaxHealth();
            return this.health;
        }
    }

    public void setHealth(double health) {
        if (livingEntity == null) return;
        this.health = Math.min(health, this.maxHealth);
        livingEntity.setHealth(this.health);
    }

    public void syncPluginHealth(double health) {
        this.health = health;
    }

    public void heal(double healAmount) {
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

    private void setArmor() {
        if (!MobCombatSettingsConfig.isDoEliteArmor()) return;

        if (!(livingEntity instanceof Zombie || livingEntity instanceof PigZombie ||
                livingEntity instanceof Skeleton || livingEntity instanceof WitherSkeleton)) return;

        livingEntity.getEquipment().setBoots(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
        livingEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (level >= 5 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (level >= 10) livingEntity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        if (level >= 15) livingEntity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        if (level >= 20) livingEntity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        if (level >= 25 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
        if (level >= 30) livingEntity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
        if (level >= 35) livingEntity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
        if (level >= 40) livingEntity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        if (level >= 45 && MobCombatSettingsConfig.isDoEliteHelmets())
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        if (level >= 50) livingEntity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
        if (level >= 55) livingEntity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        if (level >= 60) livingEntity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        if (level >= 65) livingEntity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        if (level >= 70 && MobCombatSettingsConfig.isDoEliteHelmets())
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
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidDefensivePowers().clone(), availableDefensivePowers);

        //apply offensive powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidOffensivePowers().clone(), availableOffensivePowers);

        //apply miscellaneous powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidMiscellaneousPowers().clone(), availableMiscellaneousPowers);

        //apply major powers
        applyPowers((HashSet<PowersConfigFields>) eliteMobProperties.getValidMajorPowers().clone(), availableMajorPowers);

        new MinorPowerPowerStance(this);
        new MajorPowerPowerStance(this);

    }

    public void applyPowers(HashSet<PowersConfigFields> configFields, int availablePowerAmount) {
        configFields.removeIf(iteratedField -> !iteratedField.isEnabled());

        if (availablePowerAmount < 1) return;

        ArrayList<PowersConfigFields> localFields = new ArrayList<>(configFields);

        for (ElitePower mobPower : this.elitePowers)
            localFields.remove(mobPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localFields.size() < 1)
                break;
            else {
                PowersConfigFields selectedField = localFields.get(ThreadLocalRandom.current().nextInt(localFields.size()));
                try {
                    ElitePower.addPower(this, selectedField);
                    localFields.remove(selectedField);
                    if (selectedField.getPowerType().equals(PowersConfigFields.PowerType.MAJOR_ZOMBIE) ||
                            selectedField.getPowerType().equals(PowersConfigFields.PowerType.MAJOR_BLAZE) ||
                            selectedField.getPowerType().equals(PowersConfigFields.PowerType.MAJOR_ENDERMAN) ||
                            selectedField.getPowerType().equals(PowersConfigFields.PowerType.MAJOR_GHAST) ||
                            selectedField.getPowerType().equals(PowersConfigFields.PowerType.MAJOR_SKELETON))
                        this.majorPowerCount++;
                    else
                        this.minorPowerCount++;
                } catch (Exception ex) {
                    new WarningMessage("Failed to instance new power!");
                }
            }

    }

    public void applyPowers(HashSet<PowersConfigFields> powersConfigFields) {
        powersConfigFields.forEach(field -> ElitePower.addPower(this, field));
    }

    public boolean hasPower(ElitePower mobPower) {
        return elitePowers.contains(mobPower);
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

    public void setName(EliteMobProperties eliteMobProperties) {
        this.name = ChatColorConverter.convert(
                eliteMobProperties.getName().replace(
                        "$level", level + ""));
        livingEntity.setCustomName(this.name);
        livingEntity.setCustomNameVisible(DefaultConfig.isAlwaysShowNametags());
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
        if (antiExploitPoints > AntiExploitConfig.getAntiExploitThreshold()) {
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

    public Location getLocation() {
        if (livingEntity != null) return livingEntity.getLocation();
        if (unsyncedLivingEntity != null) return unsyncedLivingEntity.getLocation();
        return null;
    }

    public void remove(RemovalReason removalReason) {
        //This prevents the entity tracker from running this code twice when removing due to specific reasons
        //Custom bosses have their own tracking removal rules
        if (livingEntity != null && (!(this instanceof CustomBossEntity)))
            EntityTracker.getEliteMobEntities().remove(eliteUUID);
        if (livingEntity != null && !removalReason.equals(RemovalReason.DEATH))
            livingEntity.remove();
        if (livingEntity instanceof EnderDragon && removalReason.equals(RemovalReason.DEATH)) {
            ((EnderDragon) livingEntity).setPhase(EnderDragon.Phase.DYING);
            Objects.requireNonNull(((EnderDragon) livingEntity).getDragonBattle()).generateEndPortal(false);
        }
        this.livingEntity = null;
    }

    public void removeReinforcement(CustomBossEntity customBossEntity) {
        eliteReinforcementEntities.remove(customBossEntity);
        globalReinforcementEntities.remove(customBossEntity);
    }

    public int getGlobalReinforcementsCount() {
        return this.globalReinforcementEntities.size();
    }

    public HashSet<String> getTags() {
        return customMetadata;
    }

    public boolean hasTag(String tag) {
        return customMetadata.contains(tag);
    }

    public void addTag(String tag) {
        customMetadata.add(tag);
    }

    public void addTags(List<String> tags) {
        customMetadata.addAll(tags);
    }

    public void removeTag(String tag) {
        customMetadata.remove(tag);
    }

    public void removeTags(List<String> tags) {
        customMetadata.removeAll(tags);
    }

}
