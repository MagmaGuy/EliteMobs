package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.powers.meta.ElitePower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class PowerExecutionOrder {

    private PowerExecutionOrder() {
    }

    public static List<ElitePower> ordered(Collection<ElitePower> powers) {
        ArrayList<ElitePower> ordered = new ArrayList<>(powers);
        ordered.sort(Comparator.comparingInt(ElitePower::getExecutionPriority));
        return ordered;
    }
}
