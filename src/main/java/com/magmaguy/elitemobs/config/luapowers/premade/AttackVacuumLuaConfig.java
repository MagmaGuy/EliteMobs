package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackVacuumLuaConfig extends LuaPowersConfigFields {
    public AttackVacuumLuaConfig() {
        super("attack_vacuum", Material.LEAD.toString(), PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                return {
                  api_version = 1,
                  on_player_damaged_by_boss = function(context)
                    if context.player == nil then
                      return
                    end

                    local pull = context.vectors.get_vector_between_locations(
                      context.player:get_location(),
                      context.boss:get_location()
                    )
                    pull = context.vectors.normalize_vector(pull)
                    context.player:set_velocity_vector(pull)
                  end
                }
                """;
    }
}
