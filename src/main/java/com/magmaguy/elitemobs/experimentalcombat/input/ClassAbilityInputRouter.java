package com.magmaguy.elitemobs.experimentalcombat.input;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Interprets the F-key ability layer without owning combat behavior.
 * F,F selects mobility, F+left-click signature and F+right-click utility.
 * Hotbar and jump inputs also work inside the open chord. Sneak-double-F toggles
 * the layer outside managed content when permitted. Bedrock has no F-key input.
 */
public final class ClassAbilityInputRouter implements Listener {


    private final Plugin plugin;
    private final ClassAbilityInput input;
    private final ClassControlMode controlMode = new ClassControlMode();
    private final Map<UUID, PendingGesture> pendingGestures = new HashMap<>();
    private final Map<UUID, RecentDispatch> recentDispatches = new HashMap<>();
    private final Set<UUID> dispatchingAbilities = new HashSet<>();
    private long nextGestureGeneration;

    public ClassAbilityInputRouter(Plugin plugin, ClassAbilityInput input) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.input = Objects.requireNonNull(input, "input");
    }

    public boolean isGestureOpen(UUID playerId) {
        PendingGesture pending = pendingGestures.get(playerId);
        return pending != null && pending.state().isOpenAt(MonotonicTickClock.currentTick());
    }

    /** Whether this player may currently issue class-ability input. */
    public boolean controlsEnabled(Player player) {
        Objects.requireNonNull(player, "player");
        return input.hasActiveClass(player) && controlModeEnabled(player);
    }

    /** Cancels pending gesture timeouts and releases all retained player state. */
    public void shutdown() {
        for (PendingGesture gesture : pendingGestures.values()) gesture.timeoutTask().cancel();
        pendingGestures.clear();
        controlMode.clearAll();
        recentDispatches.clear();
        dispatchingAbilities.clear();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        boolean abilityInputReady = input.hasActiveClass(player) && input.fLayerSupported(player);
        ClassControlMode.FAction action = controlMode.pressF(
                playerId,
                MonotonicTickClock.currentTick(),
                player.isSneaking(),
                input.controlsAlwaysAvailable(player),
                input.outsideControlsAllowed(),
                abilityInputReady);
        switch (action) {
            case PASS_THROUGH -> closeGesture(player);
            case TOGGLE_BLOCKED -> {
                // The vanilla swap proceeds; the message only explains why the layer cannot enable.
                closeGesture(player);
                sendControlFeedback(player, action);
            }
            case TOGGLED_ON, TOGGLED_OFF -> {
                // Deliberately not cancelled: this second vanilla swap undoes the first tap's swap,
                // so the double-tap leaves the player's hands exactly as they started.
                closeGesture(player);
                input.onControlModeChanged(player);
                sendControlFeedback(player, action);
            }
            case OPEN_ABILITIES -> {
                event.setCancelled(true);
                PendingGesture pending = pendingGestures.get(playerId);
                ClassAbilityGestureState state = pending == null
                        ? ClassAbilityGestureState.closed()
                        : pending.state();
                applyGestureTransition(player, state.pressF(
                        MonotonicTickClock.currentTick(), player.isSneaking()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAbilityHotbarSelection(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PendingGesture pending = pendingGestures.get(player.getUniqueId());
        if (pending == null) return;

        // The open gesture is the authorization: it was granted at F-time at most one chord window
        // ago. Eligibility that lapsed since then is re-checked by useAbility, which gives feedback
        // instead of silently swallowing the selection.
        ClassAbilityGestureState.Transition transition = pending.state().selectHotbar(
                MonotonicTickClock.currentTick(), event.getNewSlot());
        if (transition.consumesInput()) event.setCancelled(true);
        applyGestureTransition(player, transition);
    }

    /**
     * Chord-window mouse bindings: left click selects signature, right click selects utility.
     *
     * <p>Air-click interact events are born with {@code useInteractedBlock=DENY}, which Bukkit
     * reports as cancelled — {@code ignoreCancelled} would make this handler deaf to air clicks.</p>
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChordInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PendingGesture pending = pendingGestures.get(player.getUniqueId());
        long currentTick = MonotonicTickClock.currentTick();
        if (pending == null) {
            // The other-hand half of a click that was consumed this tick must not use its item.
            RecentDispatch recent = recentDispatches.get(player.getUniqueId());
            if (recent != null && recent.serverTick() == currentTick
                    && (event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK))
                event.setCancelled(true);
            return;
        }
        ClassAbilityGestureState.Transition transition = switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> pending.state().leftClick(currentTick);
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> pending.state().rightClick(currentTick);
            default -> null;
        };
        if (transition == null) return;
        if (transition.consumesInput()) event.setCancelled(true);
        applyGestureTransition(player, transition);
    }

    /** Chord-window jump binding: a ground jump inside the open chord selects utility. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChordJump(PlayerStatisticIncrementEvent event) {
        if (event.getStatistic() != Statistic.JUMP) return;
        PendingGesture pending = pendingGestures.get(event.getPlayer().getUniqueId());
        if (pending == null) return;
        applyGestureTransition(event.getPlayer(), pending.state().jump(MonotonicTickClock.currentTick()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChordInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        PendingGesture pending = pendingGestures.get(player.getUniqueId());
        if (pending == null) {
            // A model can translate a consumed block/air click into an entity permission
            // check on the next tick. It must not also open an NPC menu behind the skill.
            RecentDispatch recent = recentDispatches.get(player.getUniqueId());
            if (recent != null && MonotonicTickClock.currentTick() - recent.serverTick() <= 1L)
                event.setCancelled(true);
            return;
        }
        ClassAbilityGestureState.Transition transition =
                pending.state().rightClick(MonotonicTickClock.currentTick());
        if (transition.consumesInput()) event.setCancelled(true);
        applyGestureTransition(player, transition);
    }

    /** Cancels the vanilla melee hit when it selects the chord signature ability. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;
        if (CombatDamageContext.isPlayerToEliteBypassActive()) return;
        if (dispatchingAbilities.contains(player.getUniqueId())) return;
        PendingGesture pending = pendingGestures.get(player.getUniqueId());
        if (pending != null) {
            ClassAbilityGestureState.Transition transition =
                    pending.state().leftClick(MonotonicTickClock.currentTick());
            if (transition.consumesInput()) event.setCancelled(true);
            applyGestureTransition(player, transition);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearPlayer(player);
        controlMode.clear(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        clearPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        clearPlayer(player);
    }

    private void openGesture(Player player, ClassAbilityGestureState state) {
        UUID playerId = player.getUniqueId();
        closeGesture(player);
        long generation = ++nextGestureGeneration;
        BukkitTask timeout = Bukkit.getScheduler().runTaskLater(plugin,
                () -> expireGesture(playerId, generation),
                ClassAbilityGestureState.CHORD_WINDOW_TICKS + 1L);
        pendingGestures.put(playerId, new PendingGesture(state, generation, timeout));
        // A vanilla client sends no slot-change packet when the pressed digit is the already-held
        // slot, so advertise that ability's mirror key (7/8/9) instead of a key that cannot work.
        int heldSlot = player.getInventory().getHeldItemSlot();
        ActionBarCompositor.show(
                player,
                ActionBarCompositor.Source.ABILITY_INPUT,
                ChatColor.GRAY + "[" + (heldSlot == 0 ? "7" : "1") + "/F] "
                        + ChatColor.WHITE
                        + input.abilityName(player, AbilitySlot.MOBILITY)
                        + ChatColor.GRAY + "  [" + (heldSlot == 1 ? "8" : "2") + "/LMB] "
                        + ChatColor.WHITE
                        + input.abilityName(player, AbilitySlot.SIGNATURE)
                        + ChatColor.GRAY + "  [" + (heldSlot == 2 ? "9" : "3") + "/RMB/Jump] "
                        + ChatColor.WHITE
                        + input.abilityName(player, AbilitySlot.UTILITY),
                ClassAbilityGestureState.CHORD_WINDOW_TICKS + 1L);
    }

    private void expireGesture(UUID playerId, long generation) {
        PendingGesture gesture = pendingGestures.get(playerId);
        if (gesture == null || gesture.generation() != generation) return;
        pendingGestures.remove(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) ActionBarCompositor.clear(player, ActionBarCompositor.Source.ABILITY_INPUT);
    }

    private void closeGesture(Player player) {
        PendingGesture gesture = pendingGestures.remove(player.getUniqueId());
        if (gesture == null) return;
        gesture.timeoutTask().cancel();
        ActionBarCompositor.clear(player, ActionBarCompositor.Source.ABILITY_INPUT);
    }

    private void applyGestureTransition(
            Player player,
            ClassAbilityGestureState.Transition transition) {
        closeGesture(player);
        switch (transition.outcome()) {
            case CHORD_OPENED -> openGesture(player, transition.next());
            case MOBILITY -> dispatch(player, AbilitySlot.MOBILITY);
            case SIGNATURE -> dispatch(player, AbilitySlot.SIGNATURE);
            case UTILITY -> dispatch(player, AbilitySlot.UTILITY);
            case PASS_THROUGH -> {
            }
        }
    }

    private void clearPlayer(Player player) {
        closeGesture(player);
        recentDispatches.remove(player.getUniqueId());
        dispatchingAbilities.remove(player.getUniqueId());
    }

    private boolean controlModeEnabled(Player player) {
        return controlMode.enabled(
                player.getUniqueId(),
                input.controlsAlwaysAvailable(player),
                input.outsideControlsAllowed());
    }

    private static void sendControlFeedback(Player player, ClassControlMode.FAction action) {
        String header = ClassPresentationTheme.gradient(
                ClassPresentationTheme.PURPLE, "Class Controls");
        String message = switch (action) {
            case TOGGLED_ON -> header + " &8» &aON &8- &7F,F: Mobility | F+LMB: Signature | F+RMB: Utility";
            case TOGGLED_OFF -> header + " &8» &cOFF &8- &7F swaps hands again";
            case TOGGLE_BLOCKED -> header + " &8» &cBLOCKED &8- &7outside EliteMobs worlds";
            default -> null;
        };
        if (message != null)
            player.sendMessage(com.magmaguy.magmacore.util.ChatColorConverter.convert(message));
    }

    private void dispatch(Player player, AbilitySlot abilitySlot) {
        UUID playerId = player.getUniqueId();
        long currentTick = MonotonicTickClock.currentTick();
        RecentDispatch recent = recentDispatches.get(playerId);
        if (recent != null && recent.serverTick() == currentTick && recent.abilitySlot() == abilitySlot) return;
        recentDispatches.put(playerId, new RecentDispatch(currentTick, abilitySlot));
        if (!dispatchingAbilities.add(playerId)) return;
        try {
            input.useAbility(player, abilitySlot);
        } finally {
            dispatchingAbilities.remove(playerId);
        }
    }

    private record PendingGesture(
            ClassAbilityGestureState state,
            long generation,
            BukkitTask timeoutTask) {
    }

    private record RecentDispatch(long serverTick, AbilitySlot abilitySlot) {
    }
}
