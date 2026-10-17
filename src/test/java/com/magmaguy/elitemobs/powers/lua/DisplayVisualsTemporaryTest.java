package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.GameClock;
import com.magmaguy.magmacore.scripting.ScriptDefinition;
import com.magmaguy.shaded.luaj.vm2.Globals;
import com.magmaguy.shaded.luaj.vm2.LuaTable;
import com.magmaguy.shaded.luaj.vm2.LuaValue;
import com.magmaguy.shaded.luaj.vm2.lib.jse.JsePlatform;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Temporary check: Lua display visuals and their Bedrock stand-in, through the real power world table. */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class DisplayVisualsTemporaryTest {
    private ServerMock server;
    private World world;
    private JavaPlugin previousPlugin;
    private final List<Runnable> powerCleanups = new ArrayList<>();
    private LuaPowerEntityTables entityTables;
    private Globals lua;

    @BeforeEach
    void open() {
        server = MockBukkit.mock();
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        world = server.addSimpleWorld("display_world");
        Location bossLocation = new Location(world, 0, 70, 0);
        EliteEntity elite = mock(EliteEntity.class);
        when(elite.getLocation()).thenReturn(bossLocation);
        ScriptDefinition definition = mock(ScriptDefinition.class);
        when(definition.getFileName()).thenReturn("display_visuals_test.lua");
        LuaPowerSupport support = new LuaPowerSupport(definition, elite);
        LuaPowerScriptApi.OwnedTaskController tasks = new LuaPowerScriptApi.OwnedTaskController() {
            public int runLater(int ticks, Runnable runnable) { return 0; }
            public int runRepeating(int initialDelayTicks, int intervalTicks, Runnable runnable) { return 0; }
            public void cancel(int taskId) { }
            public void ownCleanup(Runnable cleanup) { powerCleanups.add(cleanup); }
        };
        LuaPowerScriptApi.CallbackInvoker callbacks = (failure, callback, args) -> callback.invoke(LuaValue.varargsOf(args));
        entityTables = new LuaPowerEntityTables(definition, elite, support, tasks, callbacks);
        LuaTable worldTable = new LuaWorldTableBuilder(definition, elite, support, entityTables, tasks, callbacks).build();
        lua = JsePlatform.standardGlobals();
        lua.set("world", worldTable);
        lua.load("function at(x, y, z, yaw) return { world = 'display_world', x = x, y = y, z = z, yaw = yaw or 0, pitch = 0 } end").call();
    }

    @AfterEach
    void close() {
        GameClock.shutdown();
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = previousPlugin;
    }

    private LuaValue run(String source) { return lua.load(source).call(); }

    @Test
    void itemDisplaysAreCenteredTemporaryAndConfigured() {
        // Billboard, brightness and glow are checked natively: MockBukkit does not implement them.
        LuaValue handle = run("return world:spawn_display_at_location(at(1, 72, 1), { item = 'AMETHYST_BLOCK', scale = 0.5,"
                + " teleport_duration = 1 })");
        assertTrue(handle.istable());
        ItemDisplay display = only(ItemDisplay.class);
        assertEquals(Material.AMETHYST_BLOCK, display.getItemStack().getType());
        assertEquals(ItemDisplay.ItemDisplayTransform.NONE, display.getItemDisplayTransform());
        assertEquals(new Vector3f(0.5f), display.getTransformation().getScale());
        assertEquals(new Vector3f(), display.getTransformation().getTranslation(), "Item displays draw centered already");
        assertFalse(display.isPersistent(), "Visuals must never be saved with the chunk");
        assertEquals(1, display.getTeleportDuration());
        assertEquals(new Location(world, 1, 72, 1), display.getLocation());
        assertEquals(display.getUniqueId().toString(), handle.get("uuid").tojstring());
    }

    @Test
    void blockDisplaysAreShiftedSoTheirCenterIsOnTheLocation() {
        // Block data with states, such as lantern[hanging=true], is checked natively; MockBukkit cannot parse it.
        run("return world:spawn_display_at_location(at(0, 72, 0), { block = 'lantern', scale = { x = 1, y = 2, z = 1 } })");
        BlockDisplay display = only(BlockDisplay.class);
        assertEquals(Material.LANTERN, display.getBlock().getMaterial());
        assertEquals(new Vector3f(-0.5f, -1f, -0.5f), display.getTransformation().getTranslation());
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(strings = {"{}", "{ item = 'STONE', block = 'STONE' }", "{ item = 'STONE', scale = 0 }",
            "{ item = 'NOT_A_MATERIAL' }", "{ block = 'not_a_block[nope]' }"})
    void invalidOptionsSpawnNothing(String options) {
        assertTrue(run("return world:spawn_display_at_location(at(0, 72, 0), " + options + ")").isnil());
        assertTrue(world.getEntitiesByClass(Display.class).isEmpty());
    }

    @Test
    void scriptsMoveAndRemoveTheirDisplays() {
        run("visual = world:spawn_display_at_location(at(0, 72, 0), { item = 'TNT' })");
        ItemDisplay display = only(ItemDisplay.class);
        run("visual:teleport_to_location(at(3, 74, 3))");
        assertEquals(new Location(world, 3, 74, 3), display.getLocation());
        assertTrue(run("return visual:is_alive()").toboolean());
        run("visual:remove()");
        assertFalse(display.isValid());
        assertFalse(run("return visual:is_alive()").toboolean());
    }

    @Test
    void durationAndPowerCleanupEndDisplays() {
        run("world:spawn_display_at_location(at(0, 72, 0), { item = 'TNT', duration = 5 })");
        run("world:spawn_display_at_location(at(2, 72, 0), { item = 'ANVIL' })");
        ItemDisplay timed = byItem(Material.TNT), untimed = byItem(Material.ANVIL);
        server.getScheduler().performTicks(4);
        assertTrue(timed.isValid());
        server.getScheduler().performTicks(3);
        assertFalse(timed.isValid(), "duration removes the display");
        assertTrue(untimed.isValid());
        powerCleanups.forEach(Runnable::run);
        assertFalse(untimed.isValid(), "the power's cleanup removes what its script left behind");
    }

    @Test
    // MockBukkit does not implement per-player entity visibility, so the stand-in itself cannot be spawned here.
    void standInsAreSkippedWithoutBedrockPlayersOrWhenDisabled() {
        server.addPlayer().teleport(new Location(world, 5, 70, 5));
        run("world:spawn_display_at_location(at(0, 72, 0), { item = 'ANVIL' })");
        assertTrue(world.getEntitiesByClass(ArmorStand.class).isEmpty(), "No Bedrock players, no stand-in");

        PlayerMock bedrock = new PlayerMock(server, ".BedrockSteve0002", new UUID(0L, 78L));
        server.addPlayer(bedrock);
        bedrock.teleport(new Location(world, -5, 70, -5));
        run("world:spawn_display_at_location(at(0, 80, 0), { item = 'TNT', bedrock_fallback = false })");
        assertTrue(world.getEntitiesByClass(ArmorStand.class).isEmpty(), "bedrock_fallback = false spawns no stand-in");
    }

    @Test
    void armorStandsAndScriptEntitiesHaveNativeMethods() {
        run("stand = world:spawn_entity_at_location('ARMOR_STAND', at(0, 72, 0), { persistent = false })"
                + " stand:set_marker(true) stand:set_small(true) stand:set_base_plate(false) stand:set_arms(true)"
                + " stand:set_invisible(true) stand:set_silent(true)");
        ArmorStand stand = only(ArmorStand.class);
        assertFalse(stand.isPersistent());
        assertTrue(stand.isMarker() && stand.isSmall() && !stand.hasBasePlate() && stand.hasArms());
        assertTrue(stand.isInvisible() && stand.isSilent());
        assertTrue(run("return stand:remove()").toboolean());
        assertFalse(stand.isValid());

        PlayerMock player = server.addPlayer();
        LuaTable playerTable = entityTables.createEntityTable(player);
        assertFalse(playerTable.get("remove").call(playerTable).toboolean(), "Scripts cannot remove players");
        assertTrue(player.isValid());
        assertTrue(playerTable.get("set_marker").isnil(), "Armor stand methods exist only on armor stands");
    }

    private <T extends org.bukkit.entity.Entity> T only(Class<T> type) {
        var found = world.getEntitiesByClass(type);
        assertEquals(1, found.size(), "Expected exactly one " + type.getSimpleName() + ", found " + found.size());
        return found.iterator().next();
    }

    private ItemDisplay byItem(Material material) {
        return world.getEntitiesByClass(ItemDisplay.class).stream()
                .filter(display -> display.getItemStack().getType() == material).findFirst().orElseThrow();
    }
}
