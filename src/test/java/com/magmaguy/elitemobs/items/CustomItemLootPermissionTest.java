package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Item construction is supplied; permission checks and ground-loot delivery execute normally. */
class CustomItemLootPermissionTest {
    private static final String PERMISSION = "elitemobs.loot.fixture";
    private ServerMock server;
    private World world;
    private Location location;
    private JavaPlugin previousPlugin;

    @BeforeEach
    void open() {
        previousPlugin = MetadataHandler.PLUGIN;
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        var backingWorld = server.addSimpleWorld("loot");
        world = spy(backingWorld);
        // MockBukkit supports actual drops but not Item.setOwner. Observe that Bukkit call directly.
        doAnswer(call -> {
            Item dropped = spy(backingWorld.dropItem(call.getArgument(0), call.getArgument(1)));
            doNothing().when(dropped).setOwner(any());
            return dropped;
        }).when(world).dropItem(any(Location.class), any(ItemStack.class));
        location = new Location(world, 0, 64, 0);
    }

    @AfterEach
    void close() {
        server.getScheduler().cancelTasks(MetadataHandler.PLUGIN);
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = previousPlugin;
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(booleans = {false, true})
    void permissionGatedLootWithoutAPlayerIsRejectedWithoutThrowing(boolean exact) throws Exception {
        CustomItem customItem = item(PERMISSION);

        assertNull(drop(customItem, null, exact));

        assertTrue(location.getWorld().getEntitiesByClass(Item.class).isEmpty());
        verify(customItem, never()).generateItemStack(anyInt(), any(), any());
        verify(customItem, never()).generateItemStackExact(anyInt(), any(), any());
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(booleans = {false, true})
    void playerWithoutPermissionCannotReceiveGatedLoot(boolean exact) throws Exception {
        var player = server.addPlayer();

        assertNull(drop(item(PERMISSION), player, exact));
        assertTrue(location.getWorld().getEntitiesByClass(Item.class).isEmpty());
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(booleans = {false, true})
    void permittedPlayerReceivesLootWithTheirOwnership(boolean exact) throws Exception {
        var player = server.addPlayer();
        player.addAttachment(MetadataHandler.PLUGIN, PERMISSION, true);

        Item dropped = drop(item(PERMISSION), player, exact);

        assertNotNull(dropped);
        verify(dropped).setOwner(player.getUniqueId());
        assertEquals(new ItemStack(Material.DIAMOND, 2), dropped.getItemStack());
        assertEquals(1, location.getWorld().getEntitiesByClass(Item.class).size());
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(booleans = {false, true})
    void unrestrictedLootWithoutAPlayerStillDropsForSharedPickup(boolean exact) throws Exception {
        Item dropped = drop(item(""), null, exact);

        assertNotNull(dropped);
        verify(dropped, never()).setOwner(any());
        assertEquals(new ItemStack(Material.DIAMOND, 2), dropped.getItemStack());
        assertEquals(1, location.getWorld().getEntitiesByClass(Item.class).size());
    }

    private CustomItem item(String requiredPermission) throws ReflectiveOperationException {
        CustomItem customItem = mock(CustomItem.class, CALLS_REAL_METHODS);
        var permission = CustomItem.class.getDeclaredField("permission");
        permission.setAccessible(true);
        permission.set(customItem, requiredPermission);
        doReturn(CustomItem.Scalability.FIXED).when(customItem).getScalability();
        doAnswer(ignored -> new ItemStack(Material.DIAMOND, 2)).when(customItem)
                .generateItemStack(anyInt(), any(), any());
        doAnswer(ignored -> new ItemStack(Material.DIAMOND, 2)).when(customItem)
                .generateItemStackExact(anyInt(), any(), any());
        return customItem;
    }

    private Item drop(CustomItem item, Player player, boolean exact) {
        return exact ? item.dropPlayerLootExact(player, 10, location, null)
                : item.dropPlayerLoot(player, 10, location, null);
    }
}
