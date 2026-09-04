package com.magmaguy.elitemobs.experimentalcombat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentalFoodItemsTest {
    @Test
    void vanillaPrototypeTableContainsTheSupportedFoodFamilies() {
        assertEquals(40, ExperimentalFoodItems.vanillaFoodMaterials().size());
        assertTrue(ExperimentalFoodItems.vanillaFoodMaterials().contains(Material.APPLE));
        assertTrue(ExperimentalFoodItems.vanillaFoodMaterials().contains(Material.COOKED_BEEF));
        assertTrue(ExperimentalFoodItems.vanillaFoodMaterials().contains(Material.RABBIT_STEW));
        assertTrue(ExperimentalFoodItems.vanillaFoodMaterials().contains(Material.GOLDEN_CARROT));
        assertTrue(ExperimentalFoodItems.vanillaFoodMaterials().contains(Material.SUSPICIOUS_STEW));
    }

    @Test
    void cookedBeefUsesSaturationPointsRatherThanTheLegacyModifier() {
        assertEquals(12.8D, ExperimentalFoodItems.vanillaSaturation(Material.COOKED_BEEF), .0001D);
    }
}
