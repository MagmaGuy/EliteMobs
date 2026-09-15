package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.inventory.meta.ItemMetaMock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlternativeDurabilityLossTest {
    private MockedStatic<ItemSettingsConfig> settings;
    private MockedStatic<EliteItemManager> classification;
    private MockedStatic<ItemTagger> enchantments;
    private MockedStatic<WeaponIdentityResolver> identities;
    private Player player;
    private PlayerInventory inventory;

    @BeforeEach void open() {
        MockBukkit.mock();
        MetadataHandler.PLUGIN = MockBukkit.createMockPlugin();
        settings = mockStatic(ItemSettingsConfig.class);
        settings.when(ItemSettingsConfig::isEliteDurability).thenReturn(true);
        settings.when(ItemSettingsConfig::getEliteDurabilityMultiplier).thenReturn(1D);
        settings.when(ItemSettingsConfig::isPreventEliteItemsFromBreaking).thenReturn(true);
        classification = mockStatic(EliteItemManager.class);
        classification.when(() -> EliteItemManager.isEliteMobsItem(any())).thenReturn(true);
        classification.when(() -> EliteItemManager.isWeapon(any())).thenAnswer(call ->
                ((ItemStack) call.getArgument(0)).getType() == Material.DIAMOND_SWORD);
        enchantments = mockStatic(ItemTagger.class);
        identities = mockStatic(WeaponIdentityResolver.class);
        player = mock(Player.class);
        inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getArmorContents()).thenReturn(new ItemStack[4]);
    }

    @AfterEach void close() {
        identities.close();
        enchantments.close();
        classification.close();
        settings.close();
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = null;
    }

    @ParameterizedTest
    @CsvSource({"0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "0,false", "1,false", "2,false", "3,false", "4,false", "5,false"})
    void deathHandlesOverflowInEverySlotAndProcessesRemainingEquipment(int damagedSlot, boolean preventBreaking) {
        settings.when(ItemSettingsConfig::isPreventEliteItemsFromBreaking).thenReturn(preventBreaking);
        Material[] materials = {Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS,
                Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET,
                Material.DIAMOND_SWORD, Material.DIAMOND_SWORD};
        ItemStack[] items = new ItemStack[6];
        for (int i = 0; i < items.length; i++)
            items[i] = item(materials[i], 384, i == damagedSlot ? 300 : 0);
        when(inventory.getArmorContents()).thenReturn(java.util.Arrays.copyOf(items, 4));
        when(inventory.getItemInMainHand()).thenReturn(items[4]);
        when(inventory.getItemInOffHand()).thenReturn(items[5]);
        PlayerDeathEvent death = mock(PlayerDeathEvent.class);
        when(death.getEntity()).thenReturn(player);

        assertDoesNotThrow(() -> new AlternativeDurabilityLoss().onPlayerDeath(death));

        for (int i = 0; i < items.length; i++) {
            assertEquals(i == damagedSlot && !preventBreaking ? 0 : 1, items[i].getAmount());
            if (items[i].getAmount() > 0)
                assertEquals(i == damagedSlot ? 383 : (i < 4 ? 118 : 155), damage(items[i]));
        }
    }

    @ParameterizedTest
    @CsvSource({"0,155", "228,383", "229,383", "230,383", "383,383"})
    void preservesPenaltyAndClampsAtTheBreakBoundary(int initialDamage, int expectedDamage) {
        ItemStack item = item(Material.DIAMOND_SWORD, 384, initialDamage);
        when(inventory.getItemInMainHand()).thenReturn(item);
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        assertEquals(expectedDamage, damage(item));
        assertEquals(1, item.getAmount());
    }

    @Test void breaksAtExactMaximumWithoutWritingDamage() {
        settings.when(ItemSettingsConfig::isPreventEliteItemsFromBreaking).thenReturn(false);
        ItemStack item = item(Material.DIAMOND_SWORD, 384, 229);
        when(inventory.getItemInMainHand()).thenReturn(item);
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        assertEquals(0, item.getAmount());
        verify(item, never()).setItemMeta(any());
    }

    @Test void respectsAuthoredMaximumInsteadOfMaterialDurability() {
        ItemStack item = item(Material.DIAMOND_SWORD, 1000, 900);
        when(inventory.getItemInMainHand()).thenReturn(item);
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        assertEquals(999, damage(item));
    }

    @Test void retainsVanillaMaximumWhenNoOverrideExists() {
        ItemStack item = item(Material.DIAMOND_SWORD, null, 1500);
        when(inventory.getItemInMainHand()).thenReturn(item);
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        assertEquals(Material.DIAMOND_SWORD.getMaxDurability() - 1, damage(item));
    }

    @Test void preparesLegacyMagicWeaponBeforeApplyingThePenalty() {
        ItemStack item = item(Material.DIAMOND_SWORD, null, 300);
        identities.when(() -> WeaponIdentityResolver.isMagicWeapon(item)).thenReturn(true);
        when(inventory.getItemInMainHand()).thenReturn(item);
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        assertEquals(384, ((Damageable) item.getItemMeta()).getMaxDamage());
        assertEquals(383, damage(item));
    }

    @Test void disabledDurabilityLeavesInventoryUntouched() {
        settings.when(ItemSettingsConfig::isEliteDurability).thenReturn(false);
        AlternativeDurabilityLoss.doDurabilityLoss(player);
        verifyNoInteractions(inventory);
    }

    private static int damage(ItemStack item) {
        return ((Damageable) item.getItemMeta()).getDamage();
    }

    private static ItemStack item(Material material, Integer maximum, int damage) {
        ItemStack item = mock(ItemStack.class);
        var initial = new BoundedMeta();
        initial.setMaxDamage(maximum);
        initial.setDamage(damage);
        var metadata = new AtomicReference<ItemMeta>(initial);
        var amount = new AtomicInteger(1);
        when(item.getType()).thenReturn(material);
        when(item.getItemMeta()).thenAnswer(call -> metadata.get().clone());
        when(item.setItemMeta(any())).thenAnswer(call -> {
            metadata.set(((ItemMeta) call.getArgument(0)).clone());
            return true;
        });
        when(item.getAmount()).thenAnswer(call -> amount.get());
        doAnswer(call -> { amount.set(call.getArgument(0)); return null; }).when(item).setAmount(anyInt());
        return item;
    }

    // MockBukkit 4.116.1 omits Paper's explicit max_damage check. Enforce the real
    // CraftMetaItem contract here while retaining its metadata copy semantics.
    private static class BoundedMeta extends ItemMetaMock {
        BoundedMeta() { }
        BoundedMeta(ItemMeta source) { super(source); }

        @Override public void setDamage(int damage) {
            if (hasMaxDamage() && damage > getMaxDamage())
                throw new IllegalArgumentException("Damage cannot exceed max damage");
            super.setDamage(damage);
        }

        @Override public BoundedMeta clone() { return new BoundedMeta(this); }
    }
}
