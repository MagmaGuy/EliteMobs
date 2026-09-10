package com.magmaguy.elitemobs.experimentalcombat.input;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityResult;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import org.bukkit.entity.Player;

/**
 * Narrow boundary between Bukkit input events and the [Alpha] Advanced Combat System domain module.
 *
 * <p>The router owns only control interpretation. Eligibility, run-locked input selection,
 * resources and ability execution remain behind this interface.</p>
 */
public interface ClassAbilityInput {

    boolean hasActiveClass(Player player);

    /** Whether this client can express the F-key layer at all (Bedrock clients cannot). */
    boolean fLayerSupported(Player player);

    boolean controlsAlwaysAvailable(Player player);

    boolean outsideControlsAllowed();

    void onControlModeChanged(Player player);

    String abilityName(Player player, AbilitySlot slot);

    AbilityResult useAbility(Player player, AbilitySlot slot);
}
