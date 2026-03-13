package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class BonusLootLuaConfig extends InlineLuaPowerConfig {
    public BonusLootLuaConfig() {
        super("bonus_loot", Material.CHEST.toString(), PowersConfigFields.PowerType.MISCELLANEOUS, """
                return {
                  api_version = 1,
                  on_death = function(context)
                    context.world:generate_player_loot(1)
                  end
                }
                """);
    }
}
