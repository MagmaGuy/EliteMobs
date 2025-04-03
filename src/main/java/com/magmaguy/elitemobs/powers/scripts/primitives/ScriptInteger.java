package com.magmaguy.elitemobs.powers.scripts.primitives;

import java.util.concurrent.ThreadLocalRandom;

public class ScriptInteger {
    private Integer value = null;
    private Integer lowestRange = null;
    private Integer highestRange = null;
    public ScriptInteger(int lowestRange, int highestRange) {
        this.lowestRange = lowestRange;
        this.highestRange = highestRange;
    }
    public ScriptInteger(int value) {
        this.value = value;
    }

    public boolean isRandom() {
        return value == null;
    }

    public Integer getValue() {
        if (value != null) return value;
        return ThreadLocalRandom.current().nextInt(lowestRange, highestRange+1);
    }
}
