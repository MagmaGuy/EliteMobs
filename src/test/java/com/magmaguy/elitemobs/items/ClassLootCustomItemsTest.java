package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.*;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ClassLootCustomItemsTest {
    @BeforeEach void open() {
        MockBukkit.mock();
        com.magmaguy.elitemobs.MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
    }
    @AfterEach void close() {
        CustomItem.getCustomItems().clear();
        CustomItem.getCustomItemStackList().clear();
        CustomItem.getCustomItemStackShopList().clear();
        MockBukkit.unmock();
    }

    private CustomItemsConfigFields parse(String yaml) throws Exception {
        var fields = new CustomItemsConfigFields("borrowed_forever.yml", true);
        var config = new YamlConfiguration();
        config.loadFromString(yaml);
        fields.setFileConfiguration(config);
        fields.processConfigFields();
        return fields;
    }

    @Test void ordinaryItemFormatRecognizesClassLootWithoutBossMetadata() throws Exception {
        var fields = parse("""
                isEnabled: true
                itemType: CLASS_LOOT
                material: DIAMOND_SWORD
                name: Borrowed Forever
                lore: [Goblin signed receipt., Receipt says MINE.]
                """);
        assertEquals("CLASS_LOOT", fields.getItemType().name());
        assertEquals("Borrowed Forever", fields.getName());
        assertEquals(2, fields.getLore().size());
        assertFalse(fields.getFileConfiguration().contains("entityType"));
        assertEquals(ClassLootFamily.SWORDS, fields.getClassLootFamily());
    }

    @Test void armorRequiresItsRoleAndRejectsMismatchedSlots() throws Exception {
        var ambiguous = parse("itemType: CLASS_LOOT\nmaterial: DIAMOND_HELMET\nname: Helm\nlore: []\n");
        assertFalse(ambiguous.isEnabled());
        var tank = parse("itemType: CLASS_LOOT\nmaterial: DIAMOND_HELMET\nclassLootFamily: TANK_HELMETS\nname: Helm\nlore: []\n");
        assertTrue(tank.isEnabled());
        assertEquals(ClassLootFamily.TANK_HELMETS, tank.getClassLootFamily());
        var wrong = parse("itemType: CLASS_LOOT\nmaterial: DIAMOND_HELMET\nclassLootFamily: TANK_BOOTS\nname: Helm\nlore: []\n");
        assertFalse(wrong.isEnabled());
    }

    @Test void magicFamiliesUseExistingWeaponType() throws Exception {
        var wand = parse("itemType: CLASS_LOOT\nmaterial: BLAZE_ROD\nweaponType: WANDS\nname: Wand\nlore: []\n");
        assertTrue(wand.isEnabled());
        assertEquals(ClassLootFamily.WANDS, wand.getClassLootFamily());
        assertFalse(parse("itemType: CLASS_LOOT\nmaterial: BLAZE_ROD\nname: Stick\nlore: []\n").isEnabled());
    }

    @Test void malformedIdentityDoesNotBecomeAPlaceholderSword() throws Exception {
        assertFalse(parse("itemType: CLASS_LOOT\nmaterial: WRONG\nname: Broken\nlore: []\n").isEnabled());
        assertFalse(parse("itemType: CLASS_LOOT\nmaterial: DIAMOND_SWORD\nlore: []\n").isEnabled());
        assertFalse(parse("itemType: CLASS_LOOT\nmaterial: DIAMOND_SWORD\nname: Broken\nlore: [123]\n").isEnabled());
    }

    @Test void legacyTypesKeepTheirExistingParsingRules() throws Exception {
        for (String type : List.of("CUSTOM", "UNIQUE", "DROPPABLE")) {
            var fields = parse("itemType: " + type + "\nmaterial: DIAMOND_HELMET\nname: Existing\nlore: []\nscalability: FIXED\n");
            assertTrue(fields.isEnabled());
            assertEquals(type, fields.getItemType().name());
            assertEquals(CustomItem.Scalability.FIXED, fields.getScalability());
        }
    }

    private CustomItem register() throws Exception {
        return new CustomItem(parse("itemType: CLASS_LOOT\nmaterial: DIAMOND_SWORD\nname: Borrowed Forever\nlore: []\n"));
    }

    @Test void classItemsRegisterByOrdinaryFilenameWithoutEnteringRandomPoolsOrShops() throws Exception {
        var item = register();
        assertSame(item, CustomItem.getCustomItem("borrowed_forever.yml"));
        assertFalse(CustomItem.getScalableItems().contains(item));
        assertTrue(CustomItem.getCustomItemStackShopList().isEmpty());
        assertTrue(CustomItem.getTieredLoot().values().stream().allMatch(List::isEmpty));
    }

    @Test void normalLootTableKeepsClassReferencesButDoesNotIndependentlyDropThem() throws Exception {
        register();
        var boss = new CustomBossesConfigFields("goblin.yml", true);
        boss.setUniqueLootList(List.of(Map.of("filename", "borrowed_forever.yml", "chance", 1.0)));
        var table = new CustomLootTable(boss);
        assertEquals(1, table.getEntries().size());
        var classEntry = (EliteCustomLootEntry) table.getEntries().getFirst();
        assertTrue(classEntry.isClassLoot());
        // Null drop context would fail inside a normal item drop. CLASS_LOOT must be excluded before rolling.
        table.bossDrop(null, 100, null, null);
        var ordinary = new CustomLootEntry() {
            @Override public void locationDrop(int level, org.bukkit.entity.Player player,
                    org.bukkit.Location location, com.magmaguy.elitemobs.mobconstructor.EliteEntity entity) { delivered++; }
            @Override public void directDrop(int level, org.bukkit.entity.Player player,
                    com.magmaguy.elitemobs.mobconstructor.EliteEntity entity) { delivered++; }
        };
        table.getEntries().add(ordinary);
        table.bossDrop(null, 100, null, null);
        assertEquals(1, delivered);
    }
    private int delivered;

    @Test void candidateEligibilityRespectsChanceAmountPermissionsAndDifficulty() throws Exception {
        register();
        var entries = new ArrayList<CustomLootEntry>();
        var entry = new EliteCustomLootEntry(entries, Map.of("filename", "borrowed_forever.yml", "difficultyID", List.of("0", "2")), "goblin.yml");
        var player = MockBukkit.getMock().addPlayer();
        var resolver = new com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver("dungeon", List.of());
        assertTrue(entry.eligibleForClassLoot(player, ids -> resolver.matches(ids, "Mythic", "goblin")));
        assertFalse(entry.eligibleForClassLoot(player, ids -> resolver.matches(ids, "Hard", "goblin")));
        entry.setChance(0);
        assertFalse(entry.eligibleForClassLoot(player, ids -> true));
        entry.setChance(1); entry.setAmount(0);
        assertFalse(entry.eligibleForClassLoot(player, ids -> true));
        entry.setAmount(1); entry.setPermission("loot.secret");
        assertFalse(entry.eligibleForClassLoot(player, ids -> true));
    }
}
