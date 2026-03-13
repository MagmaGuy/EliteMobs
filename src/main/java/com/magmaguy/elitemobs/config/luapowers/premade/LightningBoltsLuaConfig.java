package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class LightningBoltsLuaConfig extends LuaPowersConfigFields {
    public LightningBoltsLuaConfig() {
        super("lightning_bolts", null, PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                local function strike_after_warning(context, location, delay)
                  context.scheduler:run_after(delay, function()
                    local task_id
                    local counter = 0
                    task_id = context.scheduler:run_every(1, function()
                      counter = counter + 1
                      if counter > 40 then
                        context.scheduler:cancel_task(task_id)
                        context.world:strike_lightning_at_location(location)
                        return
                      end

                      context.world:spawn_particle_at_location(location, {
                        particle = "CRIT",
                        amount = 10,
                        x = 0.5,
                        y = 1.5,
                        z = 0.5,
                        speed = 0.3
                      }, 10)
                    end)
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.cooldowns.local_ready("lightning_bolts") or not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_local(400, "lightning_bolts")
                    context.cooldowns.set_global(100)
                    context.boss:set_ai_enabled(false, 80)

                    for _, player in ipairs(context.boss:get_nearby_players(20)) do
                      local player_location = player:get_location()
                      local boss_location = context.boss:get_location()
                      local ray = context.vectors.get_vector_between_locations(boss_location, player_location)
                      ray = context.vectors.normalize_vector(ray)
                      local dx = player_location.x - boss_location.x
                      local dy = player_location.y - boss_location.y
                      local dz = player_location.z - boss_location.z
                      local steps = math.max(1, math.floor(math.sqrt(dx * dx + dy * dy + dz * dz)))

                      for step = 1, steps do
                        local location = em.create_location(
                          boss_location.x + ray.x * step,
                          boss_location.y + ray.y * step,
                          boss_location.z + ray.z * step,
                          boss_location.world
                        )
                        strike_after_warning(context, location, step * 5)
                      end
                    end
                  end
                }
                """;
    }
}
