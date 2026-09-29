package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Difficulty;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig.Rank;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.ClassLootCoverage;
import com.magmaguy.elitemobs.items.ClassLootFamily;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.ClassLootItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** The actual loot table filters and delivers; only item construction is supplied. */
class ExternalInstanceLootTest {
    private static final Material[] MATERIALS = {Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT};
    private ServerMock server;
    private PlayerMock player;
    private Location location;
    private MockedStatic<PlayerData> playerData;
    private final Map<Field, Object> previousConfig = new HashMap<>();

    @BeforeEach void open() throws Exception {
        server = MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin("EliteMobs");
        player = server.addPlayer();
        player.openInventory(server.createInventory(player, 9));
        location = player.getLocation().clone();
        playerData = mockStatic(PlayerData.class);
        config("directDropCustomLootMessage", "Received $itemName");
    }

    @AfterEach void close() throws Exception {
        try {
            CustomItem.getCustomItems().clear();
            if (playerData != null) playerData.close();
            for (var entry : previousConfig.entrySet()) entry.getKey().set(null, entry.getValue());
        } finally {
            MockBukkit.unmock();
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @CsvSource({"false,0", "false,1", "false,2", "true,0", "true,1", "true,2"})
    void bossDifficultyOverridesAConflictingRecipientDungeonForOrdinaryDrops(boolean direct, int tier) throws Exception {
        config("putLootDirectlyIntoPlayerInventory", direct);
        recipientDifficulty((tier + 1) % 3);
        var table = table(false);
        var source = new InstancedBossEntity(bossFields(), location, 30, Integer.toString(tier));

        table.bossDrop(player, 30, location, source);

        for (int candidate = 0; candidate < MATERIALS.length; candidate++)
            assertEquals(candidate == tier ? 1 : 0, awarded(MATERIALS[candidate]),
                    "Only the source boss's difficulty may award its item");
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(booleans = {false, true})
    void sourceLessChestRetainsTheRecipientsDungeonDifficulty(boolean direct) throws Exception {
        config("putLootDirectlyIntoPlayerInventory", direct);
        recipientDifficulty(1);

        table(false).treasureChestDropAtLevel(player, 30, location);

        assertEquals(0, awarded(Material.DIAMOND));
        assertEquals(1, awarded(Material.EMERALD));
        assertEquals(0, awarded(Material.GOLD_INGOT));
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(ints = {0, 1, 2})
    void classLootCandidateSelectionUsesTheExternalBossDifficulty(int tier) {
        recipientDifficulty((tier + 1) % 3);
        var table = table(true);
        var fields = spy(bossFields());
        doReturn(table).when(fields).getCustomLootTable();
        var boss = new InstancedBossEntity(fields, location, 30, Integer.toString(tier));
        var difficulty = Difficulty.values()[tier];
        // Profile construction has separate coverage. This isolates eligibility before its roll.
        try (var construction = mockStatic(ClassLootItemConstructor.class)) {
            construction.when(() -> ClassLootItemConstructor.available(any(), eq(difficulty), eq(Rank.TRASH)))
                    .thenReturn(true);

            var candidates = ClassLootCoverage.candidates(fields, difficulty, Rank.TRASH, player, boss);

            assertEquals(List.of("tier_" + tier + ".yml"), candidates.stream()
                    .map(EliteCustomLootEntry::getFilename).toList());
            assertTrue(candidates.getFirst().willDrop(player, boss),
                    "The post-selection chance roll must retain the source's difficulty context");
        }
    }

    private CustomBossesConfigFields bossFields() {
        return new CustomBossesConfigFields("external_guard.yml", EntityType.ZOMBIE, true, "External guard", "dynamic");
    }

    private CustomLootTable table(boolean classLoot) {
        var table = new CustomLootTable();
        for (int tier = 0; tier < MATERIALS.length; tier++) {
            String filename = "tier_" + tier + ".yml";
            item(filename, MATERIALS[tier], classLoot);
            new EliteCustomLootEntry(table.getEntries(), Map.of("filename", filename, "chance", 1,
                    "difficultyID", List.of(Integer.toString(tier))), "external_guard.yml");
        }
        return table;
    }

    private void item(String filename, Material material, boolean classLoot) {
        var fields = mock(CustomItemsConfigFields.class);
        when(fields.isEnabled()).thenReturn(true);
        when(fields.getMaterial()).thenReturn(material);
        when(fields.getClassLootFamily()).thenReturn(ClassLootFamily.SWORDS);
        var item = mock(CustomItem.class);
        when(item.getCustomItemsConfigFields()).thenReturn(fields);
        when(item.getPermission()).thenReturn("");
        when(item.getItemType()).thenReturn(classLoot ? CustomItem.ItemType.CLASS_LOOT : CustomItem.ItemType.UNIQUE);
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

    private void recipientDifficulty(int tier) {
        var dungeon = mock(DungeonInstance.class);
        var resolver = new DifficultyResolver("recipient.yml", List.of());
        when(dungeon.matchesDifficulty(anyList(), anyString())).thenAnswer(call ->
                resolver.matches(call.getArgument(0), Integer.toString(tier), call.getArgument(1)));
        playerData.when(() -> PlayerData.getMatchInstance(player)).thenReturn(dungeon);
    }

    private int awarded(Material material) {
        int stored = java.util.Arrays.stream(player.getInventory().getContents()).filter(java.util.Objects::nonNull)
                .filter(item -> item.getType() == material).mapToInt(ItemStack::getAmount).sum();
        return stored + location.getWorld().getEntitiesByClass(Item.class).stream()
                .map(Item::getItemStack).filter(item -> item.getType() == material).mapToInt(ItemStack::getAmount).sum();
    }

    private void config(String name, Object value) throws Exception {
        var field = ItemSettingsConfig.class.getDeclaredField(name);
        field.setAccessible(true);
        previousConfig.putIfAbsent(field, field.get(null));
        field.set(null, value);
    }
}
