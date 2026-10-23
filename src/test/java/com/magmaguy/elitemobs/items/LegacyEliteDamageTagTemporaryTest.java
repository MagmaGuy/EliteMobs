package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;

/** Temporary ticket check (stuee147, 10.9.10): a byte-stored eliteDamage tag threw on join. */
class LegacyEliteDamageTagTemporaryTest {
    @BeforeEach void open() {
        MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
    }

    @AfterEach void close() {
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = null;
    }

    @Test void byteAndFloatTagsAreReadInsteadOfThrowing() {
        ItemStack sword = tagged(Material.DIAMOND_SWORD, meta ->
                meta.getPersistentDataContainer().set(ItemTagger.getELITE_DAMAGE(), PersistentDataType.BYTE, (byte) 7));
        // The read 10.9.10 did. Paper throws "ByteTag cannot store Double" here; MockBukkit returns null, so the old
        // code read 0 in this test instead of throwing. Either way the stored 7 was lost.
        assertNull(sword.getItemMeta().getPersistentDataContainer().get(ItemTagger.getELITE_DAMAGE(), PersistentDataType.DOUBLE));
        assertEquals(7D, ItemTagger.getEliteDamageAttribute(sword));

        ItemStack helmet = tagged(Material.DIAMOND_HELMET, meta ->
                meta.getPersistentDataContainer().set(ItemTagger.getELITE_DEFENSE(), PersistentDataType.BYTE, (byte) 3));
        assertEquals(3D, ItemTagger.getEliteDefenseAttribute(helmet));
        ItemStack floatHelmet = tagged(Material.DIAMOND_HELMET, meta ->
                meta.getPersistentDataContainer().set(ItemTagger.getELITE_DEFENSE(), PersistentDataType.FLOAT, 2.5F));
        assertEquals(2.5D, ItemTagger.getEliteDefenseAttribute(floatHelmet));

        // Current items keep their double values, and untagged items still read 0.
        ItemStack current = tagged(Material.DIAMOND_SWORD, meta ->
                meta.getPersistentDataContainer().set(ItemTagger.getELITE_DAMAGE(), PersistentDataType.DOUBLE, 12.25D));
        assertEquals(12.25D, ItemTagger.getEliteDamageAttribute(current));
        assertEquals(0D, ItemTagger.getEliteDamageAttribute(new ItemStack(Material.DIAMOND_SWORD)));
        System.out.println("PASS byte eliteDamage 7.0, byte eliteDefense 3.0, float eliteDefense 2.5, double 12.25");
    }

    private static ItemStack tagged(Material material, java.util.function.Consumer<ItemMeta> tag) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        tag.accept(meta);
        item.setItemMeta(meta);
        return item;
    }
}
