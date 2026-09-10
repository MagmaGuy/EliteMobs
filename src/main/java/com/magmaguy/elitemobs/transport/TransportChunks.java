package com.magmaguy.elitemobs.transport;

import com.magmaguy.magmacore.ai.route.CurvedRoute;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Bounded asynchronous loading of existing terrain; shared tickets are reference-counted. */
final class TransportChunks implements AutoCloseable {
    private final Plugin plugin;
    private final Map<Key, Entry> entries = new HashMap<>();
    private final Queue<PendingLoad> pending = new ArrayDeque<>();
    private final org.bukkit.scheduler.BukkitTask loader;
    private volatile boolean closed;
    TransportChunks(Plugin plugin) {
        this.plugin = plugin;
        loader = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Spigot has no async chunk API. Bound its fallback to one existing chunk per tick.
            PendingLoad load;
            do { load = pending.poll(); } while (load != null && load.result.isDone());
            if (load == null) return;
            try {
                Key key = load.key;
                if (Bukkit.getWorld(key.world.getUID()) != key.world || !key.world.loadChunk(key.x, key.z, false))
                    throw new IllegalStateException("Route terrain is unavailable");
                load.result.complete(key.world.getChunkAt(key.x, key.z));
            } catch (RuntimeException failure) { load.result.completeExceptionally(failure); }
        }, 1, 1);
    }

    /** Paper's optional API is discovered without introducing a Paper-only linkage into EM. */
    @SuppressWarnings("unchecked")
    CompletableFuture<Chunk> loadExisting(World world, int x, int z) {
        if (closed) return CompletableFuture.failedFuture(new IllegalStateException("Transport is stopping"));
        try {
            var method = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            return (CompletableFuture<Chunk>) method.invoke(world, x, z, false);
        } catch (NoSuchMethodException spigot) {
            if (pending.size() >= 512) return CompletableFuture.failedFuture(new IllegalStateException("Transport loading queue is full"));
            CompletableFuture<Chunk> result = new CompletableFuture<>();
            pending.add(new PendingLoad(new Key(world, x, z), result));
            return result;
        } catch (ReflectiveOperationException failure) { return CompletableFuture.failedFuture(failure); }
    }

    Lease acquire(World world, CurvedRoute route, TransportClearance.Footprint footprint) {
        if (closed) throw new IllegalStateException("Transport is stopping");
        Set<Key> keys = new HashSet<>();
        for (double d = 0; d < route.length() + .5; d += .5) {
            var p = route.at(Math.min(d, route.length()));
            int minX = (int) Math.floor(p.getX() - footprint.radius()) >> 4;
            int maxX = (int) Math.floor(p.getX() + footprint.radius()) >> 4;
            int minZ = (int) Math.floor(p.getZ() - footprint.radius()) >> 4;
            int maxZ = (int) Math.floor(p.getZ() + footprint.radius()) >> 4;
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++) {
                    keys.add(new Key(world, x, z));
                    if (keys.size() > 128)
                        throw new IllegalArgumentException("Route terrain budget exceeded; shorten the route or wait for another flight");
                }
        }
        if (keys.size() > 128 || entries.size() + keys.stream().filter(k -> !entries.containsKey(k)).count() > 256)
            throw new IllegalArgumentException("Route terrain budget exceeded; shorten the route or wait for another flight");
        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (Key key : keys) {
            Entry entry = entries.get(key);
            if (entry == null) {
                entry = new Entry(); entries.put(key, entry);
                entry.references++;
                load(key, entry);
            } else entry.references++;
            futures.add(entry.ready);
        }
        return new Lease(keys, CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)));
    }

    private void load(Key key, Entry entry) {
        try {
            entry.loading = loadExisting(key.world, key.x, key.z);
            entry.loading.whenComplete((chunk, error) -> {
                if (closed || !plugin.isEnabled()) { entry.ready.cancel(false); return; }
                try { Bukkit.getScheduler().runTask(plugin, () -> {
                    if (closed || entry.references == 0) { entry.ready.cancel(false); return; }
                    if (error != null || chunk == null) {
                        entry.ready.completeExceptionally(new IllegalStateException("Route terrain is unavailable", error));
                        return;
                    }
                    try {
                        if (Bukkit.getWorld(key.world.getUID()) != key.world)
                            throw new IllegalStateException("Route world was unloaded");
                        entry.ticket = key.world.addPluginChunkTicket(key.x, key.z, plugin);
                        entry.ready.complete(chunk);
                    } catch (RuntimeException failure) { entry.ready.completeExceptionally(failure); }
                }); } catch (RuntimeException stopping) { entry.ready.completeExceptionally(stopping); }
            });
        } catch (RuntimeException failure) { entry.ready.completeExceptionally(failure); }
    }

    final class Lease implements AutoCloseable {
        private final Set<Key> keys;
        final CompletableFuture<Void> ready;
        private boolean released;
        Lease(Set<Key> keys, CompletableFuture<Void> ready) { this.keys = keys; this.ready = ready; }
        @Override public void close() {
            if (released) return;
            released = true;
            for (Key key : keys) {
                Entry entry = entries.get(key);
                if (entry != null && --entry.references == 0) {
                    entries.remove(key);
                    if (entry.loading != null) entry.loading.cancel(false);
                    if (entry.ticket) key.world.removePluginChunkTicket(key.x, key.z, plugin);
                    entry.ready.cancel(false);
                }
            }
        }
    }
    @Override public void close() {
        closed = true;
        loader.cancel();
        pending.forEach(load -> load.result.cancel(false)); pending.clear();
        entries.forEach((key, entry) -> {
            if (entry.ticket) key.world.removePluginChunkTicket(key.x, key.z, plugin);
            if (entry.loading != null) entry.loading.cancel(false);
            entry.ready.cancel(false);
        });
        entries.clear();
    }
    private record Key(World world, int x, int z) {}
    private record PendingLoad(Key key, CompletableFuture<Chunk> result) {}
    private static final class Entry {
        int references;
        boolean ticket;
        CompletableFuture<Chunk> loading;
        final CompletableFuture<Chunk> ready = new CompletableFuture<>();
    }
}
