package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class BonusLootConfig extends PowersConfigFields {
    public BonusLootConfig() {
        super("bonus_loot",
                true,
                "Bonus Loot",
                Material.CHEST.toString());
    }
}
