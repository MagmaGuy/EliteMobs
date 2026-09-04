package com.magmaguy.elitemobs.skills;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

/** Runs a max-health mutation without changing the player's health percentage. */
public final class HealthPercentagePreserver {

    private HealthPercentagePreserver() {
    }

    public static void during(Player player, AttributeInstance maximumHealth, Runnable mutation) {
        during(new HealthPool() {
            @Override
            public double current() {
                return player.getHealth();
            }

            @Override
            public double maximum() {
                return maximumHealth.getValue();
            }

            @Override
            public boolean dead() {
                return player.isDead();
            }

            @Override
            public void setCurrent(double health) {
                player.setHealth(health);
            }
        }, mutation);
    }

    static void during(HealthPool healthPool, Runnable mutation) {
        double oldMaximum = positiveMaximum(healthPool.maximum());
        double oldHealth = healthPool.current();
        double healthRatio = Double.isFinite(oldHealth)
                ? Math.max(0D, Math.min(1D, oldHealth / oldMaximum))
                : 0D;

        mutation.run();

        if (healthPool.dead()) return;
        double newMaximum = positiveMaximum(healthPool.maximum());
        healthPool.setCurrent(Math.max(0.01D, Math.min(newMaximum, newMaximum * healthRatio)));
    }

    private static double positiveMaximum(double maximum) {
        return Double.isFinite(maximum) && maximum > 0D ? maximum : 1D;
    }

    interface HealthPool {
        double current();

        double maximum();

        boolean dead();

        void setCurrent(double health);
    }
}
