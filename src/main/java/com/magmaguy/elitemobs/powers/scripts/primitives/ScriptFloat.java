package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptFloat {
    private Float value = null;
    private Float lowestRange = null;
    private Float highestRange = null;

    public ScriptFloat(float lowestRange, float highestRange) {
        this.lowestRange = lowestRange;
        this.highestRange = highestRange;
    }

    public ScriptFloat(float value) {
        this.value = value;
    }

    public boolean isRandom() {
        return value == null;
    }

    public Float getValue() {
        if (value != null) return value;
        return ThreadLocalRandom.current().nextFloat(lowestRange, highestRange+1);
    }

}
