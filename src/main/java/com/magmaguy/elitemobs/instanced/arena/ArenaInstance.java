package com.magmaguy.elitemobs.instanced.arena;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.ArenaCompleteEvent;
import com.magmaguy.elitemobs.api.ArenaStartEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.mythicmobs.MythicMobsInterface;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;

import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ArenaInstance extends MatchInstance {

    @Getter
    private static final HashMap<String, ArenaInstance> arenaInstances = new HashMap<>();
    private static final Map<String, ArenaContainer> containers = new HashMap<>();

    // Arenas whose configured worlds aren't loaded yet. On every WorldLoadEvent we
    // try to drain matches whose corner1 world name equals the freshly-loaded
    // world's name. Keyed by world name (lower-case) for deterministic lookup
    // even when worlds are registered under odd-cased filenames. Multimap shape
    // because multiple arenas can live in the same world.
    private static final Map<String, List<CustomArenasConfigFields>> pendingArenas = new ConcurrentHashMap<>();

    public static void shutdown() {
        arenaInstances.clear();
        pendingArenas.clear();
        containers.clear();
    }

    @Getter
    private final HashSet<CustomBossEntity> customBosses = new HashSet<>();
    private final HashSet<Entity> nonEliteMobsEntities = new HashSet<>();
    private final HashMap<Integer, String> waveMessage = new HashMap<>();
    @Getter
    private final HashMap<Player, Double> roundDamage = new HashMap<>();

    @Getter
    private CustomArenasConfigFields customArenasConfigFields;
    @Getter
    private ArenaWaves arenaWaves;
    @Getter
    private final ArenaContainer container;
    @Getter
    private int currentWave = 0;
    @Getter
    private ArenaState arenaState = ArenaState.IDLE;

    private int highestArenaMobLevel = -1;

    public ArenaInstance(CustomArenasConfigFields customArenasConfigFields, Location corner1, Location corner2, Location startLocation, Location exitLocation) {
        this(customArenasConfigFields, new ArenaContainer(customArenasConfigFields, corner1, corner2, startLocation, exitLocation));
    }

    public ArenaInstance(CustomArenasConfigFields customArenasConfigFields, ArenaContainer container) {
        super(container.start(), container.exit(), customArenasConfigFields.getMinimumPlayerCount(), customArenasConfigFields.getMaximumPlayerCount());
        this.container = container;
        if (cancelled) return;
        super.lobbyLocation = container.lobby();
        this.world = container.start().getWorld();
        this.state = InstancedRegionState.WAITING;

        this.customArenasConfigFields = customArenasConfigFields;
        this.arenaWaves = new ArenaWaves(customArenasConfigFields.getBossList());


        arenaInstances.put(customArenasConfigFields.getFilename(), this);
        arenaWatchdog();
        for (String string : customArenasConfigFields.getArenaMessages()) {
            String[] splitString = string.split(":");
            String message = "";
            int wave = 0;
            for (String subString : splitString) {
                String[] finalString = subString.split("=", 2);
                if (finalString.length < 2) {
                    Logger.warn("Failed to parse arena message entry " + subString + " for arena " + customArenasConfigFields.getFilename());
                    continue;
                }
                String key = ChatColor.stripColor(finalString[0]).toLowerCase(Locale.ROOT);
                switch (key) {
                    case "wave":
                        try {
                            wave = Integer.parseInt(finalString[1]);
                        } catch (Exception ex) {
                            Logger.warn("Failed to parse wave for entry " + subString + " for arena " + customArenasConfigFields.getFilename());
                        }
                        break;
                    case "message":
                        message = finalString[1];
                        break;
                    default:
                        Logger.warn("Failed to parse arena message entry " + subString + " for arena " + customArenasConfigFields.getFilename());
                }
            }
            if (!message.isEmpty() && wave > 0) waveMessage.put(wave, ChatColorConverter.convert(message));
        }

        super.permission = customArenasConfigFields.getPermission();
    }

    public static void initializeArena(CustomArenasConfigFields customArenasConfigFields) {
        if (!Bukkit.isPrimaryThread()) {
            // MatchInstance's constructor fires the synchronous MatchInstantiateEvent,
            // which Bukkit refuses off the main thread, and pendingArenas is otherwise
            // only touched on the main thread. Async initialization — e.g. the reload
            // right after a DLC install when the arena's world is already loaded —
            // must hop before constructing anything.
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN,
                    () -> initializeArena(customArenasConfigFields));
            return;
        }
        if (arenaInstances.containsKey(customArenasConfigFields.getFilename())) return;
        CustomArenasConfigFields geometry;
        try {
            geometry = geometryDefinition(customArenasConfigFields);
        } catch (IllegalArgumentException failure) {
            Logger.warn(failure.getMessage());
            return;
        }
        Location corner1 = ConfigurationLocation.serialize(geometry.getCorner1());
        Location corner2 = ConfigurationLocation.serialize(geometry.getCorner2());
        Location startLocation = ConfigurationLocation.serialize(geometry.getStartLocation());
        Location exitLocation = ConfigurationLocation.serialize(geometry.getExitLocation());
        if (corner1 == null || corner2 == null || startLocation == null || exitLocation == null) {
            //Logger.warn("Failed to correctly initialize arena " + customArenasConfigFields.getFilename() + " due to invalid locations for corner1/corner2/startLocation/exitLocation");
            return;
        }
        if (corner1.getWorld() == null || corner2.getWorld() == null || startLocation.getWorld() == null || exitLocation.getWorld() == null) {
            // World not loaded yet (typical: EliteMobs enables before Multiverse / before
            // server-startup world load finishes). Park the config in pendingArenas keyed by
            // the corner1 world name; ArenaInstanceEvents.onWorldLoad will retry as worlds
            // come up. Without this, the arena is silently dropped and NPCs reporting it as
            // their target fail with "Invalid arena name!" until /em reload is run.
            String worldName = extractWorldName(geometry.getCorner1());
            if (worldName != null) {
                pendingArenas.computeIfAbsent(worldName.toLowerCase(Locale.ROOT), k -> new ArrayList<>())
                        .add(customArenasConfigFields);
            }
            return;
        }
        ArenaContainer container = containers.computeIfAbsent(geometry.getFilename(), ignored ->
                new ArenaContainer(geometry, corner1, corner2, startLocation, exitLocation));
        new ArenaInstance(customArenasConfigFields, container);
    }

    private static CustomArenasConfigFields geometryDefinition(CustomArenasConfigFields definition) {
        java.util.Set<String> visited = new HashSet<>();
        while (true) {
            if (!visited.add(definition.getFilename()))
                throw new IllegalArgumentException("Cyclic arenaContainer reference: " + visited);
            String reference = definition.getArenaContainer();
            if (reference == null || reference.isBlank()) return definition;
            var parent = com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig.getCustomArena(reference);
            if (parent == null)
                throw new IllegalArgumentException("Unknown arenaContainer '" + reference + "' in " + definition.getFilename());
            definition = parent;
        }
    }

    /**
     * Pulls the world-name token out of a ConfigurationLocation string of shape
     * {@code worldName,x,y,z[,yaw,pitch]}. Used to route pending arenas to the
     * right bucket without materialising a Location (whose world is null at
     * this point anyway).
     */
    private static String extractWorldName(String configurationLocationString) {
        if (configurationLocationString == null) return null;
        int comma = configurationLocationString.indexOf(',');
        if (comma <= 0) return null;
        return configurationLocationString.substring(0, comma).trim();
    }

    @Override
    protected boolean isInRegion(Location location) {
        return container.contains(location);
    }

    @Override
    public boolean isAcceptingNewPlayers() {
        return !resetting && super.isAcceptingNewPlayers() && container.availableTo(this);
    }

    @Override
    protected boolean reserveAdmission() { return container.acquire(this); }

    @Override
    protected void abortAdmission() {
        if (players.isEmpty() && spectators.isEmpty()) container.release(this);
    }

    @Override
    protected boolean isAcceptingSpectator(Player player, boolean wasPlayer) {
        return !isDefunct() && container.availableTo(this);
    }

    @Override
    public void removeSpectator(Player player) {
        super.removeSpectator(player);
        if (state == InstancedRegionState.WAITING) abortAdmission();
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
            victory();
            return;
        }

        int delayBetweenWaves = customArenasConfigFields.getDelayBetweenWaves();
        if (customArenasConfigFields.getIntermissionWaves().contains(currentWave)) {
            delayBetweenWaves *= 2;
        }

        long scheduledRun = runGeneration;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            if (scheduledRun != runGeneration || arenaState == ArenaState.IDLE) return;
            String title = ArenasConfig.getWaveTitle();
            String subtitle = ArenasConfig.getWaveSubtitle();
            if (title == null) title = "";
            if (subtitle == null) subtitle = "";

            String finalTitle = title;
            String finalSubtitle = subtitle;
            players.forEach(player -> player.sendTitle(finalTitle.replace("$wave", currentWave + ""), finalSubtitle.replace("$wave", currentWave + ""), 0, 20, 0));
            spectators.forEach(player -> player.sendTitle(finalTitle.replace("$wave", currentWave + ""), finalSubtitle.replace("$wave", currentWave + ""), 0, 20, 0));
            spawnBosses();
            arenaState = ArenaState.ACTIVE;
            roundDamage.clear();
        }, 20L * delayBetweenWaves);
    }

    private void arenaWatchdog() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arenaState != ArenaState.ACTIVE) return;
                for (CustomBossEntity customBossEntity : (HashSet<CustomBossEntity>) customBosses.clone())
                    if (!customBossEntity.exists()) removeBoss(customBossEntity);
                if (!nonEliteMobsEntities.isEmpty())
                    for (Entity entity : (HashSet<Entity>) nonEliteMobsEntities.clone())
                        if (!entity.isValid()) removeBoss(entity);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
    }

    public void removeBoss(CustomBossEntity customBossEntity) {
        customBosses.remove(customBossEntity);
        if (customBosses.isEmpty() && nonEliteMobsEntities.isEmpty()) nextWave();
    }

    public void removeBoss(Entity nonEliteEntity) {
        nonEliteMobsEntities.remove(nonEliteEntity);
        if (customBosses.isEmpty() && nonEliteMobsEntities.isEmpty()) nextWave();
    }

    private void spawnBosses() {
        if (arenaWaves.getWaveEntities(currentWave) == null) return;
        highestArenaMobLevel = -1;
        for (ArenaEntity arenaEntity : arenaWaves.getWaveEntities(currentWave)) {
            if (!arenaEntity.isMythicMob()) {
                CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(arenaEntity.getBossfile());
                if (customBossEntity == null) {
                    Logger.warn("Failed to generate custom boss " + arenaEntity.getBossfile() + " because the filename was not valid!");
                    continue;
                }
                customBossEntity.setNormalizedCombat();
                customBossEntity.setEliteLoot(false);
                customBossEntity.setVanillaLoot(false);
                customBossEntity.setRandomLoot(false);
                customBossEntity.spawn(container.spawnPoint(arenaEntity.getSpawnPointName()), true);
                if (customBossEntity.getLevel() > highestArenaMobLevel)
                    highestArenaMobLevel = customBossEntity.getLevel();
                if (!customBossEntity.exists()) {
                    Logger.warn("Arena " + getCustomArenasConfigFields().getArenaName() + " failed to spawn boss " + customBossEntity.getCustomBossesConfigFields().getFilename());
                    continue;
                } else customBosses.add(customBossEntity);

            } else {
                //MythicMobs integration
                try {
                    Entity mythicMob = MythicMobsInterface.spawn(container.spawnPoint(arenaEntity.getSpawnPointName()), arenaEntity.getBossfile(), arenaEntity.getLevel());
                    if (mythicMob != null) nonEliteMobsEntities.add(mythicMob);
                    else
                        Logger.warn("Failed to spawn MythicMobs entity '" + arenaEntity.getBossfile() + "' at spawn point " + arenaEntity.getSpawnPointName() + " with level " + arenaEntity.getLevel() + " because MythicMobs did not recognize the name of the entity!");
                } catch (Exception e) {
                    Logger.warn("Failed to spawn MythicMobs entity '" + arenaEntity.getBossfile() + "' at spawn point " + arenaEntity.getSpawnPointName() + " with level " + arenaEntity.getLevel() + " due to a MythicMobs error - there is a high chance mob spawning is being prevented in this area!");
                }
            }
        }
    }

    private void doRewards() {
        //Note: here current wave is decreased by 1 because the reward runs at the top of the next wave method.
        //So basically it runs at the start of the wave, hence it should reward whatever the previous wave had.
        ArrayList<Player> validPlayers = new ArrayList<>();
        double minimumDamageThreshold = 0;
        double totalDamage = 0;
        for (Map.Entry<Player, Double> entry : roundDamage.entrySet())
            totalDamage += entry.getValue();

        minimumDamageThreshold = totalDamage * .1;
        for (Map.Entry<Player, Double> entry : roundDamage.entrySet())
            if (entry.getValue() >= minimumDamageThreshold) validPlayers.add(entry.getKey());


        super.players.forEach(player -> {
            if (highestArenaMobLevel > 0) {
                // Guild rank loot limiter removed
                if (Math.abs(ElitePlayerInventory.getPlayer(player).getFullPlayerTier(true) - highestArenaMobLevel) > ItemSettingsConfig.getLootLevelDifferenceLockout()) {
                    Logger.sendSimpleMessage(player, ItemSettingsConfig.getLevelRangeTooDifferent()
                            .replace("$playerLevel", ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false) + "")
                            .replace("$bossLevel", highestArenaMobLevel + ""));
                    return;
                }
            }
            customArenasConfigFields.getArenaRewards().arenaReward(player, currentWave - 1);
        });
    }

    @Override
    protected void endMatch() {
        super.endMatch();
        arenaState = ArenaState.IDLE;
        //victory state
        if (currentWave > getCustomArenasConfigFields().getWaveCount()) {
            participants.forEach(player -> player.sendTitle(ArenasConfig.getVictoryTitle().replace("$wave", customArenasConfigFields.getWaveCount() + ""), ArenasConfig.getVictorySubtitle().replace("$wave", customArenasConfigFields.getWaveCount() + ""), 20, 20 * 10, 20));
            StringBuilder playerNames = new StringBuilder();
            for (Player player : participants)
                playerNames.append(player.getName()).append(" ");
            Bukkit.getServer().broadcastMessage(ArenasConfig.getVictoryBroadcast().replace("$players", playerNames.toString()).replace("$arenaName", customArenasConfigFields.getArenaName()));
            ArenaCompleteEvent.ArenaCompleteEventHandler.call(this);
        } else
            participants.forEach(player -> player.sendTitle(ArenasConfig.getDefeatTitle().replace("$wave", currentWave + ""), ArenasConfig.getDefeatSubtitle().replace("$wave", currentWave + ""), 20, 20 * 10, 20));

        long endingRun = runGeneration;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
            if (endingRun == runGeneration) destroyMatch();
        }, customArenasConfigFields.getDelayBetweenWaves());
    }

    private long runGeneration;
    private boolean resetting;

    @Override
    protected void destroyMatch() {
        if (resetting) return;
        resetting = true;
        runGeneration++;
        try {
        arenaState = ArenaState.IDLE;
        currentWave = 0;
        new HashSet<>(customBosses).forEach(customBoss -> customBoss.remove(RemovalReason.ARENA_RESET));
        customBosses.clear();
        nonEliteMobsEntities.forEach(Entity::remove);
        nonEliteMobsEntities.clear();
        super.destroyMatch();
        container.release(this);
        } finally {
            resetting = false;
        }
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

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onEliteDamages(EliteMobDamagedByPlayerEvent event) {
            if (PlayerData.getMatchInstance(event.getPlayer()) == null) return;
            if (!(PlayerData.getMatchInstance(event.getPlayer()) instanceof ArenaInstance arenaInstance)) return;
            arenaInstance.getRoundDamage().merge(event.getPlayer(), event.getDamage(), Double::sum);
        }
    }

    /**
     * Drains pendingArenas as worlds come online. Fixes the "Invalid arena
     * name!" symptom on first server start where arena worlds (Multiverse or
     * just slow startup) aren't loaded yet when EliteMobs's
     * {@code asyncInitialization} runs, and the arena was therefore silently
     * skipped instead of being registered.
     */
    public static class ArenaInstanceLoader implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onWorldLoad(WorldLoadEvent event) {
            String worldKey = event.getWorld().getName().toLowerCase(Locale.ROOT);
            List<CustomArenasConfigFields> pending = pendingArenas.remove(worldKey);
            if (pending == null || pending.isEmpty()) return;
            for (CustomArenasConfigFields fields : pending) {
                // initializeArena re-parses locations; the world is now resolvable,
                // so the second pass will succeed and the arena lands in arenaInstances.
                initializeArena(fields);
            }
        }
    }
}
