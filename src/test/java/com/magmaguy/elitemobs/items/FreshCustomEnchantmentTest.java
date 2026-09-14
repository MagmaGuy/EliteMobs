package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.StaticItemNamesConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.potioneffects.PotionEffectsConfig;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.items.upgradesystem.EliteEnchantmentItems;
import com.magmaguy.elitemobs.items.upgradesystem.UpgradeSystem;
import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.enchantments.EnchantmentItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.InputStream;
import java.nio.file.Files;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.List;
import com.google.common.collect.ImmutableMultimap;
import org.mockbukkit.mockbukkit.registry.RegistryMock;
import org.mockbukkit.mockbukkit.inventory.meta.ArmorMetaMock;
import org.mockbukkit.mockbukkit.inventory.meta.LeatherArmorMetaMock;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;

import static org.junit.jupiter.api.Assertions.*;

class FreshCustomEnchantmentTest {
    private final Map<NamespacedKey, ItemType> originalItemTypes = new java.util.HashMap<>();
    private MagmaCore previousCore;
    private static final CustomItemTagContainer EMPTY_OLD_TAGS = (CustomItemTagContainer) Proxy.newProxyInstance(
            CustomItemTagContainer.class.getClassLoader(), new Class<?>[]{CustomItemTagContainer.class},
            (proxy, method, arguments) -> {
                if (method.getName().equals("hasCustomTag")) return false;
                if (method.getName().equals("isEmpty")) return true;
                throw new UnsupportedOperationException(method.getName());
            });

    // Fresh items have no pre-PDC legacy tags. Preserve real MockBukkit metadata and PDC handling.
    public static class FreshLeatherMeta extends LeatherArmorMetaMock {
        public FreshLeatherMeta() { }
        public FreshLeatherMeta(ItemMeta meta) { super(meta); }
        @Override public CustomItemTagContainer getCustomTagContainer() { return EMPTY_OLD_TAGS; }
        @Override public FreshLeatherMeta clone() { return new FreshLeatherMeta(this); }
    }

    public static class FreshArmorMeta extends ArmorMetaMock {
        public FreshArmorMeta() { }
        public FreshArmorMeta(ItemMeta meta) { super(meta); }
        @Override public CustomItemTagContainer getCustomTagContainer() { return EMPTY_OLD_TAGS; }
        @Override public FreshArmorMeta clone() { return new FreshArmorMeta(this); }
    }

    public static class ResourcePlugin extends JavaPlugin {
        @Override public InputStream getResource(String name) {
            return FreshCustomEnchantmentTest.class.getClassLoader().getResourceAsStream(name);
        }
    }

    @BeforeEach void open() throws Exception {
        MockBukkit.mock();
        supplyHelmetHandAttributes();
        var plugin = MockBukkit.loadSimple(ResourcePlugin.class);
        MetadataHandler.PLUGIN = plugin;
        // Production initializes once per plugin classloader. Each mocked server needs its own owner.
        var coreInstance = MagmaCore.class.getDeclaredField("instance");
        coreInstance.setAccessible(true);
        previousCore = MagmaCore.getInstance();
        coreInstance.set(null, null);
        MagmaCore.createInstance(plugin);
        Files.createDirectories(plugin.getDataFolder().toPath());
        Files.writeString(plugin.getDataFolder().toPath().resolve("ProceduralItemGenerationSettings.yml"),
                "customEnchantmentsChance: 1.0\n");
        new ItemSettingsConfig();
        new ProceduralItemGenerationSettingsConfig();
        new EconomySettingsConfig();
        new StaticItemNamesConfig();
        EliteEnchantmentCatalog.prepare();
        EliteEnchantmentCatalog.publish();
        new EnchantmentsConfig();
        PotionEffectsConfig.initializeConfigs();
    }

    /** MockBukkit lacks vanilla attributes. Helmets have no modifiers when held in the hand. */
    @SuppressWarnings("unchecked")
    private void supplyHelmetHandAttributes() throws Exception {
        var mapField = RegistryMock.class.getDeclaredField("keyedMap");
        mapField.setAccessible(true);
        var entries = (Map<NamespacedKey, ItemType>) mapField.get(Registry.ITEM);
        for (String name : new String[]{"leather_helmet", "diamond_helmet"}) {
            var key = NamespacedKey.minecraft(name);
            var original = Registry.ITEM.get(key);
            originalItemTypes.put(key, original);
            var replacement = (ItemType) Proxy.newProxyInstance(ItemType.class.getClassLoader(),
                    new Class<?>[]{ItemType.Typed.class}, (proxy, method, arguments) -> {
                        if (method.getName().equals("getItemMetaClass"))
                            return name.equals("leather_helmet") ? FreshLeatherMeta.class : FreshArmorMeta.class;
                        if (method.getName().equals("getDefaultAttributeModifiers")
                                && arguments != null && arguments.length == 1
                                && arguments[0] == org.bukkit.inventory.EquipmentSlot.HAND)
                            return ImmutableMultimap.of();
                        try { return method.invoke(original, arguments); }
                        catch (InvocationTargetException failure) { throw failure.getCause(); }
                    });
            entries.put(key, replacement);
        }
    }

