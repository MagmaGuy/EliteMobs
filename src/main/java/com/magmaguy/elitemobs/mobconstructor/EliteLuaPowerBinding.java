package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerActivity;
import com.magmaguy.elitemobs.api.power.ElitePowerActionRequest;
import com.magmaguy.elitemobs.api.power.ElitePowerActionResult;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Fresh per-actor Lua runtimes for one exact, owner-scoped power loadout. */
final class EliteLuaPowerBinding implements AutoCloseable {
    private final Plugin owner;
    private final List<AttachedPower> attachedPowers;
    private final ElitePowerPauseState pauseState = new ElitePowerPauseState();
    private boolean closed;

    EliteLuaPowerBinding(Plugin owner, List<AttachedPower> attachedPowers) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.attachedPowers = List.copyOf(attachedPowers);
    }

    boolean isOwnedBy(Plugin plugin) {
        return owner == plugin;
    }

    List<EliteLuaPowerProgram> descriptors() {
        return attachedPowers.stream()
                .map(attachedPower -> attachedPower.entry().descriptor())
                .toList();
    }

    List<EliteLuaPowerActivity> activity() {
        ArrayList<EliteLuaPowerActivity> result = new ArrayList<>(attachedPowers.size());
        for (AttachedPower attachedPower : attachedPowers) {
            result.add(attachedPower.power().activitySnapshot(attachedPower.entry().descriptor()));
        }
        return List.copyOf(result);
    }

    List<ElitePower> powers() {
        return attachedPowers.stream()
                .map(AttachedPower::power)
                .map(power -> (ElitePower) power)
                .toList();
    }

    List<NamespacedKey> powerKeys() {
        return attachedPowers.stream()
                .map(attachedPower -> attachedPower.entry().key())
                .toList();
    }

    boolean containsPower(NamespacedKey key) {
        for (AttachedPower attachedPower : attachedPowers) {
            if (attachedPower.entry().key().equals(key)) return true;
        }
        return false;
    }

    boolean matchesAttachmentOrder(List<ElitePower> currentPowers) {
        if (currentPowers.size() != attachedPowers.size()) return false;
        for (int index = 0; index < currentPowers.size(); index++) {
            if (currentPowers.get(index) != attachedPowers.get(index).power()) return false;
        }
        return true;
    }

    ElitePowerActionResult dispatchMindAction(ElitePowerActionRequest request) {
        if (closed || pauseState.isPaused()) return ElitePowerActionResult.REJECTED;
        boolean deferred = false;
        for (AttachedPower attachedPower : attachedPowers) {
            ElitePowerActionResult result = attachedPower.power().handleMindAction(request);
            if (result == ElitePowerActionResult.ACCEPTED) {
                return result;
            }
            deferred |= result == ElitePowerActionResult.DEFERRED;
        }
        return deferred ? ElitePowerActionResult.DEFERRED : ElitePowerActionResult.REJECTED;
    }

    void stopRuntimes() {
        RuntimeException failure = null;
        for (int index = attachedPowers.size() - 1; index >= 0; index--) {
            try {
                attachedPowers.get(index).power().closeRuntime();
            } catch (RuntimeException exception) {
                if (failure == null) failure = exception;
                else failure.addSuppressed(exception);
            }
        }
        if (failure != null) throw failure;
    }

    void restartRuntimes() {
        if (closed) throw new IllegalStateException("Lua power binding is closed");
        stopRuntimes();
        try {
            for (AttachedPower attachedPower : attachedPowers) {
                attachedPower.power().startRuntimeOrThrow();
            }
        } catch (RuntimeException exception) {
            try {
                stopRuntimes();
            } catch (RuntimeException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    void setPaused(boolean paused) {
        setPauseReason(ElitePowerPauseReason.MIND_SERVICE, paused);
    }

    void setPauseReason(ElitePowerPauseReason reason, boolean paused) {
        if (closed) throw new IllegalStateException("Lua power binding is closed");
        pauseState.set(reason, paused);
        for (AttachedPower attachedPower : attachedPowers) {
            attachedPower.power().setRuntimePauseReason(reason, paused);
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        stopRuntimes();
    }

    record AttachedPower(EliteLuaPowerCatalog.Entry entry, LuaElitePower power) {
        AttachedPower {
            Objects.requireNonNull(entry, "entry");
            Objects.requireNonNull(power, "power");
        }
    }
}
