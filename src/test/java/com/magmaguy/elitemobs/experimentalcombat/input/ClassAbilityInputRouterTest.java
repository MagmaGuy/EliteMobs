package com.magmaguy.elitemobs.experimentalcombat.input;

import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityContribution;
import com.magmaguy.elitemobs.experimentalcombat.abilities.AbilityResult;
import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.progression.InputProfile;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassAbilityInputRouterTest {

    private final List<String> actionBars = new ArrayList<>();
    private final RecordingAbilityInput input = new RecordingAbilityInput();
    private final AtomicInteger heldSlot = new AtomicInteger();
    private final AtomicBoolean sneaking = new AtomicBoolean();
    private Player player;
    private ClassAbilityInputRouter router;

    @BeforeEach
    void setUp() throws Exception {
        UUID playerId = UUID.fromString("6eaef6b7-b2f9-31f7-9f97-752dbf39702d");
        PlayerInventory inventory = proxy(PlayerInventory.class, (proxy, method, arguments) ->
                method.getName().equals("getHeldItemSlot")
                        ? heldSlot.get()
                        : defaultValue(method.getReturnType()));
        Player.Spigot spigot = new Player.Spigot() {
            @Override
            public void sendMessage(ChatMessageType position, BaseComponent component) {
                captureActionBar(position, component);
            }

            @Override
            public void sendMessage(ChatMessageType position, BaseComponent... components) {
                captureActionBar(position, components);
            }
        };
        player = proxy(Player.class, (proxy, method, arguments) -> switch (method.getName()) {
            case "getUniqueId" -> playerId;
            case "getInventory" -> inventory;
            case "isOnline" -> true;
            case "isSneaking" -> sneaking.get();
            case "spigot" -> spigot;
            default -> defaultValue(method.getReturnType());
        });

        BukkitTask task = proxy(BukkitTask.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType()));
        BukkitScheduler scheduler = proxy(BukkitScheduler.class, (proxy, method, arguments) ->
                method.getName().startsWith("runTask") ? task : defaultValue(method.getReturnType()));
        PluginManager pluginManager = proxy(PluginManager.class,
                (proxy, method, arguments) -> defaultValue(method.getReturnType()));
        Server server = proxy(Server.class, (proxy, method, arguments) -> switch (method.getName()) {
            case "getLogger" -> Logger.getLogger(ClassAbilityInputRouterTest.class.getName());
            case "getName" -> "EliteMobs input test";
            case "getVersion", "getBukkitVersion" -> "test";
            case "getScheduler" -> scheduler;
            case "getPluginManager" -> pluginManager;
            case "getPlayer" -> player;
            case "isPrimaryThread" -> true;
            default -> defaultValue(method.getReturnType());
        });
        Bukkit.setServer(server);
        ActionBarCompositor.start();

        Plugin plugin = proxy(Plugin.class, (proxy, method, arguments) ->
                method.getName().equals("getName") ? "EliteMobs" : defaultValue(method.getReturnType()));
        router = new ClassAbilityInputRouter(plugin, input);
    }

    private void captureActionBar(ChatMessageType position, BaseComponent... components) {
        if (position != ChatMessageType.ACTION_BAR) return;
        StringBuilder message = new StringBuilder();
        for (BaseComponent component : components) message.append(component.toLegacyText());
        actionBars.add(ChatColor.stripColor(message.toString()));
    }

    @AfterEach
    void tearDown() throws Exception {
        ActionBarCompositor.shutdown();
        Field server = Bukkit.class.getDeclaredField("server");
        server.setAccessible(true);
        server.set(null, null);
    }

    @ParameterizedTest(name = "held slot {0} prompts {1}")
    @CsvSource({
            "0, '[7/LMB] Blink  [2/RMB] Arcane Bolt  [3/Jump] Mana Ward'",
            "1, '[1/LMB] Blink  [8/RMB] Arcane Bolt  [3/Jump] Mana Ward'",
            "2, '[1/LMB] Blink  [2/RMB] Arcane Bolt  [9/Jump] Mana Ward'",
            "4, '[1/LMB] Blink  [2/RMB] Arcane Bolt  [3/Jump] Mana Ward'"
    })
    void fPromptSubstitutesTheMirrorKeyForTheAbilityOnTheHeldSlot(int selectedSlot, String expectedPrompt) {
        // Pressing the digit of the already-held slot sends no packet, so that key is advertised
        // as its 7/8/9 mirror instead.
        heldSlot.set(selectedSlot);
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));

        assertEquals(List.of(expectedPrompt), actionBars);
    }

    @Test
    void secondFInsideTheOpenChordExecutesBlink() {
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));

        assertEquals(List.of(AbilitySlot.MOBILITY), input.usedSlots);
    }

    @Test
    void hotbarSelectionInsideTheOpenChordExecutesTheAbility() {
        heldSlot.set(4);
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        PlayerItemHeldEvent selection = new PlayerItemHeldEvent(player, 4, 0);
        router.onAbilityHotbarSelection(selection);

        assertEquals(List.of(AbilitySlot.MOBILITY), input.usedSlots);
        assertTrue(selection.isCancelled());
    }

    @Test
    void mirrorKeyInsideTheOpenChordExecutesTheAbilityForTheHeldSlot() {
        heldSlot.set(0);
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        PlayerItemHeldEvent selection = new PlayerItemHeldEvent(player, 0, 6);
        router.onAbilityHotbarSelection(selection);

        assertEquals(List.of(AbilitySlot.MOBILITY), input.usedSlots);
        assertTrue(selection.isCancelled());
    }

    @Test
    void leftClickInsideTheOpenChordExecutesSignature() {
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        PlayerInteractEvent click = new PlayerInteractEvent(
                player, Action.LEFT_CLICK_AIR, null, null, BlockFace.SELF);
        router.onChordInteract(click);

        assertEquals(List.of(AbilitySlot.SIGNATURE), input.usedSlots);
        // A null-block interact event is born with useInteractedBlock=DENY, so the item result is
        // the only honest cancellation signal.
        assertEquals(Event.Result.DENY, click.useItemInHand());
    }

    @Test
    void rightClickInsideTheOpenChordExecutesUtility() {
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        PlayerInteractEvent click = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_AIR, null, null, BlockFace.SELF);
        router.onChordInteract(click);

        assertEquals(List.of(AbilitySlot.UTILITY), input.usedSlots);
        assertEquals(Event.Result.DENY, click.useItemInHand());
    }

    @Test
    void jumpInsideTheOpenChordExecutesUtility() {
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        router.onChordJump(new PlayerStatisticIncrementEvent(player, Statistic.JUMP, 0, 1));

        assertEquals(List.of(AbilitySlot.UTILITY), input.usedSlots);
    }

    @Test
    void clicksAndJumpOutsideAnOpenChordPassThrough() {
        PlayerInteractEvent click = new PlayerInteractEvent(
                player, Action.RIGHT_CLICK_AIR, null, null, BlockFace.SELF);
        router.onChordInteract(click);
        router.onChordJump(new PlayerStatisticIncrementEvent(player, Statistic.JUMP, 0, 1));

        assertEquals(List.of(), input.usedSlots);
        assertEquals(Event.Result.DEFAULT, click.useItemInHand());
    }

    @Test
    void sneakHeldDoubleFOutsideCombatContentTogglesTheClassControlLayer() {
        input.alwaysAvailable = false;
        sneaking.set(true);
        PlayerSwapHandItemsEvent firstTap = new PlayerSwapHandItemsEvent(player, null, null);
        PlayerSwapHandItemsEvent secondTap = new PlayerSwapHandItemsEvent(player, null, null);
        router.onSwapHands(firstTap);
        router.onSwapHands(secondTap);

        // Neither tap is consumed: the second vanilla swap undoes the first, restoring the hands.
        assertFalse(firstTap.isCancelled());
        assertFalse(secondTap.isCancelled());
        assertEquals(List.of(), input.usedSlots);
        assertEquals(List.of(), actionBars);

        // With the layer now enabled, a plain F opens the ability chord.
        sneaking.set(false);
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        assertEquals(1, actionBars.size());
    }

    @Test
    void everyInputMethodStaysActiveOnTheFocusItemProfile() {
        // Input profiles choose which controls are granted and advertised, never which ones are
        // accepted: the F chord and hotbar selection must keep working for a focus-profile player.
        input.activeProfile = InputProfile.FOCUS_ITEM;
        router.onSwapHands(new PlayerSwapHandItemsEvent(player, null, null));
        PlayerItemHeldEvent selection = new PlayerItemHeldEvent(player, 4, 1);
        router.onAbilityHotbarSelection(selection);

        assertEquals(List.of(AbilitySlot.SIGNATURE), input.usedSlots);
        assertTrue(selection.isCancelled());
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == void.class) return null;
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        throw new IllegalArgumentException("Unsupported primitive " + type.getName());
    }

    private static final class RecordingAbilityInput implements ClassAbilityInput {

        private final List<AbilitySlot> usedSlots = new ArrayList<>();
        private InputProfile activeProfile = InputProfile.JAVA_HOTBAR_LAYER;
        private boolean alwaysAvailable = true;

        @Override
        public boolean mechanicsActive(Player player) {
            return true;
        }

        @Override
        public boolean hasActiveClass(Player player) {
            return true;
        }

        @Override
        public boolean fLayerSupported(Player player) {
            return true;
        }

        @Override
        public boolean controlsAlwaysAvailable(Player player) {
            return alwaysAvailable;
        }

        @Override
        public boolean outsideControlsAllowed() {
            return true;
        }

        @Override
        public void onControlModeChanged(Player player) {
        }

        @Override
        public InputProfile activeInputProfile(Player player) {
            return activeProfile;
        }

        @Override
        public String abilityName(Player player, AbilitySlot slot) {
            return switch (slot) {
                case MOBILITY -> "Blink";
                case SIGNATURE -> "Arcane Bolt";
                case UTILITY -> "Mana Ward";
            };
        }

        @Override
        public AbilityResult useAbility(Player player, AbilitySlot slot) {
            usedSlots.add(slot);
            return AbilityResult.success("spellcaster.mobility", AbilityContribution.NONE);
        }
    }
}
