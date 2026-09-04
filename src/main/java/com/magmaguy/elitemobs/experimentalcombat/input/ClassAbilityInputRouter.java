package com.magmaguy.elitemobs.experimentalcombat.input;

import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.experimentalcombat.MonotonicTickClock;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Interprets every Experimental Combat control method concurrently without owning combat behavior.
 *
 * <p>Java players press F, then use hotbar keys 1, 2 or 3 without changing the held item; a second
 * plain F selects mobility directly, and left click, right click or jump inside the open chord
 * select mobility, signature and utility. Outside EliteMobs combat content, a sneak-held double-F
 * in quick succession toggles that class-control layer for the session, while a single sneak-held
 * F stays a vanilla hand swap. The Focus item works for any player holding one and is recognized
 * solely through a plugin-owned persistent-data tag; the stored input profile only chooses which
 * controls are granted and advertised, never which ones are accepted. Bedrock clients cannot
 * express the F layer, so it stays disabled for them.</p>
 */
public final class ClassAbilityInputRouter implements Listener {

    private static final String FOCUS_ITEM_KEY = "experimental_combat_focus";
    private static final int HOTBAR_SIZE = 9;

    private final Plugin plugin;
    private final ClassAbilityInput input;
    private final NamespacedKey focusItemKey;
    private final ClassControlMode controlMode = new ClassControlMode();
    private final Map<UUID, PendingGesture> pendingGestures = new HashMap<>();
    private final Map<UUID, RecentDispatch> recentDispatches = new HashMap<>();
    private final Map<UUID, Integer> focusSlotsPendingRespawn = new HashMap<>();
    private final Set<UUID> dispatchingAbilities = new HashSet<>();
    private long nextGestureGeneration;

