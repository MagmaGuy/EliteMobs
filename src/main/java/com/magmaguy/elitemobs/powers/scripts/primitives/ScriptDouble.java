package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptDouble {
    private Double value = null;
    private Double lowestRange = null;
    private Double highestRange = null;
    public ScriptDouble(double lowestRange, double highestRange) {
        if (!Double.isFinite(lowestRange) || !Double.isFinite(highestRange)
                || lowestRange > highestRange || !Double.isFinite(highestRange - lowestRange))
            throw new IllegalArgumentException("Invalid floating script range: " + lowestRange + "~" + highestRange);
        if (lowestRange == highestRange) this.value = lowestRange;
        this.lowestRange = lowestRange;
        this.highestRange = highestRange;
    }
    public ScriptDouble(double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Nonfinite script value: " + value);
        this.value = value;
    }

    public boolean isRandom() {
        return value == null;
    }

    public Double getValue() {
        if (value != null) return value;
        return ThreadLocalRandom.current().nextDouble(lowestRange, highestRange);
    }
}
