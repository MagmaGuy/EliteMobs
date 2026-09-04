package com.magmaguy.elitemobs.mobconstructor.custombosses;

import java.util.Objects;
import java.util.function.Supplier;

final class BossTrackingLifecycle<T extends BossTrackingLifecycle.Handle> {
    interface Handle {
        void dispose();

        void notifyPlayers();
    }

    private final Runnable register;
    private final Runnable unregister;
    private final Supplier<T> handleFactory;
    private T current;

    BossTrackingLifecycle(Runnable register, Runnable unregister, Supplier<T> handleFactory) {
        this.register = Objects.requireNonNull(register, "register");
        this.unregister = Objects.requireNonNull(unregister, "unregister");
        this.handleFactory = Objects.requireNonNull(handleFactory, "handleFactory");
    }

    void activate(boolean notifyPlayers) {
        if (current != null) deactivate();
        register.run();
        try {
            current = Objects.requireNonNull(handleFactory.get(), "handleFactory returned null");
        } catch (RuntimeException | Error failure) {
            unregister.run();
            throw failure;
        }
        if (notifyPlayers) current.notifyPlayers();
    }

    void deactivate() {
        if (current != null) {
            T previous = current;
            current = null;
            previous.dispose();
        }
        unregister.run();
    }

    void deactivate(T expected) {
        if (expected == null) return;
        if (current == expected) {
            deactivate();
            return;
        }
        expected.dispose();
    }

    T current() {
        return current;
    }
}