    public ClassAbilityInputRouter(Plugin plugin, ClassAbilityInput input) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.input = Objects.requireNonNull(input, "input");
        this.focusItemKey = new NamespacedKey(plugin, FOCUS_ITEM_KEY);
    }

    /**
     * Gives the player one Focus item without replacing any existing inventory contents.
     *
     * <p>The preferred slot is a hotbar index from 0 through 8. When it is occupied, the first
     * empty storage slot is used. An existing tagged Focus item is reused instead of duplicating
     * it.</p>
     */
    public FocusItemGiveResult giveFocusItem(Player player, int preferredSlot) {
        requirePrimaryThread("giveFocusItem");
        Objects.requireNonNull(player, "player");
        if (preferredSlot < 0 || preferredSlot >= HOTBAR_SIZE)
            return new FocusItemGiveResult(FocusItemGiveStatus.INVALID_PREFERRED_SLOT, -1);

        PlayerInventory inventory = player.getInventory();
        ItemStack[] storage = inventory.getStorageContents();
        int existingSlot = normalizeFocusItems(player);
        if (existingSlot != Integer.MIN_VALUE)
            return new FocusItemGiveResult(FocusItemGiveStatus.ALREADY_PRESENT, existingSlot);

        if (isEmpty(storage[preferredSlot])) {
            inventory.setItem(preferredSlot, createFocusItem());
            return new FocusItemGiveResult(FocusItemGiveStatus.GIVEN_TO_PREFERRED_SLOT, preferredSlot);
        }

        for (int slot = 0; slot < storage.length; slot++) {
            if (!isEmpty(storage[slot])) continue;
            inventory.setItem(slot, createFocusItem());
            return new FocusItemGiveResult(FocusItemGiveStatus.GIVEN_TO_FALLBACK_SLOT, slot);
        }
        return new FocusItemGiveResult(FocusItemGiveStatus.INVENTORY_FULL, -1);
    }

    private ItemStack createFocusItem() {
        ItemStack focus = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = focus.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Class Focus");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Left-click: " + ChatColor.WHITE + "Mobility");
        lore.add(ChatColor.GRAY + "Right-click: " + ChatColor.WHITE + "Signature");
        lore.add(ChatColor.GRAY + "Sneak + right-click: " + ChatColor.WHITE + "Utility");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Experimental Combat control item");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(focusItemKey, PersistentDataType.BYTE, (byte) 1);
        focus.setItemMeta(meta);
        return focus;
    }

    public boolean isFocusItem(ItemStack itemStack) {
        return itemStack != null
                && itemStack.getType() != Material.AIR
                && itemStack.hasItemMeta()
                && itemStack.getItemMeta().getPersistentDataContainer()
                .has(focusItemKey, PersistentDataType.BYTE);
    }

    /** Removes only plugin-tagged Focus controls, preserving every ordinary nether star. */
    public int removeFocusItems(Player player) {
        requirePrimaryThread("removeFocusItems");
        Objects.requireNonNull(player, "player");
        int removed = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!isFocusItem(item)) continue;
            removed += item.getAmount();
            player.getInventory().setItem(slot, null);
        }
        ItemStack cursor = player.getItemOnCursor();
        if (isFocusItem(cursor)) {
            removed += cursor.getAmount();
            player.setItemOnCursor(null);
        }
        focusSlotsPendingRespawn.remove(player.getUniqueId());
        clearPlayer(player);
        return removed;
    }

    public boolean hasFocusItem(Player player) {
        Objects.requireNonNull(player, "player");
        if (isFocusItem(player.getItemOnCursor())) return true;
        for (ItemStack item : player.getInventory().getContents())
            if (isFocusItem(item)) return true;
        return false;
    }

    /** Keeps at most one tagged control item in the player's inventory or cursor. */
    private int normalizeFocusItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        int existingSlot = Integer.MIN_VALUE;
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!isFocusItem(item)) continue;
            if (existingSlot == Integer.MIN_VALUE) {
                item.setAmount(1);
                existingSlot = slot;
            } else {
                inventory.setItem(slot, null);
            }
        }

        ItemStack cursor = player.getItemOnCursor();
        if (!isFocusItem(cursor)) return existingSlot;
        if (existingSlot == Integer.MIN_VALUE) {
            cursor.setAmount(1);
            return -1;
        }
        player.setItemOnCursor(null);
        return existingSlot;
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
        for (Player player : Bukkit.getOnlinePlayers()) removeFocusItems(player);
        for (PendingGesture gesture : pendingGestures.values()) gesture.timeoutTask().cancel();
        pendingGestures.clear();
        controlMode.clearAll();
        recentDispatches.clear();
        focusSlotsPendingRespawn.clear();
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
     * Chord-window mouse bindings: left click selects mobility, right click selects signature.
     *
     * <p>Air-click interact events are born with {@code useInteractedBlock=DENY}, which Bukkit
     * reports as cancelled — {@code ignoreCancelled} would make this handler deaf to air clicks.</p>
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChordInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isFocusItem(event.getItem())) return; // Focus clicks keep their own bindings
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

    // No ignoreCancelled: air-click interact events are born cancelled (null block = DENY) and
    // air clicks are this binding's primary input.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onFocusInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isFocusItem(event.getItem())) return;
        event.setCancelled(true);
        if (!usesFocusItem(player)) return;

        AbilitySlot abilitySlot = switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> AbilitySlot.MOBILITY;
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> player.isSneaking()
                    ? AbilitySlot.UTILITY
                    : AbilitySlot.SIGNATURE;
            default -> null;
        };
        if (abilitySlot == null) return;
        dispatch(player, abilitySlot);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFocusInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isFocusItem(player.getInventory().getItemInMainHand())) {
            PendingGesture pending = pendingGestures.get(player.getUniqueId());
            if (pending == null) return;
            ClassAbilityGestureState.Transition transition =
                    pending.state().rightClick(MonotonicTickClock.currentTick());
            if (transition.consumesInput()) event.setCancelled(true);
            applyGestureTransition(player, transition);
            return;
        }
        event.setCancelled(true);
        if (!usesFocusItem(player)) return;
        dispatch(player, player.isSneaking() ? AbilitySlot.UTILITY : AbilitySlot.SIGNATURE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFocusArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (isFocusItem(event.getPlayerItem())) event.setCancelled(true);
    }

    /** Cancels the vanilla melee hit when it is a chord mobility selection or the Focus item's left-click binding. */
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
        if (!usesFocusItem(player)) return;
        event.setCancelled(true);
        dispatch(player, AbilitySlot.MOBILITY);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        boolean currentFocus = isFocusItem(current);
        boolean cursorFocus = isFocusItem(cursor);
        int hotbarButton = event.getHotbarButton();
        boolean hotbarFocus = hotbarButton >= 0
                && isFocusItem(player.getInventory().getItem(hotbarButton));
        boolean offhandTransfer = event.getClick() == ClickType.SWAP_OFFHAND
                && isFocusItem(player.getInventory().getItemInOffHand());
        if (!currentFocus && !cursorFocus && !hotbarFocus && !offhandTransfer) return;

        int topSize = event.getView().getTopInventory().getSize();
        boolean clickedTop = event.getRawSlot() >= 0 && event.getRawSlot() < topSize;
        boolean clickedOutside = event.getRawSlot() < 0;
        InventoryType topType = event.getView().getTopInventory().getType();
        boolean externalTop = topType != InventoryType.CRAFTING && topType != InventoryType.CREATIVE;
        boolean shiftIntoExternalStorage = currentFocus
                && event.isShiftClick()
                && event.getClickedInventory() instanceof PlayerInventory
                && externalTop;
        boolean moveIntoTop = clickedTop && (cursorFocus || hotbarFocus || offhandTransfer);
        boolean bundleInsertion = (cursorFocus && isBundle(current))
                || (currentFocus && isBundle(cursor));
        boolean droppingFocus = switch (event.getAction()) {
            case DROP_ALL_CURSOR, DROP_ONE_CURSOR -> cursorFocus;
            case DROP_ALL_SLOT, DROP_ONE_SLOT -> currentFocus;
            default -> false;
        };

        if ((clickedOutside && cursorFocus)
                || droppingFocus
                || shiftIntoExternalStorage
                || moveIntoTop
                || bundleInsertion) {
            event.setCancelled(true);
            normalizeFocusItems(player);
            return;
        }

        // Normalize after Bukkit applies an allowed move within the player's own inventory.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) normalizeFocusItems(player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !isFocusItem(event.getOldCursor())) return;
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
            normalizeFocusItems(player);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) normalizeFocusItems(player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusInventoryMove(InventoryMoveItemEvent event) {
        if (isFocusItem(event.getItem())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusInventoryPickup(InventoryPickupItemEvent event) {
        if (!isFocusItem(event.getItem().getItemStack())) return;
        event.setCancelled(true);
        event.getItem().remove();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusDrop(PlayerDropItemEvent event) {
        if (!isFocusItem(event.getItemDrop().getItemStack())) return;
        event.setCancelled(true);
        normalizeFocusItems(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusItemSpawn(ItemSpawnEvent event) {
        if (isFocusItem(event.getEntity().getItemStack())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusPickup(EntityPickupItemEvent event) {
        if (!isFocusItem(event.getItem().getItemStack())) return;
        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }
        if (normalizeFocusItems(player) != Integer.MIN_VALUE) {
            event.setCancelled(true);
            event.getItem().remove();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) normalizeFocusItems(player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFocusDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        int focusSlot = normalizeFocusItems(player);
        event.getDrops().removeIf(this::isFocusItem);
        focusSlotsPendingRespawn.remove(player.getUniqueId());
        if (event.getKeepInventory() || focusSlot == Integer.MIN_VALUE) return;

        // This API level has no per-item keep list. The Focus is a synthetic control item, so
        // suppress its drop and recreate exactly one after Bukkit has rebuilt the inventory.
        int preferredSlot = focusSlot >= 0 && focusSlot < HOTBAR_SIZE ? focusSlot : HOTBAR_SIZE - 1;
        focusSlotsPendingRespawn.put(player.getUniqueId(), preferredSlot);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusPrepareCraft(PrepareItemCraftEvent event) {
        if (containsFocus(event.getInventory().getMatrix())) event.getInventory().setResult(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFocusCraft(CraftItemEvent event) {
        if (containsFocus(event.getInventory().getMatrix())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeFocusItems(player);
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
        Integer preferredSlot = focusSlotsPendingRespawn.remove(player.getUniqueId());
        if (preferredSlot == null) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()
                    && input.mechanicsActive(player)
                    && input.activeInputProfile(player) == InputProfile.FOCUS_ITEM)
                giveFocusItem(player, preferredSlot);
        });
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
                ChatColor.GRAY + "[" + (heldSlot == 0 ? "7" : "1") + "/LMB] "
                        + ChatColor.WHITE
                        + input.abilityName(player, AbilitySlot.MOBILITY)
                        + ChatColor.GRAY + "  [" + (heldSlot == 1 ? "8" : "2") + "/RMB] "
                        + ChatColor.WHITE
                        + input.abilityName(player, AbilitySlot.SIGNATURE)
                        + ChatColor.GRAY + "  [" + (heldSlot == 2 ? "9" : "3") + "/Jump] "
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
            case TOGGLED_ON -> header + " &8» &aON &8- &7F for abilities &8(&7F+1/2/3, clicks,"
                    + " jump&8)";
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

    private boolean usesFocusItem(Player player) {
        return input.mechanicsActive(player)
                && isFocusItem(player.getInventory().getItemInMainHand());
    }

    private static boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    private static boolean isBundle(ItemStack itemStack) {
        return !isEmpty(itemStack) && itemStack.getType().name().endsWith("BUNDLE");
    }

    private boolean containsFocus(ItemStack[] items) {
        for (ItemStack item : items)
            if (isFocusItem(item)) return true;
        return false;
    }

    private static void requirePrimaryThread(String operation) {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("ClassAbilityInputRouter." + operation
                    + " must run on the server thread");
    }

    public enum FocusItemGiveStatus {
        GIVEN_TO_PREFERRED_SLOT,
        GIVEN_TO_FALLBACK_SLOT,
        ALREADY_PRESENT,
        INVENTORY_FULL,
        INVALID_PREFERRED_SLOT
    }

    public record FocusItemGiveResult(FocusItemGiveStatus status, int slot) {
        public FocusItemGiveResult {
            Objects.requireNonNull(status, "status");
            if (slot < -1) throw new IllegalArgumentException("slot must be -1 or a storage slot");
        }

        public boolean given() {
            return status == FocusItemGiveStatus.GIVEN_TO_PREFERRED_SLOT
                    || status == FocusItemGiveStatus.GIVEN_TO_FALLBACK_SLOT;
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
