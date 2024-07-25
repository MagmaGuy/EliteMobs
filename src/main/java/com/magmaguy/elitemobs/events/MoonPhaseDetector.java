package com.magmaguy.elitemobs.events;

import com.magmaguy.magmacore.util.Logger;
import org.bukkit.World;

public class MoonPhaseDetector {

    public static MoonPhase detectMoonPhase(World eventWorld) {

        int days = (int) eventWorld.getFullTime() / 24000;
        int phase = Math.abs(days % 8);

        switch (phase) {

            case 0:
                return MoonPhase.FULL_MOON;
            case 1:
                return MoonPhase.WANING_GIBBOUS;
            case 2:
                return MoonPhase.LAST_QUARTER;
            case 3:
                return MoonPhase.WANING_CRESCENT;
            case 4:
                return MoonPhase.NEW_MOON;
            case 5:
                return MoonPhase.WAXING_CRESCENT;
            case 6:
                return MoonPhase.FIRST_QUARTER;
            case 7:
                return MoonPhase.WAXING_GIBBOUS;
            default:
                Logger.info("Unhandled moon phase. Phase " + phase + " was " + days + ". Defaulting to full moon...");
                return MoonPhase.FULL_MOON;

        }

    }

    public enum MoonPhase {
        FULL_MOON,
        WANING_GIBBOUS,
        LAST_QUARTER,
        WANING_CRESCENT,
        NEW_MOON,
        WAXING_CRESCENT,
        FIRST_QUARTER,
        WAXING_GIBBOUS
    }

}
