package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class SummonEmbersLuaConfig extends InlineLuaPowerConfig {
    public SummonEmbersLuaConfig() {
        super("summon_embers", null, PowersConfigFields.PowerType.UNIQUE, """
                local function offset_location(location, x, y, z)
                  return em.create_location(
                    location.x + x,
                    location.y + y,
                    location.z + z,
                    location.world,
                    location.yaw,
                    location.pitch
                  )
                end

                local function do_summon(context)
                  for _ = 1, 10 do
                    local spawn_location = context.boss:get_location()
                    context.world.spawn_boss_at_location("ember.yml", spawn_location, context.boss.level)
                    context.boss:set_velocity_vector(em.create_vector(
                      math.random() - 0.5,
                      0.5,
                      math.random() - 0.5
                    ))
                  end
                end

                local function do_summon_particles(context)
                  context.boss:set_ai_enabled(false)
                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    counter = counter + 1
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(task_id)
                      return
                    end
                    context.world.spawn_particle_at_location(
                      offset_location(context.boss:get_location(), 0, 1, 0),
                      {
                        particle = "FLAME",
                        amount = 50,
                        x = 0.0001,
                        y = 0.0001,
                        z = 0.0001
                      }
                    )
                    if counter < 60 then
                      return
                    end
                    context.scheduler:cancel_task(task_id)
                    do_summon(context)
                    context.boss:set_ai_enabled(true)
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if math.random() > 0.25
                      or not context.boss:is_ai_enabled()
                      or not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(400)
                    do_summon_particles(context)
                  end
                }
                """);
    }
}
