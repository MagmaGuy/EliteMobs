package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Manages world operations (cloning, loading, unloading) in a queue to prevent
 * concurrent operations that can cause server crashes or performance issues.
 */
public class WorldOperationQueue {

    private static final Queue<WorldOperation> operationQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean isProcessing = new AtomicBoolean(false);

    /**
     * Queues a world operation for execution.
     *
     * @param player          The player initiating the operation (for feedback)
     * @param asyncOperation  The async operation (e.g., file cloning) - runs off main thread
     * @param syncOperation   The sync operation (e.g., world loading) - runs on main thread after async completes
     * @param operationName   Human-readable name for feedback messages
     */
    public static void queueOperation(Player player,
                                       Supplier<Boolean> asyncOperation,
                                       Runnable syncOperation,
                                       String operationName) {
        WorldOperation operation = new WorldOperation(player, asyncOperation, syncOperation, operationName);
        operationQueue.add(operation);

        int queuePosition = operationQueue.size();
        if (queuePosition > 1) {
            player.sendMessage("[EliteMobs] Preparing your dungeon... (Queue position: " + queuePosition + ")");
        } else {
            player.sendMessage("[EliteMobs] Preparing your dungeon...");
        }

        processNextOperation();
    }

    private static void processNextOperation() {
        if (!isProcessing.compareAndSet(false, true)) {
            return; // Another operation is already processing
        }

        WorldOperation operation = operationQueue.poll();
        if (operation == null) {
            isProcessing.set(false);
            return;
        }

        // Notify queued players of updated positions
        notifyQueuePositions();

        // Run async operation first
        CompletableFuture.supplyAsync(() -> {
            try {
                return operation.asyncOperation.get();
            } catch (Exception e) {
                Logger.warn("World operation failed during async phase: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }).thenAccept(success -> {
            if (!success) {
                if (operation.player.isOnline()) {
                    operation.player.sendMessage("[EliteMobs] Failed to prepare dungeon. Please try again.");
                }
                isProcessing.set(false);
                processNextOperation();
                return;
            }

            // Run sync operation on main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        operation.syncOperation.run();
                    } catch (Exception e) {
                        Logger.warn("World operation failed during sync phase: " + e.getMessage());
                        e.printStackTrace();
                        if (operation.player.isOnline()) {
                            operation.player.sendMessage("[EliteMobs] Failed to load dungeon. Please try again.");
                        }
                    } finally {
                        isProcessing.set(false);
                        // Process next operation after a short delay to let the server breathe
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                processNextOperation();
                            }
                        }.runTaskLater(MetadataHandler.PLUGIN, 10L); // 0.5 second delay between operations
                    }
                }
            }.runTask(MetadataHandler.PLUGIN);
        });
    }

    private static void notifyQueuePositions() {
        int position = 1;
        for (WorldOperation op : operationQueue) {
            if (op.player.isOnline()) {
                op.player.sendMessage("[EliteMobs] Preparing your dungeon... (Queue position: " + position + ")");
            }
            position++;
        }
    }

    /**
     * Gets the current queue size.
     */
    public static int getQueueSize() {
        return operationQueue.size() + (isProcessing.get() ? 1 : 0);
    }

    /**
     * Clears the queue (used during plugin shutdown).
     */
    public static void shutdown() {
        for (WorldOperation op : operationQueue) {
            if (op.player.isOnline()) {
                op.player.sendMessage("[EliteMobs] Dungeon preparation cancelled due to server shutdown.");
            }
        }
        operationQueue.clear();
    }

    private record WorldOperation(
            Player player,
            Supplier<Boolean> asyncOperation,
            Runnable syncOperation,
            String operationName
    ) {}
}
