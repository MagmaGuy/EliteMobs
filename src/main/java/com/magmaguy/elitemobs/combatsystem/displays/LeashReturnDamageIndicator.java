package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Displays leash-return immunity through the same packet-backed animated text
 * mechanism used by normal damage indicators.
 */
public final class LeashReturnDamageIndicator {

    private static final int POPUP_DURATION_TICKS = 20;
    private static final Map<PopupKey, Long> popupExpiry = new HashMap<>();

    private LeashReturnDamageIndicator() {
    }

    public static void show(RegionalBossEntity regionalBossEntity, Player viewer) {
        if (!MobCombatSettingsConfig.isDisplayDamageOnHit()) return;
        if (regionalBossEntity == null || viewer == null || !viewer.isOnline()) return;

        LivingEntity livingEntity = regionalBossEntity.getUnsyncedLivingEntity();
        if (livingEntity == null || !livingEntity.isValid()) return;

        // One popup per (boss, viewer) per popup lifetime; the animation itself is handled by
        // the combat popup lifecycle.
        PopupKey popupKey = new PopupKey(livingEntity.getUniqueId(), viewer.getUniqueId());
        long now = System.currentTimeMillis();
        // Prune expired entries so the map stays bounded: boss entity UUIDs churn on every
        // respawn, so without this the map only ever grows until shutdown.
        popupExpiry.values().removeIf(expiryTime -> expiryTime <= now);
        Long expiry = popupExpiry.get(popupKey);
        if (expiry != null && now < expiry) return;
        popupExpiry.put(popupKey, now + POPUP_DURATION_TICKS * 50L);

        Vector offset = new Vector(
                ThreadLocalRandom.current().nextDouble(-1.5, 1.5),
                0,
                ThreadLocalRandom.current().nextDouble(-1.5, 1.5));
        Location location = livingEntity.getLocation().clone().add(
                offset.getX(), livingEntity.getEyeHeight() + 0.3, offset.getZ());

        CombatPopupManager.createImmunePopup(location, viewer);
    }

    public static void shutdown() {
        popupExpiry.clear();
    }

    private record PopupKey(UUID bossUuid, UUID viewerUuid) {
    }
}
