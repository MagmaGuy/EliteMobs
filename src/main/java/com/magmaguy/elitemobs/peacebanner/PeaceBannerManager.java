package com.magmaguy.elitemobs.peacebanner;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import org.bukkit.scheduler.BukkitTask;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import java.util.*;

public class PeaceBannerManager {

    // World UUID -> (chunk key -> ref count)
    private static final HashMap<UUID, HashMap<Long, Integer>> protectedChunks = new HashMap<>();
    // Banner location key -> set of (worldUUID + chunk key) pairs encoded as strings
    private static final HashMap<String, Set<ChunkEntry>> bannerChunkMap = new HashMap<>();
    // Banner location key -> stored data (for persistence)
    private static final HashMap<String, BannerData> bannerDataMap = new HashMap<>();

    private static File dataFile;
    private static BukkitTask pendingSave;
    private static CompletableFuture<Void> writing;
    private static long revision;
    private static volatile long persistedRevision;
    private static volatile boolean closing;
    // This indexes source banners, unlike protectedChunks which indexes their entire coverage.
    private static final Map<ChunkEntry, Set<String>> sourceChunks = new HashMap<>();
    private static final Map<UUID, Set<String>> worldBanners = new HashMap<>();

    // --- Core API ---

    /**
     * Check if a location is in a protected chunk. O(1) lookup.
     */
    public static boolean isProtected(Location location) {
        if (!PeaceBannerConfig.isEnabled()) return false;
        if (location == null || location.getWorld() == null) return false;
        UUID worldUID = location.getWorld().getUID();
        HashMap<Long, Integer> worldChunks = protectedChunks.get(worldUID);
        if (worldChunks == null) return false;
        long chunkKey = chunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        return worldChunks.containsKey(chunkKey);
    }

    /**
     * Register a new peace banner at a block location.
     */
    public static void registerBanner(Location bannerLocation) {
        String locKey = locationKey(bannerLocation);
        if (bannerChunkMap.containsKey(locKey)) return; // Already registered

        BannerData data = new BannerData(bannerLocation.getWorld().getName(), bannerLocation.getWorld().getUID(),
                bannerLocation.getBlockX(), bannerLocation.getBlockY(), bannerLocation.getBlockZ(), PeaceBannerConfig.getChunkRadius());
        addRecord(locKey, data);
        activate(locKey, data);
        saveData();
    }

    /**
     * Unregister a peace banner. Decrements ref counts.
     *
     * @return true if a banner was actually unregistered, false if no banner was tracked at this location
     */
    public static boolean unregisterBanner(Location bannerLocation) {
        String locKey = locationKey(bannerLocation);
        if (!removeRecord(locKey)) return false;
        saveData();
        return true;
    }

    private static boolean removeRecord(String locKey) {
        BannerData data = bannerDataMap.remove(locKey);
        if (data == null) return false;
        Set<ChunkEntry> coverage = bannerChunkMap.remove(locKey);
        if (coverage != null) for (ChunkEntry entry : coverage) {
            HashMap<Long, Integer> chunks = protectedChunks.get(entry.worldUID());
            if (chunks == null) continue;
            chunks.computeIfPresent(entry.chunkKey(), (key, count) -> count <= 1 ? null : count - 1);
            if (chunks.isEmpty()) protectedChunks.remove(entry.worldUID());
        }
        ChunkEntry source = new ChunkEntry(data.worldUUID(), chunkKey(data.x() >> 4, data.z() >> 4));
        removeIndex(sourceChunks, source, locKey);
        removeIndex(worldBanners, data.worldUUID(), locKey);
        return true;
    }

    private static <K> void removeIndex(Map<K, Set<String>> index, K key, String location) {
        Set<String> entries = index.get(key);
        if (entries == null) return;
        entries.remove(location);
        if (entries.isEmpty()) index.remove(key);
    }

