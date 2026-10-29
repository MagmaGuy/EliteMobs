package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.magmacore.util.HealthBarText;

/** Shared numeric presentation rules for overhead health and transient combat displays. */
final class DisplayTextFormatter {

    private DisplayTextFormatter() {
    }

    static String number(double value) {
        return HealthBarText.number(value);
    }

    static String percentage(double ratio) {
        return HealthBarText.percentage(ratio);
    }
}
