package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages animated XP progress boss bars for skills.
 * <p>
 * Features:
 * - Smooth animated progress bar that creeps up
 * - Particle and sound effects on level up
 * - Only one bar per skill per player (reuses existing bars)
 * - Auto-hides after inactivity
 */
public class SkillXPBar implements Listener {

    private static final int DISPLAY_DURATION_TICKS = 20 * 5; // 5 seconds
    private static final int ANIMATION_TICKS = 20; // 1 second animation
    private static final Map<UUID, Map<SkillType, SkillBarData>> playerBars = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeAllBars(event.getPlayer());
    }

    /**
     * Shows or updates the XP bar for a skill.
     *
     * @param player    The player
     * @param skillType The skill type
     * @param oldXP     XP before the gain
     * @param newXP     XP after the gain
     * @param xpGained  Amount of XP gained
     */
    public static void showXPGain(Player player, SkillType skillType, long oldXP, long newXP, long xpGained) {
        if (!SkillsConfig.isSkillSystemEnabled()) return;
        if (!SkillsConfig.isShowXPBar()) return;

        UUID uuid = player.getUniqueId();
        playerBars.computeIfAbsent(uuid, k -> new HashMap<>());

        Map<SkillType, SkillBarData> skillBars = playerBars.get(uuid);
        SkillBarData barData = skillBars.get(skillType);

        int oldLevel = SkillXPCalculator.levelFromTotalXP(oldXP);
        int newLevel = SkillXPCalculator.levelFromTotalXP(newXP);
        boolean leveledUp = newLevel > oldLevel;

        if (barData == null || !barData.isValid()) {
            // Create new bar
            barData = new SkillBarData(player, skillType);
            skillBars.put(skillType, barData);
        }

        // Update the bar with new XP info
        barData.updateProgress(oldXP, newXP, xpGained, leveledUp, newLevel);
    }

    /**
     * Removes all bars for a player.
     *
     * @param player The player
     */
    public static void removeAllBars(Player player) {
        Map<SkillType, SkillBarData> skillBars = playerBars.remove(player.getUniqueId());
        if (skillBars != null) {
            skillBars.values().forEach(SkillBarData::destroy);
        }
    }

    /**
     * Clears all bars.
     * Called on plugin shutdown.
     */
    public static void shutdown() {
        for (Map<SkillType, SkillBarData> skillBars : playerBars.values()) {
            skillBars.values().forEach(SkillBarData::destroy);
        }
        playerBars.clear();
    }

    /**
     * Data class for managing a single skill's XP bar.
     */
    private static class SkillBarData {
        private final UUID playerUUID;
        private final SkillType skillType;
        private final BossBar bossBar;
        private BukkitTask animationTask;
        private BukkitTask hideTask;
        private double currentDisplayProgress;
        private double targetProgress;
        private int displayLevel;

        SkillBarData(Player player, SkillType skillType) {
            this.playerUUID = player.getUniqueId();
            this.skillType = skillType;
            this.bossBar = Bukkit.createBossBar(
                    getBarTitle(skillType, 1, 0),
                    getBarColor(skillType),
                    BarStyle.SOLID
            );
            this.bossBar.addPlayer(player);
            this.bossBar.setVisible(true);
            this.currentDisplayProgress = 0;
            this.targetProgress = 0;
            this.displayLevel = 1;
        }

        private Player getPlayer() {
            return Bukkit.getPlayer(playerUUID);
        }

        boolean isValid() {
            Player player = getPlayer();
            return bossBar != null && player != null && player.isOnline();
        }

        void updateProgress(long oldXP, long newXP, long xpGained, boolean leveledUp, int newLevel) {
            Player player = getPlayer();
            if (player == null || !player.isOnline()) return;

            // Cancel existing tasks
            if (animationTask != null) {
                animationTask.cancel();
                animationTask = null;
            }
            if (hideTask != null) {
                hideTask.cancel();
                hideTask = null;
            }

            // Always remove and re-add player to ensure fresh reference
            bossBar.removeAll();
            bossBar.addPlayer(player);
            bossBar.setVisible(true);

            // Calculate progress values
            double oldProgress = SkillXPCalculator.levelProgress(oldXP);
            double newProgress = SkillXPCalculator.levelProgress(newXP);

            // Handle level up - animate from old progress to 1.0, then 0 to new progress
            if (leveledUp) {
                animateLevelUp(oldProgress, newProgress, newLevel, xpGained);
            } else {
                // Simple progress animation
                this.displayLevel = newLevel;
                this.currentDisplayProgress = oldProgress;
                this.targetProgress = newProgress;
                animateProgress(xpGained);
            }

            // Schedule hide
            scheduleHide();
        }

        private void animateLevelUp(double oldProgress, double newProgress, int newLevel, long xpGained) {
            int oldLevel = newLevel - 1;
            this.displayLevel = oldLevel;
            this.currentDisplayProgress = oldProgress;

            // First animate to full, then level up effects, then animate from 0
            animationTask = new BukkitRunnable() {
                int tick = 0;
                boolean filledBar = false;
                boolean showedLevelUp = false;

                @Override
                public void run() {
                    Player player = getPlayer();
                    if (player == null || !player.isOnline()) {
                        cancel();
                        return;
                    }

                    if (!filledBar) {
                        // Animate to 100%
                        double progress = currentDisplayProgress + (1.0 - currentDisplayProgress) * (tick / (double) (ANIMATION_TICKS / 2));
                        progress = Math.min(1.0, progress);
                        bossBar.setProgress(progress);
                        bossBar.setTitle(getBarTitle(skillType, displayLevel, xpGained));

                        if (tick >= ANIMATION_TICKS / 2) {
                            filledBar = true;
                            tick = 0;
                        }
                    } else if (!showedLevelUp) {
                        // Level up effects!
                        displayLevel = newLevel;
                        playLevelUpEffects();
                        bossBar.setProgress(0);
                        bossBar.setTitle(getLevelUpTitle(skillType, newLevel));
                        bossBar.setColor(BarColor.YELLOW);
                        showedLevelUp = true;
                        tick = 0;
                    } else {
                        // Animate from 0 to new progress
                        double progress = newProgress * (tick / (double) ANIMATION_TICKS);
                        progress = Math.min(newProgress, progress);
                        bossBar.setProgress(progress);
                        bossBar.setColor(getBarColor(skillType));
                        bossBar.setTitle(getBarTitle(skillType, newLevel, xpGained));

                        if (tick >= ANIMATION_TICKS) {
                            bossBar.setProgress(newProgress);
                            currentDisplayProgress = newProgress;
                            cancel();
                            return;
                        }
                    }
                    tick++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        }

        private void animateProgress(long xpGained) {
            final double startProgress = currentDisplayProgress;
            final double endProgress = targetProgress;

            animationTask = new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    Player player = getPlayer();
                    if (player == null || !player.isOnline()) {
                        cancel();
                        return;
                    }

                    double progress = startProgress + (endProgress - startProgress) * (tick / (double) ANIMATION_TICKS);
                    progress = Math.min(Math.max(0, progress), 1.0);

                    bossBar.setProgress(progress);
                    bossBar.setTitle(getBarTitle(skillType, displayLevel, xpGained));

                    if (tick >= ANIMATION_TICKS) {
                        bossBar.setProgress(endProgress);
                        currentDisplayProgress = endProgress;
                        cancel();
                        return;
                    }
                    tick++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        }

        private void playLevelUpEffects() {
            Player player = getPlayer();
            if (player == null || !player.isOnline()) return;

            // Play sound
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // Spawn particles around player
            new BukkitRunnable() {
                int tick = 0;
                double angle = 0;

                @Override
                public void run() {
                    Player p = getPlayer();
                    if (p == null || !p.isOnline() || tick > 20) {
                        cancel();
                        return;
                    }

                    // Spiral particles going up
                    for (int i = 0; i < 3; i++) {
                        double x = Math.cos(angle + i * 2.094) * 1.0;
                        double z = Math.sin(angle + i * 2.094) * 1.0;
                        double y = tick * 0.1;

                        p.getWorld().spawnParticle(
                                Particle.TOTEM_OF_UNDYING,
                                p.getLocation().add(x, y + 0.5, z),
                                1, 0, 0, 0, 0
                        );
                    }

                    // Burst particles at different heights
                    if (tick % 5 == 0) {
                        p.getWorld().spawnParticle(
                                Particle.HAPPY_VILLAGER,
                                p.getLocation().add(0, 1 + tick * 0.05, 0),
                                10, 0.5, 0.3, 0.5, 0
                        );
                    }

                    angle += 0.5;
                    tick++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
        }

        private void scheduleHide() {
            hideTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (bossBar != null) {
                        bossBar.setVisible(false);
                    }
                }
            }.runTaskLater(MetadataHandler.PLUGIN, DISPLAY_DURATION_TICKS);
        }

        void destroy() {
            if (animationTask != null) {
                animationTask.cancel();
            }
            if (hideTask != null) {
                hideTask.cancel();
            }
            if (bossBar != null) {
                bossBar.removeAll();
            }
        }

        private static String getBarTitle(SkillType skillType, int level, long xpGained) {
            String xpText = xpGained > 0 ? " &a(+" + formatNumber(xpGained) + " XP)" : "";
            return ChatColorConverter.convert("&6" + skillType.getDisplayName() + " &7Lv." + level + xpText);
        }

        private static String getLevelUpTitle(SkillType skillType, int newLevel) {
            return ChatColorConverter.convert("&e&l✦ " + skillType.getDisplayName() + " LEVEL UP! &7→ &eLv." + newLevel + " &e&l✦");
        }

        private static BarColor getBarColor(SkillType skillType) {
            return switch (skillType) {
                case ARMOR -> BarColor.BLUE;
                case SWORDS -> BarColor.RED;
                case AXES -> BarColor.RED;
                case BOWS -> BarColor.GREEN;
                case CROSSBOWS -> BarColor.GREEN;
                case TRIDENTS -> BarColor.BLUE;
                case HOES -> BarColor.PURPLE;
            };
        }

        private static String formatNumber(long number) {
            if (number >= 1_000_000) {
                return String.format("%.1fM", number / 1_000_000.0);
            } else if (number >= 1_000) {
                return String.format("%.1fK", number / 1_000.0);
            } else {
                return String.valueOf(number);
            }
        }
    }
}
