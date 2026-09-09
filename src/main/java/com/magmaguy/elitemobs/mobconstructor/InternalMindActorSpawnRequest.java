package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.magmacore.ai.MindProgram;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * EliteMobs-internal request for one transient native actor with a fresh Java Mind program.
 * This deliberately does not broaden the public Lua Mind API.
 */
public record InternalMindActorSpawnRequest(
        Plugin systemOwner,
        UUID playerOwnerId,
        Location location,
        int level,
        EliteMindBodyProfile bodyProfile,
        MindProgram program,
        Consumer<EliteEntity> initializer,
        com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields customBoss) {

    public InternalMindActorSpawnRequest(Plugin owner, UUID player, Location location, int level,
                                         EliteMindBodyProfile profile, MindProgram program,
                                         Consumer<EliteEntity> initializer) {
        this(owner, player, location, level, profile, program, initializer, null);
    }

    public InternalMindActorSpawnRequest {
        Objects.requireNonNull(systemOwner, "systemOwner");
        Objects.requireNonNull(playerOwnerId, "playerOwnerId");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(bodyProfile, "bodyProfile");
        Objects.requireNonNull(program, "program");
        Objects.requireNonNull(initializer, "initializer");
        if (location.getWorld() == null) throw new IllegalArgumentException("location must have a world");
        if (level < 1) throw new IllegalArgumentException("level must be positive");
        NamespacedKey programKey = NamespacedKey.fromString(program.identifier());
        if (programKey == null) throw new IllegalArgumentException("program has an invalid Bukkit key");
        String ownerNamespace = new NamespacedKey(systemOwner, "ownership_probe").getNamespace();
        if (!ownerNamespace.equals(programKey.getNamespace())) {
            throw new IllegalArgumentException("program namespace does not belong to systemOwner");
        }
        location = location.clone();
    }

    @Override
    public Location location() {
        return location.clone();
    }
}
