package com.magmaguy.elitemobs.items;

import com.google.common.collect.ImmutableMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.customitems.premade.ScrapEliteTinyConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.menus.RepairMenu;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.inventory.meta.ItemMetaMock;
import org.mockbukkit.mockbukkit.registry.RegistryMock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class RepairMenuBehaviorTest {
    private final FreshCustomEnchantmentTest itemFixture = new FreshCustomEnchantmentTest();
    private final Map<NamespacedKey, ItemType> originalItemTypes = new HashMap<>();
    private final Map<Material, Object> originalItemTypeSuppliers = new HashMap<>();
    private MockedStatic<DefaultConfig> defaults;
    private PlayerMock player;
    private Inventory menu;

    // MockBukkit does not implement the pre-PDC tag API; these fixtures contain only real PDC tags.
    private static final CustomItemTagContainer NO_LEGACY_TAGS =
            (CustomItemTagContainer) Proxy.newProxyInstance(CustomItemTagContainer.class.getClassLoader(),
                    new Class<?>[]{CustomItemTagContainer.class}, (proxy, method, arguments) -> {
                        if (method.getName().equals("hasCustomTag")) return false;
                        if (method.getName().equals("isEmpty")) return true;
                        throw new UnsupportedOperationException(method.getName());
                    });

    public static class PdcItemMeta extends ItemMetaMock {
        public PdcItemMeta() { }
        public PdcItemMeta(ItemMeta source) { super(source); }
        @Override public CustomItemTagContainer getCustomTagContainer() { return NO_LEGACY_TAGS; }
        @Override public PdcItemMeta clone() { return new PdcItemMeta(this); }
    }

    @BeforeEach void open() throws Exception {
        itemFixture.open();
        supplyLegacyTagReader("diamond_sword", "white_dye", "red_dye");
        defaults = mockStatic(DefaultConfig.class);
        configureMenu();
        var server = MockBukkit.getMock();
        player = server.addPlayer();
        server.getPluginManager().registerEvents(new RepairMenu.RepairMenuEvents(), MetadataHandler.PLUGIN);
    }

    @AfterEach void close() throws Exception {
        if (player != null) player.closeInventory();
        RepairMenu.shutdown();
        if (defaults != null) defaults.close();
        registryEntries().putAll(originalItemTypes);
        var itemTypeField = Material.class.getDeclaredField("itemType");
        itemTypeField.setAccessible(true);
        for (var entry : originalItemTypeSuppliers.entrySet())
            itemTypeField.set(entry.getKey(), entry.getValue());
        itemFixture.close();
    }

    @ParameterizedTest
    @EnumSource(value = Material.class, names = {"DIAMOND_HELMET", "DIAMOND_SWORD"})
    void existingIdentifiedScrapRepairsArmorAndWeaponsAfterItsDefinitionIsRestored(Material material) {
        // This item already exists before the clean definition is loaded. Its stable ID is enough.
        ItemStack scrap = heldScrap(Material.WHITE_DYE, "elite_scrap_tiny.yml");
        assertNull(ItemConsumables.resolve(scrap));
        new CustomItem(new ScrapEliteTinyConfig());
        assertTrue(ItemConsumables.is(scrap, ItemConsumables.Type.REPAIR_SCRAP));
        assertEquals(1, ItemConsumables.repairTier(scrap));

        ItemStack damaged = new ItemStack(material);
        var meta = (Damageable) damaged.getItemMeta();
        meta.setDamage(200);
        ItemTagger.registerEliteItem(meta);
        damaged.setItemMeta(meta);
        player.getInventory().setItem(0, damaged);
        player.getInventory().setItem(1, scrap);
        openMenu();
        clickPlayerSlot(0);
        assertEquals(damaged, menu.getItem(RepairMenuConfig.eliteItemInputSlot));
        clickPlayerSlot(1);

        assertEquals(100, ((Damageable) menu.getItem(RepairMenuConfig.outputSlot).getItemMeta()).getDamage());
        clickRaw(RepairMenuConfig.confirmSlot);
        ItemStack repaired = java.util.Arrays.stream(player.getInventory().getStorageContents())
                .filter(item -> item != null && item.getType() == material).findFirst().orElseThrow();
        assertEquals(100, ((Damageable) repaired.getItemMeta()).getDamage());
        assertTrue(ItemTagger.isEliteItem(repaired));
        assertNull(menu.getItem(RepairMenuConfig.eliteItemInputSlot));
        assertNull(menu.getItem(RepairMenuConfig.eliteScrapInputSlot));
        assertNull(menu.getItem(RepairMenuConfig.outputSlot));
        assertEquals(1, java.util.Arrays.stream(player.getInventory().getStorageContents())
                .filter(item -> item != null && !item.getType().isAir()).mapToInt(ItemStack::getAmount).sum());
    }

    @Test void unidentifiedOrMismatchedScrapIsRejectedWithoutConsumingEitherItem() {
        new CustomItem(new ScrapEliteTinyConfig());
        var noId = heldScrap(Material.WHITE_DYE, null);
        var wrongMaterial = heldScrap(Material.RED_DYE, "elite_scrap_tiny.yml");
        var unknownId = heldScrap(Material.WHITE_DYE, "missing_scrap.yml");
        for (var item : List.of(noId, wrongMaterial, unknownId)) {
            assertNull(ItemConsumables.resolve(item));
            assertEquals(0, ItemConsumables.repairTier(item));
        }
        player.getInventory().setItem(0, noId);
        player.getInventory().setItem(1, wrongMaterial);
        player.getInventory().setItem(2, unknownId);
        var damaged = new ItemStack(Material.DIAMOND_HELMET);
        var damagedMeta = (Damageable) damaged.getItemMeta();
        damagedMeta.setDamage(200);
        ItemTagger.registerEliteItem(damagedMeta);
        damaged.setItemMeta(damagedMeta);
        player.getInventory().setItem(3, damaged);
        openMenu();
        clickPlayerSlot(3);
        assertEquals(damaged, menu.getItem(RepairMenuConfig.eliteItemInputSlot));
        clickPlayerSlot(0);
        clickPlayerSlot(1);
        clickPlayerSlot(2);
        clickRaw(RepairMenuConfig.confirmSlot);
        assertNull(menu.getItem(RepairMenuConfig.eliteScrapInputSlot));
        assertNull(menu.getItem(RepairMenuConfig.outputSlot));
        assertEquals(noId, player.getInventory().getItem(0));
        assertEquals(wrongMaterial, player.getInventory().getItem(1));
        assertEquals(unknownId, player.getInventory().getItem(2));
        assertEquals(damaged, menu.getItem(RepairMenuConfig.eliteItemInputSlot));
        player.closeInventory();
        ItemStack returned = java.util.Arrays.stream(player.getInventory().getStorageContents())
                .filter(item -> item != null && item.getType() == Material.DIAMOND_HELMET)
                .findFirst().orElseThrow();
        assertEquals(damaged, returned, "Invalid scrap must not consume or alter the repair target");
    }

    private ItemStack heldScrap(Material material, String id) {
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.setDisplayName("Previously acquired scrap");
        meta.setLore(List.of("Existing item lore"));
        if (id != null) ItemTagger.registerCustomItemId(meta, id);
        item.setItemMeta(meta);
        return item;
    }

    private void openMenu() {
        new RepairMenu().constructRepairMenu(player);
        menu = player.getOpenInventory().getTopInventory();
    }

    private void clickPlayerSlot(int slot) {
        var view = player.getOpenInventory();
        for (int raw = menu.getSize(); raw < view.countSlots(); raw++) {
            if (view.convertSlot(raw) == slot) {
                clickRaw(raw);
                return;
            }
        }
        fail("Player slot is missing from the open inventory view");
    }

    private void clickRaw(int raw) {
        var event = new InventoryClickEvent(player.getOpenInventory(), InventoryType.SlotType.CONTAINER,
                raw, ClickType.LEFT, InventoryAction.PICKUP_ALL) {
            // MockBukkit's view converts chest slots correctly but getItem(raw) reads
            // player slots linearly. Native events use the converted inventory slot.
            @Override public ItemStack getCurrentItem() {
                var clicked = getClickedInventory();
                return clicked == null ? null : clicked.getItem(getSlot());
            }
        };
        assertEquals(player.getOpenInventory().convertSlot(raw), event.getSlot());
        assertNotNull(event.getCurrentItem(), "The click must target the actual occupied inventory slot");
        MockBukkit.getMock().getPluginManager().callEvent(event);
        assertTrue(event.isCancelled(), "The menu owns moving its inputs and output");
    }

    private void configureMenu() {
        RepairMenuConfig.shopName = "Repair";
        RepairMenuConfig.infoSlot = 4;
        RepairMenuConfig.eliteItemInputInformationSlot = 20;
        RepairMenuConfig.eliteScrapInputInformationSlot = 22;
        RepairMenuConfig.outputInformationSlot = 24;
        RepairMenuConfig.eliteItemInputSlot = 29;
        RepairMenuConfig.eliteScrapInputSlot = 31;
        RepairMenuConfig.outputSlot = 33;
        RepairMenuConfig.cancelSlot = 27;
        RepairMenuConfig.confirmSlot = 35;
        RepairMenuConfig.infoButton = button(Material.PAPER);
        RepairMenuConfig.eliteItemInputInfoButton = button(Material.GREEN_BANNER);
        RepairMenuConfig.eliteScrapInputInfoButton = button(Material.GREEN_BANNER);
        RepairMenuConfig.outputInfoButton = button(Material.RED_BANNER);
        RepairMenuConfig.cancelButton = button(Material.BARRIER);
        RepairMenuConfig.confirmButton = button(Material.EMERALD);
    }

    private ItemStack button(Material material) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.setLore(List.of("Repair menu control"));
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<NamespacedKey, ItemType> registryEntries() throws ReflectiveOperationException {
        var field = RegistryMock.class.getDeclaredField("keyedMap");
        field.setAccessible(true);
        return (Map<NamespacedKey, ItemType>) field.get(Registry.ITEM);
    }

    private void supplyLegacyTagReader(String... materials) throws ReflectiveOperationException {
        var entries = registryEntries();
        var itemTypeField = Material.class.getDeclaredField("itemType");
        itemTypeField.setAccessible(true);
        for (String material : materials) {
            var key = NamespacedKey.minecraft(material);
            var original = Registry.ITEM.get(key);
            originalItemTypes.put(key, original);
            var replacement = (ItemType) Proxy.newProxyInstance(ItemType.class.getClassLoader(),
                    new Class<?>[]{ItemType.Typed.class}, (proxy, method, arguments) -> {
                        if (method.getName().equals("getItemMetaClass")) return PdcItemMeta.class;
                        // Dyes have no native equipment modifiers; MockBukkit leaves this API unimplemented.
                        if (material.endsWith("_dye") && method.getName().equals("getDefaultAttributeModifiers"))
                            return ImmutableMultimap.of();
                        try { return method.invoke(original, arguments); }
                        catch (InvocationTargetException failure) { throw failure.getCause(); }
                    });
            entries.put(key, replacement);
            // Paper memoizes Material.asItemType independently of the registry. Bind this
            // fixture's type explicitly and restore the prior supplier during teardown.
            Material materialValue = Material.matchMaterial(material);
            originalItemTypeSuppliers.put(materialValue, itemTypeField.get(materialValue));
            itemTypeField.set(materialValue, (Supplier<ItemType>) () -> replacement);
        }
    }
}
