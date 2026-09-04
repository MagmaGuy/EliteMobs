package com.magmaguy.elitemobs.experimentalcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.combattag.DungeonCombatRuntime;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.ExperimentalCombatConfig;
import com.magmaguy.elitemobs.experimentalcombat.presentation.ClassPresentationTheme;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.HealthDisplayCoordinator;
import com.magmaguy.elitemobs.skills.HealthPercentagePreserver;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Cake;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Bukkit adapter for the fixed health, hunger, food-healing, and out-of-combat baseline.
 */
public final class ExperimentalCombatRuntime implements Listener, PlayerCombatState.Listener {

    private static final String HEALTH_MODIFIER_KEY = "experimental_combat_health";
    private static final String ENTRY_WARNING =
            "&6&lExperimental Combat &7is active here. You are testing unfinished combat and class systems; &fplease send feedback to the developer&7. This feature will live, evolve, or die by tester feedback.";
    private static final String COMBAT_STARTED = "&cIn combat &8- &7passive healing paused";
    private static final String COMBAT_ENDED = "&aOut of combat &8- &7slow healing resumed";

    private static ExperimentalCombatRuntime instance;
    private static Function<Player, String> hudProvider = ExperimentalCombatRuntime::defaultHud;

    private final PlayerCombatState combatState;
    private final Map<UUID, PlayerBaseline> activePlayers = new HashMap<>();
    private final ExperimentalCombatShutdownPolicy shutdownPolicy = new ExperimentalCombatShutdownPolicy();
    private BukkitTask reconciliationTask;
    private BukkitTask hungerEnforcementTask;

    public ExperimentalCombatRuntime(PlayerCombatState combatState) {
        if (instance != null)
            throw new IllegalStateException("Experimental Combat runtime is already initialized.");
        this.combatState = combatState;
        instance = this;
    }

    public static boolean isActive(Player player) {
        return instance != null && instance.activePlayers.containsKey(player.getUniqueId());
    }

    public static void shutdownIfInitialized() {
        if (instance != null) instance.shutdown();
    }

    /** Keeps the owned max-health modifier in place while /em reload rebuilds the runtime. */
    public static void prepareForSoftReload() {
        if (instance != null) instance.shutdownPolicy.prepareSoftReload();
    }

    public static void installHudProvider(Function<Player, String> provider) {
        hudProvider = Objects.requireNonNull(provider, "provider");
    }

    public static void clearHudProvider() {
        hudProvider = ExperimentalCombatRuntime::defaultHud;
    }

    public void start() {
        if (reconciliationTask != null)
            throw new IllegalStateException("Experimental Combat runtime is already running.");
        combatState.addListener(this);
        reconciliationTask = Bukkit.getScheduler().runTaskTimer(
                MetadataHandler.PLUGIN,
                this::reconcilePlayers,
                1L,
                ExperimentalCombatRules.RECONCILIATION_INTERVAL_TICKS);
        hungerEnforcementTask = Bukkit.getScheduler().runTaskTimer(
                MetadataHandler.PLUGIN,
                this::enforceActiveHunger,
                1L,
                1L);
    }

