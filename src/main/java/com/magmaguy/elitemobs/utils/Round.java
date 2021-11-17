package com.magmaguy.elitemobs.utils;

public class Round {

    private Round() {
    }

    public static double twoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
