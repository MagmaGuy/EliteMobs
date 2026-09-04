package com.magmaguy.elitemobs.experimentalcombat.menu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassMenuInventoryAdapterTest {

    @Test
    void keepsAnAbilityDescriptionOnOneLoreLine() {
        String description = "Leap forward and strike nearby enemies when you land.";
        ClassMenuView.AbilityView ability = new ClassMenuView.AbilityView("Crater Leap", description);

        List<String> lore = ClassMenuInventoryAdapter.abilityLore(ability);

        assertEquals(List.of("&fCrater Leap", "", "&7" + description), lore);
    }
}
