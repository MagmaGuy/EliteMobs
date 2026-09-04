package com.magmaguy.elitemobs.api.mind;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

/**
 * EliteMobs-owned interface for Lua-authored native mob minds.
 *
 * <p>Consumers obtain this interface from Bukkit's services manager after
 * {@link com.magmaguy.elitemobs.api.EliteMobsInitializedEvent}. All mutations must run on the
 * server thread. Registered Lua source is validated immediately, while each entity receives a
 * fresh executable program and Lua environment when it is bound.</p>
 */
public interface EliteMindService {

    /**
     * Registers or advances one owner-scoped Lua module before programs reference it.
     *
     * <p>The Lua module id and revision must match the supplied key and revision. Repeating the
     * exact identity and source is idempotent; changing source requires a strictly newer
     * revision. Dependencies are restricted to the same plugin namespace.</p>
     */
    EliteMindModule registerLuaModule(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source);

    /**
     * Registers or advances one owner-scoped Lua program.
     *
     * <p>The key namespace must belong to {@code owner}. The Lua program id and revision must
     * match the supplied key and revision. Repeating an identical registration is idempotent;
     * replacing source requires a strictly newer revision. Referenced modules must be registered
     * first, and the returned descriptor snapshots their exact transitive revisions and
     * composition fingerprint.</p>
     */
    EliteMindProgram registerLuaProgram(
            Plugin owner,
            NamespacedKey key,
            long revision,
            String source);

    /**
     * Registers one owner-scoped natural-spawn provider.
     *
     * <p>EliteMobs invokes providers only after its global natural-spawn exemptions and before
     * probabilistic conversion. A provider can leave a carrier alone, suppress it, or request an
     * exact native Mind and power loadout. Selection must not mutate the world because conflicting
     * claims fail closed.</p>
     */
    void registerNaturalSpawnProvider(Plugin owner, EliteNaturalSpawnProvider provider);

    /**
     * Closes the plugin's module and natural-spawn scopes, removes its programs, and removes
     * actors still running those programs.
     */
    void unregisterOwner(Plugin owner);

    /** Returns body features supported by the active native Minecraft adapter. */
    EliteMindBodyCapabilities bodyCapabilities();

    /**
     * Spawns an EliteMobs actor on MagmaCore's native mind body and binds the requested program.
     */
    EliteEntity spawn(EliteMindSpawnRequest request);

    /**
     * Replaces the program on an actor already backed by a MagmaCore mind body.
     *
     * <p>This does not convert arbitrary vanilla-backed elites. Program replacement commits at a
     * native mind safe point, so the returned snapshot may still describe the previous revision
     * until that boundary is reached.</p>
     */
    EliteMindSnapshot setProgram(
            EliteEntity eliteEntity,
            NamespacedKey key,
            EliteMindTransferPolicy transferPolicy);

    /**
     * Idempotently pauses or resumes an actor's native Mind and attached power runtimes.
     *
     * <p>A paused actor retains its entity, program, memory, and power VM state, but performs no
     * Mind ticks, semantic actions, Lua hooks, or power-owned callbacks until resumed. A request
     * made from inside a Mind tick commits at that tick's safe boundary.</p>
     */
    EliteMindSnapshot setPaused(EliteEntity eliteEntity, boolean paused);

    /**
     * Stops the program and terminally removes its native actor.
     *
     * <p>The current fixed native body has no vanilla fallback behavior, so leaving it behind
     * after clearing a program would create an inert, unbindable EliteEntity.</p>
     */
    boolean clearProgram(EliteEntity eliteEntity);

    /** Returns the current native runtime state when the actor has a mind binding. */
    Optional<EliteMindSnapshot> inspect(EliteEntity eliteEntity);
}
