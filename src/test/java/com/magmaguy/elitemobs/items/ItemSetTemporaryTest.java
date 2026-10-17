package com.magmaguy.elitemobs.items;

import com.magmaguy.magmacore.enchantments.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.magmaguy.magmacore.enchantments.EnchantmentDefinition.Slot;
import static org.junit.jupiter.api.Assertions.*;

/** Temporary item-set check: MagmaCore's real catalog, provider bridge, dispatcher and set lore on MockBukkit. */
@EnabledIfEnvironmentVariable(named = "NIGHTBREAK_TEMP_TICKET_TESTS", matches = "true")
class ItemSetTemporaryTest {
    private static final String SET = "testsets:set", PLAIN = "testsets:plain";
    private static final String SET_YAML = """
            name: Test Set
            maxLevel: 1
            validSlots: [HEAD, CHEST, LEGS, FEET]
            script: set.lua
            parameters:
              lifetime_ticks: 200
              stop_on_unequip: true
            set:
              pieces:
                HEAD: Test Helm
                CHEST: Test Chest
                LEGS: Test Legs
                FEET: Test Boots
              lore:
                - "&6$setName &7($equippedPieces/$totalPieces)"
                - "$setPieces"
                - "$ifSetComplete&aBonus active"
            """;
    private static final String SET_LUA = """
            return {
              api_version = 1,
              on_take_damage = function(context)
                context.player:send_message("set " .. context.set.id .. " " .. context.source.slot)
                context.action:temporary_potion(context.player.uuid, "LUCK", 200, 0)
              end,
            }
            """;
    private static final String PLAIN_YAML = "name: Plain Mark\nmaxLevel: 1\nscript: plain.lua\n";
    private static final String PLAIN_LUA = "return { api_version = 1, on_attack_entity = function(context) end }\n";

    @TempDir Path folder;
    private ServerMock server;
    private PlayerMock player;
    private JavaPlugin plugin;
    private EnchantmentDefinitions.HostedCatalog hosted;
    private EnchantmentItems items;
    private com.magmaguy.magmacore.MagmaCore previousCore;

    @BeforeEach
    void open() throws Exception {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("ItemSetProvider");
        // Script scheduling belongs to the host's MagmaCore instance; each mocked server needs its own owner.
        var coreInstance = com.magmaguy.magmacore.MagmaCore.class.getDeclaredField("instance");
        coreInstance.setAccessible(true);
        previousCore = com.magmaguy.magmacore.MagmaCore.getInstance();
        coreInstance.set(null, null);
        com.magmaguy.magmacore.MagmaCore.createInstance(plugin);
        player = server.addPlayer();
        Path catalog = Files.createDirectories(folder.resolve("catalog"));
        Files.writeString(catalog.resolve("set.yml"), SET_YAML);
        Files.writeString(catalog.resolve("set.lua"), SET_LUA);
        Files.writeString(catalog.resolve("plain.yml"), PLAIN_YAML);
        Files.writeString(catalog.resolve("plain.lua"), PLAIN_LUA);
        hosted = EnchantmentDefinitions.publishActions(plugin, EnchantmentCatalog.load("testsets", catalog, EnchantmentInputs.HOOKS),
                Set.of(), Set.of(), EnchantmentInputs.HOOKS, (operation, request) -> Map.of("supported", false));
        items = new EnchantmentItems(EnchantmentDefinitions::resolve, EnchantmentItemProfile::vanilla);
    }

    @AfterEach
    void close() throws Exception {
        if (hosted != null) hosted.close();
        MockBukkit.unmock();
        var coreInstance = com.magmaguy.magmacore.MagmaCore.class.getDeclaredField("instance");
        coreInstance.setAccessible(true);
        coreInstance.set(null, previousCore);
    }

    /** The refresher answers inventory events a tick later; MockBukkit only fires close for some views. */
    private void closeInventoryAndSettle() {
        server.getPluginManager().callEvent(new org.bukkit.event.inventory.InventoryCloseEvent(player.getOpenInventory()));
        server.getScheduler().performTicks(2);
    }

    @Test
    void theProviderBridgeCarriesTheSetDefinition() {
        var resolved = EnchantmentDefinitions.resolve(SET);
        assertNotNull(resolved);
        var set = resolved.definition().set();
        assertNotNull(set, "The set section must survive the provider record");
        assertEquals(List.of(Slot.HEAD, Slot.CHEST, Slot.LEGS, Slot.FEET), List.copyOf(set.pieces().keySet()));
        assertEquals("Test Boots", set.pieces().get(Slot.FEET));
        assertEquals(List.of("&6$setName &7($equippedPieces/$totalPieces)", "$setPieces", "$ifSetComplete&aBonus active"), set.lore());
        assertNull(EnchantmentDefinitions.resolve(PLAIN).definition().set());
    }

