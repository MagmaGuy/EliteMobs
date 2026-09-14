package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the real loot-table and entry delivery methods; no server or database is launched. */
class TreasureChestLootDeliveryTest {
    private ServerMock server;
    private PlayerMock player;
    private Location location;
    private MockedStatic<PlayerData> playerData;
    private final Map<Field, Object> previousConfig = new java.util.HashMap<>();

    @BeforeEach void open() throws Exception {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        player = server.addPlayer();
        // MockBukkit does not provide the player's native crafting view until an inventory is opened.
        player.openInventory(server.createInventory(player, 9));
        location = player.getLocation().clone();
        playerData = mockStatic(PlayerData.class);
        config(DefaultConfig.class, "treasureChestNoDropMessage", "EMPTY_CHEST");
        config(ItemSettingsConfig.class, "directDropCustomLootMessage", "Received $itemName");
        config(ItemSettingsConfig.class, "directDropMinecraftLootMessage", "Received $itemName");
        config(SoundsConfig.class, "treasureChestOpenSound", "minecraft:block.chest.open");
        for (String field : List.of("greedListTitle", "greedListLore1", "greedListLore2", "greedListLore3",
                "needListTitle", "needListLore1", "needListLore2", "needListLore3"))
            config(com.magmaguy.elitemobs.config.menus.premade.LootMenuConfig.class, field, field);
    }

    @AfterEach void close() throws Exception {
        try {
            assertAll(TreasureChest::shutdown, SharedLootTable::shutdown,
                    com.magmaguy.elitemobs.menus.LootMenu::shutdown,
                    () -> CustomItem.getCustomItems().clear(),
                    () -> { if (playerData != null) playerData.close(); },
                    () -> { for (var old : previousConfig.entrySet()) old.getKey().set(null, old.getValue()); });
        } finally {
            MockBukkit.unmock();
        }
    }

