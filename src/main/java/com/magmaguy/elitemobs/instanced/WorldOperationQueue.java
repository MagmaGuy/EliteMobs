package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Manages world operations (cloning, loading, unloading) in a queue to prevent
 * concurrent operations that can cause server crashes or performance issues.
 */
public class WorldOperationQueue {

    private static final Queue<WorldOperation> operationQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private static final AtomicInteger operationGeneration = new AtomicInteger(0);
    private static volatile WorldOperation activeOperation;

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
        queueOperation(player, asyncOperation, syncOperation, operationName, () -> {
        });
    }

    /**
     * Queues a world operation. Once accepted into the queue, {@code terminalCallback} is
     * invoked exactly once on success, failure, or shutdown cancellation.
     */
    public static void queueOperation(Player player,
                                      Supplier<Boolean> asyncOperation,
                                      Runnable syncOperation,
                                      String operationName,
                                      Runnable terminalCallback) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN,
                    () -> queueOperation(player, asyncOperation, syncOperation, operationName, terminalCallback));
            return;
        }

        WorldOperation operation = new WorldOperation(
                player, asyncOperation, syncOperation, operationName, terminalCallback,
                operationGeneration.get(), new AtomicBoolean(false));
        operationQueue.add(operation);

        int queuePosition = operationQueue.size() + (isProcessing.get() ? 1 : 0);
        if (queuePosition > 1) {
            player.sendMessage(DungeonsConfig.getDungeonPreparingQueueMessage().replace("$position", String.valueOf(queuePosition)));
        } else {
            player.sendMessage(DungeonsConfig.getDungeonPreparingMessage());
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
        activeOperation = operation;

        // Notify queued players of updated positions
        notifyQueuePositions();

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean success;
                try {
                    success = operation.asyncOperation.get();
                } catch (Exception e) {
                    Logger.warn("World operation '" + operation.operationName + "' failed during async phase: " + e.getMessage());
                    e.printStackTrace();
                    success = false;
                }

                if (operation.generation != operationGeneration.get() || MetadataHandler.shutdownRequested) return;
                boolean asyncSuccess = success;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        completeAsyncPhase(operation, asyncSuccess);
                    }
                }.runTask(MetadataHandler.PLUGIN);
            }
        }.runTaskAsynchronously(MetadataHandler.PLUGIN);
    }

    private static void completeAsyncPhase(WorldOperation operation, boolean success) {
            if (operation.generation != operationGeneration.get() || MetadataHandler.shutdownRequested) {
                //Do NOT release isProcessing here: a generation mismatch means this is a stale operation from a
                //previous plugin lifecycle (a reload bumped the generation). A newer-lifecycle operation may
                //currently own the processing slot, so clearing the flag would let a second world clone run
                //concurrently. shutdown() owns resetting isProcessing.
                return;
            }

            if (!success) {
                if (operation.player.isOnline()) {
                    operation.player.sendMessage(DungeonsConfig.getDungeonCopyFailedMessage());
                    operation.player.sendMessage(DungeonsConfig.getDungeonPrepareFailedMessage());
                }
                runTerminalCallback(operation);
                isProcessing.set(false);
                processNextOperation();
                return;
            }

            try {
                operation.syncOperation.run();
            } catch (Exception e) {
                Logger.warn("World operation '" + operation.operationName + "' failed during sync phase: " + e.getMessage());
                e.printStackTrace();
                if (operation.player.isOnline()) {
                    operation.player.sendMessage(DungeonsConfig.getDungeonLoadFailedMessage());
                }
            } finally {
                runTerminalCallback(operation);
                isProcessing.set(false);
                if (operation.generation != operationGeneration.get() || MetadataHandler.shutdownRequested) return;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (operation.generation != operationGeneration.get() || MetadataHandler.shutdownRequested) return;
                        processNextOperation();
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 10L);
            }
    }

    private static void notifyQueuePositions() {
        int position = 1;
        for (WorldOperation op : operationQueue) {
            if (op.player.isOnline()) {
                op.player.sendMessage(DungeonsConfig.getDungeonPreparingQueueMessage().replace("$position", String.valueOf(position)));
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
        operationGeneration.incrementAndGet();
        WorldOperation currentOperation = activeOperation;
        if (currentOperation != null) runTerminalCallback(currentOperation);
        for (WorldOperation op : operationQueue) {
            if (op.player.isOnline()) {
                op.player.sendMessage(DungeonsConfig.getDungeonCancelledShutdownMessage());
            }
            runTerminalCallback(op);
        }
        operationQueue.clear();
        activeOperation = null;
        isProcessing.set(false);
    }

    private record WorldOperation(
            Player player,
            Supplier<Boolean> asyncOperation,
            Runnable syncOperation,
            String operationName,
            Runnable terminalCallback,
            int generation,
            AtomicBoolean terminalized
    ) {}

    private static void runTerminalCallback(WorldOperation operation) {
        if (!operation.terminalized.compareAndSet(false, true)) return;
        try {
            operation.terminalCallback.run();
        } catch (RuntimeException exception) {
            Logger.warn("World operation '" + operation.operationName
                    + "' terminal callback failed: " + exception.getMessage());
        } finally {
            if (activeOperation == operation) activeOperation = null;
        }
    }
}
