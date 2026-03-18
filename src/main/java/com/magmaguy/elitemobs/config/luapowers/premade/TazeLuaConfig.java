package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class TazeLuaConfig extends LuaPowersConfigFields {
    public TazeLuaConfig() {
        super("taze", null, PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                local function taze_player(context, player_location, depth)
                  if depth > 2 or context.player == nil then
                    return
                  end

                  local push = context.vectors.get_vector_between_locations(player_location, context.player:get_location())
                  if push.x == 0 and push.y == 0 and push.z == 0 then
                    push = em.create_vector(0, 0.5, 0)
                  else
                    push = context.vectors.normalize_vector(push)
                  end

                  context.world:spawn_particle_at_location(context.player:get_location(), {
                    particle = "FIREWORKS_SPARK",
                    amount = 20,
                    x = 0.5,
                    y = 0.5,
                    z = 0.5,
                    speed = 0.1
                  })
                  context.player:show_title("&7Tazed!", "", 5, 30, 5)
                  context.player:set_velocity_vector(push)
                  context.player:apply_potion_effect("SLOWNESS", 30, 5)
                  context.scheduler:run_after(5, function()
                    taze_player(context, player_location, depth + 1)
                  end)
                end

                return {
                  api_version = 1,
                  on_player_damaged_by_boss = function(context)
                    if context.player == nil
                      or not context.cooldowns.local_ready("taze")
                      or not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_local(60, "taze")
                    context.cooldowns.set_global(20)
                    taze_player(context, context.boss:get_location(), 0)
                  end
                }
                """;
    }
}
