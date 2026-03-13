package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class SpiritWalkLuaConfig extends InlineLuaPowerConfig {
    public SpiritWalkLuaConfig() {
        super("spirit_walk", null, PowersConfigFields.PowerType.UNIQUE, """
                return {
                  api_version = 1,
                  on_boss_damaged = function(context)
                    if context.event.damage_cause == nil then
                      return
                    end
                    context.boss:handle_spirit_walk_damage(context.event.damage_cause)
                  end
                }
                """);
    }
}
