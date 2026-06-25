package com.magmaguy.elitemobs.items.customitems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomItemTest {

    @Test
    void limitItemLevelPreservesRequestedDropLevel() {
        assertEquals(23, CustomItem.limitItemLevel(null, 23));
    }
}
