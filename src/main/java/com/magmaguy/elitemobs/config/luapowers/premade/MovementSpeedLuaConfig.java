package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class MovementSpeedLuaConfig extends LuaPowersConfigFields {
    public MovementSpeedLuaConfig() {
        super("movement_speed", Material.GOLDEN_BOOTS.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
    }

    @Override
    public String getSource() {
        return """
                return {
                  api_version = 1,
                  on_spawn = function(context)
                    context.scheduler:run_after(1, function()
                      if context.boss:is_alive() then
                        context.boss:apply_potion_effect("SPEED", 100000, 1)
                      end
                    end)
                  end
                }
                """;
    }
}
