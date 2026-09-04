package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindSnapshot;
import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.magmacore.ai.MindHandle;
import com.magmaguy.magmacore.ai.MindSnapshot;
import com.magmaguy.magmacore.ai.MobBody;
import com.magmaguy.magmacore.ai.StateTransfer;
import com.magmaguy.magmacore.ai.SwapReceipt;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/** One actor's direct native Mind binding. */
final class EliteMindBinding implements AutoCloseable {
    private final MindHandle handle;
    private EliteMindProgramEntry activeEntry;
    private EliteMindProgramEntry pendingEntry;
    private long pendingGeneration = -1L;
    private boolean closed;

    EliteMindBinding(
            EliteMindProgramEntry entry,
            MindHandle handle,
            MobBody body) {
        this.activeEntry = Objects.requireNonNull(entry, "entry");
        this.handle = Objects.requireNonNull(handle, "handle");
        attachBody(body);
    }

    boolean isOwnedBy(Plugin owner) {
        refreshCommittedEntry();
        return activeEntry.owner() == owner
                || pendingEntry != null && pendingEntry.owner() == owner;
    }

    void replace(EliteMindProgramEntry entry, StateTransfer transfer) {
        requireOpen();
        refreshCommittedEntry();
        Plugin expectedOwner = pendingEntry == null
                ? activeEntry.owner()
                : pendingEntry.owner();
        if (entry.owner() != expectedOwner) {
            throw new IllegalArgumentException(
                    "A Mind program cannot transfer actor ownership implicitly");
        }
        SwapReceipt receipt = handle.replace(
                entry.instantiate(),
                Objects.requireNonNull(transfer, "transfer"));
        if (receipt.queued()) {
            pendingEntry = entry;
            pendingGeneration = receipt.requestedGeneration();
        } else {
            activeEntry = entry;
            pendingEntry = null;
            pendingGeneration = -1L;
        }
    }

    Plugin ownerForGeneration(long generation) {
        requireOpen();
        if (generation < 1L) throw new IllegalArgumentException("generation must be positive");
        if (pendingEntry != null && generation >= pendingGeneration) return pendingEntry.owner();
        return activeEntry.owner();
    }

    void attachBody(MobBody body) {
        requireOpen();
        handle.attachBody(Objects.requireNonNull(body, "body"));
    }

    void suspendBody() {
        requireOpen();
        handle.suspendBody();
    }

    void setPaused(boolean paused) {
        requireOpen();
        handle.setPaused(paused);
    }

    EliteMindSnapshot snapshot(EliteEntity eliteEntity) {
        requireOpen();
        MindSnapshot snapshot = handle.inspect();
        refreshCommittedEntry(snapshot);
        NamespacedKey currentKey = NamespacedKey.fromString(snapshot.programIdentifier());
        if (currentKey == null) {
            throw new IllegalStateException("Native mind returned an invalid program key: "
                    + snapshot.programIdentifier());
        }
        EliteMindProgram program = activeEntry.descriptor();
        if (!program.key().equals(currentKey) || program.revision() != snapshot.programRevision()) {
            throw new IllegalStateException(
                    "Native mind inspection does not match the EliteMobs program catalog");
        }
        return new EliteMindSnapshot(
                eliteEntity.getEliteUUID(),
                currentKey,
                snapshot.programRevision(),
                snapshot.generation(),
                snapshot.activeBehaviors(),
                snapshot.bodyEntityId().isPresent(),
                snapshot.paused(),
                program.resolvedModules(),
                program.compositionFingerprint());
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try {
            handle.detachBody();
        } finally {
            handle.close();
        }
    }

    private void requireOpen() {
        if (closed) throw new IllegalStateException("Elite mind binding is closed");
    }

    private void refreshCommittedEntry() {
        if (pendingEntry != null) refreshCommittedEntry(handle.inspect());
    }

    private void refreshCommittedEntry(MindSnapshot snapshot) {
        if (pendingEntry == null || snapshot.generation() < pendingGeneration) return;
        activeEntry = pendingEntry;
        pendingEntry = null;
        pendingGeneration = -1L;
    }
}
