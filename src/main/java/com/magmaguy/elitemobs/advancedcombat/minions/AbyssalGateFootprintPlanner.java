package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Pure, bounded search for an all-clear two-by-three portal surface above solid ground. */
public final class AbyssalGateFootprintPlanner {
    private static final int[][] HORIZONTAL_SEARCH = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
    };
    private static final int[] VERTICAL_SEARCH = {0, -1, -2, -3, 1, 2};

    private AbyssalGateFootprintPlanner() {
    }

    public static Optional<AbyssalGateFootprint> find(
            GateBlockPosition aimed,
            GateAxis axis,
            Probe probe) {
        Objects.requireNonNull(aimed, "aimed");
        Objects.requireNonNull(axis, "axis");
        Objects.requireNonNull(probe, "probe");

        for (int[] horizontal : HORIZONTAL_SEARCH) {
            for (int vertical : VERTICAL_SEARCH) {
                GateBlockPosition bottom = aimed.offset(horizontal[0], vertical, horizontal[1]);
                List<GateBlockPosition> surface = surface(bottom, axis);
                if (!loaded(surface, probe) || !grounded(bottom, axis, probe)) continue;
                if (surface.stream().allMatch(probe::available)) {
                    return Optional.of(new AbyssalGateFootprint(axis, surface));
                }
            }
        }
        return Optional.empty();
    }

    private static List<GateBlockPosition> surface(GateBlockPosition bottom, GateAxis axis) {
        List<GateBlockPosition> blocks = new ArrayList<>(6);
        for (int y = 0; y < 3; y++) {
            blocks.add(bottom.offset(0, y, 0));
            blocks.add(axis == GateAxis.X
                    ? bottom.offset(1, y, 0)
                    : bottom.offset(0, y, 1));
        }
        return List.copyOf(blocks);
    }

    private static boolean grounded(GateBlockPosition bottom, GateAxis axis, Probe probe) {
        GateBlockPosition first = bottom.offset(0, -1, 0);
        GateBlockPosition second = axis == GateAxis.X
                ? bottom.offset(1, -1, 0)
                : bottom.offset(0, -1, 1);
        return probe.loaded(first) && probe.loaded(second)
                && probe.solid(first) && probe.solid(second);
    }

    private static boolean loaded(List<GateBlockPosition> surface, Probe probe) {
        return surface.stream().allMatch(probe::loaded);
    }

    public interface Probe {
        boolean loaded(GateBlockPosition position);

        boolean solid(GateBlockPosition position);

        /** True only for air or a Nether portal block already owned by another active gate. */
        boolean available(GateBlockPosition position);
    }
}
