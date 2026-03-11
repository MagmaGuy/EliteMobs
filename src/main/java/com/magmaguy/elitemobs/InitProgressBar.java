package com.magmaguy.elitemobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Displays a boss bar progress indicator to admins during plugin initialization.
 * Shows the current initialization step and overall progress.
 * Automatically adds admins who join mid-initialization.
 */
public class InitProgressBar implements Listener {

    private static final String PERMISSION = "elitemobs.*";
    private static BossBar bossBar;
    private static int totalSteps;
    private static int currentStep;
    private static InitProgressBar instance;

    /**
     * Creates and displays the initialization progress bar.
     * Must be called on the main thread.
     *
     * @param totalSteps the total number of initialization steps
     */
    public static void start(int totalSteps) {
        InitProgressBar.totalSteps = totalSteps;
        InitProgressBar.currentStep = 0;
        bossBar = Bukkit.createBossBar(
                ChatColor.GREEN + "" + ChatColor.BOLD + "EliteMobs " + ChatColor.WHITE + "▸ Initializing...",
                BarColor.GREEN,
                BarStyle.SEGMENTED_10);
        bossBar.setProgress(0);
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.hasPermission(PERMISSION))
                bossBar.addPlayer(player);
        instance = new InitProgressBar();
        Bukkit.getPluginManager().registerEvents(instance, MetadataHandler.PLUGIN);
    }

    /**
     * Advances the progress bar by one step with a description of the current work.
     * Safe to call from any thread — async calls are dispatched to the main thread.
     *
     * @param description what is currently being initialized
     */
    public static void step(String description) {
        currentStep++;
        double progress = Math.min((double) currentStep / totalSteps, 1.0);
        if (Bukkit.isPrimaryThread())
            updateBar(description, progress);
        else
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> updateBar(description, progress));
    }

    private static void updateBar(String description, double progress) {
        if (bossBar == null) return;
        bossBar.setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "EliteMobs " + ChatColor.WHITE + "▸ Initializing: " + ChatColor.YELLOW + description);
        bossBar.setProgress(progress);
    }

    /**
     * Removes the progress bar from all players and unregisters the join listener.
     */
    public static void complete() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        if (instance != null) {
            HandlerList.unregisterAll(instance);
            instance = null;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (bossBar != null && event.getPlayer().hasPermission(PERMISSION))
            bossBar.addPlayer(event.getPlayer());
    }
}
