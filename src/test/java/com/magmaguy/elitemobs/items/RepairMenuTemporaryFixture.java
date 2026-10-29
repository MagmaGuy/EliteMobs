package com.magmaguy.elitemobs.items;

import com.google.common.collect.ImmutableMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.menus.premade.RepairMenuConfig;
import com.magmaguy.elitemobs.menus.RepairMenu;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
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
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mockStatic;

/** Repair menu with the production economy over an in-memory balance. Temporary scrap-retirement checks only. */
abstract class RepairMenuTemporaryFixture {
    private final FreshCustomEnchantmentTest itemFixture = new FreshCustomEnchantmentTest();
    private final Map<NamespacedKey, ItemType> originalItemTypes = new HashMap<>();
    private final Map<Material, Object> originalItemTypeSuppliers = new HashMap<>();
    private MockedStatic<DefaultConfig> defaults;
    private MockedStatic<PlayerData> playerData;
    protected PlayerMock player;
    protected Inventory menu;
    protected double balance;

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

    @BeforeEach void openRepairMenuFixture() throws Exception {
        itemFixture.open();
        supplyLegacyTagReader("diamond_chestplate", "white_dye", "purple_dye");
        defaults = mockStatic(DefaultConfig.class);
        playerData = mockStatic(PlayerData.class);
        playerData.when(() -> PlayerData.isDataLoaded(any(UUID.class))).thenReturn(true);
        playerData.when(() -> PlayerData.getCurrency(any(UUID.class))).thenAnswer(call -> balance);
        playerData.when(() -> PlayerData.setCurrency(any(UUID.class), anyDouble()))
                .thenAnswer(call -> { balance = call.getArgument(1); return null; });
        playerData.when(() -> PlayerData.setCurrencyAndGamblingDebt(any(UUID.class), anyDouble(), anyDouble()))
                .thenAnswer(call -> { balance = call.getArgument(1); return null; });
        configureMenu();
        var server = MockBukkit.getMock();
        player = server.addPlayer();
        server.getPluginManager().registerEvents(new RepairMenu.RepairMenuEvents(), MetadataHandler.PLUGIN);
    }

    @AfterEach void closeRepairMenuFixture() throws Exception {
        if (player != null) player.closeInventory();
        RepairMenu.shutdown();
        if (playerData != null) playerData.close();
        if (defaults != null) defaults.close();
        registryEntries().putAll(originalItemTypes);
        var itemTypeField = Material.class.getDeclaredField("itemType");
        itemTypeField.setAccessible(true);
        for (var entry : originalItemTypeSuppliers.entrySet())
            itemTypeField.set(entry.getKey(), entry.getValue());
        itemFixture.close();
    }

    /** A level 100 elite diamond chestplate with this much damage. */
    protected ItemStack chestplate(int damage) {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        var meta = (Damageable) item.getItemMeta();
        meta.setDamage(damage);
        ItemTagger.registerEliteItem(meta);
        item.setItemMeta(meta);
        ItemTagger.setItemLevel(item, 100);
        return item;
    }

    protected static int damage(ItemStack item) {
        return ((Damageable) item.getItemMeta()).getDamage();
    }

    protected void openMenu() {
        new RepairMenu().constructRepairMenu(player);
        menu = player.getOpenInventory().getTopInventory();
    }

    protected void clickPlayerSlot(int slot) {
        var view = player.getOpenInventory();
        for (int raw = menu.getSize(); raw < view.countSlots(); raw++) {
            if (view.convertSlot(raw) == slot) {
                clickRaw(raw);
                return;
            }
        }
        fail("Player slot is missing from the open inventory view");
    }

    protected void clickRaw(int raw) {
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
        MockBukkit.getMock().getPluginManager().callEvent(event);
        assertTrue(event.isCancelled(), "The menu owns moving its inputs and output");
    }

    private void configureMenu() {
        RepairMenuConfig.shopName = "Repair";
        RepairMenuConfig.infoSlot = 4;
        RepairMenuConfig.eliteItemInputInformationSlot = 20;
        RepairMenuConfig.repairCostInformationSlot = 22;
        RepairMenuConfig.outputInformationSlot = 24;
        RepairMenuConfig.eliteItemInputSlot = 29;
        RepairMenuConfig.repairCostSlot = 31;
        RepairMenuConfig.outputSlot = 33;
        RepairMenuConfig.cancelSlot = 27;
        RepairMenuConfig.confirmSlot = 35;
        RepairMenuConfig.infoButton = button(Material.PAPER, "Info", "Repair menu control");
        RepairMenuConfig.eliteItemInputInfoButton = button(Material.GREEN_BANNER, "Item", "Repair menu control");
        RepairMenuConfig.repairCostInfoButton = button(Material.GOLD_INGOT, "Cost", "Repair menu control");
        RepairMenuConfig.repairCostButton = button(Material.GOLD_NUGGET, "Cost $price $currencyName", "Have $currentAmount");
        RepairMenuConfig.outputInfoButton = button(Material.RED_BANNER, "Output", "Repair menu control");
        RepairMenuConfig.cancelButton = button(Material.BARRIER, "Cancel", "Repair menu control");
        RepairMenuConfig.confirmButton = button(Material.EMERALD, "Confirm", "Repair menu control");
    }

    private ItemStack button(Material material, String name, String lore) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(lore));
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
