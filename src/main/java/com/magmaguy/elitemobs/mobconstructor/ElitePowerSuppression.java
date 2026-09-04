package com.magmaguy.elitemobs.mobconstructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Actor-owned, independently releasable leases for temporary power suppression. */
public final class ElitePowerSuppression implements AutoCloseable {
    private final Map<ElitePowerPauseReason, Integer> leaseCounts =
            new EnumMap<>(ElitePowerPauseReason.class);
    private final TransitionListener transitionListener;
    private boolean closed;

    ElitePowerSuppression(TransitionListener transitionListener) {
        this.transitionListener = Objects.requireNonNull(transitionListener, "transitionListener");
    }

    public Lease acquire(ElitePowerPauseReason reason) {
        Objects.requireNonNull(reason, "reason");
        if (closed) throw new IllegalStateException("Elite power suppression is closed");
        int current = leaseCounts.getOrDefault(reason, 0);
        if (current == Integer.MAX_VALUE) {
            throw new IllegalStateException("Elite power suppression lease count is exhausted");
        }
        leaseCounts.put(reason, current + 1);
        if (current == 0) {
            try {
                transitionListener.changed(reason, true);
            } catch (RuntimeException exception) {
                leaseCounts.remove(reason);
                throw exception;
            }
        }
        return new LeaseImpl(reason);
    }

    public boolean isSuppressed() {
        return !leaseCounts.isEmpty();
    }

    public boolean isSuppressed(ElitePowerPauseReason reason) {
        return leaseCounts.containsKey(Objects.requireNonNull(reason, "reason"));
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        for (ElitePowerPauseReason reason : new ArrayList<>(leaseCounts.keySet())) {
            leaseCounts.remove(reason);
            try {
                transitionListener.changed(reason, false);
            } catch (RuntimeException exception) {
                if (failure == null) failure = exception;
                else failure.addSuppressed(exception);
            }
        }
        if (failure != null) throw failure;
    }

    @FunctionalInterface
    interface TransitionListener {
        void changed(ElitePowerPauseReason reason, boolean paused);
    }

    public interface Lease extends AutoCloseable {
        @Override
        void close();
    }

    private final class LeaseImpl implements Lease {
        private final ElitePowerPauseReason reason;
        private boolean released;

        private LeaseImpl(ElitePowerPauseReason reason) {
            this.reason = reason;
        }

        @Override
        public void close() {
            if (released) return;
            released = true;
            release(reason);
        }
    }

    private void release(ElitePowerPauseReason reason) {
        Integer current = leaseCounts.get(reason);
        if (current == null) return;
        if (current > 1) {
            leaseCounts.put(reason, current - 1);
            return;
        }
        leaseCounts.remove(reason);
        transitionListener.changed(reason, false);
    }
}
