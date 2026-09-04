package com.magmaguy.elitemobs.skills;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthPercentagePreserverTest {

    @Test
    void restoresPercentageAfterAReplacementTemporarilyClampsHealth() {
        MutableHealthPool pool = new MutableHealthPool(34D, 68D);

        HealthPercentagePreserver.during(pool, () -> {
            pool.maximum = 20D;
            pool.current = Math.min(pool.current, pool.maximum);
            pool.maximum = 68D;
        });

        assertEquals(34D, pool.current, 1.0E-9);
    }

    @Test
    void rescalesCurrentHealthWhenTheFinalMaximumChanges() {
        MutableHealthPool pool = new MutableHealthPool(25D, 50D);

        HealthPercentagePreserver.during(pool, () -> {
            pool.maximum = 20D;
            pool.current = Math.min(pool.current, pool.maximum);
        });

        assertEquals(10D, pool.current, 1.0E-9);
    }

    @Test
    void deadPlayersAreNotRevivedByAttributeCleanup() {
        MutableHealthPool pool = new MutableHealthPool(0D, 68D);
        pool.dead = true;

        HealthPercentagePreserver.during(pool, () -> pool.maximum = 20D);

        assertEquals(0D, pool.current, 1.0E-9);
    }

    private static final class MutableHealthPool implements HealthPercentagePreserver.HealthPool {
        private double current;
        private double maximum;
        private boolean dead;

        private MutableHealthPool(double current, double maximum) {
            this.current = current;
            this.maximum = maximum;
        }

        @Override
        public double current() {
            return current;
        }

        @Override
        public double maximum() {
            return maximum;
        }

        @Override
        public boolean dead() {
            return dead;
        }

        @Override
        public void setCurrent(double health) {
            current = health;
        }
    }
}
