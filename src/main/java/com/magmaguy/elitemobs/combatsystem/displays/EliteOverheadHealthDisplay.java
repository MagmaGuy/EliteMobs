package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.easyminecraftgoals.internal.PacketNameTagTeams;
import com.magmaguy.easyminecraftgoals.internal.StackedText;
import com.magmaguy.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Owns the overhead visual and numeric health display for one elite entity.
 * <p>
 * Java rows ride the elite's body whenever every Java viewer's client tracks it, so they move with the body's own
 * client-side smoothing instead of chasing it by teleports, which ran ahead of it. Bedrock rows keep teleporting.
 */
final class EliteOverheadHealthDisplay {

    private static final String FULL_BAR = "▌";
    private static final String EMPTY_BAR = "▌";
    private static final String COLOR_FULL_HIGH = "&a";
    private static final String COLOR_FULL_MED = "&e";
    private static final String COLOR_FULL_LOW = "&c";
    private static final String COLOR_FULL_CRITICAL = "&4";
    private static final String COLOR_EMPTY = "&8";
    private static final int BARS_PER_ROW = 10;
    private static final double VIEW_DISTANCE = 30;
    private static final int BACKGROUND_ARGB = Color.fromARGB(80, 0, 0, 0).asARGB();

    private final EliteEntity eliteEntity;
    // Rows top to bottom: the numeric health, then the bar rows. The bottom row sits at the base location.
    private final StackedText rows = new StackedText();
    private final Set<UUID> nameTagViewers = new HashSet<>();
    private final double healthMultiplier;
    private UUID nameTagSubject;
    private long lastCombatTime;
    private boolean dirty = true;

    EliteOverheadHealthDisplay(EliteEntity eliteEntity) {
        this.eliteEntity = eliteEntity;
        this.lastCombatTime = System.currentTimeMillis();
        this.healthMultiplier = eliteEntity instanceof CustomBossEntity customBoss
                ? customBoss.getHealthMultiplier()
                : eliteEntity.getHealthMultiplier();
        rows.setLineGap(0);
        rows.setBackgroundArgb(BACKGROUND_ARGB);
        rows.setShadow(true);
    }