    @Test
    void loreFollowsWornPiecesAndKeepsTheEnchantmentSectionInPlace() {
        ItemStack helm = piece(Material.LEATHER_HELMET, "Test Helm", List.of("Flavor", "$itemSet", "After"), Map.of(SET, 1));
        ItemStack chest = piece(Material.LEATHER_CHESTPLATE, "Test Chest", List.of(), Map.of(SET, 1, PLAIN, 1));
        // Put the ordinary enchantment section after the set section, so a set redraw that changes length must move it.
        chest = items.refreshPresentation(chest, hostLore -> hostLore, List::size);
        assertEquals(1, lore(chest).stream().filter(line -> line.contains("Plain Mark")).count());
        ItemStack legs = piece(Material.LEATHER_LEGGINGS, "Test Legs", List.of(), Map.of(SET, 1));
        ItemStack boots = piece(Material.LEATHER_BOOTS, "Test Boots", List.of(), Map.of(SET, 1));

        List<String> placed = lore(helm);
        assertEquals("Flavor", placed.getFirst());
        assertEquals("After", placed.getLast());
        assertFalse(placed.contains("$itemSet"));
        assertTrue(placed.stream().noneMatch(line -> line.endsWith("Test Set")), "A set must not also show as an enchantment line: " + placed);

        var inventory = player.getInventory();
        inventory.setHelmet(helm); inventory.setChestplate(chest); inventory.setLeggings(legs); inventory.setItem(0, boots);
        closeInventoryAndSettle();
        assertWornState(inventory.getHelmet(), 3, Set.of("Test Helm", "Test Chest", "Test Legs"));
        assertWornState(inventory.getItem(0), 3, Set.of("Test Helm", "Test Chest", "Test Legs"));

        inventory.setBoots(inventory.getItem(0)); inventory.setItem(0, null);
        closeInventoryAndSettle();
        for (ItemStack worn : inventory.getArmorContents())
            assertWornState(worn, 4, Set.of("Test Helm", "Test Chest", "Test Legs", "Test Boots"));
        // The redraw grew by the $ifSetComplete line; the shared section still validates at its moved position.
        ItemStack redrawn = items.refreshPresentation(inventory.getChestplate());
        assertEquals(lore(inventory.getChestplate()), lore(redrawn));
        assertEquals("Plain Mark", strip(lore(redrawn).getLast()));

        inventory.setItem(1, inventory.getBoots()); inventory.setBoots(null);
        closeInventoryAndSettle();
        assertWornState(inventory.getChestplate(), 3, Set.of("Test Helm", "Test Chest", "Test Legs"));
        assertDoesNotThrow(() -> items.refreshPresentation(inventory.getChestplate()));
    }

    @Test
    void setHooksRunOnceAndOnlyWhileEveryPieceIsWorn() {
        var inventory = player.getInventory();
        ItemStack helm = piece(Material.LEATHER_HELMET, "Test Helm", List.of(), Map.of(SET, 1));
        inventory.setHelmet(helm);
        inventory.setChestplate(piece(Material.LEATHER_CHESTPLATE, "Test Chest", List.of(), Map.of(SET, 1)));
        inventory.setLeggings(piece(Material.LEATHER_LEGGINGS, "Test Legs", List.of(), Map.of(SET, 1)));
        // Per-item capture skips sets entirely; only the set dispatcher may run them.
        assertTrue(EnchantmentInputs.capture(player, helm, Slot.HEAD, 39, null, "", UUID.randomUUID()).isEmpty());
        // Each step the dispatcher relies on, so a failure names the one that lost the worn pieces.
        var captured = EnchantmentActions.captureEquipment(player);
        assertEquals(Set.of("HEAD", "CHEST", "LEGS"), captured.keySet(), "Captured equipment");
        assertEquals(Map.of(SET, 1), EnchantmentItems.inspectCustom(captured.get("HEAD").getItemMeta()), "A captured piece keeps its set");
        assertEquals(EnumSet.of(Slot.HEAD, Slot.CHEST, Slot.LEGS),
                ItemSets.equippedPieces(captured, EnchantmentDefinitions::resolve).get(SET), "Worn piece slots");

        EnchantmentInputs.dispatchSets(plugin, player, EnchantmentInputs.TAKE_DAMAGE, null, "partial");
        assertNull(player.nextMessage(), "An incomplete set must not run");
        assertFalse(player.hasPotionEffect(PotionEffectType.LUCK));

        inventory.setBoots(piece(Material.LEATHER_BOOTS, "Test Boots", List.of(), Map.of(SET, 1)));
        EnchantmentInputs.dispatchSets(plugin, player, EnchantmentInputs.TAKE_DAMAGE, null, "complete");
        assertEquals("set " + SET + " HEAD", player.nextMessage());
        assertNull(player.nextMessage(), "A complete set runs once per input, not once per piece");
        assertTrue(player.hasPotionEffect(PotionEffectType.LUCK));

        inventory.setBoots(null);
        server.getScheduler().performTicks(2);
        assertFalse(player.hasPotionEffect(PotionEffectType.LUCK), "stop_on_unequip ends the bonus when a piece comes off");
    }

