package com.magmaguy.elitemobs.peacebanner;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.BannerMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeaceBannerFeatureTest {
    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        world = server.addSimpleWorld("peace_banner_world");

        setPeaceBannerField("enabled", true);
        setPeaceBannerField("craftable", true);
        setPeaceBannerField("chunkRadius", 0);
        setPeaceBannerField("suppressEvents", true);
        setPeaceBannerField("recipeShape", List.of("BBB", "BWB", "BBB"));
        setPeaceBannerField("recipeIngredients", Map.of("B", "BONE", "W", "ANY_BANNER"));
        setPeaceBannerField("itemName", "&aPeace Banner");
        setPeaceBannerField("itemLore", List.of("&7Place to create a peaceful zone", "&7Prevents elite mob spawning"));
        setPeaceBannerField("placedMessage", "&aPeace Banner placed! Elite mobs will not spawn within a &f$radius chunk &aradius.");
        setPeaceBannerField("removedMessage", "&cPeace Banner removed. Elite mobs can now spawn in this area again.");

        PeaceBannerManager.shutdown();
    }

    @AfterEach
    void tearDown() {
        PeaceBannerManager.shutdown();
        MetadataHandler.PLUGIN = null;
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void createsTaggedPeaceBannerItemAndRejectsOrdinaryBanners() {
        ItemStack peaceBanner = PeaceBannerItem.createPeaceBanner();

        assertEquals(Material.BLUE_BANNER, peaceBanner.getType());
        assertTrue(PeaceBannerItem.isPeaceBanner(peaceBanner));
        assertFalse(PeaceBannerItem.isPeaceBanner(new ItemStack(Material.BLUE_BANNER)));

        BannerMeta meta = assertInstanceOf(BannerMeta.class, peaceBanner.getItemMeta());
        assertEquals(11, meta.getPatterns().size());
        assertTrue(meta.hasLore());
        assertEquals(2, meta.getLore().size());
        assertTrue(meta.hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));

        Pattern firstPattern = meta.getPatterns().get(0);
        assertNotNull(firstPattern.getColor());
        assertNotNull(firstPattern.getPattern());
    }

    @Test
    void protectedChunkReferenceCountsSurviveOneOfTwoBannersBeingRemoved() {
        Location firstBanner = new Location(world, 2, 64, 2);
        Location secondBanner = new Location(world, 8, 64, 8);

        PeaceBannerManager.registerBanner(firstBanner);
        PeaceBannerManager.registerBanner(secondBanner);

        assertEquals(2, PeaceBannerManager.getBannerCount());
        assertTrue(PeaceBannerManager.isProtected(firstBanner));

        assertTrue(PeaceBannerManager.unregisterBanner(firstBanner));
        assertEquals(1, PeaceBannerManager.getBannerCount());
        assertTrue(PeaceBannerManager.isProtected(firstBanner));

        assertTrue(PeaceBannerManager.unregisterBanner(secondBanner));
        assertEquals(0, PeaceBannerManager.getBannerCount());
        assertFalse(PeaceBannerManager.isProtected(secondBanner));
    }

    @Test
    void chunkValidationRemovesProtectionWhenTrackedBannerBlockIsGone() {
        Location bannerLocation = new Location(world, 4, 64, 4);
        bannerLocation.getBlock().setType(Material.BLUE_BANNER);
        PeaceBannerManager.registerBanner(bannerLocation);

        assertTrue(PeaceBannerManager.isProtected(bannerLocation));

        bannerLocation.getBlock().setType(Material.AIR);
        PeaceBannerManager.validateChunk(bannerLocation.getChunk());

        assertEquals(0, PeaceBannerManager.getBannerCount());
        assertFalse(PeaceBannerManager.isProtected(bannerLocation));
    }

    @Test
    void listenerRegistersPlacedPeaceBannerAndUnregistersBrokenBannerBlock() {
        PeaceBannerListener listener = new PeaceBannerListener();
        PlayerMock player = server.addPlayer();
        Location bannerLocation = new Location(world, 12, 64, 12);
        Block bannerBlock = bannerLocation.getBlock();
        BlockState replacedState = bannerBlock.getState();
        bannerBlock.setType(Material.BLUE_BANNER);

        listener.onBlockPlace(new BlockPlaceEvent(
                bannerBlock,
                replacedState,
                bannerLocation.clone().subtract(0, 1, 0).getBlock(),
                PeaceBannerItem.createPeaceBanner(),
                player,
                true,
                EquipmentSlot.HAND));

        assertTrue(PeaceBannerManager.isProtected(bannerLocation));
        assertEquals(1, PeaceBannerManager.getBannerCount());

        listener.onBlockBreak(new BlockBreakEvent(bannerBlock, player));

        assertFalse(PeaceBannerManager.isProtected(bannerLocation));
        assertEquals(0, PeaceBannerManager.getBannerCount());
    }

    private static void setPeaceBannerField(String fieldName, Object value) throws Exception {
        Field field = PeaceBannerConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