    private static void addRecord(String key, BannerData data) {
        if (bannerDataMap.putIfAbsent(key, data) != null) return;
        worldBanners.computeIfAbsent(data.worldUUID(), ignored -> new HashSet<>()).add(key);
        sourceChunks.computeIfAbsent(new ChunkEntry(data.worldUUID(), chunkKey(data.x() >> 4, data.z() >> 4)),
                ignored -> new HashSet<>()).add(key);
    }

    private static void activate(String key, BannerData data) {
        if (bannerChunkMap.containsKey(key)) return;
        Set<ChunkEntry> coverage = new HashSet<>();
        int radius = data.chunkRadius();
        int chunkX = data.x() >> 4, chunkZ = data.z() >> 4;
        HashMap<Long, Integer> chunks = protectedChunks.computeIfAbsent(data.worldUUID(), ignored -> new HashMap<>());
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
            long covered = chunkKey(chunkX + x, chunkZ + z);
            coverage.add(new ChunkEntry(data.worldUUID(), covered));
            chunks.merge(covered, 1, Integer::sum);
        }
        bannerChunkMap.put(key, coverage);
    }

    /** UUID owns persisted protection; a same-named replacement world does not inherit it. */
    public static void activateWorld(World world) {
        Set<String> keys = worldBanners.get(world.getUID());
        if (keys == null) return;
        Set<ChunkEntry> loadedSources = new HashSet<>();
        for (String key : Set.copyOf(keys)) {
            BannerData data = bannerDataMap.get(key);
            if (data == null) continue;
            activate(key, data);
            int x = data.x() >> 4, z = data.z() >> 4;
            if (world.isChunkLoaded(x, z)) loadedSources.add(new ChunkEntry(world.getUID(), chunkKey(x, z)));
        }
        // Spawn chunks can finish loading before WorldLoadEvent is delivered.
        for (ChunkEntry source : loadedSources)
            validateChunk(world.getChunkAt((int) (source.chunkKey() >> 32), (int) source.chunkKey()));
    }

    // --- Chunk key encoding ---

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static String locationKey(Location loc) {
        return loc.getWorld().getUID() + ":" + loc.getBlockX() + ":" +
                loc.getBlockY() + ":" + loc.getBlockZ();
    }

    // --- Persistence ---

    public static void loadData() {
        closing = false;
        if (dataFile != null && revision != persistedRevision) {
            MetadataHandler.PLUGIN.getLogger().warning("Retaining unsaved Peace Banner changes after failed shutdown flush.");
            for (World world : Bukkit.getWorlds()) activateWorld(world);
            saveData();
            return;
        }
        dataFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/peace-banners.yml");
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) continue;

            String worldName = section.getString("worldName");
            String worldUUIDString = section.getString("worldUUID");
            if (worldUUIDString == null) continue;
            UUID worldUUID;
            try {
                worldUUID = UUID.fromString(worldUUIDString);
            } catch (IllegalArgumentException e) {
                continue; // Skip corrupted entry
            }
            int x = section.getInt("x");
            int y = section.getInt("y");
            int z = section.getInt("z");
            int chunkRadius = section.getInt("chunkRadius", PeaceBannerConfig.getChunkRadius());

            BannerData data = new BannerData(worldName, worldUUID, x, y, z, chunkRadius);

            String location = worldUUID + ":" + x + ":" + y + ":" + z;
            addRecord(location, data);
            World world = Bukkit.getWorld(worldUUID);
            if (world != null) activate(location, data);
            else if (worldName != null && Bukkit.getWorld(worldName) != null)
                MetadataHandler.PLUGIN.getLogger().warning("Peace Banner at " + location
                        + " belongs to another world UUID; the same-named world was left unchanged.");
        }
    }

    public static void saveData() {
        if (dataFile == null || closing) return;
        revision++;
        queueSave();
    }

    private static void queueSave() {
        if (closing || pendingSave != null || writing != null) return;
        pendingSave = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            pendingSave = null;
            long capturedRevision = revision;
            List<BannerData> snapshot = List.copyOf(bannerDataMap.values());
            File target = dataFile;
            CompletableFuture<Void> completion = new CompletableFuture<>();
            writing = completion;
            try {
                Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
                    try {
                        writeSnapshot(snapshot, target);
                        persistedRevision = capturedRevision;
                        completion.complete(null);
                    } catch (RuntimeException failure) {
                        MetadataHandler.PLUGIN.getLogger().warning("Peace Banner changes remain unsaved: " + failure.getMessage());
                        completion.completeExceptionally(failure);
                    } finally {
                        if (!closing) try {
                            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                                if (writing != completion) return;
                                writing = null;
                                // A failed unchanged snapshot waits for another mutation or shutdown.
                                if (revision > capturedRevision) queueSave();
                            });
                        } catch (RuntimeException ignored) { /* shutdown flush owns the snapshot */ }
                    }
                });
            } catch (RuntimeException failure) {
                writing = null;
                completion.completeExceptionally(failure);
                MetadataHandler.PLUGIN.getLogger().warning("Could not schedule Peace Banner save: " + failure.getMessage());
            }
        }, 1L);
    }

    private static void writeSnapshot(List<BannerData> snapshot, File target) {
        YamlConfiguration yaml = new YamlConfiguration();
        int index = 0;
        for (BannerData data : snapshot) {
            String key = "banner_" + index++;
            yaml.set(key + ".worldName", data.worldName());
            yaml.set(key + ".worldUUID", data.worldUUID().toString());
            yaml.set(key + ".x", data.x());
            yaml.set(key + ".y", data.y());
            yaml.set(key + ".z", data.z());
            yaml.set(key + ".chunkRadius", data.chunkRadius());
        }
        ConfigurationEngine.fileSaverSerialized(yaml.saveToString(), target);
    }

    /**
     * Called on chunk load - validate banners in this chunk still exist.
     */
    public static void validateChunk(Chunk chunk) {
        ChunkEntry source = new ChunkEntry(chunk.getWorld().getUID(), chunkKey(chunk.getX(), chunk.getZ()));
        Set<String> keys = sourceChunks.get(source);
        if (keys == null) return;
        boolean changed = false;
        for (String key : Set.copyOf(keys)) {
            BannerData data = bannerDataMap.get(key);
            if (data == null) continue;
            Block block = chunk.getWorld().getBlockAt(data.x(), data.y(), data.z());
            if (!(block.getState() instanceof org.bukkit.block.Banner)) changed |= removeRecord(key);
        }
        if (changed) saveData();
    }

    /**
     * Returns the banner data map for admin list command.
     */
    public static Map<String, BannerData> getAllBannerData() {
        return Collections.unmodifiableMap(bannerDataMap);
    }

    /**
     * Returns total banner count.
     */
    public static int getBannerCount() {
        return bannerDataMap.size();
    }

    public static void shutdown() {
        closing = true;
        if (pendingSave != null) pendingSave.cancel();
        pendingSave = null;
        if (writing != null) {
            try { writing.join(); } catch (java.util.concurrent.CompletionException ignored) { /* retry latest below */ }
            writing = null;
        }
        if (dataFile != null && revision != persistedRevision) {
            try {
                writeSnapshot(List.copyOf(bannerDataMap.values()), dataFile);
                persistedRevision = revision;
            } catch (RuntimeException failure) {
                MetadataHandler.PLUGIN.getLogger().severe("Peace Banner shutdown save failed; retaining changes in memory: " + failure.getMessage());
                return;
            }
        }
        protectedChunks.clear();
        bannerChunkMap.clear();
        bannerDataMap.clear();
        sourceChunks.clear();
        worldBanners.clear();
    }

    // Inner record for chunk entries (world + chunk key pair)
    record ChunkEntry(UUID worldUID, long chunkKey) {
    }

    // Inner record for persistence
    public record BannerData(String worldName, UUID worldUUID, int x, int y, int z, int chunkRadius) {
    }
}
