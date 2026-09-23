package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.CombatTagConfig;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.utils.GameClock;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTag implements Listener {
    private static final int SAFETY_TICKS = 20 * 60;
    private static final Map<UUID, FlightSafety> flights = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player player = CombatParticipantResolver.resolveEliteCombatPlayer(event);
        if (player == null || player.getGameMode() == GameMode.CREATIVE || !player.isFlying()) return;
        player.setFlying(false);
        ActionBarCompositor.show(player, ActionBarCompositor.Source.COMBAT_TRANSITION,
                CombatTagConfig.getCombatTagMessage());
        clearFlightSafetyEffect(player);
        GameClock.initialize();
        FlightSafety flight = new FlightSafety(player);
        if (!player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, SAFETY_TICKS, 0))) return;
        flights.put(player.getUniqueId(), flight);
        try {
            flight.task = player.getServer().getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
                if (!player.isOnline() || player.isDead() || player.isOnGround()
                        || !player.getWorld().getUID().equals(flight.worldId)
                        || GameClock.getCurrentTick() - flight.started >= SAFETY_TICKS) flight.close();
            }, 1, 1);
        } catch (RuntimeException failure) {
            flight.close();
            throw failure;
        }
    }

    static void clearFlightSafetyEffect(Player player) {
        FlightSafety flight = flights.get(player.getUniqueId());
        if (flight != null) flight.close();
    }

    @EventHandler public void onQuit(PlayerQuitEvent event) { clearFlightSafetyEffect(event.getPlayer()); }
    @EventHandler public void onDeath(PlayerDeathEvent event) { clearFlightSafetyEffect(event.getEntity()); }
    @EventHandler public void onWorldChange(PlayerChangedWorldEvent event) { clearFlightSafetyEffect(event.getPlayer()); }

    public static void shutdown() {
        for (FlightSafety flight : flights.values().toArray(FlightSafety[]::new)) flight.close();
    }

    private static final class FlightSafety {
        final Player player;
        final UUID worldId;
        final long started;
        final PotionEffect previous;
        BukkitTask task;

        FlightSafety(Player player) {
            this.player = player;
            worldId = player.getWorld().getUID();
            started = GameClock.getCurrentTick();
            previous = player.getPotionEffect(PotionEffectType.SLOW_FALLING);
        }

        void close() {
            if (!flights.remove(player.getUniqueId(), this)) return;
            if (task != null) task.cancel();
            long elapsed = Math.max(0, GameClock.getCurrentTick() - started);
            long expected = Math.max(0, SAFETY_TICKS - elapsed);
            PotionEffect current = player.getPotionEffect(PotionEffectType.SLOW_FALLING);
            if (expected == 0 || current == null || current.getAmplifier() != 0 || current.isAmbient()
                    || !current.hasParticles() || !current.hasIcon()
                    || Math.abs(current.getDuration() - expected) > 1) return;
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            if (previous == null || player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
            int remaining = previous.isInfinite() ? -1 : (int) Math.max(0, previous.getDuration() - elapsed);
            if (remaining != 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,
                    remaining, previous.getAmplifier(), previous.isAmbient(), previous.hasParticles(), previous.hasIcon()));
        }
    }
}
