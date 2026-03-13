package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FrostWalkerLuaConfig extends InlineLuaPowerConfig {
    public FrostWalkerLuaConfig() {
        super("frost_walker", null, PowersConfigFields.PowerType.MISCELLANEOUS, """
                return {
                  api_version = 1,
                  on_spawn = function(context)
                    context.scheduler:run_after(1, function(context)
                      context.boss:set_equipment("FEET", "LEATHER_BOOTS", {
                        enchantments = {
                          { type = "FROST_WALKER", level = 2 },
                          { type = "DEPTH_STRIDER", level = 3 }
                        }
                      })
                    end)
                  end
                }
                """);
    }
}
