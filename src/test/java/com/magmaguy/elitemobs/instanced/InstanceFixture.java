package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerTeleportEvent;
import com.magmaguy.elitemobs.api.instanced.MatchDestroyEvent;
import com.magmaguy.elitemobs.api.instanced.MatchEndEvent;
import com.magmaguy.elitemobs.api.instanced.MatchInstantiateEvent;
import com.magmaguy.elitemobs.api.instanced.MatchJoinEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.api.instanced.MatchStartEvent;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.MagmaCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins EliteMobs' instance behaviour through the classes and events add-ons use, so the move
 * onto MagmaCore's match core can be checked against it. Uses the real player database and
 * listeners; only the world copy that normally creates a dungeon is replaced by a test instance.
 */
abstract class InstanceFixture {
    protected ServerMock server;
    protected WorldMock overworld;
    protected WorldMock instanceWorld;
    protected PlayerMock alex;
    protected PlayerMock bea;
    protected PlayerMock cam;
    protected PlayerMock stranger;
    /** Every EliteMobs instanced API event, in order, as "event:detail". */
    protected final List<String> events = new ArrayList<>();
    private JavaPlugin previousPlugin;
    private Boolean previousSpectatorSetting;

    @BeforeEach
    final void openInstanceServer() throws Exception {
        resetMagmaCore();
        server = MockBukkit.mock();
        overworld = server.addSimpleWorld("world");
        instanceWorld = server.addSimpleWorld("em_instance");
        previousPlugin = MetadataHandler.PLUGIN;
        MetadataHandler.PLUGIN = MockBukkit.loadWith(PluginMock.class, new ByteArrayInputStream("""
                name: InstanceTest
                version: 1
                main: org.mockbukkit.mockbukkit.plugin.PluginMock
                authors: [Autotester]
                """.getBytes(StandardCharsets.UTF_8)));
        MagmaCore.createInstance(MetadataHandler.PLUGIN);
        new ArenasConfig();
        new DungeonsConfig();

        alex = online("Alex");
        bea = online("Bea");
        cam = online("Cam");
        stranger = online("Stranger");
        PlayerData.initializeDatabaseConnection();
        waitForPlayerData();

        var plugins = server.getPluginManager();
        plugins.registerEvents(new MatchInstance.MatchInstanceEvents(), MetadataHandler.PLUGIN);
        plugins.registerEvents(new PlayerTeleportEvent.PlayerTeleportEventExecutor(), MetadataHandler.PLUGIN);
        plugins.registerEvents(new Recorder(), MetadataHandler.PLUGIN);
    }

    @AfterEach
    final void closeInstanceServer() throws Exception {
        try {
            MatchInstance.shutdown();
        } finally {
            try {
                if (previousSpectatorSetting != null) setAllowSpectators(previousSpectatorSetting);
                PlayerData.closeConnection();
                server.getScheduler().waitAsyncTasksFinished();
            } finally {
                MockBukkit.unmock();
                MetadataHandler.PLUGIN = previousPlugin;
                resetMagmaCore();
            }
        }
    }

    private PlayerMock online(String name) {
        PlayerMock player = server.addPlayer(name);
        player.teleport(overworld.getSpawnLocation());
        return player;
    }

    private void waitForPlayerData() throws InterruptedException {
        var scheduler = server.getScheduler();
        scheduler.performOneTick();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (Bukkit.getOnlinePlayers().stream().anyMatch(player -> !PlayerData.isDataLoaded(player.getUniqueId()))
                && System.nanoTime() < deadline) Thread.sleep(5);
        for (var player : Bukkit.getOnlinePlayers())
            assertTrue(PlayerData.isDataLoaded(player.getUniqueId()), "Timed out loading " + player.getName());
        scheduler.performOneTick();
    }

    protected static Location at(World world, double x, double y, double z) {
        return new Location(world, x, y, z);
    }

    protected Location start() {
        return at(instanceWorld, 0.5, 64, 0.5);
    }

    protected Location exit() {
        return at(overworld, 50.5, 64, 50.5);
    }

    protected TestInstance instance(int minPlayers, int maxPlayers) {
        return new TestInstance(instanceWorld, start(), exit(), minPlayers, maxPlayers);
    }

    protected void ticks(int ticks) {
        server.getScheduler().performTicks(ticks);
    }

    protected List<String> eventsMatching(Predicate<String> filter) {
        return events.stream().filter(filter).toList();
    }

    protected void setAllowSpectators(boolean allowed) throws ReflectiveOperationException {
        Field field = DungeonsConfig.class.getDeclaredField("allowSpectatorsInInstancedContent");
        field.setAccessible(true);
        if (previousSpectatorSetting == null) previousSpectatorSetting = field.getBoolean(null);
        field.setBoolean(null, allowed);
    }

    private static void resetMagmaCore() {
        try {
            Field instance = MagmaCore.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private final class Recorder implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void instantiate(MatchInstantiateEvent event) {
            events.add("instantiate" + (event.isCancelled() ? ":cancelled" : ""));
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void join(MatchJoinEvent event) {
            events.add("join:" + event.getPlayer().getName() + (event.isCancelled() ? ":cancelled" : ""));
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void leave(MatchLeaveEvent event) {
            events.add("leave:" + event.getPlayer().getName());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void start(MatchStartEvent event) {
            events.add("start");
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void end(MatchEndEvent event) {
            events.add("end:" + event.getInstance().getState());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void destroy(MatchDestroyEvent event) {
            events.add("destroy");
        }
    }

    /** The smallest real MatchInstance: one world is the whole region. */
    static final class TestInstance extends MatchInstance {
        TestInstance(World world, Location start, Location exit, int minPlayers, int maxPlayers) {
            super(start, exit, minPlayers, maxPlayers);
            this.world = world;
        }

        @Override
        protected boolean isInRegion(Location location) {
            return location != null && world.equals(location.getWorld());
        }

        void lobby(Location lobby) {
            lobbyLocation = lobby;
        }

        void permission(String permission) {
            this.permission = permission;
        }

        void win() {
            victory();
        }

        void lose() {
            defeat();
        }

        void destroyMatchNow() {
            destroyMatch();
        }
    }
}
