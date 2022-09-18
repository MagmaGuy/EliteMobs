package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.mobconstructor.*;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlock;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.modelengine.CustomModel;
import com.magmaguy.elitemobs.utils.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class CustomBossEntity extends EliteEntity implements Listener, PersistentObject, PersistentMovingEntity {

    @Getter
    protected static HashSet<CustomBossEntity> trackableCustomBosses = new HashSet<>();
    private final List<BukkitTask> globalReinforcements = new ArrayList<>();
    @Getter
    private final BossTrace bossTrace = new BossTrace();
    @Getter
    protected CustomBossesConfigFields customBossesConfigFields;
    protected CustomBossEntity customBossMount = null;
    protected LivingEntity livingEntityMount = null;
    protected CustomBossEntity mount;
    protected PersistentObjectHandler persistentObjectHandler = null;
    @Setter
    protected Location persistentLocation;
    @Getter
    @Setter
    protected Location spawnLocation;
    protected CustomBossTrail customBossTrail;
    @Getter
    protected CustomBossBossBar customBossBossBar;
    protected Integer escapeMechanism;
    @Getter
    protected PhaseBossEntity phaseBossEntity = null;
    protected String worldName;
    private long lastTick = 0;
    private int attemptsCounter = 1;
    //For use by the phase switcher, which requires passing a specific spawn location for the phase, but when it's for a
    //regional boss this causes issues such as reinforcements getting shifted over to the new spawn location
    @Getter
    @Setter
    private Location respawnOverrideLocation;
    @Getter
    @Setter
    private EMPackage emPackage = null;
    @Setter
    private CustomSpawn customSpawn = null;
    private int existsFailureCount = 0;
    @Getter
    @Setter
    private boolean isMount = false;
    @Getter
    @Setter
    private CustomModel customModel = null;
    @Getter
    private boolean normalizedCombat;
    @Getter
    @Setter
    private BossMusic bossMusic = null;

    /**
     * Uses a builder pattern in order to construct a CustomBossEntity at an arbitrary point in the future. Does not
     * spawn an entity until the spawn method is invoked. The customBossesConfigFields value isn't final.
     * <p>
     * The {@link CustomBossEntity#setSpawnLocation(Location)} can be assigned at an arbitrary point in the future before the spawn is invoked.
     * Uses:
     * - {@link CustomEvent} queueing through the {@link CustomSpawn} system
     */
    public CustomBossEntity(CustomBossesConfigFields customBossesConfigFields) {
        //This creates a placeholder empty EliteMobEntity to be filled in later
        super();
        if (customBossesConfigFields.getSong() != null) bossMusic = new BossMusic(customBossesConfigFields.getSong());
        //This stores everything that will need to be initialized for the EliteMobEntity
        setCustomBossesConfigFields(customBossesConfigFields);
        super.setPersistent(customBossesConfigFields.isPersistent());
        //Phases are final
        if (customBossesConfigFields.getPhases() != null)
            this.phaseBossEntity = new PhaseBossEntity(this);
        if (this instanceof RegionalBossEntity)
            super.bypassesProtections = true;
        this.emPackage = EMPackage.getContent(customBossesConfigFields.getFilename());
    }

    @Nullable
    public static CustomBossEntity createCustomBossEntity(String filename) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null)
            return null;
        return new CustomBossEntity(customBossesConfigFields);
    }

    @Override
    public void setSummoningEntity(EliteEntity summoningEntity) {
        this.summoningEntity = summoningEntity;
        if (summoningEntity instanceof CustomBossEntity)
            this.emPackage = ((CustomBossEntity) summoningEntity).emPackage;
    }

    /**
     * The customBossesConfigFields is not final, and changes if the boss has phases, loading the configuration from the phases as the fight goes along.
     *
     * @param customBossesConfigFields Configuration file to be used as the basis of the values of the boss.
     */
    public void setCustomBossesConfigFields(CustomBossesConfigFields customBossesConfigFields) {
        this.customBossesConfigFields = customBossesConfigFields;
        normalizedCombat = customBossesConfigFields.isNormalizedCombat();
        super.setDamageMultiplier(customBossesConfigFields.getDamageMultiplier());
        super.setHealthMultiplier(customBossesConfigFields.getHealthMultiplier());
        super.setEliteLoot(customBossesConfigFields.isDropsEliteMobsLoot());
        super.setRandomLoot(customBossesConfigFields.isDropsRandomLoot());
        super.setVanillaLoot(customBossesConfigFields.isDropsVanillaLoot());
        super.setLevel(customBossesConfigFields.getLevel());
        setPluginName();
        super.elitePowers = ElitePowerParser.parsePowers(customBossesConfigFields);
        if (this instanceof RegionalBossEntity) {
            ((RegionalBossEntity) this).setOnSpawnTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnSpawnBlockStates(), customBossesConfigFields.getFilename()));
            ((RegionalBossEntity) this).setOnRemoveTransitiveBlocks(TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnRemoveBlockStates(), customBossesConfigFields.getFilename()));
        }
    }

    public void spawn(Location spawnLocation, int level, boolean silent) {
        bossTrace.spawnPreprocessor(1);
        super.level = level;
        this.spawnLocation = spawnLocation;
        spawn(spawnLocation, silent);
    }

    public void spawn(Location spawnLocation, boolean silent) {
        bossTrace.spawnPreprocessor(2);
        if (spawnLocation != null && spawnLocation.getWorld() != null && lastTick == spawnLocation.getWorld().getFullTime())
            attemptsCounter++;
        else
            attemptsCounter = 1;
        if (spawnLocation != null && spawnLocation.getWorld() != null)
            lastTick = spawnLocation.getWorld().getFullTime();
        //Use exists() here as the persistent entities will not try to respawn through this method
        if (exists()) {
            new WarningMessage("Warning: " + customBossesConfigFields.getFilename() + " attempted to double spawn " + attemptsCounter + " times!", true);
            return;
        }
        this.spawnLocation = spawnLocation;
        this.persistentLocation = spawnLocation;
        spawn(silent);
    }

    public void spawn(boolean silent) {
        bossTrace.setSpawn();
        if (livingEntity != null && livingEntity.isValid())
            return;

        if (isPersistent && persistentObjectHandler == null)
            persistentObjectHandler = new PersistentObjectHandler(this);
        else if (isPersistent) persistentObjectHandler.updatePersistentLocation(spawnLocation);

        if (spawnLocation == null) {
            new WarningMessage("Boss " + customBossesConfigFields.getFilename() + " has a null location! This is probably due to an incorrectly configured regional location!");
            return;
        }

        if (level == -1 && this instanceof RegionalBossEntity)
            new WarningMessage("Boss " + getCustomBossesConfigFields().getFilename() + " is regional and has a dynamic level!" +
                    " This is a terrible idea. Change this as soon as possible!");

        //This is a bit dumb but -1 is reserved for dynamic levels
        if (level == -1)
            if (!(this instanceof RegionalBossEntity) || spawnLocation.getWorld() != null)
                getDynamicLevel(spawnLocation);
            else
                level = 1;

        if (ChunkLocationChecker.locationIsLoaded(spawnLocation) || isMount) {
            super.livingEntity = new CustomBossMegaConsumer(this).spawn();
            setNormalizedHealth();
            if (super.livingEntity == null)
                new WarningMessage("Something just prevented EliteMobs from spawning a Custom Boss! More info up next.");
        }
        if (!exists()) {
            existsFailureCount++;
            //this may seem odd but not setting it to null can cause double spawn attempts as the plugin catches itself
            //correctly as not have a valid living entity but the checks are set up in such a way that if a living entity
            //object is referenced then trying to spawn it again is a double spawn of the same entity
            super.livingEntity = null;
            if (existsFailureCount > 10) {
                if (existsFailureCount == 11) {
                    new WarningMessage("EliteMobs tried and failed to spawn " + customBossesConfigFields.getFilename() + " " + existsFailureCount + "times, probably due to regional protections or third party plugin incompatibilities.");
                    new WarningMessage("To avoid cluttering up console, these warnings will now only appear once 6000 attempts for this boss. ");
                }
                if (existsFailureCount % 6000 == 0) {
                    new WarningMessage("EliteMobs tried and failed to spawn " + customBossesConfigFields.getFilename() + " " + existsFailureCount + "times, probably due to regional protections or third party plugin incompatibilities.");
                    new WarningMessage("To avoid cluttering up console, these warnings will now only appear once 6000 attempts for this boss. ");
                }
                return;
            }
            if (existsFailureCount > 1) {
                new WarningMessage("EliteMobs tried and failed to spawn " + customBossesConfigFields.getFilename() + " " + existsFailureCount + "times, probably due to regional protections.");
                return;
            }
            new WarningMessage("EliteMobs tried and failed to spawn " + customBossesConfigFields.getFilename() + " . Possible reasons for this:");
            new WarningMessage("- The region was protected by a plugin (most likely)");
            new WarningMessage("- The spawn was interfered with by some incompatible third party plugin");
            new WarningMessage("Debug data: ");
            new WarningMessage("Chunk is loaded: " + ChunkLocationChecker.locationIsLoaded(spawnLocation));
            new WarningMessage("Attempted spawn location: " + spawnLocation.toString(), true);
            return;
        }

        //It isn't worth initializing things that will notify players or spawn additional entities until we are certain that the boss has actually spawned
        if (livingEntity != null) {
            if (bossMusic != null)
                bossMusic.start(this);
            startBossTrails();
            mountEntity();
            //Prevent custom bosses from getting removed when far away, this is important for mounts and reinforcements in large arenas
            getLivingEntity().setRemoveWhenFarAway(false);
        } else
            persistentLocation = spawnLocation;

        if (!silent)
            announceSpawn();

        if (summoningEntity != null)
            summoningEntity.addReinforcement(this);

        if (spawnLocation.getWorld() != null)
            for (ElitePower elitePower : elitePowers)
                if (elitePower instanceof CustomSummonPower)
                    ((CustomSummonPower) elitePower).getCustomBossReinforcements().forEach((customBossReinforcement -> {
                        if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.GLOBAL))
                            globalReinforcements.add(CustomSummonPower.summonGlobalReinforcement(customBossReinforcement, this));
                    }));

        CommandRunner.runCommandFromList(customBossesConfigFields.getOnSpawnCommands(), new ArrayList<>());
    }

    private void setNormalizedHealth() {
        if (normalizedCombat)
            super.setNormalizedMaxHealth();
    }

    public void setNormalizedCombat() {
        normalizedCombat = true;
        if (livingEntity != null)
            setNormalizedMaxHealth();
    }

    private void setPluginName() {
        if (customBossesConfigFields.getName() == null) return;
        if (this.level == -1)
            setName(ChatColorConverter.convert(customBossesConfigFields.getName().replace("$level", "?" + "")
                            .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + "?" + "&2]&f"))
                            .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + "?" + "&6〗&f"))
                            .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + "?" + "&4』&f"))
                            .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + "?" + "&8〕&f")
                            .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + "?" + "&4」&f"))),
                    false);
        else
            setName(ChatColorConverter.convert(customBossesConfigFields.getName().replace("$level", this.level + "")
                            .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + this.level + "&2]&f"))
                            .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + this.level + "&6〗&f"))
                            .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + this.level + "&4』&f"))
                            .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + this.level + "&8〕&f")
                            .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + this.level + "&4」&f"))),
                    false);
    }

    @Override
    public void setName(String name, boolean applyToLivingEntity) {
        super.setName(name, applyToLivingEntity);
        if (isValid() && customModel != null)
            customModel.setName(name, true);
    }

    @Override
    public void setName(EliteMobProperties eliteMobProperties) {
        super.setName(eliteMobProperties);
        if (isValid() && customModel != null)
            customModel.setName(name, DefaultConfig.isAlwaysShowNametags());
    }

    @Override
    public void setNameVisible(boolean isVisible) {
        //Check if the boss is already dead
        if (livingEntity == null) return;
        super.setNameVisible(isVisible);
        DisguiseEntity.setDisguiseNameVisibility(isVisible, livingEntity, name);
        if (customModel != null && isValid())
            customModel.setNameVisible(isVisible);
    }

    public void announceSpawn() {
        setTracking();
        spawnMessage();
        startEscapeMechanismDelay();
    }

    private void setTracking() {
        if (customBossesConfigFields.getAnnouncementPriority() < 1 ||
                !MobCombatSettingsConfig.isShowCustomBossLocation() ||
                customBossesConfigFields.getLocationMessage() == null)
            return;
        trackableCustomBosses.add(this);
        customBossBossBar = new CustomBossBossBar(this);
    }

    private void spawnMessage() {
        if (customBossesConfigFields.getSpawnMessage() == null) return;
        if (customBossesConfigFields.getAnnouncementPriority() < 1) return;
        if (getLocation() == null) return;
        if (getLocation().getWorld() == null) return;
        if (!EventsConfig.isAnnouncementBroadcastWorldOnly())
            Bukkit.broadcastMessage(ChatColorConverter.convert(customBossesConfigFields.getSpawnMessage()));
        else
            for (Player player : getLocation().getWorld().getPlayers())
                player.sendMessage(ChatColorConverter.convert(customBossesConfigFields.getSpawnMessage()));
        if (customBossesConfigFields.getAnnouncementPriority() < 3) return;
        new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossesConfigFields.getSpawnMessage()));
    }

    public void getDynamicLevel(Location bossLocation) {
        int bossLevel = 1;
        for (Entity entity : bossLocation.getWorld().getNearbyEntities(bossLocation, Math.max(Bukkit.getViewDistance() * 16, 5 * 16), 256, Bukkit.getViewDistance() * 16D))
            if (entity instanceof Player)
                if (ElitePlayerInventory.playerInventories.get(entity.getUniqueId()) != null)
                    if (ElitePlayerInventory.playerInventories.get(entity.getUniqueId()).getNaturalMobSpawnLevel(true) > bossLevel)
                        bossLevel = ElitePlayerInventory.playerInventories.get(entity.getUniqueId()).getNaturalMobSpawnLevel(false);
        super.setLevel(bossLevel);
    }

    private void startBossTrails() {
        customBossTrail = new CustomBossTrail(this);
    }

    private void mountEntity() {
        mount = CustomBossMount.generateMount(this);
    }

    private void startEscapeMechanismDelay() {
        escapeMechanism = CustomBossEscapeMechanism.startEscape(customBossesConfigFields.getTimeout(), this);
    }

    private void cullReinforcements(boolean cullGlobal) {
        if (cullGlobal)
            globalReinforcementEntities.forEach(customBossEntity -> customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL));
        for (CustomBossEntity customBossEntity : eliteReinforcementEntities)
            if (customBossEntity != null)
                customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL);
        for (Entity entity : nonEliteReinforcementEntities)
            if (entity != null)
                entity.remove();

        if (cullGlobal)
            globalReinforcements.clear();
        eliteReinforcementEntities.clear();
        nonEliteReinforcementEntities.clear();
    }

    @Override
    public Location getLocation() {
        if (getLivingEntity() != null) return getLivingEntity().getLocation();
        else return persistentLocation;
    }

    @Override
    public Location getPersistentLocation() {
        if (persistentLocation == null && getLocation() != null) persistentLocation = getLocation();
        if (persistentLocation == null && spawnLocation != null) persistentLocation = spawnLocation;
        return persistentLocation;
    }

    public String getWorldName() {
        return worldName;
    }


    public double getDamageModifier(Material material) {
        return customBossesConfigFields.getDamageModifier(material);
    }

    public void resetLivingEntity(LivingEntity livingEntity, CreatureSpawnEvent.SpawnReason spawnReason) {
        super.setLivingEntity(livingEntity, spawnReason);
        new CustomBossMegaConsumer(this).applyBossFeatures(livingEntity);
    }

    @Override
    public boolean exists() {
        return super.exists() || persistentObjectHandler != null;
    }

    @Override
    public void fullHeal() {
        if (customBossesConfigFields.isInstanced()) return;
        if (phaseBossEntity == null || phaseBossEntity.isInFirstPhase()) {
            super.fullHeal();
            return;
        }
        phaseBossEntity.resetToFirstPhase();
        super.damagers.clear();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        bossTrace.setRemove(removalReason);
        if (DebugMessage.isDebugMode())
            if (this instanceof RegionalBossEntity && this.phaseBossEntity != null)
                new DebugMessage("Regional + Phase boss removal. Reason: " + removalReason);
        if (livingEntity != null) persistentLocation = livingEntity.getLocation();
        //Remove the living entity
        super.remove(removalReason);
        //Remove things tied to the living entity
        if (customBossTrail != null) customBossTrail.terminateTrails();
        if (livingEntityMount != null) livingEntityMount.remove();
        if (customBossMount != null) customBossMount.remove(RemovalReason.REINFORCEMENT_CULL);
        if (customBossesConfigFields.isCullReinforcements() || removalReason.equals(RemovalReason.PHASE_BOSS_RESET))
            cullReinforcements(false);

        if (removalReason.equals(RemovalReason.PHASE_BOSS_PHASE_END))
            if (inCombat)
                new EventCaller(new EliteMobExitCombatEvent(this, EliteMobExitCombatEvent.EliteMobExitCombatReason.PHASE_SWITCH));

        boolean bossInstanceEnd = removalReason.equals(RemovalReason.KILL_COMMAND) ||
                removalReason.equals(RemovalReason.DEATH) ||
                removalReason.equals(RemovalReason.BOSS_TIMEOUT) ||
                removalReason.equals(RemovalReason.SHUTDOWN) ||
                removalReason.equals(RemovalReason.ARENA_RESET) ||
                removalReason.equals(RemovalReason.REMOVE_COMMAND);

        if (!isPersistent) bossInstanceEnd = true;

        if (bossInstanceEnd) {
            if (!(this instanceof RegionalBossEntity))
                EntityTracker.getEliteMobEntities().remove(super.eliteUUID);
            new EventCaller(new EliteMobRemoveEvent(this, removalReason));
            if (escapeMechanism != null) Bukkit.getScheduler().cancelTask(escapeMechanism);
            trackableCustomBosses.remove(this);
            if (persistentObjectHandler != null) {
                persistentObjectHandler.remove();
                persistentObjectHandler = null;
            }
            if (customBossBossBar != null)
                customBossBossBar.remove();
            if (!removalReason.equals(RemovalReason.SHUTDOWN) &&
                    !removalReason.equals(RemovalReason.DEATH) &&
                    !removalReason.equals(RemovalReason.ARENA_RESET))
                if (phaseBossEntity != null)
                    phaseBossEntity.silentReset();
            globalReinforcements.forEach((bukkitTask -> {
                if (bukkitTask != null)
                    bukkitTask.cancel();
            }));
            globalReinforcements.clear();
            if (!removalReason.equals(RemovalReason.REINFORCEMENT_CULL)) {
                if (summoningEntity != null)
                    summoningEntity.removeReinforcement(this);
                if (customSpawn != null)
                    customSpawn.setKeepTrying(false);
            }

            if (customBossesConfigFields.isCullReinforcements()) cullReinforcements(true);

        } else if (removalReason.equals(RemovalReason.CHUNK_UNLOAD) || removalReason.equals(RemovalReason.WORLD_UNLOAD))
            //when bosses get removed due to chunk unloads and are persistent they should remain stored
            if (persistentObjectHandler != null)
                persistentObjectHandler.updatePersistentLocation(getPersistentLocation());

        if (!removalReason.equals(RemovalReason.PHASE_BOSS_PHASE_END) && bossMusic != null) bossMusic.stop();
    }

    @Override
    public void chunkLoad() {
        respawnOverrideLocation = persistentLocation;
        spawn(true);
    }

    @Override
    public void chunkUnload() {
        remove(RemovalReason.CHUNK_UNLOAD);
    }

    @Override
    public void worldLoad(World world) {
        if (spawnLocation != null)
            spawnLocation.setWorld(world);
    }

    @Override
    public void worldUnload() {
        remove(RemovalReason.WORLD_UNLOAD);
    }

    public static class CustomBossEntityEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void removeSlowEvent(EliteMobEnterCombatEvent eliteMobEnterCombatEvent) {
            if (!(eliteMobEnterCombatEvent.getEliteMobEntity() instanceof CustomBossEntity)) return;
            if (eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().getPotionEffect(PotionEffectType.SLOW) == null)
                return;
            if (Objects.requireNonNull(eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().getPotionEffect(PotionEffectType.SLOW)).getAmplifier() == 10)
                return;
            eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().removePotionEffect(PotionEffectType.SLOW);
        }

        @EventHandler(ignoreCancelled = true)
        public void onExitCombat(EliteMobExitCombatEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            if (!((CustomBossEntity) event.getEliteMobEntity()).customBossesConfigFields.isCullReinforcements())
                return;
            ((CustomBossEntity) event.getEliteMobEntity()).cullReinforcements(false);
        }
    }

}
