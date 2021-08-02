package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.events.CustomEvent;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntityInterface;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.DeveloperMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;

public class CustomBossEntity extends EliteEntity implements Listener, SimplePersistentEntityInterface {

    protected static HashSet<CustomBossEntity> trackableCustomBosses = new HashSet<>();
    protected CustomBossesConfigFields customBossesConfigFields;
    protected CustomBossEntity customBossMount = null;
    protected LivingEntity livingEntityMount = null;
    protected CustomBossEntity mount;
    protected SimplePersistentEntity simplePersistentEntity;
    protected Location persistentLocation;
    protected Location spawnLocation;
    protected CustomBossTrail customBossTrail;
    protected CustomBossBossBar customBossBossBar;
    protected Integer escapeMechanism;
    protected PhaseBossEntity phaseBossEntity = null;
    protected String worldName;
    protected boolean chunkLoad = false;

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
        //This stores everything that will need to be initialized for the EliteMobEntity
        setCustomBossesConfigFields(customBossesConfigFields);
        super.setPersistent(customBossesConfigFields.isPersistent());
        //Phases are final
        if (!customBossesConfigFields.getPhases().isEmpty())
            this.phaseBossEntity = new PhaseBossEntity(this);
        if (this instanceof RegionalBossEntity)
            super.bypassesProtections = true;
    }

    @Nullable
    public static CustomBossEntity createCustomBossEntity(String filename) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
        if (customBossesConfigFields == null)
            return null;
        return new CustomBossEntity(customBossesConfigFields);
    }

    public static HashSet<CustomBossEntity> getTrackableCustomBosses() {
        return trackableCustomBosses;
    }

    public PhaseBossEntity getPhaseBossEntity() {
        return phaseBossEntity;
    }

    public String getWorldName() {
        return worldName;
    }

    public CustomBossBossBar getCustomBossBossBar() {
        return customBossBossBar;
    }

    public CustomBossesConfigFields getCustomBossesConfigFields() {
        return customBossesConfigFields;
    }

    /**
     * The customBossesConfigFields is not final, and changes if the boss has phases, loading the configuration from the phases as the fight goes along.
     *
     * @param customBossesConfigFields Configuration file to be used as the basis of the values of the boss.
     */
    public void setCustomBossesConfigFields(CustomBossesConfigFields customBossesConfigFields) {
        this.customBossesConfigFields = customBossesConfigFields;
        super.setDamageMultiplier(customBossesConfigFields.getDamageMultiplier());
        super.setHealthMultiplier(customBossesConfigFields.getHealthMultiplier());
        super.setHasSpecialLoot(customBossesConfigFields.getDropsEliteMobsLoot());
        super.setHasVanillaLoot(customBossesConfigFields.getDropsVanillaLoot());
        super.setLevel(customBossesConfigFields.getLevel());
        setPluginName();
        super.elitePowers = ElitePowerParser.parsePowers(customBossesConfigFields.getPowers());
    }

    //spawnLocation is only used for CustomBossEntity spawns queued for later
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public void spawn(Location spawnLocation, int level, boolean silent) {
        super.level = level;
        this.spawnLocation = spawnLocation;
        spawn(spawnLocation, silent);
    }

    public void spawn(Location spawnLocation, boolean silent) {
        this.spawnLocation = spawnLocation;
        spawn(silent);
    }

    public void spawn(boolean silent) {
        //This is a bit dumb but -1 is reserved for dynamic levels
        if (level == -1)
            getDynamicLevel(spawnLocation);

        if (chunkLoad || ChunkLocationChecker.locationIsLoaded(spawnLocation)) {
            chunkLoad = false;
            super.livingEntity = new CustomBossMegaConsumer(this).spawn();
            super.setLivingEntity(livingEntity, spawnReason);
            simplePersistentEntity = null;
        } else
            simplePersistentEntity = new SimplePersistentEntity(this, getLocation());

        if (livingEntity == null && simplePersistentEntity == null) {
            new WarningMessage("EliteMobs tried and failed to spawn " + customBossesConfigFields.getFilename() + " at " + spawnLocation.toString());
            return;
        }

        //It isn't worth initializing things that will notify players or spawn additional entities until we are certain that the boss has actually spawned
        if (livingEntity != null) {
            startBossTrails();
            mountEntity();
            if (this instanceof RegionalBossEntity) {
                if (!spawnLocation.getBlock().isPassable())
                    new WarningMessage("Warning: Location " + customBossesConfigFields.getFilename() + " for boss " +
                            customBossesConfigFields.getFilename() + " seems to be inside of a solid block!");
                ((RegionalBossEntity) this).checkLeash();
                getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 3));
            }
        } else
            persistentLocation = spawnLocation;

        if (!silent) {
            announceSpawn();
            startEscapeMechanismDelay();
        }

        if (summoningEntity != null)
            summoningEntity.addReinforcement(this);

        CommandRunner.runCommandFromList(customBossesConfigFields.getOnSpawnCommands(), new ArrayList<>());
    }

    private void setPluginName(){
        if (this.level == -1)
            super.setName(ChatColorConverter.convert(customBossesConfigFields.getName().replace("$level", "?" + "")
                            .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + "?" + "&2]&f"))
                            .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + "?" + "&6〗&f"))
                            .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + "?" + "&4』&f"))
                            .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + "?" + "&8〕&f")
                            .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + "?" + "&4」&f"))),
                    false);
            else
        super.setName(ChatColorConverter.convert(customBossesConfigFields.getName().replace("$level", this.level + "")
                .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + this.level + "&2]&f"))
                .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + this.level + "&6〗&f"))
                .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + this.level + "&4』&f"))
                .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + this.level + "&8〕&f")
                .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + this.level + "&4」&f"))),
                false);
    }

    private void announceSpawn() {
        setTracking();
        spawnMessage();
    }

    private void setTracking() {
        if (customBossesConfigFields.getAnnouncementPriority() < 1 ||
                !MobCombatSettingsConfig.showCustomBossLocation ||
                customBossesConfigFields.getLocationMessage() == null)
            return;
        trackableCustomBosses.add(this);
        customBossBossBar = new CustomBossBossBar(this);
    }

    private void spawnMessage() {
        if (customBossesConfigFields.getSpawnMessage() == null) return;
        if (customBossesConfigFields.getAnnouncementPriority() < 1) return;
        if (!EventsConfig.ANNOUNCEMENT_BROADCAST_WORLD_ONLY)
            Bukkit.broadcastMessage(ChatColorConverter.convert(customBossesConfigFields.getSpawnMessage()));
        else
            for (Player player : livingEntity.getWorld().getPlayers())
                player.sendMessage(ChatColorConverter.convert(customBossesConfigFields.getSpawnMessage()));
        if (customBossesConfigFields.getAnnouncementPriority() < 3) return;
        new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossesConfigFields.getSpawnMessage()));
    }

    //todo: review this code, is it still good?
    public void getDynamicLevel(Location bossLocation) {
        int bossLevel = 1;
        for (Entity entity : bossLocation.getWorld().getNearbyEntities(bossLocation, Math.max(Bukkit.getViewDistance() * 16, 5 * 16), 256, Bukkit.getViewDistance() * 16))
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

    private void cullReinforcements() {
        for (CustomBossEntity customBossEntity : eliteReinforcementEntities)
            if (customBossEntity != null)
                customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL);
        for (Entity entity : nonEliteReinforcementEntities)
            if (entity != null)
                entity.remove();
        eliteReinforcementEntities.clear();
        nonEliteReinforcementEntities.clear();
    }

    public Location getLocation() {
        if (getLivingEntity() != null) return getLivingEntity().getLocation();
        else return persistentLocation;
    }

    public double getDamageModifier(Material material) {
        return customBossesConfigFields.getDamageModifier(material);
    }

    @Override
    public void fullHeal() {
        if (phaseBossEntity == null || !phaseBossEntity.isInFirstPhase()) {
            super.fullHeal();
            return;
        }
        phaseBossEntity.resetToFirstPhase();
    }

    @Override
    public void remove(RemovalReason removalReason) {
        if (livingEntity != null) persistentLocation = livingEntity.getLocation();
        //Remove the living entity
        super.remove(removalReason);
        //Remove things tied to the living entity
        if (customBossTrail != null) customBossTrail.terminateTrails();
        if (livingEntityMount != null) livingEntityMount.remove();
        if (customBossMount != null) customBossMount.remove(RemovalReason.REINFORCEMENT_CULL);
        if (customBossesConfigFields.getCullReinforcements()) cullReinforcements();

        boolean bossInstanceEnd = removalReason.equals(RemovalReason.KILL_COMMAND) ||
                removalReason.equals(RemovalReason.DEATH) ||
                removalReason.equals(RemovalReason.BOSS_TIMEOUT) ||
                removalReason.equals(RemovalReason.OTHER);

        if (!isPersistent) bossInstanceEnd = true;

        if (bossInstanceEnd) {
            if (escapeMechanism != null) Bukkit.getScheduler().cancelTask(escapeMechanism);
            trackableCustomBosses.remove(this);
            if (simplePersistentEntity != null)
                simplePersistentEntity.remove();
            if (customBossBossBar != null)
                customBossBossBar.remove();
            if (phaseBossEntity != null)
                phaseBossEntity.deathReset();
        } else {
            simplePersistentEntity = new SimplePersistentEntity(this, getLocation());
        }
    }

    @Override
    public void chunkLoad() {
        chunkLoad = true;
        spawn(persistentLocation, true);
    }

    @Override
    public void chunkUnload() {
        remove(RemovalReason.CHUNK_UNLOAD);
    }

    @Override
    public void worldLoad() {
        spawnLocation.setWorld(Bukkit.getWorld(worldName));
        spawn(true);
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
            if (eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().getPotionEffect(PotionEffectType.SLOW).getAmplifier() == 10)
                return;
            eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().removePotionEffect(PotionEffectType.SLOW);
        }

        @EventHandler(ignoreCancelled = true)
        public void onExitCombat(EliteMobExitCombatEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            if (!((CustomBossEntity) event.getEliteMobEntity()).customBossesConfigFields.getCullReinforcements())
                return;
            ((CustomBossEntity) event.getEliteMobEntity()).cullReinforcements();
        }
    }

}
