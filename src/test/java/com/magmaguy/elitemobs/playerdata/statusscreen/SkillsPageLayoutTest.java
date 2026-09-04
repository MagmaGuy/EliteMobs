package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.skills.SkillType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillsPageLayoutTest {

    @Test
    void inventoryStatusPageHasRoomForEveryIndependentSkill() {
        assertTrue(SkillsPage.skillSlotCapacity() >= SkillType.values().length);
    }
}