    EliteEntity eliteEntity() {
        return eliteEntity;
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

    void markDirty() {
        dirty = true;
    }

    void refreshIfDirty() {
        if (!dirty || !isValid()) return;
        dirty = false;
        double currentHealth = eliteEntity.getHealth();
        double maxHealth = eliteEntity.getMaxHealth();
        List<String> lines = new ArrayList<>();
        if (MobCombatSettingsConfig.isDisplayNumericHealth()) lines.add(numericLine(currentHealth, maxHealth));
        if (MobCombatSettingsConfig.isDisplayVisualHealthBars()) lines.addAll(barLines(currentHealth, maxHealth));
        rows.setLines(lines);
    }

    /** Runs on the primary thread every tick. */
    void updatePositions() {
        if (!isValid()) return;
        Location baseLocation = getBaseLocation();
        if (baseLocation == null) return;
        LivingEntity body = eliteEntity.getLivingEntity();
        CustomModel model = eliteEntity instanceof CustomBossEntity customBoss ? customBoss.getCustomModel() : null;
        LivingEntity vehicle = ridingVehicle(body, model);
        rows.ride(vehicle);
        rows.move(baseLocation);
        List<Player> viewers = viewers(baseLocation, vehicle, model);
        rows.syncViewers(viewers);
        // Only a plain body shows its own vanilla name; a model's name is its own riding nameplate.
        reconcileNameTags(vehicle != null && model == null ? vehicle : null, viewers);
    }

    void cleanup() {
        rows.remove();
        releaseNameTags();
    }

    /**
     * The body the rows ride, or null to teleport them. A rider is only seated by clients that track the body:
     * every nearby client tracks a plain body, while a modeled body is shown only to the model's viewers, and only
     * while its bones ride it. A disguise seats riders at its own height, not the body's, so it keeps teleports.
     */
    private static LivingEntity ridingVehicle(LivingEntity body, CustomModel model) {
        if (body == null || !body.isValid() || DisguiseEntity.isDisguised(body)) return null;
        if (model == null) return body;
        return model.bonesRideEntity() ? body : null;
    }

    private static List<Player> viewers(Location base, LivingEntity vehicle, CustomModel model) {
        Set<UUID> modelViewers = vehicle != null && model != null ? model.getModelViewers() : null;
        List<Player> viewers = new ArrayList<>();
        for (Entity entity : base.getWorld().getNearbyEntities(base, VIEW_DISTANCE, VIEW_DISTANCE, VIEW_DISTANCE,
                entity -> entity instanceof Player)) {
            Player player = (Player) entity;
            if (player.getLocation().distanceSquared(base) > VIEW_DISTANCE * VIEW_DISTANCE) continue;
            // A Java rider on a body the client does not track would be left where it spawned.
            if (modelViewers != null && !BedrockChecker.isBedrock(player)
                    && !modelViewers.contains(player.getUniqueId())) continue;
            viewers.add(player);
        }
        return viewers;
    }

    /**
     * A rider hides a team-less mob's name tag. While the rows ride a plain body that has no real team on a Java
     * viewer's scoreboard, that viewer gets a client-only team that keeps the name tag as it was without the rider.
     */
    private void reconcileNameTags(LivingEntity subject, List<Player> viewers) {
        if (nameTagSubject != null && (subject == null || !nameTagSubject.equals(subject.getUniqueId())))
            releaseNameTags();
        if (subject == null) return;
        nameTagSubject = subject.getUniqueId();
        String entry = PacketNameTagTeams.scoreboardEntry(subject);
        Set<UUID> kept = new HashSet<>();
        for (Player viewer : viewers) {
            // Bedrock rows do not ride, and a real team already decides the name tag on that viewer's client.
            if (BedrockChecker.isBedrock(viewer) || viewer.getScoreboard().getEntryTeam(entry) != null) continue;
            PacketNameTagTeams.keep(viewer, subject);
            kept.add(viewer.getUniqueId());
        }
        for (UUID viewer : nameTagViewers)
            if (!kept.contains(viewer)) PacketNameTagTeams.release(Bukkit.getPlayer(viewer), nameTagSubject);
        nameTagViewers.clear();
        nameTagViewers.addAll(kept);
    }

    private void releaseNameTags() {
        if (nameTagSubject == null) return;
        for (UUID viewer : nameTagViewers) PacketNameTagTeams.release(Bukkit.getPlayer(viewer), nameTagSubject);
        nameTagViewers.clear();
        nameTagSubject = null;
    }

    /** Bar rows top to bottom; the bottom row fills first. */
    private List<String> barLines(double currentHealth, double maxHealth) {
        BarLayout layout = calculateBarLayout();
        double healthRatio = maxHealth <= 0 ? 0 : Math.max(0, Math.min(1, currentHealth / maxHealth));
        int filledBars = (int) Math.ceil(healthRatio * layout.totalBars());
        String filledColor = healthColor(healthRatio * 100);
        int barsRemaining = filledBars;
        int totalBarsRemaining = layout.totalBars();

        List<String> lines = new ArrayList<>();
        for (int row = 0; row < layout.rows(); row++) {
            int rowSize = Math.min(layout.barsPerRow(), totalBarsRemaining);
            int filledInRow = Math.min(rowSize, barsRemaining);
            StringBuilder text = new StringBuilder();
            for (int index = 0; index < filledInRow; index++) text.append(filledColor).append(FULL_BAR);
            for (int index = filledInRow; index < rowSize; index++) text.append(COLOR_EMPTY).append(EMPTY_BAR);
            lines.add(ChatColorConverter.convert(text.toString()));
            barsRemaining -= filledInRow;
            totalBarsRemaining -= rowSize;
        }
        Collections.reverse(lines);
        return lines;
    }

    private String numericLine(double currentHealth, double maxHealth) {
        double healthRatio = maxHealth <= 0 ? 0 : currentHealth / maxHealth;
        String color = healthColor(healthRatio * 100);
        String text = eliteEntity.isScaledCombat()
                ? color + "&l" + DisplayTextFormatter.percentage(healthRatio)
                : color + "&l" + DisplayTextFormatter.number(currentHealth) +
                MobCombatSettingsConfig.getHealthDisplaySeparator() + color + "&l" +
                DisplayTextFormatter.number(maxHealth);
        return ChatColorConverter.convert(text);
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

    private record BarLayout(int totalBars, int barsPerRow, int rows) {
    }
}
