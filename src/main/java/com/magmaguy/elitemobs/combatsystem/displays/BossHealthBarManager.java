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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
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
 * Owns health boss-bar candidacy and the three reusable bar slots available to each player.
 * Combat display code registers candidates; this class alone decides visibility and owns
 * every Bukkit {@link BossBar} created for health display.
 * <p>
 * A boss that merely targets a player gives them a bar only while they have none, so walking past
 * several bosses never stacks bars. Further bars come from bosses the player is fighting: ones they
 * damaged, or that damaged them, within the combat display timeout.
 */
final class BossHealthBarManager {

    private static final int MAX_VISIBLE_BARS_PER_PLAYER = 3;

    private static final Map<UUID, BossCandidate> candidatesByBoss = new HashMap<>();
    private static final Map<UUID, PlayerBarPool> poolsByPlayer = new HashMap<>();

    private BossHealthBarManager() {
    }

    static void registerBoss(EliteEntity eliteEntity) {
        if (eliteEntity == null || !eliteEntity.isValid()) return;
        candidatesByBoss.computeIfAbsent(
                eliteEntity.getEliteUUID(), ignored -> new BossCandidate(eliteEntity));
    }

    static void removeBoss(EliteEntity eliteEntity) {
        if (eliteEntity == null) return;
        candidatesByBoss.remove(eliteEntity.getEliteUUID());
    }

    /** Records that the player damaged, or was damaged by, a boss that has a health bar candidacy. */
    static void markEngaged(EliteEntity eliteEntity, Player player) {
        if (eliteEntity == null || player == null) return;
        BossCandidate candidate = candidatesByBoss.get(eliteEntity.getEliteUUID());
        if (candidate != null) candidate.lastEngagedMillis.put(player.getUniqueId(), System.currentTimeMillis());
    }

    static void update() {
        Map<UUID, Player> candidatePlayers = new HashMap<>();
        Map<UUID, Map<UUID, Choice>> choicesByPlayer = new HashMap<>();
        long engagementCutoff = System.currentTimeMillis()
                - MobCombatSettingsConfig.getCombatDisplayTimeoutSeconds() * 1000L;

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
            if (!eliteEntity.isInCombat()) continue;

            Player aggroTarget = currentAggroTarget(eliteEntity);
            if (aggroTarget != null) {
                candidatePlayers.put(aggroTarget.getUniqueId(), aggroTarget);
                choicesByPlayer.computeIfAbsent(aggroTarget.getUniqueId(), ignored -> new HashMap<>())
                        .put(eliteEntity.getEliteUUID(), new Choice(eliteEntity, false));
            }

            candidate.lastEngagedMillis.values().removeIf(lastEngaged -> lastEngaged < engagementCutoff);
            for (UUID playerUUID : candidate.lastEngagedMillis.keySet()) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline() || player.isDead() || !player.isValid() ||
                        !player.getWorld().equals(bossLocation.getWorld())) continue;
                candidatePlayers.put(playerUUID, player);
                choicesByPlayer.computeIfAbsent(playerUUID, ignored -> new HashMap<>())
                        .put(eliteEntity.getEliteUUID(), new Choice(eliteEntity, true));
            }
        }

        Set<UUID> updatedPlayers = new HashSet<>();
        for (Map.Entry<UUID, Map<UUID, Choice>> entry : choicesByPlayer.entrySet()) {
            Player player = candidatePlayers.get(entry.getKey());
            PlayerBarPool pool = poolsByPlayer.computeIfAbsent(entry.getKey(), PlayerBarPool::new);
            pool.update(player, select(player, pool.shownBosses, entry.getValue()));
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

    static Player currentAggroTarget(EliteEntity eliteEntity) {
        if (eliteEntity == null || !eliteEntity.isValid() || !eliteEntity.isInCombat()) return null;
        LivingEntity livingEntity = eliteEntity.getLivingEntity();
        if (!(livingEntity instanceof Mob mob) || !(mob.getTarget() instanceof Player player)) return null;
        if (!player.isOnline() || player.isDead() || !player.isValid()) return null;
        if (!player.getWorld().equals(mob.getWorld())) return null;
        return player;
    }

    /**
     * Keeps the player's current bars in place while their bosses still qualify, then adds bosses the player is
     * fighting, nearest first, up to the cap. A boss that only targets the player is added only to an empty set.
     */
    private static List<EliteEntity> select(Player player, List<UUID> shownBosses, Map<UUID, Choice> choices) {
        List<EliteEntity> selected = new ArrayList<>();
        for (UUID bossUUID : shownBosses) {
            Choice kept = choices.remove(bossUUID);
            if (kept != null && selected.size() < MAX_VISIBLE_BARS_PER_PLAYER) selected.add(kept.eliteEntity());
        }
        Comparator<Choice> nearestFirst = Comparator
                .comparingDouble((Choice choice) -> distanceSquared(player, choice.eliteEntity()))
                .thenComparing(choice -> choice.eliteEntity().getEliteUUID());
        List<Choice> remaining = new ArrayList<>(choices.values());
        remaining.sort(nearestFirst);
        for (Choice choice : remaining) {
            if (selected.size() >= MAX_VISIBLE_BARS_PER_PLAYER) break;
            if (choice.engaged()) selected.add(choice.eliteEntity());
        }
        if (selected.isEmpty() && !remaining.isEmpty()) selected.add(remaining.get(0).eliteEntity());
        return selected;
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
        // Players who damaged this boss or were damaged by it, with the time of the latest hit either way.
        private final Map<UUID, Long> lastEngagedMillis = new HashMap<>();

        private BossCandidate(EliteEntity eliteEntity) {
            this.eliteEntity = eliteEntity;
        }
    }

    /** A boss a player may see a bar for; engaged when they are fighting it rather than only being targeted. */
    private record Choice(EliteEntity eliteEntity, boolean engaged) {
    }

    /** Fixed slots are updated in place, so changing candidates never creates bar churn. */
    private static final class PlayerBarPool {
        private final UUID playerUUID;
        private final List<BossBar> slots = new ArrayList<>(MAX_VISIBLE_BARS_PER_PLAYER);
        private List<UUID> shownBosses = List.of();
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

                bossBar.setTitle(name == null ? MobCombatSettingsConfig.getFallbackBossBarName() : name);
                bossBar.setProgress(progress);
                bossBar.setColor(colorFor(progress));

                if (index >= visibleSlots)
                    BossBarOrderManager.show(player, bossBar,
                            BossBarOrderManager.sortKeyFor(playerUUID) + index);
            }

            for (int index = selectedBosses.size(); index < visibleSlots; index++)
                BossBarOrderManager.hide(player, slots.get(index));
            visibleSlots = selectedBosses.size();
            shownBosses = selectedBosses.stream().map(EliteEntity::getEliteUUID).toList();
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
            shownBosses = List.of();
        }
    }
}
