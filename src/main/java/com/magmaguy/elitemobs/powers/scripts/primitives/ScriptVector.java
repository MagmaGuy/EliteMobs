package com.magmaguy.elitemobs.powers.scripts.primitives;

import org.bukkit.util.Vector;

public class ScriptVector {
    private Vector value = null;
    private ScriptFloat x = null;
    private ScriptFloat y = null;
    private ScriptFloat z = null;

    public ScriptVector(ScriptFloat x, ScriptFloat y, ScriptFloat z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (!x.isRandom() && !y.isRandom() && !z.isRandom())
            value = new Vector(x.getValue(), y.getValue(), z.getValue());
    }

    public boolean isRandom() {
        return value == null;
    }

    public Vector getValue() {
        if (value != null) return value;
        return new Vector(x.getValue(), y.getValue(), z.getValue());
    }
}
