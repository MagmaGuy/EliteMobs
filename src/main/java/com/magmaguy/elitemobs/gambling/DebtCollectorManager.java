package com.magmaguy.elitemobs.gambling;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the Debt Collector boss that spawns for players who are in gambling debt.
 * The Debt Collector tracks down players who owe the casino money.
 */
// TODO: Review this class - needs changes to how messages appear and when the debt collector spawns
public class DebtCollectorManager implements Listener {

    private static final String DEBT_COLLECTOR_FILENAME = "debt_collector.yml";
    private static final Map<UUID, Long> lastSpawnTime = new HashMap<>();
    private static final Map<UUID, CustomBossEntity> activeDebtCollectors = new HashMap<>();
    // Track which boss is targeting which player
    private static final Map<UUID, UUID> bossToPlayerTarget = new HashMap<>();
    private static BukkitTask checkTask;

    /**
     * Initializes the Debt Collector system.
     */
    public static void initialize() {
        int checkIntervalMinutes = GamblingConfig.getDebtCollectorCheckIntervalMinutes();
        long checkIntervalTicks = 20L * 60L * checkIntervalMinutes;

        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkAndSpawnDebtCollector(player);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, checkIntervalTicks, checkIntervalTicks);
    }

    /**
     * Checks if a Debt Collector should spawn for a player and spawns one if conditions are met.
     */
    private static void checkAndSpawnDebtCollector(Player player) {
        UUID uuid = player.getUniqueId();

        // Must be in debt
        if (!PlayerData.hasGamblingDebt(uuid)) return;

        // Already has an active debt collector
        if (activeDebtCollectors.containsKey(uuid)) {
            CustomBossEntity existingBoss = activeDebtCollectors.get(uuid);
            if (existingBoss != null && existingBoss.exists()) {
                return; // Still active
            }
            // Clean up dead reference
            activeDebtCollectors.remove(uuid);
        }

        // Check cooldown
        long now = System.currentTimeMillis();
        Long lastSpawn = lastSpawnTime.get(uuid);
        if (lastSpawn != null) {
            long cooldownMs = GamblingConfig.getDebtCollectorCheckIntervalMinutes() * 60L * 1000L;
            if (now - lastSpawn < cooldownMs) return;
        }

        // Random chance
        if (ThreadLocalRandom.current().nextDouble() > GamblingConfig.getDebtCollectorSpawnChance()) return;

        // Spawn the Debt Collector
        spawnDebtCollector(player);
        lastSpawnTime.put(uuid, now);
    }

    /**
     * Spawns a Debt Collector for a specific player.
     */
    public static void spawnDebtCollector(Player player) {
        // Find spawn location near player
        Location spawnLoc = findSpawnLocation(player);
        if (spawnLoc == null) {
            spawnLoc = player.getLocation().add(
                    ThreadLocalRandom.current().nextDouble(-5, 5),
                    0,
                    ThreadLocalRandom.current().nextDouble(-5, 5)
            );
        }

        // Create custom boss
        CustomBossEntity boss = CustomBossEntity.createCustomBossEntity(DEBT_COLLECTOR_FILENAME);
        if (boss == null) {
            return; // Config not found
        }

        // Calculate level based on player's combat level or a minimum
        int playerLevel = Math.max(1, com.magmaguy.elitemobs.skills.CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
        boss.spawn(spawnLoc, playerLevel, false);

        if (!boss.exists()) {
            return; // Failed to spawn
        }

        UUID bossUUID = boss.getEliteUUID();
        activeDebtCollectors.put(player.getUniqueId(), boss);
        bossToPlayerTarget.put(bossUUID, player.getUniqueId());

        // Set target
        if (boss.getLivingEntity() != null && boss.getLivingEntity() instanceof org.bukkit.entity.Mob mob) {
            mob.setTarget(player);
        }

        // Send spawn message
        double debt = PlayerData.getGamblingDebt(player.getUniqueId());
        player.sendMessage(ChatColorConverter.convert(
                GamblingConfig.getDebtCollectorSpawnMessage()
                        .replace("$player", player.getName())
                        .replace("$amount", String.format("%.0f", debt))
        ));

        // Set up timeout
        int timeoutSeconds = GamblingConfig.getDebtCollectorTimeoutSeconds();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.exists()) {
                    // Timeout message
                    player.sendMessage(ChatColorConverter.convert(
                            GamblingConfig.getDebtCollectorTimeoutMessage()
                                    .replace("$player", player.getName())
                    ));
                    boss.remove(com.magmaguy.elitemobs.api.internal.RemovalReason.BOSS_TIMEOUT);
                    activeDebtCollectors.remove(player.getUniqueId());
                    bossToPlayerTarget.remove(bossUUID);
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, timeoutSeconds * 20L);
    }

    /**
     * Finds a suitable spawn location near the player.
     */
    private static Location findSpawnLocation(Player player) {
        Location playerLoc = player.getLocation();

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = ThreadLocalRandom.current().nextDouble(5, 10);
            double x = playerLoc.getX() + Math.cos(angle) * distance;
            double z = playerLoc.getZ() + Math.sin(angle) * distance;

            Location testLoc = new Location(playerLoc.getWorld(), x, playerLoc.getY(), z);

            // Find ground
            testLoc = testLoc.getWorld().getHighestBlockAt(testLoc).getLocation().add(0, 1, 0);

            // Check if valid spawn location
            if (testLoc.getBlock().isPassable() && testLoc.clone().add(0, 1, 0).getBlock().isPassable()) {
                return testLoc;
            }
        }

        return null; // Use fallback in caller
    }

    /**
     * Gets the Debt Collector targeting a specific player.
     */
    public static CustomBossEntity getDebtCollectorForPlayer(UUID playerUUID) {
        return activeDebtCollectors.get(playerUUID);
    }

    /**
     * Checks if a player has an active Debt Collector.
     */
    public static boolean hasActiveDebtCollector(UUID playerUUID) {
        CustomBossEntity boss = activeDebtCollectors.get(playerUUID);
        return boss != null && boss.exists();
    }

    /**
     * Shuts down the Debt Collector system.
     */
    public static void shutdown() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }

        // Remove all active debt collectors
        for (CustomBossEntity boss : activeDebtCollectors.values()) {
            if (boss != null && boss.exists()) {
                boss.remove(com.magmaguy.elitemobs.api.internal.RemovalReason.SHUTDOWN);
            }
        }

        activeDebtCollectors.clear();
        bossToPlayerTarget.clear();
        lastSpawnTime.clear();
    }

    /**
     * Handles when a Debt Collector is killed.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDebtCollectorDeath(EliteMobDeathEvent event) {
        EliteEntity eliteEntity = event.getEliteEntity();
        if (!(eliteEntity instanceof CustomBossEntity boss)) return;

        if (boss.getCustomBossesConfigFields() == null) return;
        if (!DEBT_COLLECTOR_FILENAME.equals(boss.getCustomBossesConfigFields().getFilename())) return;

        UUID bossUUID = boss.getEliteUUID();
        UUID targetPlayerUUID = bossToPlayerTarget.get(bossUUID);

        if (targetPlayerUUID != null) {
            Player targetPlayer = Bukkit.getPlayer(targetPlayerUUID);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColorConverter.convert(
                        GamblingConfig.getDebtCollectorDeathMessage()
                                .replace("$player", targetPlayer.getName())
                ));
            }
        }

        // Clean up
        activeDebtCollectors.values().removeIf(b -> b.equals(boss));
        bossToPlayerTarget.remove(bossUUID);
    }

    /**
     * Handles when a player is killed - check if it was by the Debt Collector.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageByEntityEvent lastDamage = player.getLastDamageCause() instanceof EntityDamageByEntityEvent ?
                (EntityDamageByEntityEvent) player.getLastDamageCause() : null;

        if (lastDamage == null) return;

        // Check if killed by an elite
        EliteEntity killer = EntityTracker.getEliteMobEntity(lastDamage.getDamager());
        if (!(killer instanceof CustomBossEntity killerBoss)) return;

        if (killerBoss.getCustomBossesConfigFields() == null) return;
        if (!DEBT_COLLECTOR_FILENAME.equals(killerBoss.getCustomBossesConfigFields().getFilename())) return;

        // Player was killed by the Debt Collector - reduce debt
        UUID uuid = player.getUniqueId();
        double debtReduction = GamblingConfig.getDebtReductionOnPlayerDeath();
        PlayerData.reduceGamblingDebt(uuid, debtReduction);

        double remainingDebt = PlayerData.getGamblingDebt(uuid);

        // Send message
        player.sendMessage(ChatColorConverter.convert(
                GamblingConfig.getDebtCollectorKillMessage()
                        .replace("$player", player.getName())
        ));

        if (remainingDebt > 0) {
            player.sendMessage(ChatColorConverter.convert(
                    GamblingConfig.getDebtPaidMessage()
                            .replace("$amount", String.format("%.0f", debtReduction))
                            .replace("$remaining", String.format("%.0f", remainingDebt))
            ));
        } else {
            player.sendMessage(ChatColorConverter.convert(
                    GamblingConfig.getDebtClearedMessage()
            ));
        }

        // Remove the debt collector after killing player
        UUID bossUUID = killerBoss.getEliteUUID();
        activeDebtCollectors.remove(uuid);
        bossToPlayerTarget.remove(bossUUID);

        // Remove the boss after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (killerBoss.exists()) {
                    killerBoss.remove(com.magmaguy.elitemobs.api.internal.RemovalReason.KILL_COMMAND);
                }
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 100L); // 5 seconds
    }
}