    @AfterEach @SuppressWarnings("unchecked") void close() throws Exception {
        EliteEnchantmentCatalog.close();
        CustomItem.getCustomItems().clear();
        CustomItem.getCustomItemStackList().clear();
        CustomItem.getCustomItemStackShopList().clear();
        CustomItem.getScalableItems().clear();
        CustomItem.getLimitedItems().clear();
        CustomItem.getFixedItems().clear();
        CustomItem.getWeighedFixedItems().clear();
        CustomItem.getTieredLoot().clear();
        var mapField = RegistryMock.class.getDeclaredField("keyedMap");
        mapField.setAccessible(true);
        ((Map<NamespacedKey, ItemType>) mapField.get(Registry.ITEM)).putAll(originalItemTypes);
        MockBukkit.unmock();
        var coreInstance = MagmaCore.class.getDeclaredField("instance");
        coreInstance.setAccessible(true);
        coreInstance.set(null, previousCore);
    }

    @Test void reportedHelmetRetainsHunterDataAndVisibleLoreOnCreationAndRedraw() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.loadFromString("""
                isEnabled: true
                material: LEATHER_HELMET
                name: '&5Night Wanderer''s Hood'
                lore:
                - '&7Embrace the shadows...'
                - '&7Reduces vision, but sharpens the mind.'
                enchantments:
                - PROTECTION_PROJECTILE,5
                - UNBREAKING,3
                - elitemobs:hunter,3
                potionEffects:
                - NIGHT_VISION,0,self,continuous
                - INCREASE_DAMAGE,1,self,continuous
                itemType: unique
                scalability: scalable
                level: 70
                soulbound: true
                """);
        var fields = new CustomItemsConfigFields("em_colosseum_main_quest_reward_helmet.yml", true);
        fields.setFileConfiguration(yaml);
        fields.processConfigFields();
        var custom = new CustomItem(fields);
        assertEquals(3, custom.getCustomEnchantments().get("elitemobs:hunter"));
        assertHunter(custom.generateDefaultsItemStack(null, false, null), 3);
        assertHunter(CustomItem.getCustomItemStackList().getFirst(), 3);
        var item = custom.generateItemStack(70, null, null);
        assertHunter(item, 3);
        new EliteItemLore(item, false);
        assertHunter(item, 3);
        System.out.println("Reported helmet tooltip after redraw: "
                + item.getItemMeta().getLore().stream().map(ChatColor::stripColor).toList());
    }

    @Test void proceduralHelmetRetainsRolledHunterDataAndVisibleLore() {
        assertEquals(1.0, ProceduralItemGenerationSettingsConfig.getCustomEnchantmentChance());
        var item = ItemConstructor.constructItemWithMaterial(Material.DIAMOND_HELMET, 70, null, false);
        int hunter = EnchantmentItems.inspectCustom(item.getItemMeta()).getOrDefault("elitemobs:hunter", 0);
        assertTrue(hunter >= 1 && hunter <= 3, "Guaranteed custom-enchantment roll must produce Hunter");
        assertHunter(item, hunter);
    }

    @Test void templatePositionSurvivesBookUpgradeAndRemovalThenReaddition() {
        var structure = ItemSettingsConfig.getLoreStructure();
        structure.clear();
        structure.addAll(List.of("Before", "$ifCustomEnchantmentsCustom Enchants", "$customEnchantments", "After"));
        var item = ItemConstructor.constructItemWithMaterial(Material.DIAMOND_HELMET, 70, null, false);
        item = EliteEnchantmentItems.ITEMS.previewAuthoredCustom(item, Map.of("elitemobs:hunter", 1)).apply(item);
        var blankBook = new ItemStack(Material.ENCHANTED_BOOK);
        var book = EliteEnchantmentItems.ITEMS.previewAuthoredCustom(blankBook, Map.of("elitemobs:hunter", 1)).apply(blankBook);
        var upgraded = UpgradeSystem.upgrade(item, book);
        assertEquals(List.of("Before", "Custom Enchants", "Hunter II", "After"), plainLore(upgraded));
        assertEquals(2, EnchantmentItems.inspectCustom(upgraded.getItemMeta()).get("elitemobs:hunter"));
        assertEquals(plainLore(upgraded), plainLore(EliteEnchantmentItems.ITEMS.refreshPresentation(upgraded)));

        var removed = EliteEnchantmentItems.ITEMS.previewAuthoredCustom(upgraded, Map.of()).apply(upgraded);
        new EliteItemLore(removed, false);
        assertEquals(List.of("Before", "After"), plainLore(removed));
        assertTrue(EnchantmentItems.inspectCustom(removed.getItemMeta()).isEmpty());
        var readded = UpgradeSystem.upgrade(removed, book);
        assertEquals(List.of("Before", "Custom Enchants", "Hunter I", "After"), plainLore(readded));
    }

    @Test void omittedPlaceholderHidesOnlyPresentationAndCanBeRestored() {
        var item = ItemConstructor.constructItemWithMaterial(Material.DIAMOND_HELMET, 70, null, false);
        var levels = EnchantmentItems.inspectCustom(item.getItemMeta());
        var structure = ItemSettingsConfig.getLoreStructure();
        var original = List.copyOf(structure);
        structure.clear();
        structure.add("Host lore");
        new EliteItemLore(item, false);
        assertEquals(List.of("Host lore"), plainLore(item));
        assertEquals(levels, EnchantmentItems.inspectCustom(item.getItemMeta()));
        assertEquals(List.of("Host lore"), plainLore(EliteEnchantmentItems.ITEMS.refreshPresentation(item)));
        structure.clear();
        structure.addAll(original);
        new EliteItemLore(item, false);
        assertHunter(item, levels.get("elitemobs:hunter"));
    }

    @Test void existingPrefixMovesToTemplateAndExternalEditsStillFailAtomically() {
        var item = ItemConstructor.constructItemWithMaterial(Material.DIAMOND_HELMET, 70, null, false);
        var levels = EnchantmentItems.inspectCustom(item.getItemMeta());
        item = EliteEnchantmentItems.ITEMS.refreshPresentation(item, java.util.function.UnaryOperator.identity(), host -> 0);
        var meta = item.getItemMeta();
        var root = new NamespacedKey("magmacore", "enchantment_presentation");
        var record = meta.getPersistentDataContainer().get(root, org.bukkit.persistence.PersistentDataType.TAG_CONTAINER);
        assertNotNull(record);
        record.remove(new NamespacedKey("magmacore", "position"));
        meta.getPersistentDataContainer().set(root, org.bukkit.persistence.PersistentDataType.TAG_CONTAINER, record);
        item.setItemMeta(meta);
        new EliteItemLore(item, false);
        assertHunter(item, levels.get("elitemobs:hunter"));
        meta = item.getItemMeta();
        var lore = meta.getLore();
        lore.set(indexContaining(plainLore(item), "Hunter"), "Externally changed");
        meta.setLore(lore);
        item.setItemMeta(meta);
        var snapshot = item.clone();
        var modified = item;
        assertThrows(IllegalArgumentException.class, () -> new EliteItemLore(modified, false));
        assertEquals(snapshot, item);
    }

    private static List<String> plainLore(ItemStack item) {
        return item.getItemMeta().getLore().stream().map(ChatColor::stripColor).toList();
    }

    private static void assertHunter(ItemStack item, int level) {
        assertNotNull(item);
        assertEquals(level, EnchantmentItems.inspectCustom(item.getItemMeta()).get("elitemobs:hunter"));
        assertNotNull(item.getItemMeta().getLore());
        assertTrue(item.getItemMeta().getLore().stream().map(ChatColor::stripColor)
                .anyMatch(line -> line.contains("Hunter")), "Hunter must be visible in the tooltip");
        var lore = item.getItemMeta().getLore().stream().map(ChatColor::stripColor).toList();
        int hunter = indexContaining(lore, "Hunter");
        int header = indexContaining(lore, "Custom Enchants");
        assertTrue(header >= 0 && hunter > header,
                "Hunter must appear in the configured Custom Enchants section: " + lore);
        int effects = indexContaining(lore, "Effects");
        assertTrue(effects < 0 || hunter < effects, "Hunter must precede the Effects section");
    }

    private static int indexContaining(java.util.List<String> lore, String text) {
        for (int index = 0; index < lore.size(); index++) if (lore.get(index).contains(text)) return index;
        return -1;
    }
}
