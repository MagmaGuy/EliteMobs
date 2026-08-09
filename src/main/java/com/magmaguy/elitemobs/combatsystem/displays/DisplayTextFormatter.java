package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.magmacore.util.Round;

import java.util.Locale;

/** Shared numeric presentation rules for overhead health and transient combat displays. */
final class DisplayTextFormatter {

    private static final double THOUSAND = 1_000D;
    private static final double MILLION = 1_000_000D;
    private static final double BILLION = 1_000_000_000D;
    private static final double TRILLION = 1_000_000_000_000D;

    private DisplayTextFormatter() {
    }

    static String number(double value) {
        if (value < 0) return "-" + number(-value);
        if (value >= TRILLION) return Round.twoDecimalPlaces(value / TRILLION) + "T";
        if (value >= BILLION) return Round.twoDecimalPlaces(value / BILLION) + "B";
        if (value >= MILLION) return Round.twoDecimalPlaces(value / MILLION) + "M";
        if (value >= THOUSAND) return Round.twoDecimalPlaces(value / THOUSAND) + "K";
        return String.valueOf(Round.twoDecimalPlaces(value));
    }

    static String percentage(double ratio) {
        double percent = Math.max(0, ratio) * 100;
        if (percent >= 99.95) return "100%";
        if (percent >= 10) return Math.round(percent) + "%";
        if (percent >= 1) return Round.twoDecimalPlaces(percent) + "%";
        return String.format(Locale.US, "%.3f%%", percent);
    }
}
