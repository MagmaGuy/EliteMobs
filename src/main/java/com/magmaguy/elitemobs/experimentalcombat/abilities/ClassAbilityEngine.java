package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassLineage;
import org.bukkit.entity.Player;

/**
 * Executes fixed class mechanics. Resource checks and resource spending remain
 * outside this module so failed targeting never consumes either.
 */
public interface ClassAbilityEngine extends AutoCloseable {
    AbilityResult execute(Player player, ClassLineage lineage, AbilitySlot slot, int effectiveLevel);

    /** Cancels delayed mechanics and effects owned by this caster. Must run on the server thread. */
    void deactivate(Player player);

    @Override
    void close();
}
