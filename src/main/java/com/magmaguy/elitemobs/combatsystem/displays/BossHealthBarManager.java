package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Owns health boss-bar candidacy and the four reusable bar slots available to each player.
 * Combat display code registers candidates; this class alone decides visibility and owns
 * every Bukkit {@link BossBar} created for health display.
 */
final class BossHealthBarManager {

    private static final int MAX_VISIBLE_BARS_PER_PLAYER = 4;
    private static final double PROXIMITY_RANGE = 30.0;
    private static final double PROXIMITY_REMOVAL_RANGE = PROXIMITY_RANGE + 6.0;
    private static final double PROXIMITY_RANGE_SQUARED = PROXIMITY_RANGE * PROXIMITY_RANGE;
    private static final double PROXIMITY_REMOVAL_RANGE_SQUARED =
            PROXIMITY_REMOVAL_RANGE * PROXIMITY_REMOVAL_RANGE;

    private static final Map<UUID, BossCandidate> candidatesByBoss = new HashMap<>();
    private static final Map<UUID, PlayerBarPool> poolsByPlayer = new HashMap<>();

    private BossHealthBarManager() {
    }

    static void registerCombatCandidate(EliteEntity eliteEntity, Player player) {
        if (eliteEntity == null || !eliteEntity.isValid() || player == null || !player.isValid()) return;
        candidatesByBoss
                .computeIfAbsent(eliteEntity.getEliteUUID(), ignored -> new BossCandidate(eliteEntity))
                .playerUUIDs.add(player.getUniqueId());
    }

    static void updateProximityCandidates(EliteEntity eliteEntity, double healthMultiplier) {
        if (!MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier()) return;
        if (healthMultiplier < MobCombatSettingsConfig.getProximityBossBarHealthMultiplierThreshold()) return;
        if (eliteEntity == null || !eliteEntity.isValid()) return;

        Location bossLocation = eliteEntity.getLocation();
        LivingEntity livingEntity = eliteEntity.getLivingEntity();
        if (bossLocation == null || bossLocation.getWorld() == null || livingEntity == null) return;

        BossCandidate candidate = candidatesByBoss.computeIfAbsent(
                eliteEntity.getEliteUUID(), ignored -> new BossCandidate(eliteEntity));

        for (Entity entity : livingEntity.getNearbyEntities(PROXIMITY_RANGE, PROXIMITY_RANGE, PROXIMITY_RANGE)) {
            if (!(entity instanceof Player player)) continue;
            if (player.getLocation().distanceSquared(bossLocation) <= PROXIMITY_RANGE_SQUARED)
                candidate.playerUUIDs.add(player.getUniqueId());
        }

        // The wider removal range prevents repeated add/remove cycles at the 30-block edge.
        Iterator<UUID> iterator = candidate.playerUUIDs.iterator();
        while (iterator.hasNext()) {
            Player player = Bukkit.getPlayer(iterator.next());
            if (player == null || !player.isOnline() || !player.isValid() ||
                    !player.getWorld().equals(bossLocation.getWorld())) {
                iterator.remove();
                continue;
            }
            if (player.getLocation().distanceSquared(bossLocation) > PROXIMITY_REMOVAL_RANGE_SQUARED &&
                    !eliteEntity.getDamagers().containsKey(player))
                iterator.remove();
        }
    }

    static void removeBoss(EliteEntity eliteEntity) {
        if (eliteEntity == null) return;
        candidatesByBoss.remove(eliteEntity.getEliteUUID());
    }

    static void update() {
        Map<UUID, Player> candidatePlayers = new HashMap<>();
        Map<UUID, List<EliteEntity>> candidatesByPlayer = new HashMap<>();

        Iterator<BossCandidate> bossIterator = candidatesByBoss.values().iterator();
        while (bossIterator.hasNext()) {
            BossCandidate candidate = bossIterator.next();
            EliteEntity eliteEntity = candidate.eliteEntity;
            Location bossLocation = eliteEntity == null ? null : eliteEntity.getLocation();
            if (eliteEntity == null || !eliteEntity.isValid() || bossLocation == null ||
                    bossLocation.getWorld() == null) {
                bossIterator.remove();
                continue;
            }

            Iterator<UUID> playerIterator = candidate.playerUUIDs.iterator();
            while (playerIterator.hasNext()) {
                UUID playerUUID = playerIterator.next();
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline() || !player.isValid() ||
                        !player.getWorld().equals(bossLocation.getWorld())) {
                    playerIterator.remove();
                    continue;
                }
                candidatePlayers.put(playerUUID, player);
                candidatesByPlayer.computeIfAbsent(playerUUID, ignored -> new ArrayList<>()).add(eliteEntity);
            }
        }

