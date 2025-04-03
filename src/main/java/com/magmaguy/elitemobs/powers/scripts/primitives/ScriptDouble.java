package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptDouble {
    private Double value = null;
    private Double lowestRange = null;
    private Double highestRange = null;
    public ScriptDouble(double lowestRange, double highestRange) {
        this.lowestRange = lowestRange;
        this.highestRange = highestRange;
    }
    public ScriptDouble(double value) {
        this.value = value;
    }

    public boolean isRandom() {
        return value == null;
    }

    public Double getValue() {
        if (value != null) return value;
        return ThreadLocalRandom.current().nextDouble(lowestRange, highestRange+1);
    }
}
