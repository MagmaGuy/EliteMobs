package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.entitytracker.EliteEntityTracker;
import com.magmaguy.elitemobs.entitytracker.TrackedEntity;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntityInterface;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBossEntity extends EliteMobEntity implements Listener, SimplePersistentEntityInterface {

    /**
     * Constructs a custom boss from a player command
     */
    public static CustomBossEntity constructCustomBossCommand(String fileName,
                                                              Location location,
                                                              int mobLevel) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        if (!customBossMobsConfigAttributes.isEnabled()) return null;

        try {
            LivingEntity livingEntity = (LivingEntity) location.getWorld()
                    .spawnEntity(location, EntityType.valueOf(customBossMobsConfigAttributes.getEntityType()));
            return new CustomBossEntity(
                    customBossMobsConfigAttributes,
                    livingEntity,
                    mobLevel,
                    ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()));
        } catch (Exception ex) {
            new WarningMessage("Failed to spawn a Custom Boss via command because the entity type for it is not valid!");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a living entity for all custom bosses. Player command bypasses this check in order to bypass spawn restrictions.
     */
    private static LivingEntity generateLivingEntity(Location location,
                                                     CustomBossConfigFields customBossConfigFields) {
        if (!customBossConfigFields.isEnabled()) return null;
        if (!EliteMobEntity.validSpawnLocation(location)) return null;
        try {
            WorldGuardSpawnEventBypasser.forceSpawn();
            return (LivingEntity) location.getWorld().spawnEntity(location, EntityType.valueOf(customBossConfigFields.getEntityType()));
        } catch (Exception ex) {
            new WarningMessage("Failed to spawn a Custom Boss' living entity!");
            return null;
        }
    }

    /**
     * Method used most of the time for generating custom bosses.
     */
    public static CustomBossEntity constructCustomBoss(String fileName,
                                                       Location location,
                                                       int mobLevel) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        LivingEntity livingEntity = generateLivingEntity(location, customBossMobsConfigAttributes);
        if (livingEntity == null) return null;

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                livingEntity,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()));
    }

    /**
     * Method used to generate a custom boss for phase and regional boss entities
     */
    public static CustomBossEntity constructCustomBoss(CustomBossConfigFields customBossConfigFields,
                                                       Location location,
                                                       int mobLevel,
                                                       RegionalBossEntity regionalBossEntity,
                                                       boolean isPhaseBossRespawn) {
        LivingEntity livingEntity = generateLivingEntity(location, customBossConfigFields);
        if (livingEntity == null) return null;

        return new CustomBossEntity(
                customBossConfigFields,
                livingEntity,
                mobLevel,
                ElitePowerParser.parsePowers(customBossConfigFields.getPowers()),
                regionalBossEntity,
                isPhaseBossRespawn);
    }

    /**
     * Constructs a custom boss for the phase boss system at a specific phase
     */
    public static CustomBossEntity constructCustomBoss(String fileName,
                                                       Location location,
                                                       int mobLevel,
                                                       double health,
                                                       UUID phaseBossUUID) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        LivingEntity livingEntity = generateLivingEntity(location, customBossMobsConfigAttributes);
        if (livingEntity == null) return null;

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                livingEntity,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()),
                health,
                phaseBossUUID);
    }


    public static HashSet<CustomBossEntity> trackableCustomBosses = new HashSet<>();

    public CustomBossConfigFields customBossConfigFields;
    private final HashMap<CustomItem, Double> uniqueLootList = new HashMap<>();

    public LivingEntity advancedGetEntity() {
        if (getLivingEntity() == null ||
                !getLivingEntity().isValid() && !getLivingEntity().isDead())
            setLivingEntity((LivingEntity) Bukkit.getEntity(this.uuid));
        return getLivingEntity();
    }

    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            LivingEntity livingEntity,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers) {
        super(livingEntity,
                mobLevel,
                customBossConfigFields.getName(),
                elitePowers,
                CreatureSpawnEvent.SpawnReason.CUSTOM);
        initializeCustomBoss(customBossConfigFields);
        spawnMessage();
        if (customBossConfigFields.getPhases().size() > 0)
            new PhaseBossEntity(this);
    }

    /**
     * For regional bosses
     */
    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            LivingEntity livingEntity,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers,
                            RegionalBossEntity regionalBossEntity,
                            boolean isPhaseBossRespawn) {
        super(livingEntity,
                mobLevel,
                customBossConfigFields.getName(),
                elitePowers,
                CreatureSpawnEvent.SpawnReason.CUSTOM);
        initializeCustomBoss(customBossConfigFields);
        if (!isPhaseBossRespawn)
            spawnMessage();
        if (customBossConfigFields.getPhases().size() > 0) {
            new PhaseBossEntity(this, regionalBossEntity);
        }
    }

    /**
     * For Custom bosses spawned by the phase boss system
     */
    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            LivingEntity livingEntity,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers,
                            double health,
                            UUID phaseBossUUID) {
        super(livingEntity,
                mobLevel,
                customBossConfigFields.getName(),
                elitePowers,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                health,
                phaseBossUUID);
        initializeCustomBoss(customBossConfigFields);
        this.setHealth(health * getMaxHealth());
    }

    private void initializeCustomBoss(CustomBossConfigFields customBossConfigFields) {
        if (super.getLivingEntity() == null) {
            this.uuid = null;
            new WarningMessage("Failed to spawn boss " + customBossConfigFields.getFileName() + " .");
            return;
        }
        super.setDamageMultiplier(customBossConfigFields.getDamageMultiplier());
        super.setHealthMultiplier(customBossConfigFields.getHealthMultiplier());
        super.setHasSpecialLoot(customBossConfigFields.getDropsEliteMobsLoot());
        this.customBossConfigFields = customBossConfigFields;
        setEquipment();
        setBaby();
        setPersistent();
        startBossTrails();
        setVanillaLoot();
        parseUniqueLootList();
        setTracking();
        setFollowRange();
        mountEntity();
        setDisguise();
        setFrozen();
        CommandRunner.runCommandFromList(customBossConfigFields.getOnSpawnCommands(), new ArrayList<>());
        startEscapeMechanismDelay(customBossConfigFields.getTimeout());
        super.customBossEntity = this;
    }

    /**
     * This runs when a persistent Custom Boss has been nullified by something during runtime in order to respawn it.
     */
    public void silentCustomBossInitialization() {
        setEquipment();
        setBaby();
        setFollowRange();
        mountEntity();
        setFrozen();
        resetMaxHealth();
        setHealth(getHealth());
    }

    private void setBaby() {
        if (super.getLivingEntity() instanceof Ageable)
            if (customBossConfigFields.isBaby())
                ((Ageable) super.getLivingEntity()).setBaby();
            else
                ((Ageable) super.getLivingEntity()).setAdult();
    }

    private void spawnMessage() {
        if (customBossConfigFields.getSpawnMessage() == null) return;
        if (customBossConfigFields.getAnnouncementPriority() < 1) return;
        if (customBossConfigFields.getAnnouncementPriority() == 1)
            Bukkit.broadcastMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        else
            for (Player player : getLivingEntity().getWorld().getPlayers())
                player.sendMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        if (customBossConfigFields.getAnnouncementPriority() < 3) return;
        new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
    }

    private void setPersistent() {
        super.setPersistent(customBossConfigFields.getIsPersistent());
        if (customBossConfigFields.getIsPersistent()) {
            TrackedEntity trackedEntity = TrackedEntity.trackedEntities.get(uuid);
            trackedEntity.removeWhenFarAway = false;
            TrackedEntity.trackedEntities.put(uuid, trackedEntity);
        }
    }

    private void setDisguise() {
        if (customBossConfigFields.getDisguise() == null) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        DisguiseEntity.disguise(customBossConfigFields.getDisguise(), getLivingEntity(), customBossConfigFields);
        super.setName(customBossConfigFields.getName());
    }

    private void setFrozen() {
        if (!customBossConfigFields.getFrozen()) return;
        getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10));
        getLivingEntity().setCollidable(false);
        getLivingEntity().setGravity(false);
    }

    private void setTracking() {
        if (customBossConfigFields.getAnnouncementPriority() > 1 &&
                MobCombatSettingsConfig.showCustomBossLocation &&
                customBossConfigFields.getLocationMessage() != null) {
            trackableCustomBosses.add(this);
            sendLocation();
        }
    }

    private void setFollowRange() {
        if (customBossConfigFields.getFollowRange() != null &&
                customBossConfigFields.getFollowRange() > 0 &&
                getLivingEntity() instanceof Mob)
            getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(customBossConfigFields.getFollowRange());
    }

    private CustomBossTrail customBossTrail;

    private void startBossTrails() {
        customBossTrail = new CustomBossTrail(this);
    }

    private void parseUniqueLootList() {
        for (String entry : this.customBossConfigFields.getUniqueLootList()) {
            try {
                CustomItem customItem = CustomItem.getCustomItem(entry.split(":")[0]);
                if (customItem == null)
                    throw new Exception();
                this.uniqueLootList.put(customItem, Double.parseDouble(entry.split(":")[1]));
            } catch (Exception ex) {
                new WarningMessage("Boss " + this.getName() + " has an invalid loot entry - " + entry + " - Skipping it!");
            }
        }
    }

    private void setVanillaLoot() {
        if (customBossConfigFields.getDropsVanillaLoot())
            super.setHasVanillaLoot(customBossConfigFields.getDropsVanillaLoot());
    }

    private HashMap<CustomItem, Double> getUniqueLootList() {
        return this.uniqueLootList;
    }

    private void setEquipment() {
        try {
            getLivingEntity().getEquipment().setHelmet(customBossConfigFields.getHelmet());
            getLivingEntity().getEquipment().setChestplate(customBossConfigFields.getChestplate());
            getLivingEntity().getEquipment().setLeggings(customBossConfigFields.getLeggings());
            getLivingEntity().getEquipment().setBoots(customBossConfigFields.getBoots());
            getLivingEntity().getEquipment().setItemInMainHand(customBossConfigFields.getMainHand());
            getLivingEntity().getEquipment().setItemInOffHand(customBossConfigFields.getOffHand());
        } catch (Exception ex) {
            new WarningMessage("Tried to assign a material slot to an invalid entity! Boss is from file" + customBossConfigFields.getFileName());
        }
    }

    private void sendLocation() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(getLivingEntity().getWorld())) continue;
            TextComponent interactiveMessage = new TextComponent(MobCombatSettingsConfig.bossLocationMessage);
            interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + player.getName() + " " + this.uuid));
            interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Track the " + getName()).create()));
            player.spigot().sendMessage(interactiveMessage);
        }
    }

    public final HashSet<Player> trackingPlayer = new HashSet<>();
    public HashMap<Player, BossBar> playerBossBars = new HashMap<>();
    public HashSet<CustomBossBossBar> customBossBossBars = new HashSet<>();

    public void startBossBarTask(Player player, boolean persistentTracking) {
        if (trackingPlayer.contains(player))
            if (persistentTracking) {
                //in case a player untracks a boss
                trackingPlayer.remove(player);
                return;
            } else
                //In case non-persistent tracking is activated for someone already doing persistent tracking
                return;
        //in case non-persistent tracking is activated for someone already under the effect of non-persistent tracking
        if (playerBossBars.containsKey(player)) return;
        //In case a new custom boss bar needs to be created, persistent or not
        customBossBossBars.add(new CustomBossBossBar(this, player, persistentTracking));
    }

    private BukkitTask bossLocalScan;

    public void startBossBarLocalScan() {
        CustomBossEntity customBossEntity = this;
        bossLocalScan = new BukkitRunnable() {
            @Override
            public void run() {
                if (!customBossEntity.isInCombat()) {
                    cancel();
                    return;
                }
                for (Entity entity : getLivingEntity().getNearbyEntities(20, 20, 20))
                    if (entity.getType().equals(EntityType.PLAYER))
                        customBossEntity.startBossBarTask((Player) entity, false);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    /**
     * Used not only for generating the boss bar message, but also the message that appears on the tracking screen for players
     *
     * @param player
     * @param locationString
     * @return
     */
    public String bossBarMessage(Player player, String locationString) {
        if (customBossConfigFields.getLocationMessage().contains("$distance") ||
                customBossConfigFields.getLocationMessage().contains("$location")) {
            if (!player.getLocation().getWorld().equals(getLivingEntity().getWorld()))
                return ChatColorConverter.convert(MobCombatSettingsConfig.defaultOtherWorldBossLocationMessage
                        .replace("$name", getName()));

            return ChatColorConverter.convert(customBossConfigFields.getLocationMessage()
                    .replace("$distance", "" + (int) getLivingEntity().getLocation().distance(player.getLocation())))
                    .replace("$location", locationString);
        }

        return ChatColorConverter.convert(customBossConfigFields.getLocationMessage());

    }

    public void dropLoot(Player player) {

        if (getUniqueLootList().isEmpty()) return;

        for (CustomItem customItem : getUniqueLootList().keySet())
            if (ThreadLocalRandom.current().nextDouble() < getUniqueLootList().get(customItem))
                CustomItem.dropPlayerLoot(player, (int) getTier(), customItem.getFileName(), getLivingEntity().getLocation());
    }

    public CustomBossEntity mount;

    private void mountEntity() {
        mount = CustomBossMount.generateMount(this);
    }

    private BukkitTask escapeMechanism;

    /**
     * Starts the escape mechanic for bosses that have this feature. After a set time, in minutes, the boss will escape,
     * potentially broadcasting an escape message.
     */
    private void startEscapeMechanismDelay(int timeout) {
        escapeMechanism = CustomBossEscapeMechanism.startEscape(timeout, this);
    }

    /**
     * Removal method for when the custom boss dies or is otherwise entirely removed
     */
    @Override
    public void remove(boolean removeEntity) {
        softRemove();
        if (escapeMechanism != null) escapeMechanism.cancel();
        trackableCustomBosses.remove(this);
        if (simplePersistentEntity != null) simplePersistentEntity.remove();
        for (CustomBossBossBar customBossBossBar : customBossBossBars) customBossBossBar.remove();
        super.remove(removeEntity);
    }

    /**
     * Removal method for stopping tasks when the boss gets unloaded.
     * Should remove any ongoing effects which would malfunction if unloaded.
     */
    public void softRemove() {
        if (customBossTrail != null) customBossTrail.terminateTrails();
        if (bossLocalScan != null) bossLocalScan.cancel();
    }

    /**
     * Runs on chunk load. Only for persistent entities.
     */
    @Override
    public void chunkLoad() {
        setNewLivingEntity(persistentLocation);
        //This bypasses the spawn event caller, since having it trigger the spawn message and so on every time a chunk gets loaded would be bad
        new EliteEntityTracker(this, getPersistent(), true);
        customBossTrail.restartTrails();
        setDisguise();
    }

    public SimplePersistentEntity simplePersistentEntity;
    public Location persistentLocation;

    /**
     * Runs on chunk unload.
     * If the custom boss isn't persistent, this removes the entity.
     * If the boss is persistent, this removes the LivingEntity instance, caches the location data to later spawn a
     * LivingEntity at the location this dropped off.
     */
    @Override
    public void chunkUnload() {
        if (!getPersistent()) {
            remove(true);
            return;
        }

        //For some reason sometimes the living entities are nullified on chunk unload
        advancedGetEntity();
        if (getLivingEntity() == null) {
            if (regionalBossEntity != null) {
                persistentLocation = regionalBossEntity.spawnLocation;
                    /*
                    This is a specific case where this Custom Boss is a regional boss entity, the Living Entity became
                    null while it was still meant to be alive, and the location that the boss is mean to spawn in is still
                    loaded, meaning that it will instantly respawn at its spawn point.
                     */
                if (ChunkLocationChecker.locationIsLoaded(persistentLocation)) {
                    super.remove(true);
                    customBossEntity.regionalBossEntity.spawnRegionalBoss();
                    return;
                }
            } else {
                new WarningMessage("Custom Boss Entity " + customBossConfigFields.getFileName() + " was null by the time the chunk unloaded!");
                new WarningMessage("HP was: " + getHealth());
            }
        } else persistentLocation = getLivingEntity().getLocation();

        simplePersistentEntity = new SimplePersistentEntity(this);
        softRemove();
        super.remove(true);
    }

    public Location getLocation() {
        if (advancedGetEntity() != null) return advancedGetEntity().getLocation();
        else return persistentLocation;
    }

    /**
     * Runs on boss death. This is for true natural deaths.
     * Note: Most death handling is done in the CustomBossDeath class, this is just here to unregister the active tasks.
     */
    public void doDeath() {
        remove(false);
    }

    public static class CustomBossEntityEvents implements Listener {
        @EventHandler
        public void removeSlowEvent(EliteMobEnterCombatEvent eliteMobEnterCombatEvent) {
            if (eliteMobEnterCombatEvent.getEliteMobEntity().customBossEntity == null) return;
            if (eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().getPotionEffect(PotionEffectType.SLOW) == null)
                return;
            if (eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().getPotionEffect(PotionEffectType.SLOW).getAmplifier() == 10)
                return;
            eliteMobEnterCombatEvent.getEliteMobEntity().getLivingEntity().removePotionEffect(PotionEffectType.SLOW);
        }
    }

}
