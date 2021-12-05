package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.entitytracker.NPCEntityTracker;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntity;
import com.magmaguy.elitemobs.mobconstructor.SimplePersistentEntityInterface;
import com.magmaguy.elitemobs.npcs.chatter.NPCChatBubble;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.ChunkLocationChecker;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.NonSolidBlockTypes;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NPCEntity implements SimplePersistentEntityInterface {

    public final NPCsConfigFields npCsConfigFields;
    public UUID uuid;
    public SimplePersistentEntity simplePersistentEntity;
    private Villager villager = null;
    private Location spawnLocation;
    private boolean isTalking = false;
    private ArmorStand roleDisplay;

    /**
     * Spawns NPC based off of the values in the NPCsConfig config file. Runs at startup and on reload.
     */
    public NPCEntity(NPCsConfigFields npCsConfigFields) {
        this.npCsConfigFields = npCsConfigFields;
        if (!npCsConfigFields.isEnabled()) return;
        //this is how the wandering trader works
        if (npCsConfigFields.getLocation() == null || npCsConfigFields.getLocation().equalsIgnoreCase("null"))
            return;
        setSpawnLocation();
        queueSpawn();
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

    public void queueSpawn() {
        if (spawnLocation != null && ChunkLocationChecker.locationIsLoaded(spawnLocation)) {
            spawn();
            return;
        }
        simplePersistentEntity = new SimplePersistentEntity(this);
    }

    private boolean isDisguised = false;

    private void spawn() {
        if (villager != null && villager.isValid()) return;
        WorldGuardSpawnEventBypasser.forceSpawn();
        villager = spawnLocation.getWorld().spawn(spawnLocation, Villager.class, (villager) -> {
            villager.setAI(false);
            villager.setPersistent(false);
            villager.setRemoveWhenFarAway(false);
            villager.setCustomName(ChatColorConverter.convert(npCsConfigFields.getName()));
            villager.setCustomNameVisible(true);
            villager.setProfession(npCsConfigFields.getProfession());
            setDisguise(villager);
        });
        EntityTracker.registerNPCEntity(this);
        initializeRole();
        uuid = villager.getUniqueId();
        setTimeout();
        simplePersistentEntity = null;
    }

    private void setDisguise(LivingEntity livingEntity) {
        if (npCsConfigFields.getDisguise() == null) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return;
        try {
            DisguiseEntity.disguise(npCsConfigFields.getDisguise(), livingEntity, npCsConfigFields.getCustomDisguiseData(), npCsConfigFields.getFilename());
            DisguiseEntity.setDisguiseNameVisibility(true, livingEntity);
            villager.setCustomNameVisible(true);
            isDisguised = true;
        } catch (Exception ex) {
            new WarningMessage("Failed to load LibsDisguises disguise correctly!");
        }
    }

    public Villager getVillager() {
        return villager;
    }

    public NPCsConfigFields getNpCsConfigFields() {
        return npCsConfigFields;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public boolean setSpawnLocation() {
        Location location = ConfigurationLocation.serialize(npCsConfigFields.getLocation());
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
        if (getNpCsConfigFields().getTimeout() > 0) return;
        simplePersistentEntity = new SimplePersistentEntity(this);
        if (villager != null)
            villager.remove();
    }

    @Override
    public void worldLoad() {
        setSpawnLocation();
        queueSpawn();
    }

    @Override
    public void worldUnload() {
        spawnLocation = null;
        simplePersistentEntity = new SimplePersistentEntity(this);
        remove(RemovalReason.WORLD_UNLOAD);
    }

    //Can't be used after the NPCEntity is done initialising
    private void initializeRole() {
        Vector offSet;
        if (!isDisguised)
            offSet = new Vector(0, 1.72, 0);
        else
            offSet = new Vector(0, 1.70, 0);
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
        EntityTracker.registerArmorStands(roleDisplay);
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
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 3);
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

    public void remove(RemovalReason removalReason) {
        if (roleDisplay != null)
            roleDisplay.remove();
        if (villager != null) {
            villager.remove();
            NPCEntityTracker.npcEntities.remove(villager.getUniqueId());
            this.villager = null;
        }
        boolean permanentRemoval = false;

        if (removalReason.equals(RemovalReason.REMOVE_COMMAND))
            npCsConfigFields.setEnabled(false);

        if (removalReason.equals(RemovalReason.WORLD_UNLOAD) ||
                removalReason.equals(RemovalReason.REMOVE_COMMAND))
            permanentRemoval = true;

        if (permanentRemoval) {
            spawnLocation = null;
        }
    }

    /**
     * Sends a greeting to a player from the list of greetings the NPCEntity has
     *
     * @param player Player to send the greeting to
     */
    public void sayGreeting(Player player) {
        new NPCChatBubble(selectString(npCsConfigFields.getDialog()), this, player);
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

}
