package com.magmaguy.elitemobs.npcs;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.npcs.chatter.NPCChatBubble;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NPCEntity implements PersistentObject, PersistentMovingEntity {

    private static final ArrayListMultimap<String, InstancedNPCContainer> instancedNPCEntities = ArrayListMultimap.create();
    public final NPCsConfigFields npCsConfigFields;
    @Getter
    private final UUID uuid = UUID.randomUUID();
    private boolean isInstancedDuplicate = false;
    private PersistentObjectHandler persistentObjectHandler;
    private Villager villager = null;
    private Location spawnLocation;
    private boolean isTalking = false;
    private ArmorStand roleDisplay;
    private boolean isDisguised = false;
    private String locationString;
    /**
     * Spawns NPC based off of the values in the NPCsConfig config file. Runs at startup and on reload.
     */
    public NPCEntity(NPCsConfigFields npCsConfigFields, String locationString) {
        this.npCsConfigFields = npCsConfigFields;
        this.locationString = locationString;
        if (!npCsConfigFields.isEnabled()) return;
        if (npCsConfigFields.isInstanced()) {
            Location location = ConfigurationLocation.serialize(locationString);
            String blueprintWorldName = locationString.split(",")[0];
            instancedNPCEntities.put(blueprintWorldName, new InstancedNPCContainer(location, this));
            return;
        }
        //this is how the wandering trader works
        if (locationString == null ||
                locationString.equalsIgnoreCase("null"))
            return;
        setSpawnLocation();
        spawn();
        persistentObjectHandler = new PersistentObjectHandler(this);
    }
    /**
     * Spawns NPCs for dungeon instancing.
     */
    public NPCEntity(NPCsConfigFields npCsConfigFields, Location location) {
        this.npCsConfigFields = npCsConfigFields;
        isInstancedDuplicate = true;
        if (!npCsConfigFields.isEnabled()) return;
        //this is how the wandering trader works
        this.spawnLocation = location;
        spawn();
        persistentObjectHandler = new PersistentObjectHandler(this);
    }

    /**
     * For the travelling merchant
     *
     * @param location
     */
    public NPCEntity(Location location) {
        this.npCsConfigFields = NPCsConfig.getNpcEntities().get("travelling_merchant.yml");
        if (!npCsConfigFields.isEnabled()) return;
        Location potentialLocation = location.clone();
        potentialLocation.add(potentialLocation.getDirection().normalize()).setY(location.getY());
        if (NonSolidBlockTypes.isPassthrough(potentialLocation.getBlock().getType()))
            this.spawnLocation = potentialLocation;
        else this.spawnLocation = location.clone();
        this.spawnLocation.setDirection(this.spawnLocation.getDirection().multiply(-1));
        spawn();
    }

    public static void shutdown() {
        instancedNPCEntities.clear();
    }

    public static void initializeInstancedNPCs(String blueprintWorldName, World newWorld, int playerCount, DungeonInstance dungeonInstance) {
        List<InstancedNPCContainer> rawNPCs = instancedNPCEntities.get(blueprintWorldName);
        for (InstancedNPCContainer instancedNPCContainer : rawNPCs) {
            Location newLocation = instancedNPCContainer.getLocation();
            newLocation.setWorld(newWorld);
            new NPCEntity(instancedNPCContainer.getNpcEntity().getNPCsConfigFields(), newLocation);
        }
    }

    public static void initializeNPCs(NPCsConfigFields npCsConfigFields) {
        if (npCsConfigFields.getLocations() != null && !npCsConfigFields.getLocations().isEmpty()) {
            for (String locationString : npCsConfigFields.getLocations())
                new NPCEntity(npCsConfigFields, locationString);
        } else if (npCsConfigFields.getSpawnLocation() != null && !npCsConfigFields.getSpawnLocation().isEmpty()) {
            new NPCEntity(npCsConfigFields, npCsConfigFields.getSpawnLocation());
        }
    }

    public void remove(RemovalReason removalReason) {
        if (roleDisplay != null)
            roleDisplay.remove();
        if (villager != null) {
            villager.remove();
            EntityTracker.getNpcEntities().remove(villager.getUniqueId());
            this.villager = null;
        }

        if (removalReason.equals(RemovalReason.WORLD_UNLOAD) && isInstancedDuplicate) {
            persistentObjectHandler.remove();
            return;
        }

        if (removalReason.equals(RemovalReason.REMOVE_COMMAND)) {
            if (npCsConfigFields.getLocations() != null && !npCsConfigFields.getLocations().isEmpty()) {
                npCsConfigFields.removeNPC(locationString);
                locationString = null;
                spawnLocation = null;
            } else {
                npCsConfigFields.setEnabled(false);
                spawnLocation = null;
            }
            if (persistentObjectHandler != null)
                persistentObjectHandler.remove();
        } else if (persistentObjectHandler != null)
            if (!removalReason.equals(RemovalReason.SHUTDOWN))
                persistentObjectHandler.updatePersistentLocation(getPersistentLocation());
            else
                persistentObjectHandler.remove();
    }

    private void spawn() {
        if (spawnLocation == null ||
                spawnLocation.getWorld() == null ||
                !ChunkLocationChecker.locationIsLoaded(spawnLocation)) return;
        if (villager != null && villager.isValid()) return;
        WorldGuardSpawnEventBypasser.forceSpawn();
        villager = spawnLocation.getWorld().spawn(spawnLocation, Villager.class, villagerInstance -> {
            villagerInstance.setAI(false);
            villagerInstance.setPersistent(false);
            villagerInstance.setRemoveWhenFarAway(false);
            villagerInstance.setCustomName(ChatColorConverter.convert(npCsConfigFields.getName()));
            villagerInstance.setCustomNameVisible(true);
            villagerInstance.setProfession(npCsConfigFields.getProfession());
            if (getNPCsConfigFields().getCustomModel() != null && !getNPCsConfigFields().getCustomModel().isEmpty())
                setCustomModel(villagerInstance);
            else
                setDisguise(villagerInstance);
        });
        EntityTracker.registerNPCEntity(this);
        initializeRole();
        setTimeout();
    }

    private void setDisguise(LivingEntity livingEntity) {
        if (npCsConfigFields.getDisguise() == null) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        try {
            DisguiseEntity.disguise(npCsConfigFields.getDisguise(), livingEntity, npCsConfigFields.getCustomDisguiseData(), npCsConfigFields.getFilename());
            DisguiseEntity.setDisguiseNameVisibility(true, livingEntity, npCsConfigFields.getName());
            livingEntity.setCustomNameVisible(true);
            isDisguised = true;
        } catch (Exception ex) {
            new WarningMessage("Failed to load LibsDisguises disguise correctly!");
            ex.printStackTrace();
        }
    }

    private void setCustomModel(LivingEntity livingEntity) {
        CustomModel.generateCustomModel(livingEntity, getNPCsConfigFields().getCustomModel(), getNPCsConfigFields().getName());
    }

    public Villager getVillager() {
        return villager;
    }

    public boolean isValid() {
        if (villager == null) return false;
        return villager.isValid();
    }

    public NPCsConfigFields getNPCsConfigFields() {
        return npCsConfigFields;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public boolean setSpawnLocation() {
        Location location = ConfigurationLocation.serialize(locationString);
        if (location == null) return false;
        this.spawnLocation = location;
        return true;
    }

    public boolean getIsTalking() {
        return this.isTalking;
    }

    @Override
    public void chunkLoad() {
        spawn();
    }

    @Override
    public void chunkUnload() {
        if (villager != null)
            villager.remove();
        if (getNPCsConfigFields().getTimeout() > 0) return;

        if (villager != null)
            villager.remove();
    }

    @Override
    public void worldLoad(World world) {
        setSpawnLocation();
        //queueSpawn();
    }

    @Override
    public void worldUnload() {
        spawnLocation.setWorld(null);
        remove(RemovalReason.WORLD_UNLOAD);
    }

    @Override
    public Location getPersistentLocation() {
        return getSpawnLocation();
    }

    @Override
    public String getWorldName() {
        if (getSpawnLocation() != null && getSpawnLocation().getWorld() != null)
            return getSpawnLocation().getWorld().getName();
        String world = null;
        if (locationString != null && !locationString.isEmpty())
            world = locationString.split(",")[0];
        return world;
    }

    //Can't be used after the NPCEntity is done initialising
    private void initializeRole() {
        Vector offSet;
        if (!isDisguised)
            offSet = new Vector(0, 1.72, 0);
        else
            offSet = new Vector(0, 1.55, 0);
        roleDisplay = this.villager.getWorld().spawn(villager.getLocation().add(offSet), ArmorStand.class, new Consumer<ArmorStand>() {
            @Override
            public void accept(ArmorStand armorStand) {
                armorStand.setCustomName(npCsConfigFields.getRole());
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setPersistent(false);
            }
        });
        EntityTracker.registerVisualEffects(roleDisplay);
    }

    /**
     * Starts a cooldown for the talking state. Useful to avoid overlapping speech.
     */
    public void startTalkingCooldown() {
        this.isTalking = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                isTalking = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3L);
    }

    public void setTimeout() {
        if (npCsConfigFields.getTimeout() <= 0) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                remove(RemovalReason.NPC_TIMEOUT);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, (long) (npCsConfigFields.getTimeout() * 20 * 60));
    }

    /**
     * Sends a greeting to a player from the list of greetings the NPCEntity has
     *
     * @param player Player to send the greeting to
     */
    public void sayGreeting(Player player) {
        new NPCChatBubble(selectString(npCsConfigFields.getGreetings()), this, player);
    }

    /**
     * Sends a dialog message to a player from the list of dialog messages the NPCEntity has
     *
     * @param player Player to send the dialog to
     */
    public void sayDialog(Player player) {
        new NPCChatBubble(selectString(npCsConfigFields.getDialog()), this, player);
    }

    /**
     * Sends a farewell message to a player from the list of farewell messages the NPCEntity has
     *
     * @param player
     */
    public void sayFarewell(Player player) {
        new NPCChatBubble(selectString(npCsConfigFields.getFarewell()), this, player);
    }

    /**
     * Selects a string from a list of strings
     *
     * @param strings List of string to select from
     * @return Selected String
     */
    private String selectString(List<String> strings) {
        if (strings != null && !strings.isEmpty())
            return strings.get(ThreadLocalRandom.current().nextInt(strings.size()));
        return null;
    }

    public static class NPCEntityEvents implements Listener {
        @EventHandler
        public void worldUnloadEvent(WorldUnloadEvent event) {
            for (NPCEntity npcEntity : EntityTracker.getNpcEntities().values()) {
                if (npcEntity.getSpawnLocation() != null && npcEntity.getSpawnLocation().getWorld() == event.getWorld()) {
                    npcEntity.worldUnload();
                }
            }
        }
    }

    private class InstancedNPCContainer {
        @Getter
        private final Location location;
        @Getter
        private final NPCEntity npcEntity;

        public InstancedNPCContainer(Location location, NPCEntity npcEntity) {
            this.location = location;
            this.npcEntity = npcEntity;
        }
    }

}
