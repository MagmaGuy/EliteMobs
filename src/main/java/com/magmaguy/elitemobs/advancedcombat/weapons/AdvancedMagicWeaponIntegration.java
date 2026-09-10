package com.magmaguy.elitemobs.advancedcombat.weapons;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;
import java.util.Optional;

/**
 * Optional-runtime adapter seam between [Alpha] Advanced Combat System and FMM magic weapons.
 *
 * <p>This class deliberately has no FMM type in its interface or implemented interfaces. An
 * EliteMobs server without FMM can load it safely, lock Spellcaster, and show one actionable
 * warning instead of failing class initialization.</p>
 */
public final class AdvancedMagicWeaponIntegration implements Listener, AutoCloseable {
    public static final String SPELLCASTER_ROOT_ID = "spellcaster";
    public static final String UNAVAILABLE_REASON =
            "Requires FreeMinecraftModels with its bundled staff and wand.";

    private static final String FMM_PLUGIN_NAME = "FreeMinecraftModels";
    private static final String MAGIC_SERVICE_CLASS =
            "com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponService";
    private static final long READINESS_WARNING_DELAY_TICKS = 600L;

    private final Plugin plugin;
    private volatile Connection connection;
    private BukkitTask readinessWarningTask;
    private volatile boolean started;
    private volatile boolean closed;
    private boolean warningSent;

    public AdvancedMagicWeaponIntegration(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public void start() {
        if (closed || started)
            throw new IllegalStateException("Magic weapon integration cannot start");
        started = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        connect(true);
    }

    public boolean isAvailable() {
        Connection active = connection;
        return started && !closed && active != null && active.isOperational();
    }

    public Optional<String> unavailableReason(String rootFormId) {
        if (!SPELLCASTER_ROOT_ID.equals(rootFormId) || isAvailable()) return Optional.empty();
        return Optional.of(UNAVAILABLE_REASON);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        if (!FMM_PLUGIN_NAME.equals(event.getPlugin().getName())) return;
        connect(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisabled(PluginDisableEvent event) {
        if (!FMM_PLUGIN_NAME.equals(event.getPlugin().getName())) return;
        cancelReadinessWarning();
        disconnect();
        warnUnavailable();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServiceRegistered(ServiceRegisterEvent event) {
        if (!MAGIC_SERVICE_CLASS.equals(event.getProvider().getService().getName())) return;
        connect(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServiceUnregistered(ServiceUnregisterEvent event) {
        if (!MAGIC_SERVICE_CLASS.equals(event.getProvider().getService().getName())) return;
        disconnect();
        scheduleReadinessWarning();
    }

    private synchronized void connect(boolean warnIfMissing) {
        if (!started || closed) return;
        Plugin fmm = Bukkit.getPluginManager().getPlugin(FMM_PLUGIN_NAME);
        if (fmm == null || !fmm.isEnabled()) {
            disconnect();
            if (warnIfMissing) warnUnavailable();
            return;
        }

        Connection active = connection;
        if (active != null && active.isOperational()) {
            onConnected();
            return;
        }
        disconnect();

        try {
            ConnectAttempt attempt = FmmMagicWeaponAdapter.connect(plugin);
            if (attempt.connection() != null) {
                connection = attempt.connection();
                onConnected();
                return;
            }
            switch (attempt.status()) {
                case WAITING_FOR_SERVICE -> scheduleReadinessWarning();
                case CONTENT_UNAVAILABLE -> cancelReadinessWarning();
                case INCOMPATIBLE, REGISTRATION_REJECTED -> warnUnavailable();
                case CONNECTED -> throw new IllegalStateException("Connected without an adapter");
            }
        } catch (LinkageError incompatibleFmm) {
            warnUnavailable();
        } catch (RuntimeException failure) {
            Logger.warn("Could not connect EliteMobs magic scaling to FreeMinecraftModels: "
                    + failure.getMessage());
            warnUnavailable();
        }
    }

    private void onConnected() {
        warningSent = false;
        cancelReadinessWarning();
    }

    private synchronized void disconnect() {
        Connection active = connection;
        connection = null;
        if (active == null) return;
        try {
            active.close();
        } catch (Exception ignored) {
            // The FMM service may already be gone during plugin disable.
        }
    }

    private synchronized void scheduleReadinessWarning() {
        if (closed || readinessWarningTask != null) return;
        readinessWarningTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            synchronized (AdvancedMagicWeaponIntegration.this) {
                readinessWarningTask = null;
            }
            connect(false);
            if (!isAvailable()) {
                cancelReadinessWarning();
                warnUnavailable();
            }
        }, READINESS_WARNING_DELAY_TICKS);
    }

    private synchronized void cancelReadinessWarning() {
        if (readinessWarningTask == null) return;
        readinessWarningTask.cancel();
        readinessWarningTask = null;
    }

    private synchronized void warnUnavailable() {
        if (warningSent || closed) return;
        warningSent = true;
        cancelReadinessWarning();
        Logger.warn("Spellcaster is locked. Install or update FreeMinecraftModels with its "
                + "bundled staff and wand, then restart the server.");
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        started = false;
        cancelReadinessWarning();
        disconnect();
        HandlerList.unregisterAll(this);
    }

    interface Connection extends AutoCloseable {
        boolean isOperational();

        @Override
        void close();
    }

    enum ConnectStatus {
        CONNECTED,
        WAITING_FOR_SERVICE,
        CONTENT_UNAVAILABLE,
        INCOMPATIBLE,
        REGISTRATION_REJECTED
    }

    record ConnectAttempt(ConnectStatus status, Connection connection) {
        ConnectAttempt {
            Objects.requireNonNull(status, "status");
            if ((status == ConnectStatus.CONNECTED) != (connection != null))
                throw new IllegalArgumentException("Only connected attempts carry an adapter");
        }

        static ConnectAttempt connected(Connection connection) {
            return new ConnectAttempt(ConnectStatus.CONNECTED,
                    Objects.requireNonNull(connection, "connection"));
        }

        static ConnectAttempt unavailable(ConnectStatus status) {
            return new ConnectAttempt(status, null);
        }
    }
}
