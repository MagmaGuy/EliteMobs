package com.magmaguy.elitemobs.mobconstructor.custombosses;

import java.util.Objects;
import java.util.function.Consumer;

final class SpawnLifecycle {
    enum Context {
        ANNOUNCED(true, true, true, true),
        RESTORED(true, false, false, true),
        SILENT(false, false, false, false);

        private final boolean tracking;
        private final boolean trackingNotification;
        private final boolean announcement;
        private final boolean escapeTask;

        Context(boolean tracking, boolean trackingNotification, boolean announcement, boolean escapeTask) {
            this.tracking = tracking;
            this.trackingNotification = trackingNotification;
            this.announcement = announcement;
            this.escapeTask = escapeTask;
        }
    }

    private SpawnLifecycle() {
    }

    static Context fromSilentFlag(boolean silent) {
        return silent ? Context.SILENT : Context.ANNOUNCED;
    }

    static void apply(Context context,
                      Consumer<Boolean> tracking,
                      Runnable announcement,
                      Runnable escapeTask) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(tracking, "tracking");
        Objects.requireNonNull(announcement, "announcement");
        Objects.requireNonNull(escapeTask, "escapeTask");
        if (context.tracking) tracking.accept(context.trackingNotification);
        if (context.announcement) announcement.run();
        if (context.escapeTask) escapeTask.run();
    }
}
