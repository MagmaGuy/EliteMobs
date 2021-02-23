package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntityInterface;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBossEntity extends EliteMobEntity implements Listener, SimplePersistentEntityInterface {

    /**
     * Generates a living entity for all custom bosses. Player command bypasses this check in order to bypass spawn restrictions.
     */
    private static LivingEntity generateLivingEntity(Location location,
                                                     CustomBossConfigFields customBossConfigFields) {
        if (customBossConfigFields == null || !customBossConfigFields.isEnabled()) {
            new WarningMessage("Attempted to spawn a boss which had its configuration file disabled! Boss file: " + customBossConfigFields.getFileName());
            return null;
        }
        if (!EliteMobEntity.validSpawnLocation(location)) return null;
        try {
            WorldGuardSpawnEventBypasser.forceSpawn();
            return (LivingEntity) location.getWorld().spawnEntity(location, EntityType.valueOf(customBossConfigFields.getEntityType()));
        } catch (Exception ex) {
            new WarningMessage("Failed to spawn a Custom Boss' living entity! Is the region protected against spawns? Custom boss: " + customBossConfigFields.getFileName() + " entry: " + location.toString());
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
     * Method used by mounts
     * @param fileName
     * @param location
     * @param mobLevel
     * @return
     */
    public static CustomBossEntity constructCustomBossMount(String fileName,
                                                       Location location,
                                                       int mobLevel) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        customBossMobsConfigAttributes.setIsPersistent(false);
        LivingEntity livingEntity = generateLivingEntity(location, customBossMobsConfigAttributes);
        if (livingEntity == null) return null;

        CustomBossEntity mount =  new CustomBossEntity(
                customBossMobsConfigAttributes,
                livingEntity,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()));

        //prevent the mount from despawning based on distance, does not mount unloading based on chunks
        mount.getLivingEntity().setRemoveWhenFarAway(false);

        return mount;
    }

    /**
     * Method used by commands
     */
    public static CustomBossEntity constructCustomBoss(String fileName,
                                                       Location location) {
        CustomBossConfigFields customBossConfigFields = CustomBossesConfig.getCustomBoss(fileName);
        LivingEntity livingEntity = generateLivingEntity(location, customBossConfigFields);
        if (livingEntity == null) return null;

        return new CustomBossEntity(
                customBossConfigFields,
                livingEntity,
                customBossConfigFields.getLevel(),
                ElitePowerParser.parsePowers(customBossConfigFields.getPowers()));
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
                                                       double health) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        LivingEntity livingEntity = generateLivingEntity(location, customBossMobsConfigAttributes);
        if (livingEntity == null) return null;

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                livingEntity,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()),
                health);
    }


    public static HashSet<CustomBossEntity> trackableCustomBosses = new HashSet<>();

    public CustomBossConfigFields customBossConfigFields;
    public CustomBossEntity customBossMount = null;
    public LivingEntity livingEntityMount = null;
    private final HashMap<CustomItem, Double> uniqueLootList = new HashMap<>();

    public LivingEntity advancedGetEntity() {
        if (getLivingEntity() == null ||
                !getLivingEntity().isValid() && !getLivingEntity().isDead())
            setLivingEntity((LivingEntity) Bukkit.getEntity(this.uuid));
        return getLivingEntity();
    }

    /**
     * Main custom boss constructor
     */
    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            LivingEntity livingEntity,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers) {
        super(livingEntity,
                mobLevel,
                customBossConfigFields.getName(),
                elitePowers,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                customBossConfigFields.getIsPersistent());
        initializeCustomBoss(customBossConfigFields);
        spawnMessage();
        if (customBossConfigFields.getPhases().size() > 0)
            new PhaseBossEntity(this);
    }

    /**
     * For regional & phase bosses
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
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                //regional & phase bosses HAVE to be persistent
                true);
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
                            double health) {
        super(livingEntity,
                mobLevel,
                customBossConfigFields.getName(),
                elitePowers,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                health,
                //todo: phase bosses currently have to be persistent for safety reasons, this might not adapt to every setup
                true);
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
        setName(customBossConfigFields.getName());
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
        if (customBossConfigFields.getAnnouncementPriority() == 1 && !ConfigValues.eventsConfig.getBoolean(EventsConfig.ANNOUNCEMENT_BROADCAST_WORLD_ONLY))
            Bukkit.broadcastMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        else
            for (Player player : getLivingEntity().getWorld().getPlayers())
                player.sendMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        if (customBossConfigFields.getAnnouncementPriority() < 3) return;
        new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
    }

    private void setDisguise() {
        if (customBossConfigFields.getDisguise() == null) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        try {
            DisguiseEntity.disguise(customBossConfigFields.getDisguise(), getLivingEntity(), customBossConfigFields);
        } catch (Exception ex) {
            new WarningMessage("Failed to load LibsDisguises disguise correctly!");
        }
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
            if (getLivingEntity().getEquipment().getHelmet() != null) {
                ItemMeta helmetMeta = getLivingEntity().getEquipment().getHelmet().getItemMeta();
                if (helmetMeta != null) {
                    helmetMeta.setUnbreakable(true);
                    getLivingEntity().getEquipment().getHelmet().setItemMeta(helmetMeta);
                }
            }
        } catch (Exception ex) {
            new WarningMessage("Tried to assign a material slot to an invalid entity! Boss is from file" + customBossConfigFields.getFileName());
        }
    }

    private void sendLocation() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(getLivingEntity().getWorld())) continue;
            TextComponent interactiveMessage = new TextComponent(MobCombatSettingsConfig.bossLocationMessage);
            interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + this.uuid));
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
        if (simplePersistentEntity != null) {
            //This manually dereferences simple persistent entities and doesn't remove them to avoid async conflicts
            simplePersistentEntity.customBossEntity = null;
            simplePersistentEntity = null;
        }
        for (CustomBossBossBar customBossBossBar : customBossBossBars) customBossBossBar.remove(true);
        customBossBossBars.clear();
        if (removeEntity){
            if (getLivingEntity() != null)
                getLivingEntity().remove();
            if (simplePersistentEntity != null)
                simplePersistentEntity.remove();
        }
    }

    /**
     * Removal method for stopping tasks when the boss gets unloaded.
     * Should remove any ongoing effects which would malfunction if unloaded.
     */
    public void softRemove() {
        if (customBossTrail != null) customBossTrail.terminateTrails();
        if (bossLocalScan != null) bossLocalScan.cancel();
        if (livingEntityMount != null)
            livingEntityMount.remove();
        if (customBossMount != null)
            customBossMount.remove(true);
    }

    /**
     * Runs on chunk load. Only for persistent entities.
     */
    @Override
    public void chunkLoad() {
        setNewLivingEntity(persistentLocation);
        customBossTrail.restartTrails();
        setDisguise();
        if (regionalBossEntity != null)
            regionalBossEntity.chunkLoad();
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
        if (advancedGetEntity() == null)
            if (regionalBossEntity != null)
                persistentLocation = regionalBossEntity.spawnLocation;
            else
                new WarningMessage("Failed to register persistent location before chunk unload! Boss: " + getName());
        else
            persistentLocation = getLivingEntity().getLocation();

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
