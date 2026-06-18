package com.magmaguy.elitemobs.config.mobproperties;

import com.magmaguy.elitemobs.config.mobproperties.premade.EliteEnderDragon;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EliteEnderDragonConfigTest {

    @Test
    void enderDragonIsDisabledByDefault() {
        MobPropertiesConfigFields configFields = new EliteEnderDragon();

        assertEquals(EntityType.ENDER_DRAGON, configFields.getEntityType());
        assertFalse(configFields.isEnabled());
    }

}
