package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FlamePyreLuaConfig extends InlineLuaPowerConfig {
    public FlamePyreLuaConfig() {
        super("flame_pyre", null, PowersConfigFields.PowerType.MAJOR_BLAZE, """
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

                local function damage_nearby(context, center, radius)
                  local entities = context.entities.get_nearby_entities(10, "living")
                  for index = 1, #entities do
                    local entity = entities[index]
                    local location = entity:get_location()
                    if location.world == center.world
                      and math.abs(location.x - center.x) <= radius
                      and math.abs(location.y - center.y) <= 50
                      and math.abs(location.z - center.z) <= radius then
                      entity:deal_custom_damage(1)
                    end
                  end
                end

                local function spawn_phase_one_particles(context, center, particle)
                  for _ = 1, 10 do
                    local location = offset_location(center, (math.random() - 0.5) * 0.5, 0, (math.random() - 0.5) * 0.5)
                    context.world.spawn_particle_at_location(location, {
                      particle = particle,
                      amount = 0,
                      x = 0,
                      y = 1,
                      z = 0,
                      speed = math.random() * 2
                    })
                  end
                end

                local function spawn_phase_two_particles(context, center, particle)
                  for _ = 1, 10 do
                    local location = offset_location(center, (math.random() - 0.5) * 3, 0, (math.random() - 0.5) * 3)
                    context.world.spawn_particle_at_location(location, {
                      particle = particle,
                      amount = 0,
                      x = 0,
                      y = 1,
                      z = 0,
                      speed = math.random() * 2
                    })
                  end
                end

                local function spawn_phase_three_particles(context, center, particle)
                  context.world.spawn_particle_at_location(center, {
                    particle = particle,
                    amount = 50,
                    x = 0.01,
                    y = 0.01,
                    z = 0.01,
                    speed = 0.1
                  })
                end

                local function finish_flame_pyre(context)
                  context.boss:set_ai_enabled(true)
                  context.state.flame_pyre_active = false
                end

                local function run_phase_four()
                  local counter = 0
                  local task_id
                  task_id = context.scheduler.run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler.cancel_task(task_id)
                      finish_flame_pyre(context)
                      return
                    end

                    counter = counter + 1
                    local center = context.boss:get_location()
                    spawn_phase_three_particles(context, center, "FLAME")
                    damage_nearby(context, center, 5)
                    if counter >= 40 then
                      context.scheduler.cancel_task(task_id)
                      finish_flame_pyre(context)
                    end
                  end)
                end

                local function run_phase_three()
                  local counter = 0
                  local task_id
                  task_id = context.scheduler.run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler.cancel_task(task_id)
                      finish_flame_pyre(context)
                      return
                    end

                    counter = counter + 1
                    local center = context.boss:get_location()
                    spawn_phase_two_particles(context, center, "FLAME")
                    damage_nearby(context, center, 3)
                    spawn_phase_three_particles(context, center, "SMOKE")
                    if counter >= 40 then
                      context.scheduler.cancel_task(task_id)
                      run_phase_four()
                    end
                  end)
                end

                local function run_phase_two()
                  local counter = 0
                  local task_id
                  task_id = context.scheduler.run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler.cancel_task(task_id)
                      finish_flame_pyre(context)
                      return
                    end

                    counter = counter + 1
                    local center = context.boss:get_location()
                    spawn_phase_one_particles(context, center, "FLAME")
                    damage_nearby(context, center, 0.5)
                    spawn_phase_two_particles(context, center, "SMOKE")
                    if counter >= 40 then
                      context.scheduler.cancel_task(task_id)
                      run_phase_three()
                    end
                  end)
                end

                local function run_phase_one()
                  local counter = 0
                  local task_id
                  task_id = context.scheduler.run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler.cancel_task(task_id)
                      finish_flame_pyre(context)
                      return
                    end

                    counter = counter + 1
                    spawn_phase_one_particles(context, context.boss:get_location(), "SMOKE")
                    if counter >= 40 then
                      context.scheduler.cancel_task(task_id)
                      run_phase_two()
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.state.flame_pyre_active or math.random() > 0.25 then
                      return
                    end
                    if not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(400)

                    context.state.flame_pyre_active = true
                    context.boss:set_ai_enabled(false)
                    run_phase_one()
                  end
                }
                """);
    }
}
