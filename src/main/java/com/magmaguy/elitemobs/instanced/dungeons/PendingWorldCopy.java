package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Supplier;

/** Owns an unpublished copy across worker completion and server-thread cancellation. */
final class PendingWorldCopy {
    private File output;
    private boolean cancelled;

    boolean copy(Supplier<File> copier) {
        synchronized (this) {
            if (cancelled) return false;
        }
        File copied = copier.get();
        if (copied == null) return false;
        synchronized (this) {
            if (!cancelled) {
                output = copied;
                return true;
            }
        }
        // Shutdown may have settled the purchase while this worker was still copying.
        // This output never reached native world loading, so cleanup needs no Bukkit API.
        discard(copied);
        return false;
    }

    synchronized boolean transferToInitializer() {
        if (cancelled || output == null) return false;
        output = null;
        return true;
    }

    void cancel() {
        File abandoned;
        synchronized (this) {
            cancelled = true;
            abandoned = output;
            output = null;
        }
        if (abandoned != null) discard(abandoned);
    }

    private static void discard(File copied) {
        Path root = copied.toPath();
        try {
            // Visit only the exact directory created by this copy, without following links.
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path directory, IOException failure) throws IOException {
                    if (failure != null) throw failure;
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException failure) {
            Logger.warn("Could not remove cancelled dungeon copy " + root + ": " + failure.getMessage());
        }
    }
}
