package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptFloat {
    private Float value = null;
    private Float lowestRange = null;
    private Float highestRange = null;

    public ScriptFloat(float lowestRange, float highestRange) {
        if (!Float.isFinite(lowestRange) || !Float.isFinite(highestRange)
                || lowestRange > highestRange || !Float.isFinite(highestRange - lowestRange))
            throw new IllegalArgumentException("Invalid floating script range: " + lowestRange + "~" + highestRange);
        if (lowestRange == highestRange) this.value = lowestRange;
        this.lowestRange = lowestRange;
        this.highestRange = highestRange;
    }

    public ScriptFloat(float value) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Nonfinite script value: " + value);
        this.value = value;
    }

    public boolean isRandom() {
        return value == null;
    }

    public Float getValue() {
        if (value != null) return value;
        return ThreadLocalRandom.current().nextFloat(lowestRange, highestRange);
    }

}