    @Test
    void numericReadersCountACompleteSetOnceAtItsFirstPiece() {
        var definition = EnchantmentDefinitions.resolve(SET).definition();
        Map<String, ItemStack> worn = new LinkedHashMap<>();
        worn.put("HEAD", piece(Material.LEATHER_HELMET, "Test Helm", List.of(), Map.of(SET, 1)));
        worn.put("CHEST", piece(Material.LEATHER_CHESTPLATE, "Test Chest", List.of(), Map.of(SET, 1)));
        worn.put("LEGS", piece(Material.LEATHER_LEGGINGS, "Test Legs", List.of(), Map.of(SET, 1)));
        assertFalse(ItemSets.appliesAt(definition, Slot.HEAD, worn));
        worn.put("FEET", piece(Material.LEATHER_BOOTS, "Test Boots", List.of(), Map.of(SET, 1)));
        assertTrue(ItemSets.appliesAt(definition, Slot.HEAD, worn));
        assertFalse(ItemSets.appliesAt(definition, Slot.CHEST, worn));
        assertEquals(1, ItemSets.active(worn, EnchantmentDefinitions::resolve).size());
        assertTrue(ItemSets.appliesAt(EnchantmentDefinitions.resolve(PLAIN).definition(), Slot.CHEST, worn));
    }

    @ParameterizedTest(name = "{displayName} [{index}] {arguments}")
    @ValueSource(strings = {"unknown-field", "slot-outside-valid-slots", "missing-pieces"})
    void malformedSetsRejectTheCatalog(String problem) throws IOException {
        String yaml = switch (problem) {
            case "unknown-field" -> SET_YAML.replace("  lore:", "  bonus: 1\n  lore:");
            case "slot-outside-valid-slots" -> SET_YAML.replace("validSlots: [HEAD, CHEST, LEGS, FEET]", "validSlots: [HEAD, CHEST, LEGS]");
            default -> "name: Test Set\nmaxLevel: 1\nscript: set.lua\nset:\n  lore: []\n";
        };
        Path rejected = Files.createDirectories(folder.resolve(problem));
        Files.writeString(rejected.resolve("set.yml"), yaml);
        Files.writeString(rejected.resolve("set.lua"), SET_LUA);
        assertThrows(IOException.class, () -> EnchantmentCatalog.load("rejected", rejected, EnchantmentInputs.HOOKS));
    }

    private ItemStack piece(Material material, String name, List<String> lore, Map<String, Integer> enchantments) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (!lore.isEmpty()) meta.setLore(lore);
        item.setItemMeta(meta);
        return ItemSets.placePresentation(items.previewAuthored(item, enchantments).apply(item), "$itemSet", EnchantmentDefinitions::resolve);
    }

    private static void assertWornState(ItemStack item, int equipped, Set<String> worn) {
        List<String> lore = lore(item);
        assertTrue(lore.stream().anyMatch(line -> strip(line).equals("Test Set (" + equipped + "/4)")), "Header: " + lore);
        for (String piece : List.of("Test Helm", "Test Chest", "Test Legs", "Test Boots")) {
            String line = lore.stream().filter(entry -> strip(entry).endsWith(piece)).findFirst().orElseThrow(() -> new AssertionError(piece + " missing: " + lore));
            if (worn.contains(piece)) assertTrue(line.contains("§a") && line.contains("✔"), "Worn " + piece + " should be green: " + line);
            else assertTrue(line.contains("§c") && line.contains("✘"), "Missing " + piece + " should be red: " + line);
        }
        assertEquals(equipped == 4, lore.stream().anyMatch(line -> strip(line).equals("Bonus active")), "Bonus line: " + lore);
    }

    private static List<String> lore(ItemStack item) {
        assertNotNull(item);
        List<String> lore = item.getItemMeta().getLore();
        return lore == null ? List.of() : lore;
    }

    private static String strip(String line) { return org.bukkit.ChatColor.stripColor(line).trim(); }
}
