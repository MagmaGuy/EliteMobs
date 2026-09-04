package com.magmaguy.elitemobs.api.power;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * EliteMobs-owned catalog and attachment interface for boss Lua powers.
 *
 * <p>Consumers obtain this service from Bukkit after
 * {@link com.magmaguy.elitemobs.api.EliteMobsInitializedEvent}. Mutations must run on the server
 * thread. Each attachment receives fresh Lua runtimes owned by the existing EliteMobs power
 * lifecycle.</p>
 *
 * <p>A registered script may implement {@code on_mind_action(context)}. The hook receives
 * {@code context.mind_action.key}, an immutable scalar {@code payload}, {@code game_tick}, and
 * {@code generation}, and returns {@code "accepted"}, {@code "deferred"}, or
 * {@code "rejected"}. Mind actions visit attached powers in exact attachment order and stop at
 * the first accepted result.</p>
 */
public interface EliteLuaPowerService {

    default EliteLuaPowerProgram registerLuaPower(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source) {
        return registerLuaPower(
                owner,
                key,
                revision,
                source,
                EliteLuaPowerType.MISCELLANEOUS,
                null);
    }

    EliteLuaPowerProgram registerLuaPower(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source,
            EliteLuaPowerType type,
            String effect);

    /**
     * Replaces the actor's complete power collection with fresh runtimes in the supplied order.
     * Every key must belong to {@code owner}; duplicate keys are rejected. Attachment order is
     * retained and breaks ties, while event dispatch first sorts by each program's ascending
     * {@link EliteLuaPowerProgram#executionPriority()}.
     */
    List<EliteLuaPowerProgram> setPowers(
            Plugin owner,
            EliteEntity eliteEntity,
            List<NamespacedKey> orderedPowerKeys);

    /** Returns service-managed programs in attachment order. */
    List<EliteLuaPowerProgram> inspectPowers(EliteEntity eliteEntity);

    /**
     * Returns canonical runtime activity for the actor's service-managed powers in attachment
     * order. This is live operational evidence, not a declaration inventory: counters advance
     * only when the attached MagmaCore runtime dispatches the corresponding hook.
     */
    List<EliteLuaPowerActivity> inspectPowerActivity(EliteEntity eliteEntity);

    /**
     * Adds an optional Java integration handler for one attached power and semantic action.
     * Multiple attached powers may register the same action key; handlers run in exact power
     * attachment order after Lua hooks have had the first opportunity to accept it.
     * The handler runs synchronously on the server thread and must perform or durably enqueue any
     * work before returning {@link ElitePowerActionResult#ACCEPTED}.
     */
    void registerActionHandler(
            Plugin owner,
            NamespacedKey powerKey,
            NamespacedKey actionKey,
            ElitePowerActionHandler handler);

    /** Unregisters the owner's catalog entries and detaches its runtimes from active actors. */
    void unregisterOwner(Plugin owner);
}