    public void shutdown() {
        boolean preserveHealth = shutdownPolicy.consumeHealthCleanup()
                == ExperimentalCombatShutdownPolicy.HealthCleanup.PRESERVE_FOR_SOFT_RELOAD;
        combatState.removeListener(this);
        if (reconciliationTask != null) {
            reconciliationTask.cancel();
            reconciliationTask = null;
        }
        if (hungerEnforcementTask != null) {
            hungerEnforcementTask.cancel();
            hungerEnforcementTask = null;
        }
        for (UUID playerId : activePlayers.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) leave(player, preserveHealth);
        }
        activePlayers.clear();
        HandlerList.unregisterAll(this);
        if (instance == this) instance = null;
    }

    private void reconcilePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) reconcilePlayer(player, false);
    }

    private void reconcilePlayer(Player player, boolean enteredNewWorld) {
        // The health pool, fixed hunger, and out-of-combat regen are reserved for EliteMobs
        // managed/protected worlds. The outside-worlds class-control toggle must never feed this
        // condition: toggling in a normal world grants ability input only.
        boolean shouldBeActive = ExperimentalCombatConfig.isEnabled()
                && DungeonCombatRuntime.isEligiblePlayer(player);
        boolean isActive = activePlayers.containsKey(player.getUniqueId());
        if (isActive && player.isDead()) return;
        if (shouldBeActive && !isActive) {
            enter(player);
        } else if (!shouldBeActive && isActive) {
            leave(player);
        } else if (shouldBeActive) {
            maintain(player);
            if (enteredNewWorld) player.sendMessage(ChatColorConverter.convert(ENTRY_WARNING));
        }
    }

    private void enter(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;

        // A hard server stop may have serialized our namespaced attribute modifier without the
        // in-memory baseline that normally removes it. Normalize first so the new baseline and
        // health ratio are never derived from an already-expanded value.
        clearPersistedState(player);

        PlayerBaseline baseline = new PlayerBaseline(
                player.getFoodLevel(),
                player.getSaturation(),
                player.getExhaustion());
        activePlayers.put(player.getUniqueId(), baseline);

        boolean healthChanged = updateHealthModifier(player, true);
        HealthDisplayCoordinator.acquire(player, HealthDisplayCoordinator.Owner.EXPERIMENTAL_COMBAT);
        ExperimentalFoodItems.preparePlayerInventory(player);
        maintainHunger(player);
        if (healthChanged) scheduleHungerRefresh(player);
        renderHud(player);
        player.sendMessage(ChatColorConverter.convert(ENTRY_WARNING));
        if (ExperimentalCombatModule.isInitialized()
                && !ExperimentalCombatModule.get().hasActiveClass(player)) {
            player.sendMessage(ChatColorConverter.convert(
                    ClassPresentationTheme.gradient(ClassPresentationTheme.GOLD, "No class active!")
                            + " &7Open &f/em class &7and pick a free class to use abilities here."));
            ActionBarCompositor.show(
                    player,
                    ActionBarCompositor.Source.SKILL_FEEDBACK,
                    ChatColorConverter.convert(
                            ClassPresentationTheme.gradient(ClassPresentationTheme.GOLD, "No class")
                                    + " &8» &7pick one with &f/em class"));
        }
    }

    private void maintain(Player player) {
        boolean healthChanged = updateHealthModifier(player, true);
        if (!combatState.isInCombat(player.getUniqueId())) {
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null && player.getHealth() < maxHealth.getValue()) {
                double healing = maxHealth.getValue()
                        * ExperimentalCombatRules.OUT_OF_COMBAT_HEAL_FRACTION_PER_SECOND;
                applyHealing(player, healing, EntityRegainHealthEvent.RegainReason.CUSTOM);
            }
        }
        // Healing packets carry the server's deliberately eatable food level (19). Send the
        // client-only full hunger HUD last so out-of-combat ticks never expose that detail.
        ExperimentalFoodItems.preparePlayerInventory(player);
        maintainHunger(player);
        if (healthChanged) scheduleHungerRefresh(player);
        renderHud(player);
    }

    private void leave(Player player) {
        leave(player, false);
    }

    private void leave(Player player, boolean preserveHealth) {
        PlayerBaseline baseline = activePlayers.remove(player.getUniqueId());
        ExperimentalFoodItems.restorePlayerInventory(player);
        if (!preserveHealth) updateHealthModifier(player, false);
        HealthDisplayCoordinator.release(player, HealthDisplayCoordinator.Owner.EXPERIMENTAL_COMBAT);
        ActionBarCompositor.clear(player, ActionBarCompositor.Source.CLASS_HUD);
        if (baseline == null) return;
        player.setFoodLevel(baseline.foodLevel());
        player.setSaturation(Math.min(baseline.saturation(), baseline.foodLevel()));
        player.setExhaustion(baseline.exhaustion());
        player.sendHealthUpdate();
    }

    /** Removes only state identified by EliteMobs' stable namespaced modifier key. */
    public static void clearPersistedState(Player player) {
        if (!updateHealthModifier(player, false)) return;
        HealthDisplayCoordinator.release(player, HealthDisplayCoordinator.Owner.EXPERIMENTAL_COMBAT);
        HealthDisplayCoordinator.clearStaleExperimentalDisplay(player);
        // The original pre-crash hunger baseline cannot be recovered. Full hunger is the least
        // surprising safe handoff back to vanilla and matches the mode's normal OOC guarantee.
        player.setFoodLevel(ExperimentalCombatRules.DISPLAY_FOOD_LEVEL);
        player.setSaturation(0F);
        player.setExhaustion(0F);
        player.sendHealthUpdate();
    }

    private static boolean updateHealthModifier(Player player, boolean shouldExist) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return false;
        NamespacedKey modifierKey = new NamespacedKey(MetadataHandler.PLUGIN, HEALTH_MODIFIER_KEY);
        AttributeModifier existing = maxHealth.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(modifierKey))
                .findFirst()
                .orElse(null);
        if (shouldExist == (existing != null)) return false;

        HealthPercentagePreserver.during(player, maxHealth, () -> {
            if (existing != null) maxHealth.removeModifier(existing);
            if (shouldExist) {
                maxHealth.addModifier(new AttributeModifier(
                        modifierKey,
                        ExperimentalCombatRules.maxHealthFlatIncrease(),
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY));
            }
        });
        return true;
    }

    private static void maintainHunger(Player player) {
        if (player.getFoodLevel() != ExperimentalCombatRules.SERVER_FOOD_LEVEL)
            player.setFoodLevel(ExperimentalCombatRules.SERVER_FOOD_LEVEL);
        if (player.getSaturation() != 0F) player.setSaturation(0F);
        if (player.getExhaustion() != 0F) player.setExhaustion(0F);
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double displayedHealth = player.getHealth();
        if (player.isHealthScaled() && maxHealth != null && maxHealth.getValue() > 0D)
            displayedHealth = player.getHealth() / maxHealth.getValue() * player.getHealthScale();
        player.sendHealthUpdate(
                displayedHealth,
                ExperimentalCombatRules.DISPLAY_FOOD_LEVEL,
                0F);
    }

    private void enforceActiveHunger() {
        for (UUID playerId : activePlayers.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline() || player.isDead()) continue;
            if (player.getFoodLevel() != ExperimentalCombatRules.SERVER_FOOD_LEVEL
                    || player.getSaturation() != 0F
                    || player.getExhaustion() != 0F)
                maintainHunger(player);
        }
    }

    private static void renderHud(Player player) {
        ActionBarCompositor.show(
                player,
                ActionBarCompositor.Source.CLASS_HUD,
                ChatColorConverter.convert(hudProvider.apply(player)));
    }

    private static String defaultHud(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        String current = CombatHealthFormatter.format(player.getHealth());
        String maximum = maxHealth == null
                ? current
                : CombatHealthFormatter.format(maxHealth.getValue());
        return "&cHP " + current + "/" + maximum + " &8| &7No active class &8| &e/em class";
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isActive(player)) return;
        event.setFoodLevel(ExperimentalCombatRules.SERVER_FOOD_LEVEL);
        scheduleHungerRefresh(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFoodUseAttempt(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)
                || (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;
        Block clicked = event.getClickedBlock();
        boolean eatingCake = event.getAction() == Action.RIGHT_CLICK_BLOCK
                && clicked != null
                && clicked.getType() == Material.CAKE;
        if (!isFood(event.getItem()) && !eatingCake) return;
        // CraftBukkit fires PlayerInteractEvent before the server evaluates FoodComponent's
        // can-consume predicate. CakeBlock uses the same hunger gate without a held food item, so
        // it must pass through this preflight too. Reassert the eatable server value here even in
        // PEACEFUL, whose FoodData tick otherwise raises it to 20 between scheduler heartbeats.
        player.setFoodLevel(ExperimentalCombatRules.SERVER_FOOD_LEVEL);
        player.setSaturation(0F);
        player.setExhaustion(0F);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNaturalHealing(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isActive(player)) return;
        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
            event.setCancelled(true);
            return;
        }
        scheduleHungerRefresh(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isActive(player)) return;
        if (event.getFinalDamage() > 0D) scheduleHungerRefresh(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodConsumed(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;
        double saturation = saturationOf(event.getItem());
        if (saturation <= 0D) return;

        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (!player.isOnline() || !isActive(player) || player.isDead()) return;
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth == null) return;
            double healing = saturation * ExperimentalCombatRules.FOOD_SATURATION_TO_HEALTH;
            applyHealing(player, healing, EntityRegainHealthEvent.RegainReason.EATING);
            maintainHunger(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCakeConsumed(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)
                || event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.CAKE
                || !(clicked.getBlockData() instanceof Cake cake)) return;

        int previousBites = cake.getBites();
        int maximumBites = cake.getMaximumBites();
        org.bukkit.Location cakeLocation = clicked.getLocation();
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (!player.isOnline() || !isActive(player) || player.isDead()) return;
            Block current = cakeLocation.getBlock();
            boolean sliceConsumed = current.getType() == Material.CAKE
                    ? current.getBlockData() instanceof Cake currentCake
                    && currentCake.getBites() > previousBites
                    : previousBites >= maximumBites;
            if (!sliceConsumed) return;
            applyHealing(
                    player,
                    ExperimentalCombatRules.CAKE_SLICE_SATURATION
                            * ExperimentalCombatRules.FOOD_SATURATION_TO_HEALTH,
                    EntityRegainHealthEvent.RegainReason.EATING);
            maintainHunger(player);
        });
    }

    private static void scheduleHungerRefresh(Player player) {
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (player.isOnline() && isActive(player)) maintainHunger(player);
        });
    }

    private static double applyHealing(
            Player player,
            double requestedHealing,
            EntityRegainHealthEvent.RegainReason reason) {
        if (!Double.isFinite(requestedHealing) || requestedHealing <= 0D || player.isDead()) return 0D;
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return 0D;
        double missingHealth = Math.max(0D, maxHealth.getValue() - player.getHealth());
        if (missingHealth <= 0D) return 0D;

        EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                player, Math.min(requestedHealing, missingHealth), reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !Double.isFinite(event.getAmount()) || event.getAmount() <= 0D) return 0D;

        double before = player.getHealth();
        player.setHealth(Math.min(maxHealth.getValue(), before + event.getAmount()));
        return Math.max(0D, player.getHealth() - before);
    }

    static double saturationOf(ItemStack itemStack) {
        return ExperimentalFoodItems.saturationOf(itemStack);
    }

    private static boolean isFood(ItemStack itemStack) {
        return ExperimentalFoodItems.isFood(itemStack);
    }

    @Override
    public void onCombatStarted(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && isActive(player))
            ActionBarCompositor.show(
                    player,
                    ActionBarCompositor.Source.COMBAT_TRANSITION,
                    ChatColorConverter.convert(COMBAT_STARTED));
    }

    @Override
    public void onCombatEnded(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && isActive(player))
            ActionBarCompositor.show(
                    player,
                    ActionBarCompositor.Source.COMBAT_TRANSITION,
                    ChatColorConverter.convert(COMBAT_ENDED));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, this::reconcilePlayers, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            if (player.isOnline()) reconcilePlayer(player, true);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            reconcilePlayers();
            if (player.isOnline() && isActive(player)) scheduleHungerRefresh(player);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!activePlayers.containsKey(player.getUniqueId())) return;
        // Death replaces Bukkit's FoodData. If the player later leaves the mode, restore that
        // post-respawn vanilla state rather than hunger captured before a previous life.
        activePlayers.put(player.getUniqueId(), new PlayerBaseline(
                ExperimentalCombatRules.DISPLAY_FOOD_LEVEL, 5F, 0F));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activePlayers.containsKey(event.getPlayer().getUniqueId())) leave(event.getPlayer());
    }

    private record PlayerBaseline(int foodLevel, float saturation, float exhaustion) {
    }

}
