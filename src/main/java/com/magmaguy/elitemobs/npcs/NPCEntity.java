package com.magmaguy.elitemobs.npcs;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
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
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import com.magmaguy.magmacore.util.AttributeManager;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ChunkLocationChecker;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
    private TextDisplay roleDisplay;
    private boolean isDisguised = false;
    private String locationString;
    @Getter
    private CustomModel customModel = null;

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
        if (roleDisplay != null) {
            roleDisplay.remove();
            roleDisplay = null;
        }
        // Remove house earnings display if this is the gambling den owner
        com.magmaguy.elitemobs.gambling.GamblingDenOwnerDisplay.removeDisplay(uuid);
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
                !ChunkLocationChecker.chunkAtLocationIsLoaded(spawnLocation)) return;
        if (villager != null && villager.isValid()) return;
        if (npCsConfigFields.getInteractionType().equals(NPCInteractions.NPCInteractionType.SCROLL_APPLIER) &&
                !ItemSettingsConfig.isUseEliteItemScrolls()) return;
        if (EliteMobs.worldGuardIsEnabled)
            WorldGuardSpawnEventBypasser.forceSpawn();
        villager = spawnLocation.getWorld().spawn(spawnLocation, Villager.class, villagerInstance -> {
            villagerInstance.setAI(false);
            villagerInstance.setPersistent(false);
            villagerInstance.setRemoveWhenFarAway(false);
            villagerInstance.setCustomName(ChatColorConverter.convert(npCsConfigFields.getName()));
            villagerInstance.setCustomNameVisible(true);
            villagerInstance.setProfession(npCsConfigFields.getProfession());
            AttributeManager.setAttribute(villagerInstance, "generic_scale", npCsConfigFields.getScale());
        });
        // Apply custom model or disguise after spawn completes (not inside consumer)
        if (CustomModel.customModelsEnabled() &&
                getNPCsConfigFields().getCustomModel() != null &&
                !getNPCsConfigFields().getCustomModel().isEmpty() &&
                CustomModel.modelExists(getNPCsConfigFields().getCustomModel()))
            setCustomModel(villager);
        else
            setDisguise(villager);
        EntityTracker.registerNPCEntity(this);
        initializeRole();
        setTimeout();
        // Create house earnings display if this is the gambling den owner
        com.magmaguy.elitemobs.gambling.GamblingDenOwnerDisplay.createDisplay(this);
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
            Logger.warn("Failed to load LibsDisguises disguise correctly!");
            ex.printStackTrace();
        }
    }

    private void setCustomModel(LivingEntity livingEntity) {
        customModel = CustomModel.generateCustomModel(livingEntity, getNPCsConfigFields().getCustomModel(), getNPCsConfigFields().getName(),
                (player, modeledEntity) -> NPCInteractions.handleNPCInteraction(player, this),
                (player, modeledEntity) -> NPCInteractions.handleNPCInteraction(player, this));
        if (customModel != null)
            customModel.setSyncMovement(getNPCsConfigFields().isSyncMovement());
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

    //This is not actually used because EntityTracker is the one managing the entity, not PersistentObjectTracker
    @Override
    public void chunkUnload() {
        remove(RemovalReason.CHUNK_UNLOAD);
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
        if (npCsConfigFields.getRole() == null || npCsConfigFields.getRole().isEmpty()) return;

        // Determine vertical offset based on model type
        double yOffset;
        if (customModel != null) {
            // Custom models typically need more height
            yOffset = 2.3;
        } else if (isDisguised) {
            yOffset = 1.55;
        } else {
            yOffset = 2.52;
        }

        Location roleLocation = villager.getLocation().clone().add(0, yOffset, 0);

        // Use real TextDisplay entity - Minecraft handles visibility automatically
        roleDisplay = roleLocation.getWorld().spawn(roleLocation, TextDisplay.class, textDisplay -> {
            textDisplay.setText(ChatColorConverter.convert(npCsConfigFields.getRole()));
            textDisplay.setPersistent(false);
            textDisplay.setBillboard(Display.Billboard.CENTER);
            textDisplay.setShadowed(false);
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
