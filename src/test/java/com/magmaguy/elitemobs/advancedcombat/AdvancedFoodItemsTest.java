package com.magmaguy.elitemobs.advancedcombat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedFoodItemsTest {
    @Test
    void vanillaPrototypeTableContainsTheSupportedFoodFamilies() {
        assertEquals(40, AdvancedFoodItems.vanillaFoodMaterials().size());
        assertTrue(AdvancedFoodItems.vanillaFoodMaterials().contains(Material.APPLE));
        assertTrue(AdvancedFoodItems.vanillaFoodMaterials().contains(Material.COOKED_BEEF));
        assertTrue(AdvancedFoodItems.vanillaFoodMaterials().contains(Material.RABBIT_STEW));
        assertTrue(AdvancedFoodItems.vanillaFoodMaterials().contains(Material.GOLDEN_CARROT));
        assertTrue(AdvancedFoodItems.vanillaFoodMaterials().contains(Material.SUSPICIOUS_STEW));
    }

    @Test
    void cookedBeefUsesSaturationPointsRatherThanTheLegacyModifier() {
        assertEquals(12.8D, AdvancedFoodItems.vanillaSaturation(Material.COOKED_BEEF), .0001D);
    }
}
