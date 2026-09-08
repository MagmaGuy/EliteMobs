package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.ExperimentalCombatConfig;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatRules;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Owns the single player-combat clock used by all dungeon combat features.
 */
public final class DungeonCombatRuntime implements Listener, PlayerCombatState {

    private static DungeonCombatRuntime instance;

    private final CombatSessionTracker combatSessions;
    private final List<PlayerCombatState.Listener> listeners = new CopyOnWriteArrayList<>();
    private final long foodRegenerationIntervalTicks;
    private long currentTick;
    private long nextFoodRegenerationTick;
    private BukkitTask updateTask;

    public DungeonCombatRuntime() {
        this(ExperimentalCombatConfig.isEnabled()
                        ? ExperimentalCombatRules.COMBAT_TIMEOUT_TICKS
                        : DungeonsConfig.getDungeonFoodRegenerationCombatTimeoutSeconds() * 20L,
                DungeonsConfig.getDungeonFoodRegenerationIntervalSeconds() * 20L);
    }

    public DungeonCombatRuntime(long combatTimeoutTicks, long foodRegenerationIntervalTicks) {
        if (instance != null)
            throw new IllegalStateException("Dungeon combat runtime is already initialized.");
        combatSessions = new CombatSessionTracker(combatTimeoutTicks);
        this.foodRegenerationIntervalTicks = foodRegenerationIntervalTicks;
        nextFoodRegenerationTick = foodRegenerationIntervalTicks;
        instance = this;
    }

    public static DungeonCombatRuntime getInstance() {
        if (instance == null)
            throw new IllegalStateException("Dungeon combat runtime is not initialized.");
        return instance;
    }

    public static void shutdownIfInitialized() {
        if (instance != null) instance.shutdown();
    }

    public static boolean isEligiblePlayer(Player player) {
        return DungeonFoodRegeneration.isEligibleDungeonPlayer(player);
    }

    public static boolean isInManagedCombatWorld(Player player) {
        return DungeonFoodRegeneration.isInEligibleCombatContent(player);
    }

    public void start() {
        if (updateTask != null)
            throw new IllegalStateException("Dungeon combat runtime is already running.");
        updateTask = Bukkit.getScheduler().runTaskTimer(
                MetadataHandler.PLUGIN, this::tick, 1L, 1L);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        HandlerList.unregisterAll(this);
        listeners.clear();
        if (instance == this) instance = null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnemyCombatDamage(EntityDamageByEntityEvent event) {
        double absorptionModifier = getAbsorptionDamageModifier(event);
        if (!DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(
                event.getFinalDamage(), absorptionModifier)) return;

        Player player = CombatParticipantResolver.resolveEnemyCombatPlayer(event);
        if (player == null || !DungeonFoodRegeneration.isEligibleDungeonPlayer(player)) return;

        if (combatSessions.recordDamage(player.getUniqueId(), currentTick) ==
                CombatSessionTracker.DamageResult.STARTED_COMBAT) {
            for (PlayerCombatState.Listener listener : listeners)
                listener.onCombatStarted(player.getUniqueId());
        }
    }

    private void tick() {
        currentTick++;

        for (UUID playerId : combatSessions.expire(currentTick))
            for (PlayerCombatState.Listener listener : listeners) listener.onCombatEnded(playerId);

        if (!DungeonsConfig.isEnableDungeonFoodRegeneration()) return;
        if (ExperimentalCombatConfig.isEnabled()) return;
        if (currentTick < nextFoodRegenerationTick) return;
        nextFoodRegenerationTick = currentTick + foodRegenerationIntervalTicks;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!DungeonFoodRegeneration.shouldRegenerateFood(
                    player, combatSessions.isInCombat(player.getUniqueId()))) continue;
            player.addPotionEffect(DungeonFoodRegeneration.createFoodRegenerationEffect());
        }
    }

    @Override
    public boolean isInCombat(UUID playerId) {
        return combatSessions.isInCombat(playerId);
    }

    @Override
    public void addListener(PlayerCombatState.Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Combat listener cannot be null.");
        listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerCombatState.Listener listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings({"deprecation", "removal"})
    private static double getAbsorptionDamageModifier(EntityDamageByEntityEvent event) {
        // Spigot has no modern post-blocking, pre-absorption value. Keeping this compatibility
        // adapter here counts absorbed hits without treating blocked or immune hits as combat.
        return event.isApplicable(EntityDamageEvent.DamageModifier.ABSORPTION)
                ? event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION)
                : 0D;
    }
}
