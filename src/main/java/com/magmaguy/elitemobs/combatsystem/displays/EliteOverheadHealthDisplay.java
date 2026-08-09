package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Owns the overhead visual and numeric health display for one elite entity. */
final class EliteOverheadHealthDisplay {

    private static final String FULL_BAR = "▌";
    private static final String EMPTY_BAR = "▌";
    private static final String COLOR_FULL_HIGH = "&a";
    private static final String COLOR_FULL_MED = "&e";
    private static final String COLOR_FULL_LOW = "&c";
    private static final String COLOR_FULL_CRITICAL = "&4";
    private static final String COLOR_EMPTY = "&8";
    private static final int BARS_PER_ROW = 10;

    private final EliteEntity eliteEntity;
    private final List<FakeText> healthBarDisplays = new ArrayList<>();
    private final double healthMultiplier;
    private FakeText numericDisplay;
    private long lastCombatTime;

    EliteOverheadHealthDisplay(EliteEntity eliteEntity) {
        this.eliteEntity = eliteEntity;
        this.lastCombatTime = System.currentTimeMillis();
        this.healthMultiplier = eliteEntity instanceof CustomBossEntity customBoss
                ? customBoss.getHealthMultiplier()
                : eliteEntity.getHealthMultiplier();
    }

    EliteEntity eliteEntity() {
        return eliteEntity;
    }

    double healthMultiplier() {
        return healthMultiplier;
    }

    boolean isValid() {
        return eliteEntity != null && eliteEntity.isValid();
    }

    void resetCombatTimer() {
        lastCombatTime = System.currentTimeMillis();
    }

    boolean hasTimedOut() {
        long timeoutMillis = MobCombatSettingsConfig.getCombatDisplayTimeoutSeconds() * 1000L;
        return System.currentTimeMillis() - lastCombatTime > timeoutMillis;
    }

    void rebuild() {
        if (!isValid()) return;
        cleanupVisualDisplays();
        double currentHealth = eliteEntity.getHealth();
        double maxHealth = eliteEntity.getMaxHealth();
        if (MobCombatSettingsConfig.isDisplayVisualHealthBars()) createHealthBars(currentHealth, maxHealth);
        if (MobCombatSettingsConfig.isDisplayNumericHealth()) createNumericDisplay(currentHealth, maxHealth);
    }

    void updatePositions() {
        if (!isValid()) return;
        Location baseLocation = getBaseLocation();
        if (baseLocation == null) return;
        for (int index = 0; index < healthBarDisplays.size(); index++) {
            FakeText display = healthBarDisplays.get(index);
            if (display != null) display.teleport(baseLocation.clone().add(0, index * 0.22, 0));
        }
        if (numericDisplay != null) {
            int rows = MobCombatSettingsConfig.isDisplayVisualHealthBars() ? calculateBarLayout().rows() : 0;
            numericDisplay.teleport(baseLocation.clone().add(0, rows * 0.22, 0));
        }
    }

    void cleanup() {
        cleanupVisualDisplays();
    }

    private void createHealthBars(double currentHealth, double maxHealth) {
        Location baseLocation = getBaseLocation();
        if (baseLocation == null) return;

        BarLayout layout = calculateBarLayout();
        double healthRatio = maxHealth <= 0 ? 0 : Math.max(0, Math.min(1, currentHealth / maxHealth));
        int filledBars = (int) Math.ceil(healthRatio * layout.totalBars());
        String filledColor = healthColor(healthRatio * 100);
        int barsRemaining = filledBars;
        int totalBarsRemaining = layout.totalBars();

        for (int row = 0; row < layout.rows(); row++) {
            int rowSize = Math.min(layout.barsPerRow(), totalBarsRemaining);
            int filledInRow = Math.min(rowSize, barsRemaining);
            StringBuilder text = new StringBuilder();
            for (int index = 0; index < filledInRow; index++) text.append(filledColor).append(FULL_BAR);
            for (int index = filledInRow; index < rowSize; index++) text.append(COLOR_EMPTY).append(EMPTY_BAR);

            FakeText display = createFakeText(
                    baseLocation.clone().add(0, row * 0.22, 0),
                    ChatColorConverter.convert(text.toString()));
            if (display != null) healthBarDisplays.add(display);
            barsRemaining -= filledInRow;
            totalBarsRemaining -= rowSize;
        }
    }

