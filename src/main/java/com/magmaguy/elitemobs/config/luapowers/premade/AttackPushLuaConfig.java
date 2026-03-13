package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackPushLuaConfig extends LuaPowersConfigFields {
    public AttackPushLuaConfig() {
        super("attack_push", Material.PISTON.toString(), PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                return {
                  api_version = 1,
                  on_player_damaged_by_boss = function(context)
                    if context.player == nil or not context.cooldowns.global_ready() then
                      return
                    end

                    local boss_location = context.boss:get_location()
                    local player_location = context.player:get_location()
                    local push = context.vectors.get_vector_between_locations(boss_location, player_location)
                    push = context.vectors.normalize_vector(push)

                    context.player:set_velocity_vector({
                      x = push.x * 3,
                      y = push.y * 3,
                      z = push.z * 3
                    })
                    context.cooldowns.set_global(200)
                  end
                }
                """;
    }
}
