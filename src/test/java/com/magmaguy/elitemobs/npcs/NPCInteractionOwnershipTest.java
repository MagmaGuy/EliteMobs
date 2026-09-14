package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.npcs.scripts.ScriptableNPC;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NPCInteractionOwnershipTest {
    private ServerMock server;
    private PlayerMock player;
    private Villager villager;
    private NPCEntity npc;
    private MockedStatic<PlayerData> playerData;

    @BeforeEach void open() {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        var world = server.addSimpleWorld("npc-interactions");
        player = server.addPlayer();
        villager = (Villager) world.spawnEntity(world.getSpawnLocation(), EntityType.VILLAGER);
        var config = mock(NPCsConfigFields.class);
        when(config.getName()).thenReturn("Configured NPC name");
        when(config.getInteractionType()).thenReturn(NPCInteractions.NPCInteractionType.NONE);
        npc = mock(NPCEntity.class);
        when(npc.getUuid()).thenReturn(UUID.randomUUID());
        when(npc.getVillager()).thenReturn(villager);
        when(npc.getNPCsConfigFields()).thenReturn(config);
        EntityTracker.registerNPCEntity(npc);
        playerData = mockStatic(PlayerData.class);
        server.getPluginManager().registerEvents(new NPCInteractions(), MetadataHandler.PLUGIN);
    }

    @AfterEach void close() {
        NPCInteractions.shutdown();
        EntityTracker.getNpcEntities().clear();
        if (playerData != null) playerData.close();
        MockBukkit.unmock();
    }

    @Test void nativeTradingIsBlockedByNpcOwnershipWhenTheVanillaNameIsEmpty() {
        villager.setCustomName(null);
        var event = merchantOpen(villager, "Villager");
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled());
    }

    @Test void ordinaryMerchantsAreNotBlockedByMatchingAnNpcTitle() {
        var ordinary = (Villager) villager.getWorld().spawnEntity(villager.getLocation(), EntityType.VILLAGER);
        var event = merchantOpen(ordinary, "Configured NPC name");
        server.getPluginManager().callEvent(event);
        assertFalse(event.isCancelled());
    }

    @Test void genericAndPositionedClicksBothCancelTradingButDispatchTheRoleOnce() {
        var generic = new PlayerInteractEntityEvent(player, villager);
        var positioned = new PlayerInteractAtEntityEvent(player, villager, new Vector());
        server.getPluginManager().callEvent(generic);
        server.getPluginManager().callEvent(positioned);
        assertTrue(generic.isCancelled(), "The generic interaction must not open vanilla trading");
        assertTrue(positioned.isCancelled());
        verify(npc, times(1)).runScripts(ScriptableNPC.ON_INTERACT, null, player);

        server.getScheduler().performOneTick();
        server.getPluginManager().callEvent(new PlayerInteractAtEntityEvent(player, villager, new Vector()));
        verify(npc, times(2)).runScripts(ScriptableNPC.ON_INTERACT, null, player);
    }

    @Test void cancelledAndNonNpcInteractionsDoNotDispatchAnNpcRole() {
        var cancelled = new PlayerInteractEntityEvent(player, villager);
        cancelled.setCancelled(true);
        server.getPluginManager().callEvent(cancelled);
        var ordinary = villager.getWorld().spawnEntity(villager.getLocation(), EntityType.VILLAGER);
        var unrelated = new PlayerInteractEntityEvent(player, ordinary);
        server.getPluginManager().callEvent(unrelated);
        assertTrue(cancelled.isCancelled());
        assertFalse(unrelated.isCancelled());
        verify(npc, never()).runScripts(ScriptableNPC.ON_INTERACT, null, player);
    }

    private InventoryOpenEvent merchantOpen(Villager merchant, String title) {
        var inventory = mock(MerchantInventory.class);
        when(inventory.getType()).thenReturn(InventoryType.MERCHANT);
        when(inventory.getMerchant()).thenReturn(merchant);
        var view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(inventory);
        when(view.getTitle()).thenReturn(title);
        when(view.getPlayer()).thenReturn(player);
        return new InventoryOpenEvent(view);
    }
}
