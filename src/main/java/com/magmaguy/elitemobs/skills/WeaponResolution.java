package com.magmaguy.elitemobs.skills;

import org.bukkit.NamespacedKey;

/** Explicit resolution result; malformed or unknown explicit identities never fall back to material. */
public sealed interface WeaponResolution {
    record Resolved(WeaponIdentity identity) implements WeaponResolution {
    }

    record NotWeapon() implements WeaponResolution {
    }

    record InvalidExplicit(String storedValue, NamespacedKey parsedId) implements WeaponResolution {
    }
}
