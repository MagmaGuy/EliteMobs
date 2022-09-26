package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.BonusLoot;
import org.bukkit.Material;

public class BonusLootConfig extends PowersConfigFields {
    public BonusLootConfig() {
        super("bonus_loot",
                true,
                Material.CHEST.toString(),
                BonusLoot.class,
                PowerType.MISCELLANEOUS);
    }
}
