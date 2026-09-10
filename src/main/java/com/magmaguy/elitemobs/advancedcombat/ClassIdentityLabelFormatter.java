package com.magmaguy.elitemobs.advancedcombat;

/** Formats the class identity shown by the player overhead display. */
final class ClassIdentityLabelFormatter {

    private ClassIdentityLabelFormatter() {
    }

    static String format(String className, int effectiveLevel) {
        return "&f[" + effectiveLevel + "] &b" + className;
    }
}
