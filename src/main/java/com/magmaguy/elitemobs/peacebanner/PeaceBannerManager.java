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
import java.io.IOException;
import java.util.*;

public class PeaceBannerManager {

    // World UUID -> (chunk key -> ref count)
    private static final HashMap<UUID, HashMap<Long, Integer>> protectedChunks = new HashMap<>();
    // Banner location key -> set of (worldUUID + chunk key) pairs encoded as strings
    private static final HashMap<String, Set<ChunkEntry>> bannerChunkMap = new HashMap<>();
    // Banner location key -> stored data (for persistence)
    private static final HashMap<String, BannerData> bannerDataMap = new HashMap<>();

    private static File dataFile;

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

        int radius = PeaceBannerConfig.getChunkRadius();
        int bannerChunkX = bannerLocation.getBlockX() >> 4;
        int bannerChunkZ = bannerLocation.getBlockZ() >> 4;
        UUID worldUID = bannerLocation.getWorld().getUID();

        Set<ChunkEntry> chunkEntries = new HashSet<>();
        HashMap<Long, Integer> worldChunks = protectedChunks.computeIfAbsent(worldUID, k -> new HashMap<>());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                long key = chunkKey(bannerChunkX + dx, bannerChunkZ + dz);
                chunkEntries.add(new ChunkEntry(worldUID, key));
                worldChunks.merge(key, 1, Integer::sum);
            }
        }

        bannerChunkMap.put(locKey, chunkEntries);
        bannerDataMap.put(locKey, new BannerData(
                bannerLocation.getWorld().getName(),
                worldUID,
                bannerLocation.getBlockX(),
                bannerLocation.getBlockY(),
                bannerLocation.getBlockZ(),
                radius
        ));
        saveData();
    }

    /**
     * Unregister a peace banner. Decrements ref counts.
     *
     * @return true if a banner was actually unregistered, false if no banner was tracked at this location
     */
    public static boolean unregisterBanner(Location bannerLocation) {
        String locKey = locationKey(bannerLocation);
        Set<ChunkEntry> chunkEntries = bannerChunkMap.remove(locKey);
        if (chunkEntries == null) return false;

        for (ChunkEntry entry : chunkEntries) {
            HashMap<Long, Integer> worldChunks = protectedChunks.get(entry.worldUID());
            if (worldChunks == null) continue;
            int newCount = worldChunks.merge(entry.chunkKey(), -1, Integer::sum);
            if (newCount <= 0) worldChunks.remove(entry.chunkKey());
            if (worldChunks.isEmpty()) protectedChunks.remove(entry.worldUID());
        }

        bannerDataMap.remove(locKey);
        saveData();
        return true;
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

            // Find world - match by UUID first, fall back to name
            World world = Bukkit.getWorlds().stream()
                    .filter(w -> w.getUID().equals(worldUUID))
                    .findFirst()
                    .orElseGet(() -> Bukkit.getWorld(worldName));

            if (world == null) {
                // World not loaded yet - store data but don't register chunks
                // They'll be registered when the world loads
                String locKey = worldUUID + ":" + x + ":" + y + ":" + z;
                bannerDataMap.put(locKey, data);
                continue;
            }

            Location loc = new Location(world, x, y, z);
            String locKey = locationKey(loc);
            bannerDataMap.put(locKey, data);

            // Register chunk protection (don't validate block - wait for chunk load)
            int bannerChunkX = x >> 4;
            int bannerChunkZ = z >> 4;
            Set<ChunkEntry> chunkEntries = new HashSet<>();
            HashMap<Long, Integer> worldChunks = protectedChunks.computeIfAbsent(worldUUID, k -> new HashMap<>());

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    long ck = chunkKey(bannerChunkX + dx, bannerChunkZ + dz);
                    chunkEntries.add(new ChunkEntry(worldUUID, ck));
                    worldChunks.merge(ck, 1, Integer::sum);
                }
            }
            bannerChunkMap.put(locKey, chunkEntries);
        }
    }

    public static void saveData() {
        if (dataFile == null) return;
        dataFile.getParentFile().mkdirs();
        YamlConfiguration yaml = new YamlConfiguration();

        int index = 0;
        for (Map.Entry<String, BannerData> entry : bannerDataMap.entrySet()) {
            BannerData data = entry.getValue();
            String key = "banner_" + index++;
            yaml.set(key + ".worldName", data.worldName());
            yaml.set(key + ".worldUUID", data.worldUUID().toString());
            yaml.set(key + ".x", data.x());
            yaml.set(key + ".y", data.y());
            yaml.set(key + ".z", data.z());
            yaml.set(key + ".chunkRadius", data.chunkRadius());
        }

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called on chunk load - validate banners in this chunk still exist.
     */
    public static void validateChunk(Chunk chunk) {
        UUID worldUID = chunk.getWorld().getUID();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        // Find banners whose block is in this chunk
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, BannerData> entry : bannerDataMap.entrySet()) {
            BannerData data = entry.getValue();
            if (!data.worldUUID().equals(worldUID)) continue;
            if ((data.x() >> 4) != chunkX || (data.z() >> 4) != chunkZ) continue;

            // This banner's block is in the loading chunk - validate
            Block block = chunk.getWorld().getBlockAt(data.x(), data.y(), data.z());
            if (!block.getType().name().contains("BANNER")) {
                // Banner is gone - no replacement banner either
                toRemove.add(entry.getKey());
            }
            // If a banner block IS there, keep protection (could be a replacement peace banner)
        }

        for (String locKey : toRemove) {
            BannerData data = bannerDataMap.get(locKey);
            if (data == null) continue;
            World world = Bukkit.getWorlds().stream()
                    .filter(w -> w.getUID().equals(data.worldUUID()))
                    .findFirst().orElse(null);
            if (world != null) {
                unregisterBanner(new Location(world, data.x(), data.y(), data.z()));
            } else {
                // World gone - clean up maps directly
                Set<ChunkEntry> entries = bannerChunkMap.remove(locKey);
                if (entries != null) {
                    for (ChunkEntry entry : entries) {
                        HashMap<Long, Integer> worldChunks = protectedChunks.get(entry.worldUID());
                        if (worldChunks == null) continue;
                        int nc = worldChunks.merge(entry.chunkKey(), -1, Integer::sum);
                        if (nc <= 0) worldChunks.remove(entry.chunkKey());
                        if (worldChunks.isEmpty()) protectedChunks.remove(entry.worldUID());
                    }
                }
                bannerDataMap.remove(locKey);
            }
        }

        if (!toRemove.isEmpty()) saveData();
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
        saveData();
        protectedChunks.clear();
        bannerChunkMap.clear();
        bannerDataMap.clear();
    }

    // Inner record for chunk entries (world + chunk key pair)
    record ChunkEntry(UUID worldUID, long chunkKey) {
    }

    // Inner record for persistence
    public record BannerData(String worldName, UUID worldUUID, int x, int y, int z, int chunkRadius) {
    }
}