        Set<UUID> updatedPlayers = new HashSet<>();
        for (Map.Entry<UUID, List<EliteEntity>> entry : candidatesByPlayer.entrySet()) {
            Player player = candidatePlayers.get(entry.getKey());
            List<EliteEntity> candidates = entry.getValue();
            candidates.sort(Comparator
                    .comparingDouble((EliteEntity eliteEntity) -> distanceSquared(player, eliteEntity))
                    .thenComparing(EliteEntity::getEliteUUID));

            int visibleCount = Math.min(MAX_VISIBLE_BARS_PER_PLAYER, candidates.size());
            poolsByPlayer
                    .computeIfAbsent(entry.getKey(), PlayerBarPool::new)
                    .update(player, candidates.subList(0, visibleCount));
            updatedPlayers.add(entry.getKey());
        }

        Iterator<Map.Entry<UUID, PlayerBarPool>> poolIterator = poolsByPlayer.entrySet().iterator();
        while (poolIterator.hasNext()) {
            Map.Entry<UUID, PlayerBarPool> entry = poolIterator.next();
            if (updatedPlayers.contains(entry.getKey())) continue;
            entry.getValue().cleanup(Bukkit.getPlayer(entry.getKey()));
            poolIterator.remove();
        }
    }

    static void shutdown() {
        poolsByPlayer.forEach((playerUUID, pool) -> {
            try {
                pool.cleanup(Bukkit.getPlayer(playerUUID));
            } catch (RuntimeException exception) {
                MetadataHandler.PLUGIN.getLogger().log(
                        Level.WARNING, "Failed to clean health boss bars for " + playerUUID, exception);
            }
        });
        poolsByPlayer.clear();
        candidatesByBoss.clear();
    }

    private static double distanceSquared(Player player, EliteEntity eliteEntity) {
        Location bossLocation = eliteEntity.getLocation();
        if (bossLocation == null || bossLocation.getWorld() == null ||
                !player.getWorld().equals(bossLocation.getWorld())) return Double.POSITIVE_INFINITY;
        return player.getLocation().distanceSquared(bossLocation);
    }

    private static BarColor colorFor(double healthRatio) {
        if (healthRatio > 0.75) return BarColor.GREEN;
        if (healthRatio > 0.50) return BarColor.YELLOW;
        return BarColor.RED;
    }

    private static final class BossCandidate {
        private final EliteEntity eliteEntity;
        private final Set<UUID> playerUUIDs = new HashSet<>();

        private BossCandidate(EliteEntity eliteEntity) {
            this.eliteEntity = eliteEntity;
        }
    }

    /** Fixed slots are updated in place, so changing candidates never creates bar churn. */
    private static final class PlayerBarPool {
        private final UUID playerUUID;
        private final List<BossBar> slots = new ArrayList<>(MAX_VISIBLE_BARS_PER_PLAYER);
        private int visibleSlots;

        private PlayerBarPool(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        private void update(Player player, List<EliteEntity> selectedBosses) {
            if (player == null || !player.isOnline() || !player.isValid()) return;

            for (int index = 0; index < selectedBosses.size(); index++) {
                BossBar bossBar = getOrCreateSlot(index);
                EliteEntity eliteEntity = selectedBosses.get(index);
                String name = eliteEntity.getName();
                double maxHealth = eliteEntity.getMaxHealth();
                double progress = maxHealth <= 0 ? 0 :
                        Math.max(0, Math.min(1, eliteEntity.getHealth() / maxHealth));

                bossBar.setTitle(name == null ? "Elite Boss" : name);
                bossBar.setProgress(progress);
                bossBar.setColor(colorFor(progress));

                if (index >= visibleSlots)
                    BossBarOrderManager.show(player, bossBar,
                            BossBarOrderManager.sortKeyFor(playerUUID) + index);
            }

            for (int index = selectedBosses.size(); index < visibleSlots; index++)
                BossBarOrderManager.hide(player, slots.get(index));
            visibleSlots = selectedBosses.size();
        }

        private BossBar getOrCreateSlot(int index) {
            if (index < slots.size()) return slots.get(index);
            BossBar bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_10);
            slots.add(bossBar);
            return bossBar;
        }

        private void cleanup(Player player) {
            for (BossBar bossBar : slots) {
                BossBarOrderManager.hide(player, bossBar);
                bossBar.removeAll();
            }
            slots.clear();
            visibleSlots = 0;
        }
    }
}
