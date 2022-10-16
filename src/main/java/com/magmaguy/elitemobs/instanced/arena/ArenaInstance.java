package com.magmaguy.elitemobs.instanced.arena;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.ArenaCompleteEvent;
import com.magmaguy.elitemobs.api.ArenaStartEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WarningMessage;
import com.magmaguy.elitemobs.utils.shapes.Cylinder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class ArenaInstance extends MatchInstance {

    @Getter
    private static final HashMap<String, ArenaInstance> arenaInstances = new HashMap<>();
    @Getter
    private final CustomArenasConfigFields customArenasConfigFields;
    @Getter
    private final ArenaWaves arenaWaves;
    @Getter
    private final HashSet<CustomBossEntity> customBosses = new HashSet<>();
    private final HashMap<Integer, String> waveMessage = new HashMap<>();
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;
    private final boolean cylindricalArena;
    protected HashMap<String, Location> spawnPoints = new HashMap<>();
    @Getter
    private int currentWave = 0;
    @Getter
    private ArenaState arenaState = ArenaState.IDLE;
    private Cylinder cylinder;

    public ArenaInstance(CustomArenasConfigFields customArenasConfigFields, Location corner1, Location corner2, Location startLocation, Location exitLocation) {
        super(startLocation,
                exitLocation,
                customArenasConfigFields.getMinimumPlayerCount(),
                customArenasConfigFields.getMaximumPlayerCount());
        this.cylindricalArena = customArenasConfigFields.isCylindricalArena();


        if (corner1.getX() < corner2.getX()) {
            minX = (int) corner1.getX();
            maxX = (int) corner2.getX();
        } else {
            minX = (int) corner2.getX();
            maxX = (int) corner1.getX();
        }

        if (corner1.getY() < corner2.getY()) {
            minY = (int) corner1.getY();
            maxY = (int) corner2.getY();
        } else {
            minY = (int) corner2.getY();
            maxY = (int) corner1.getY();
        }

        if (corner1.getZ() < corner2.getZ()) {
            minZ = (int) corner1.getZ();
            maxZ = (int) corner2.getZ();
        } else {
            minZ = (int) corner2.getZ();
            maxZ = (int) corner1.getZ();
        }
        this.world = corner1.getWorld();
        this.state = InstancedRegionState.WAITING;

        if (cylindricalArena)
            cylinder = new Cylinder(
                    new Vector((maxX - minX) / 2D + minX, minY,
                            (maxZ - minZ) / 2D + minZ),
                    (Math.abs(maxX) - Math.abs(minX)) / 2D, maxY - minY);

        addSpawnPoints(customArenasConfigFields.getSpawnPoints());
        this.customArenasConfigFields = customArenasConfigFields;
        this.arenaWaves = new ArenaWaves(customArenasConfigFields.getBossList());


        arenaInstances.put(customArenasConfigFields.getFilename(), this);
        arenaWatchdog();
        for (String string : customArenasConfigFields.getArenaMessages()) {
            String[] splitString = string.split(":");
            String message = "";
            int wave = 0;
            for (String subString : splitString) {
                String[] finalString = subString.split("=");
                switch (finalString[0].toLowerCase(Locale.ROOT)) {
                    case "wave":
                        try {
                            wave = Integer.parseInt(finalString[1]);
                        } catch (Exception ex) {
                            new WarningMessage("Failed to parse wave for entry " + subString + " for arena" + customArenasConfigFields.getFilename());
                        }
                        break;
                    case "message":
                        message = finalString[1];
                        break;
                    default:
                        new WarningMessage("Failed to parse arena message entry " + subString + " for arena" + customArenasConfigFields.getFilename());
                }
            }
            if (!message.isEmpty() && wave > 0)
                waveMessage.put(wave, ChatColorConverter.convert(message));
        }
    }

    public static void initializeArena(CustomArenasConfigFields customArenasConfigFields) {
        Location corner1 = ConfigurationLocation.serialize(customArenasConfigFields.getCorner1());
        Location corner2 = ConfigurationLocation.serialize(customArenasConfigFields.getCorner2());
        Location startLocation = ConfigurationLocation.serialize(customArenasConfigFields.getStartLocation());
        Location exitLocation = ConfigurationLocation.serialize(customArenasConfigFields.getExitLocation());
        if (corner1 == null || corner2 == null || startLocation == null || exitLocation == null) {
            //new WarningMessage("Failed to correctly initialize arena " + customArenasConfigFields.getFilename() + " due to invalid locations for corner1/corner2/startLocation/exitLocation");
            return;
        }
        if (corner1.getWorld() == null || corner2.getWorld() == null || startLocation.getWorld() == null || exitLocation.getWorld() == null) {
            //new WarningMessage("Failed to correctly initialize arena " + customArenasConfigFields.getFilename() + " due to invalid world for corner1/corner2/startLocation/exitLocation");
            return;
        }
        new ArenaInstance(customArenasConfigFields, corner1, corner2, startLocation, exitLocation);
    }

    public void addSpawnPoints(List<String> rawSpawnPoints) {
        for (String string : rawSpawnPoints) {
            String[] splitEntry = string.split(":");
            String name = "";
            String location = "";
            for (String subString : splitEntry) {
                String[] splitSubEntry = subString.split("=");
                switch (splitSubEntry[0].toLowerCase()) {
                    case "name":
                        name = splitSubEntry[1];
                        break;
                    case "location":
                        location = splitSubEntry[1];
                        break;
                    default:
                        new WarningMessage("Invalid entry for the spawn points of instanced content: " + splitSubEntry[0]);
                        break;
                }
            }
            spawnPoints.put(name, ConfigurationLocation.serialize(location));
        }
    }

    @Override
    protected boolean isInRegion(Location location) {
        if (!cylindricalArena)
            return location.getWorld().equals(world) &&
                    minX <= location.getX() &&
                    maxX >= location.getX() &&
                    minY <= location.getY() &&
                    maxY >= location.getY() &&
                    minZ <= location.getZ() &&
                    maxZ >= location.getZ();
        else
            return location.getWorld().equals(world) &&
                    cylinder.contains(location);
    }

    @Override
    protected void startMatch() {
        super.startMatch();
        new EventCaller(new ArenaStartEvent(this));
        nextWave();
    }

    private void nextWave() {
        currentWave++;
        arenaState = ArenaState.COOLDOWN;
        if (waveMessage.get(currentWave) != null) {
            players.forEach(player -> player.sendMessage(waveMessage.get(currentWave)));
            spectators.forEach(player -> player.sendMessage(waveMessage.get(currentWave)));
        }

        doRewards();
        if (currentWave > customArenasConfigFields.getWaveCount()) {
            endMatch();
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            if (arenaState == ArenaState.IDLE) return;
            String title = ArenasConfig.getWaveTitle();
            String subtitle = ArenasConfig.getWaveSubtitle();
            if (title == null) title = "";
            if (subtitle == null) subtitle = "";

            String finalTitle = title;
            String finalSubtitle = subtitle;
            players.forEach(player -> player.sendTitle(finalTitle.replace("$wave", currentWave + ""),
                    finalSubtitle.replace("$wave", currentWave + ""), 0, 20, 0));
            spectators.forEach(player -> player.sendTitle(finalTitle.replace("$wave", currentWave + ""),
                    finalSubtitle.replace("$wave", currentWave + ""), 0, 20, 0));
            spawnBosses();
            arenaState = ArenaState.ACTIVE;
        }, 20L * customArenasConfigFields.getDelayBetweenWaves());
    }


    private void arenaWatchdog() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arenaState != ArenaState.ACTIVE) return;
                for (CustomBossEntity customBossEntity : (HashSet<CustomBossEntity>) customBosses.clone())
                    if (!customBossEntity.exists()) removeBoss(customBossEntity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
    }

    public void removeBoss(CustomBossEntity customBossEntity) {
        customBosses.remove(customBossEntity);
        if (customBosses.isEmpty()) nextWave();
    }

    private void spawnBosses() {

        if (arenaWaves.getWaveEntities(currentWave) == null) return;
        for (ArenaEntity arenaEntity : arenaWaves.getWaveEntities(currentWave)) {
            CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(arenaEntity.getBossfile());
            if (customBossEntity == null) {
                new WarningMessage("Failed to generate custom boss " + arenaEntity.getBossfile() + " because the filename was not valid!");
                continue;
            }
            customBossEntity.setNormalizedCombat();
            customBossEntity.setEliteLoot(false);
            customBossEntity.setVanillaLoot(false);
            customBossEntity.setRandomLoot(false);
            customBossEntity.spawn(spawnPoints.get(arenaEntity.getSpawnPointName()), true);
            if (!customBossEntity.exists()) {
                new WarningMessage("Arena " + getCustomArenasConfigFields().getArenaName() + " failed to spawn boss " + customBossEntity.getCustomBossesConfigFields().getFilename());
                continue;
            } else customBosses.add(customBossEntity);
        }
    }

    private void doRewards() {
        //Note: here current wave is decreased by 1 because the reward runs at the top of the next wave method.
        //So basically it runs at the start of the wave, hence it should reward whatever the previous wave had.
        super.players.forEach(player -> customArenasConfigFields.getArenaRewards().arenaReward(player, currentWave - 1));
    }

    @Override
    protected void endMatch() {
        super.endMatch();
        arenaState = ArenaState.IDLE;
        //victory state
        if (currentWave > getCustomArenasConfigFields().getWaveCount()) {
            participants.forEach(player -> player.sendTitle(ArenasConfig.getVictoryTitle().replace("$wave", customArenasConfigFields.getWaveCount() + ""),
                    ArenasConfig.getVictorySubtitle().replace("$wave", customArenasConfigFields.getWaveCount() + ""), 20, 20 * 10, 20));
            StringBuilder playerNames = new StringBuilder();
            for (Player player : participants)
                playerNames.append(player.getName()).append(" ");
            Bukkit.getServer().broadcastMessage(ArenasConfig.getVictoryBroadcast().replace("$players", playerNames.toString()).replace("$arenaName", customArenasConfigFields.getArenaName()));
            ArenaCompleteEvent.ArenaCompleteEventHandler.call(this);
        } else
            participants.forEach(player -> player.sendTitle(ArenasConfig.getDefeatTitle().replace("$wave", currentWave + ""),
                    ArenasConfig.getDefeatSubtitle().replace("$wave", currentWave + ""), 20, 20 * 10, 20));

        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, this::resetMatch, customArenasConfigFields.getDelayBetweenWaves());
    }

    @Override
    protected void resetMatch() {
        super.resetMatch();
        arenaState = ArenaState.IDLE;
        currentWave = 0;
        customBosses.forEach(customBoss -> customBoss.remove(RemovalReason.ARENA_RESET));
        customBosses.clear();
    }

    private enum ArenaState {
        IDLE, COOLDOWN, ACTIVE
    }

    public static class ArenaInstanceEvents implements Listener {
        @EventHandler
        public void onEliteDeath(EliteMobDeathEvent event) {
            if (!(event.getEliteEntity() instanceof CustomBossEntity)) return;
            for (ArenaInstance arenaInstance : arenaInstances.values())
                if (arenaInstance.getCustomBosses().contains((CustomBossEntity) event.getEliteEntity())) {
                    arenaInstance.removeBoss((CustomBossEntity) event.getEliteEntity());
                    return;
                }
        }
    }
}
