package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/** Coordinates combat events and the independently owned display lifecycles. */
public class BossHealthDisplay implements Listener {

    private static final Map<UUID, EliteOverheadHealthDisplay> activeDisplays = new ConcurrentHashMap<>();
    private static BukkitTask masterUpdateTask;

    public static void startMasterUpdateTask() {
        if (masterUpdateTask != null) return;
        masterUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<EliteOverheadHealthDisplay> iterator = activeDisplays.values().iterator();
                while (iterator.hasNext()) {
                    EliteOverheadHealthDisplay display = iterator.next();
                    try {
                        if (!display.isValid() || display.hasTimedOut()) {
                            removeDisplay(iterator, display);
                            continue;
                        }

                        display.updatePositions();
                        BossHealthBarManager.updateProximityCandidates(
                                display.eliteEntity(), display.healthMultiplier());
                    } catch (RuntimeException exception) {
                        MetadataHandler.PLUGIN.getLogger().log(
                                Level.WARNING,
                                "Discarding a failed overhead health display for " +
                                        display.eliteEntity().getEliteUUID(),
                                exception);
                        removeDisplay(iterator, display);
                    }
                }

                // Candidate mutations are deliberately reconciled once per tick. A large
                // carcass wake/timeout wave must not repeatedly sort the same collection.
                updateSafely("health boss bars", BossHealthBarManager::update);
                updateSafely("combat popups", CombatPopupManager::update);
                updateSafely("hit effects", HitEffectManager::update);
                updateSafely("XP popups", XpPopupManager::update);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    public static void shutdown() {
        if (masterUpdateTask != null) {
            masterUpdateTask.cancel();
            masterUpdateTask = null;
        }
        activeDisplays.values().forEach(BossHealthDisplay::cleanupDisplay);
        activeDisplays.clear();
        updateSafely("health boss-bar shutdown", BossHealthBarManager::shutdown);
        updateSafely("combat-popup shutdown", CombatPopupManager::shutdown);
        updateSafely("hit-effect shutdown", HitEffectManager::shutdown);
        updateSafely("XP-popup shutdown", XpPopupManager::shutdown);
    }

    public static void removeDisplay(EliteEntity eliteEntity) {
        if (eliteEntity == null) return;
        EliteOverheadHealthDisplay display = activeDisplays.remove(eliteEntity.getEliteUUID());
        if (display != null) cleanupDisplay(display);
        BossHealthBarManager.removeBoss(eliteEntity);
    }

    /** Public facade retained for the XP reward subsystem outside this package. */
    public static void createXPPopup(Location location, Player player, long xpAmount) {
        XpPopupManager.create(location, player, xpAmount);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EliteMobDamagedByPlayerEvent event) {
        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (!eliteEntity.isValid()) return;

        Vector offset = new Vector(
                ThreadLocalRandom.current().nextDouble(-1.5, 1.5),
                0,
                ThreadLocalRandom.current().nextDouble(-1.5, 1.5));
        CombatPopupManager.createDamagePopup(
                eliteEntity, event.getDamage(), event.isCriticalStrike(),
                event.getDamageModifier(), offset, event.getPlayer());

        if (!anyHealthDisplayEnabled()) return;
        EliteOverheadHealthDisplay display = getOrCreateDisplay(eliteEntity);
        display.resetCombatTimer();
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (eliteEntity.isValid()) display.rebuild();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EliteMobHealEvent event) {
        EliteEntity eliteEntity = event.getEliteEntity();
        if (!eliteEntity.isValid()) return;
        CombatPopupManager.createHealPopup(eliteEntity, event.getHealAmount(), event.isFullHeal());
        if (!anyHealthDisplayEnabled()) return;

        EliteOverheadHealthDisplay display = activeDisplays.get(eliteEntity.getEliteUUID());
        if (display != null) display.rebuild();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterCombat(EliteMobEnterCombatEvent event) {
        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (!eliteEntity.isValid() || !anyHealthDisplayEnabled()) return;

        getOrCreateDisplay(eliteEntity).resetCombatTimer();
        if (!MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier() ||
                !(eliteEntity instanceof CustomBossEntity customBoss) ||
                customBoss.getHealthMultiplier() <= MobCombatSettingsConfig.getBossBarHealthMultiplierThreshold())
            return;
        BossHealthBarManager.registerCombatCandidate(eliteEntity, event.getTargetEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExitCombat(EliteMobExitCombatEvent event) {
        removeDisplay(event.getEliteMobEntity());
    }

    private static EliteOverheadHealthDisplay getOrCreateDisplay(EliteEntity eliteEntity) {
        return activeDisplays.computeIfAbsent(
                eliteEntity.getEliteUUID(), ignored -> new EliteOverheadHealthDisplay(eliteEntity));
    }

    private static void removeDisplay(Iterator<EliteOverheadHealthDisplay> iterator,
                                      EliteOverheadHealthDisplay display) {
        cleanupDisplay(display);
        BossHealthBarManager.removeBoss(display.eliteEntity());
        iterator.remove();
    }

    private static void cleanupDisplay(EliteOverheadHealthDisplay display) {
        try {
            display.cleanup();
        } catch (RuntimeException exception) {
            MetadataHandler.PLUGIN.getLogger().log(
                    Level.WARNING,
                    "Failed to clean an overhead health display for " +
                            display.eliteEntity().getEliteUUID(),
                    exception);
        }
    }

    private static void updateSafely(String subsystem, Runnable update) {
        try {
            update.run();
        } catch (RuntimeException exception) {
            MetadataHandler.PLUGIN.getLogger().log(
                    Level.WARNING, "Failed to update " + subsystem, exception);
        }
    }

    private static boolean anyHealthDisplayEnabled() {
        return MobCombatSettingsConfig.isDisplayVisualHealthBars() ||
                MobCombatSettingsConfig.isDisplayNumericHealth() ||
                MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier();
    }
}
