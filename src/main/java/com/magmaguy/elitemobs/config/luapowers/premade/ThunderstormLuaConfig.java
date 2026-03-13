package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class ThunderstormLuaConfig extends LuaPowersConfigFields {
    public ThunderstormLuaConfig() {
        super("thunderstorm", null, PowersConfigFields.PowerType.UNIQUE);
    }

    @Override
    public String getSource() {
        return """
                local function random_location_around(origin)
                  return em.create_location(
                    origin.x + math.random(-20, 19),
                    origin.y,
                    origin.z + math.random(-20, 19),
                    origin.world,
                    origin.yaw,
                    origin.pitch
                  )
                end

                local function strike_after_warning(context, location)
                  local task_id
                  local counter = 0
                  task_id = context.scheduler:run_every(1, function()
                    counter = counter + 1
                    if counter > 60 then
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
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.boss:is_ai_enabled() then
                      return
                    end
                    if not context.cooldowns.global_ready() or math.random() > 0.25 then
                      return
                    end

                    context.cooldowns.set_global(400)
                    context.boss:set_ai_enabled(false, 100)

                    local storm_task
                    local counter = 0
                    storm_task = context.scheduler:run_every(1, function()
                      counter = counter + 1
                      if counter > 100 or not context.boss:is_alive() then
                        context.scheduler:cancel_task(storm_task)
                        return
                      end

                      if counter % 2 == 0 then
                        strike_after_warning(context, random_location_around(context.boss:get_location()))
                      end

                      if counter % 20 == 0 then
                        for _, player in ipairs(context.boss:get_nearby_players(20)) do
                          strike_after_warning(context, player:get_location())
                        end
                      end
                    end)
                  end
                }
                """;
    }
}
