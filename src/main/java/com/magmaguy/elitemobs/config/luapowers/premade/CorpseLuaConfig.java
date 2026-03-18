package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class CorpseLuaConfig extends LuaPowersConfigFields {
    public CorpseLuaConfig() {
        super("corpse", Material.BONE_BLOCK.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
    }

    @Override
    public String getSource() {
        return """
                return {
                  api_version = 1,
                  on_death = function(context)
                    context.world:place_temporary_block_at_location(context.boss:get_location(), "BONE_BLOCK", 2400, true)
                  end
                }
                """;
    }
}
