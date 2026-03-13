package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FirestormLuaConfig extends InlineLuaPowerConfig {
    public FirestormLuaConfig() {
        super("firestorm", null, PowersConfigFields.PowerType.MAJOR_BLAZE, """
                local function clone_location(location)
                  return {
                    x = location.x,
                    y = location.y,
                    z = location.z,
                    yaw = location.yaw,
                    pitch = location.pitch,
                    world = location.world
                  }
                end

                local function offset_location(location, x, y, z)
                  return {
                    x = location.x + x,
                    y = location.y + y,
                    z = location.z + z,
                    yaw = location.yaw,
                    pitch = location.pitch,
                    world = location.world
                  }
                end

                local function location_distance_squared(first, second)
                  local dx = first.x - second.x
                  local dz = first.z - second.z
                  return (dx * dx) + (dz * dz)
                end

                local function damage_nearby(context, center, radius)
                  local entities = context.entities.get_nearby_entities(30, "living")
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

                local function spawn_particle_ring(context, center, particle, spread, amount)
                  for _ = 1, amount do
                    local location = offset_location(center, (math.random() - 0.5) * spread, 0, (math.random() - 0.5) * spread)
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

                local function spawn_center_particles(context, center, particle)
                  context.world.spawn_particle_at_location(center, {
                    particle = particle,
                    amount = 50,
                    x = 0.01,
                    y = 0.01,
                    z = 0.01,
                    speed = 0.1
                  })
                end

                local function start_fire_pyre(context, center)
                  local function phase_four()
                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      counter = counter + 1
                      spawn_center_particles(context, center, "FLAME")
                      damage_nearby(context, center, 5)
                      if counter >= 40 then
                        context.scheduler.cancel_task(task_id)
                      end
                    end)
                  end

                  local function phase_three()
                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      counter = counter + 1
                      spawn_particle_ring(context, center, "FLAME", 3, 10)
                      damage_nearby(context, center, 3)
                      spawn_center_particles(context, center, "SMOKE")
                      if counter >= 40 then
                        context.scheduler.cancel_task(task_id)
                        phase_four()
                      end
                    end)
                  end

                  local function phase_two()
                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      counter = counter + 1
                      spawn_particle_ring(context, center, "FLAME", 0.5, 10)
                      damage_nearby(context, center, 0.5)
                      spawn_particle_ring(context, center, "SMOKE", 3, 10)
                      if counter >= 40 then
                        context.scheduler.cancel_task(task_id)
                        phase_three()
                      end
                    end)
                  end

                  local counter = 0
                  local task_id
                  task_id = context.scheduler.run_every(1, function(context)
                    counter = counter + 1
                    spawn_particle_ring(context, center, "SMOKE", 0.5, 10)
                    if counter >= 40 then
                      context.scheduler.cancel_task(task_id)
                      phase_two()
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if math.random() > 0.25 or not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(400)

                    context.boss:set_ai_enabled(false)
                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      if not context.boss:is_alive() then
                        context.scheduler.cancel_task(task_id)
                        return
                      end

                      counter = counter + 1
                      if counter > 200 then
                        context.scheduler.cancel_task(task_id)
                        context.boss:set_ai_enabled(true)
                        return
                      end

                      if counter % 5 == 0 then
                        local center = offset_location(context.boss:get_location(), math.random(-20, 19), 0, math.random(-20, 19))
                        start_fire_pyre(context, center)
                      end
                    end)
                  end
                }
                """);
    }
}