    @ParameterizedTest
    @CsvSource({"false,0", "false,1", "false,2", "true,0", "true,1", "true,2"})
    void missingItemReportsAnEmptyChestInsteadOfClaimingDelivery(boolean direct, int mode) throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", direct);
        var table = table(Map.of("filename", "missing_scrap.yml", "chance", 1));
        open(table, mode);
        assertEquals("EMPTY_CHEST", player.nextMessage());
        assertEquals(0, awarded(Material.DIAMOND));
    }

    @ParameterizedTest
    @CsvSource({"false,0", "false,1", "false,2", "true,0", "true,1", "true,2"})
    void mixedMissingAndValidEntriesStillAwardTheValidItem(boolean direct, int mode) throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", direct);
        item("good.yml", Material.DIAMOND);
        var table = table(Map.of("filename", "missing_scrap.yml", "chance", 1),
                Map.of("filename", "good.yml", "chance", 1));
        open(table, mode);
        assertEquals(1, awarded(Material.DIAMOND));
        assertNoEmptyMessage();
    }

    @ParameterizedTest
    @CsvSource({"false,0", "false,1", "false,2", "true,0", "true,1", "true,2"})
    void onlyTheSelectedDungeonDifficultyCanAwardItsChestItem(boolean direct, int mode) throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", direct);
        var resolver = new DifficultyResolver("manor.yml", List.of(
                Map.of("id", 0, "name", "Normal"), Map.of("id", 1, "name", "Hard"),
                Map.of("id", 2, "name", "Mythic")));
        var dungeon = mock(DungeonInstance.class);
        when(dungeon.matchesDifficulty(anyList(), anyString())).thenAnswer(call ->
                resolver.matches(call.getArgument(0), 1, call.getArgument(1)));
        playerData.when(() -> PlayerData.getMatchInstance(player)).thenReturn(dungeon);
        item("normal.yml", Material.DIAMOND);
        item("hard.yml", Material.EMERALD);
        item("mythic.yml", Material.GOLD_INGOT);
        var table = table(Map.of("filename", "normal.yml", "chance", 1, "difficultyID", 0),
                Map.of("filename", "hard.yml", "chance", 1, "difficultyID", List.of("Hard")),
                Map.of("filename", "mythic.yml", "chance", 1, "difficultyID", 2));
        open(table, mode);
        assertEquals(0, awarded(Material.DIAMOND));
        assertEquals(1, awarded(Material.EMERALD));
        assertEquals(0, awarded(Material.GOLD_INGOT));
        assertNoEmptyMessage();
    }

    @Test void directVanillaLootOverflowDropsOnTheGroundInsteadOfDisappearing() throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", true);
        // MockBukkit's addItem scans armor/offhand slots too; fill every slot to force its overflow path.
        var full = new ItemStack[player.getInventory().getSize()];
        java.util.Arrays.setAll(full, ignored -> new ItemStack(Material.STONE, 64));
        player.getInventory().setContents(full);
        var table = new CustomLootTable();
        new VanillaCustomLootEntry(table.getEntries(), "material=DIAMOND:chance=1:amount=2", "chest.yml");
        open(table, 0);
        assertEquals(2, awarded(Material.DIAMOND));
        assertNoEmptyMessage();
    }

    @Test void failedItemConstructionDoesNotAbortTheRemainingGroundLoot() throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", false);
        var unavailable = mock(CustomItem.class, CALLS_REAL_METHODS);
        var permission = CustomItem.class.getDeclaredField("permission");
        permission.setAccessible(true);
        permission.set(unavailable, "");
        doReturn(CustomItem.Scalability.FIXED).when(unavailable).getScalability();
        doReturn(null).when(unavailable).generateItemStack(anyInt(), any(), any());
        CustomItem.getCustomItems().put("unavailable.yml", unavailable);
        item("good.yml", Material.DIAMOND);
        var table = table(Map.of("filename", "unavailable.yml", "chance", 1),
                Map.of("filename", "good.yml", "chance", 1));
        assertDoesNotThrow(() -> open(table, 0));
        assertEquals(1, awarded(Material.DIAMOND));
        assertNoEmptyMessage();
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void guaranteedCommandEntryDispatchesExactlyTheAuthoredAmount(boolean direct) throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", direct);
        var executions = new AtomicInteger();
        server.getCommandMap().register("test", new Command("chestaward") {
            @Override public boolean execute(CommandSender sender, String label, String[] arguments) {
                assertArrayEquals(new String[]{player.getName()}, arguments);
                executions.incrementAndGet();
                return true;
            }
        });
        var table = new CustomLootTable();
        new CommandLootTable(table.getEntries(), "command=chestaward %player%:chance=1:amount=2", "chest.yml");
        open(table, 0);
        assertEquals(2, executions.get());
        assertNoEmptyMessage();
    }

    @ParameterizedTest
    @CsvSource({"false,vanilla", "true,vanilla", "false,serialized", "true,serialized"})
    void genericBossOverloadsDeliverSupportedItemEntries(boolean direct, String kind) {
        CustomLootEntry entry;
        if (kind.equals("vanilla")) {
            var entries = new java.util.ArrayList<CustomLootEntry>();
            entry = new VanillaCustomLootEntry(entries, "material=DIAMOND:amount=2:chance=1", "boss.yml");
        } else {
            var serialized = mock(ItemStackCustomLootEntry.class, CALLS_REAL_METHODS);
            doReturn(new ItemStack(Material.DIAMOND)).when(serialized).generateItemStack();
            when(serialized.getAmount()).thenReturn(2);
            entry = serialized;
        }
        var boss = mock(com.magmaguy.elitemobs.mobconstructor.EliteEntity.class);
        if (direct) entry.directDrop(10, player, boss);
        else entry.locationDrop(10, player, location, boss);
        assertEquals(2, awarded(Material.DIAMOND));
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void zeroCurrencyIsNotReportedAsAnAward(boolean direct) throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", direct);
        var table = new CustomLootTable();
        new CurrencyCustomLootEntry(table.getEntries(), 0);
        try (var economy = mockStatic(com.magmaguy.elitemobs.economy.EconomyHandler.class)) {
            open(table, 0);
            assertEquals("EMPTY_CHEST", player.nextMessage());
            economy.verifyNoInteractions();
        }
    }

    @ParameterizedTest
    @CsvSource({"false", "true"})
    void bossEquipmentReportsDeliveryOnlyWhenItEntersTheSharedPool(boolean direct) {
        var other = server.addPlayer();
        other.openInventory(server.createInventory(other, 9));
        var boss = mock(com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity.class);
        when(boss.getDamagers()).thenReturn(new java.util.HashMap<>(Map.of(player, 20D, other, 20D)));
        when(boss.getLocation()).thenReturn(location);
        var dungeon = mock(DungeonInstance.class);
        when(dungeon.matchesDifficulty(anyList(), anyString())).thenReturn(true);
        playerData.when(() -> PlayerData.getMatchInstance(player)).thenReturn(dungeon);
        var fields = mock(com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields.class);
        when(fields.getWeaponType()).thenReturn(com.magmaguy.elitemobs.skills.SkillType.SWORDS);
        var item = mock(CustomItem.class);
        when(item.getCustomItemsConfigFields()).thenReturn(fields);
        when(item.generateItemStack(anyInt(), isNull(), same(boss))).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
        CustomItem.getCustomItems().put("shared_sword.yml", item);
        var entry = new EliteCustomLootEntry(new java.util.ArrayList<>(),
                Map.of("filename", "shared_sword.yml", "difficultyID", 1), "boss.yml");
        boolean delivered = direct ? entry.directDrop(10, player, boss)
                : entry.locationDrop(10, player, location, boss);
        assertTrue(delivered);
        var table = SharedLootTable.getSharedLootTables().get(boss);
        assertNotNull(table);
        assertEquals(1, table.getLoot().size());
        assertEquals(0, awarded(Material.DIAMOND_SWORD));
        when(item.generateItemStack(anyInt(), isNull(), same(boss))).thenReturn(null);
        assertFalse(direct ? entry.directDrop(10, player, boss) : entry.locationDrop(10, player, location, boss));
        assertEquals(1, table.getLoot().size(), "Failed generation must not fabricate another shared award");
    }

    @Test void sharedPoolRejectsClosedSessionsAndEntriesWithoutRecipients() throws Exception {
        var boss = mock(com.magmaguy.elitemobs.mobconstructor.EliteEntity.class);
        when(boss.getDamagers()).thenReturn(new java.util.HashMap<>(Map.of(player, 20D)));
        when(boss.getLocation()).thenReturn(location);
        var table = new SharedLootTable(boss);
        assertTrue(table.addLoot(new ItemStack(Material.DIAMOND)));
        set(table, "closed", true);
        assertFalse(table.addLoot(new ItemStack(Material.EMERALD)));
        assertEquals(1, table.getLoot().size());
        when(boss.getDamagers()).thenReturn(new java.util.HashMap<>());
        var empty = new SharedLootTable(boss);
        assertFalse(empty.addLoot(new ItemStack(Material.DIAMOND)));
        assertTrue(empty.getLoot().isEmpty());
    }

    @Test void emptySingleChestStillOpensAndEmptyGroupChestOnlyOpensOncePerPlayer() throws Exception {
        config(ItemSettingsConfig.class, "putLootDirectlyIntoPlayerInventory", false);
        location.getBlock().setType(Material.CHEST);
        var single = chest(TreasureChest.DropStyle.SINGLE);
        single.doInteraction(player);
        assertEquals("EMPTY_CHEST", player.nextMessage());
        assertEquals(Material.AIR, location.getBlock().getType());
        location.getBlock().setType(Material.CHEST);
        var group = chest(TreasureChest.DropStyle.GROUP);
        group.doInteraction(player);
        assertEquals("EMPTY_CHEST", player.nextMessage());
        assertEquals(Material.CHEST, location.getBlock().getType());
        group.doInteraction(player);
        assertNull(player.nextMessage(), "An instanced GROUP chest must remain once per player");
    }

    private TreasureChest chest(TreasureChest.DropStyle style) throws Exception {
        var fields = new CustomTreasureChestConfigFields("chest.yml", true);
        set(fields, "instanced", true);
        set(fields, "dropStyle", style);
        set(fields, "customLootTable", new CustomLootTable());
        String position = location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + ",0,0";
        return new TreasureChest(fields, position, 0);
    }

    /** Only item construction is stubbed; lookup, eligibility, chest dispatch and delivery are real. */
    private void item(String filename, Material material) {
        var item = mock(CustomItem.class);
        when(item.getScalability()).thenReturn(CustomItem.Scalability.SCALABLE);
        when(item.generateItemStack(anyInt(), any(), any())).thenAnswer(ignored -> new ItemStack(material));
        when(item.generateItemStackExact(anyInt(), any(), any())).thenAnswer(ignored -> new ItemStack(material));
        when(item.dropPlayerLoot(any(), anyInt(), any(), any())).thenAnswer(call -> {
            Location drop = call.getArgument(2);
            return drop.getWorld().dropItem(drop, new ItemStack(material));
        });
        when(item.dropPlayerLootExact(any(), anyInt(), any(), any())).thenAnswer(call -> {
            Location drop = call.getArgument(2);
            return drop.getWorld().dropItem(drop, new ItemStack(material));
        });
        CustomItem.getCustomItems().put(filename, item);
    }

    @SafeVarargs private static CustomLootTable table(Map<String, Object>... entries) {
        var table = new CustomLootTable();
        for (var entry : entries) new EliteCustomLootEntry(table.getEntries(), entry, "chest.yml");
        return table;
    }

    private void open(CustomLootTable table, int mode) {
        switch (mode) {
            case 0 -> table.treasureChestDrop(player, 1, location);
            case 1 -> table.treasureChestDropAtLevel(player, 70, location);
            case 2 -> table.treasureChestDropScalableToPlayerLevel(player, 1, 70, location);
            default -> throw new AssertionError("Unknown fixture mode");
        }
    }

    private int awarded(Material material) {
        int stored = java.util.Arrays.stream(player.getInventory().getStorageContents())
                .filter(java.util.Objects::nonNull).filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount).sum();
        return stored + location.getWorld().getEntitiesByClass(Item.class).stream()
                .filter(item -> item.getItemStack().getType() == material)
                .mapToInt(item -> item.getItemStack().getAmount()).sum();
    }

    private void assertNoEmptyMessage() {
        String message;
        while ((message = player.nextMessage()) != null) assertNotEquals("EMPTY_CHEST", message);
    }

    private void config(Class<?> owner, String name, Object value) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        if (!previousConfig.containsKey(field)) previousConfig.put(field, field.get(null));
        field.set(null, value);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
