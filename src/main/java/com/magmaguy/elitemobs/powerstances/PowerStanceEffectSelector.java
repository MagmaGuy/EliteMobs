package com.magmaguy.elitemobs.powerstances;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.meta.ElitePower;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks which powers get an orbiting item in the minor / major power stance rings.
 * <p>
 * This used to be an {@code instanceof MinorPower} / {@code instanceof MajorPower} check, but since the Lua migration
 * every power is a {@code LuaElitePower} or an {@code EliteScript} extending {@link ElitePower} directly, so those
 * abstract classes have no concrete subclasses left and the filter matched nothing. The configured
 * {@link PowersConfigFields.PowerType} is now the filter, which is the same classification
 * {@link EliteEntity#randomizePowers} uses when it counts minor and major powers, so the ring array widths and the
 * actual effect count agree.
 * <p>
 * Trails are read straight off the iterated power. Routing them through {@link EliteEntity#getPower(ElitePower)} would
 * match on {@code getClass().equals()}, and since every Lua power shares the {@code LuaElitePower} class every ring slot
 * would resolve to the first Lua power's trail - a ring of identical items.
 */
public final class PowerStanceEffectSelector {

    private PowerStanceEffectSelector() {
    }

    /**
     * @param elitePower Power to classify
     * @return Whether the power belongs to the major power ring
     */
    public static boolean isMajorPower(ElitePower elitePower) {
        if (elitePower == null) return false;
        return PowersConfigFields.isMajorPowerType(elitePower.getPowerType());
    }

    /**
     * Collects the visual trails of the powers belonging to one of the two rings, in power order.
     * <p>
     * Powers whose {@code effect} is null (custom Lua powers and a handful of premades) simply contribute no item,
     * exactly as null-effect powers always did.
     *
     * @param eliteEntity  Elite whose powers get scanned
     * @param majorPowers  True for the major power ring, false for the minor power ring
     * @return The trail strings, one per power that carries one
     */
    public static List<String> selectTrails(EliteEntity eliteEntity, boolean majorPowers) {
        List<String> trails = new ArrayList<>();
        if (eliteEntity == null) return trails;
        for (ElitePower elitePower : eliteEntity.getElitePowers()) {
            if (elitePower == null) continue;
            if (isMajorPower(elitePower) != majorPowers) continue;
            String trail = elitePower.getTrail();
            if (trail == null || trail.isEmpty()) continue;
            trails.add(trail);
        }
        return trails;
    }

}
