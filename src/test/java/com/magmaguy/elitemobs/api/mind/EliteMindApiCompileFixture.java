package com.magmaguy.elitemobs.api.mind;

import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

/** Compile-only consumer fixture for module composition and inspection metadata. */
final class EliteMindApiCompileFixture {
    private EliteMindApiCompileFixture() {
    }

    static EliteMindProgram registerComposedProgram(EliteMindService service, Plugin owner) {
        NamespacedKey moduleKey = new NamespacedKey(owner, "mind/module/targeting");
        EliteMindModule module = service.registerLuaModule(
                owner,
                moduleKey,
                1L,
                "return ai.module { id = '" + moduleKey + "', revision = 1 }");
        if (!module.key().equals(moduleKey)) throw new IllegalStateException("fixture");

        NamespacedKey programKey = new NamespacedKey(owner, "mind/program/ground_melee");
        EliteMindProgram program = service.registerLuaProgram(
                owner,
                programKey,
                1L,
                "return ai.program { id = '" + programKey + "', revision = 1, "
                        + "modules = {'" + moduleKey + "'} }");
        program.resolvedModules();
        program.compositionFingerprint();
        return program;
    }

    static void inspectComposition(EliteMindService service, com.magmaguy.elitemobs.mobconstructor.EliteEntity actor) {
        EliteMindSnapshot paused = service.setPaused(actor, true);
        if (!paused.paused()) throw new IllegalStateException("fixture");
        service.setPaused(actor, false);
        service.inspect(actor).ifPresent(snapshot -> {
            snapshot.resolvedModules();
            snapshot.compositionFingerprint();
        });
    }

    static void registerNaturalMindReplacement(EliteMindService service, Plugin owner) {
        service.registerNaturalSpawnProvider(owner, context -> {
            if (context.spawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
                return Optional.empty();
            }
            return Optional.of(new EliteNaturalSpawnSuppression("compile fixture"));
        });
    }
}