    private void createNumericDisplay(double currentHealth, double maxHealth) {
        double healthRatio = maxHealth <= 0 ? 0 : currentHealth / maxHealth;
        String color = healthColor(healthRatio * 100);
        String text = eliteEntity.isScaledCombat()
                ? color + "&l" + DisplayTextFormatter.percentage(healthRatio)
                : color + "&l" + DisplayTextFormatter.number(currentHealth) +
                MobCombatSettingsConfig.getHealthDisplaySeparator() + color + "&l" +
                DisplayTextFormatter.number(maxHealth);
        Location baseLocation = getBaseLocation();
        if (baseLocation == null) return;

        int rows = MobCombatSettingsConfig.isDisplayVisualHealthBars() ? calculateBarLayout().rows() : 0;
        numericDisplay = createFakeText(
                baseLocation.clone().add(0, rows * 0.22, 0),
                ChatColorConverter.convert(text));
    }

    private BarLayout calculateBarLayout() {
        if (MobCombatSettingsConfig.isUseFixedHealthBarSize() || healthMultiplier <= 0)
            return new BarLayout(BARS_PER_ROW, BARS_PER_ROW, 1);
        if (healthMultiplier < 1) {
            int totalBars = Math.max(1, (int) Math.round(BARS_PER_ROW * healthMultiplier));
            return new BarLayout(totalBars, totalBars, 1);
        }
        if (healthMultiplier == 1) return new BarLayout(BARS_PER_ROW, BARS_PER_ROW, 1);

        double logScale = Math.log(healthMultiplier) / Math.log(20);
        int totalBars = Math.min((int) Math.ceil(BARS_PER_ROW + BARS_PER_ROW * logScale), 20);
        if (totalBars <= BARS_PER_ROW) return new BarLayout(totalBars, totalBars, 1);
        return new BarLayout(totalBars, (int) Math.ceil(totalBars / 2.0), 2);
    }

    private String healthColor(double healthPercent) {
        if (healthPercent > 75) return COLOR_FULL_HIGH;
        if (healthPercent > 50) return COLOR_FULL_MED;
        if (healthPercent > 25) return COLOR_FULL_LOW;
        return COLOR_FULL_CRITICAL;
    }

    private Location getBaseLocation() {
        if (!isValid()) return null;
        LivingEntity entity = eliteEntity.getLivingEntity();
        if (entity == null) return null;
        if (eliteEntity instanceof CustomBossEntity customBoss && customBoss.getCustomModel() != null) {
            Location nametagLocation = customBoss.getCustomModel().getNametagBoneLocation();
            if (nametagLocation != null) return nametagLocation.clone().subtract(0, .9, 0);
        }
        return entity.getLocation().clone().add(0, entity.getEyeHeight() + 0.8, 0);
    }

    private FakeText createFakeText(Location location, String text) {
        if (location == null || location.getWorld() == null) return null;
        FakeText display = VisualDisplay.createStyledFakeText(
                location, text, Color.fromARGB(80, 0, 0, 0), true, 1.0f);
        if (display == null) return null;
        for (Player player : location.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(location) <= 900) display.displayTo(player);
        return display;
    }

    private void cleanupVisualDisplays() {
        if (numericDisplay != null) {
            numericDisplay.remove();
            numericDisplay = null;
        }
        healthBarDisplays.forEach(display -> {
            if (display != null) display.remove();
        });
        healthBarDisplays.clear();
    }

    private record BarLayout(int totalBars, int barsPerRow, int rows) {
    }
}
