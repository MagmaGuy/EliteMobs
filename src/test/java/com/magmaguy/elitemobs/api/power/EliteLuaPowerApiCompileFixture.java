package com.magmaguy.elitemobs.api.power;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.api.mind.EliteMindPowerLoadout;
import com.magmaguy.elitemobs.api.mind.EliteMindSpawnRequest;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.List;

/** Compile-only consumer fixture for the classloader-safe Lua power and Mind loadout surface. */
final class EliteLuaPowerApiCompileFixture {
    private EliteLuaPowerApiCompileFixture() {
    }

    static EliteMindSpawnRequest useApi(
            EliteLuaPowerService service,
            Plugin owner,
            EliteEntity actor,
            Location location) {
        NamespacedKey powerKey = new NamespacedKey(owner, "power/terrain_dig");
        NamespacedKey actionKey = new NamespacedKey(owner, "action/terrain_dig");
        service.registerLuaPower(
                owner,
                powerKey,
                1L,
                "return { api_version = 1, on_game_tick = function(context) end }",
                EliteLuaPowerType.OFFENSIVE,
                "CRIMSON_ROOTS");
        service.registerActionHandler(
                owner,
                powerKey,
                actionKey,
                request -> request.payload().containsKey("block")
                        ? ElitePowerActionResult.ACCEPTED
                        : ElitePowerActionResult.REJECTED);
        service.setPowers(owner, actor, List.of(powerKey));
        List<EliteLuaPowerProgram> attached = service.inspectPowers(actor);
        if (attached.size() != 1) throw new IllegalStateException("fixture");

        // Historical constructors remain source-compatible.
        new EliteMindSpawnRequest(owner, powerKey, location, 1, false);
        new EliteMindSpawnRequest(owner, powerKey, location, 1, false, EliteMindBodyProfile.GROUNDED);
        return new EliteMindSpawnRequest(
                owner,
                powerKey,
                location,
                1,
                false,
                EliteMindBodyProfile.GROUNDED,
                EliteMindPowerLoadout.exact(List.of(powerKey)));
    }
}
