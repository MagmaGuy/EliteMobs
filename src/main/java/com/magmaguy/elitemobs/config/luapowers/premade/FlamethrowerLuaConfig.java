package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FlamethrowerLuaConfig extends InlineLuaPowerConfig {
    public FlamethrowerLuaConfig() {
        super("flamethrower", null, PowersConfigFields.PowerType.UNIQUE, """
                local function clone_location(location)
                  return em.create_location(
                    location.x,
                    location.y,
                    location.z,
                    location.world,
                    location.yaw,
                    location.pitch
                  )
                end

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

                local function generate_damage_points(context, fixed_player_location)
                  local locations = {}
                  local current = clone_location(context.boss:get_location())
                  local step = context.vectors.get_vector_between_locations(current, fixed_player_location)
                  step = context.vectors.normalize_vector(step)
                  step.x = step.x * 0.5
                  step.y = step.y * 0.5
                  step.z = step.z * 0.5
                  for index = 1, 40 do
                    current = offset_location(current, step.x, step.y, step.z)
                    locations[index] = clone_location(current)
                  end
                  return locations
                end

                local function do_damage(context, locations)
                  local entities = context.entities.get_nearby_entities(30, "living")
                  for entity_index = 1, #entities do
                    local entity = entities[entity_index]
                    local entity_location = entity:get_location()
                    for point_index = 1, #locations do
                      local point = locations[point_index]
                      if entity_location.world == point.world then
                        local dx = entity_location.x - point.x
                        local dy = entity_location.y - point.y
                        local dz = entity_location.z - point.z
                        if math.abs(dx) <= 0.5 and math.abs(dy) <= 0.5 and math.abs(dz) <= 0.5 then
                          entity:deal_custom_damage(1)
                          break
                        end
                      end
                    end
                  end
                end

                local function do_particle_effect(context, fixed_player_location, particle)
                  local direction = context.vectors.get_vector_between_locations(context.boss:get_location(), fixed_player_location)
                  direction = context.vectors.normalize_vector(direction)
                  local origin = context.boss:get_eye_location()
                  origin = offset_location(origin, direction.x, -0.5, direction.z)
                  for _ = 1, 5 do
                    context.world:spawn_particle_at_location(origin, {
                      particle = particle,
                      amount = 0,
                      x = (math.random() - 0.5) * 0.1 + direction.x,
                      y = (math.random() - 0.5) * 0.1 + direction.y,
                      z = (math.random() - 0.5) * 0.1 + direction.z,
                      speed = math.random() + 0.05
                    })
                  end
                end

                local function finish_flamethrower(context)
                  context.boss:set_ai_enabled(true)
                  context.state.flamethrower_active = false
                end

                local function run_phase_three(context, fixed_player_location)
                  local timer = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(task_id)
                      context.state.flamethrower_active = false
                      return
                    end

                    timer = timer + 1
                    do_particle_effect(context, fixed_player_location, "SMOKE")
                    if timer >= 20 then
                      context.scheduler:cancel_task(task_id)
                      finish_flamethrower(context)
                    end
                  end)
                end

                local function run_phase_two(context, fixed_player_location)
                  local damage_points = generate_damage_points(context, fixed_player_location)
                  local timer = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(task_id)
                      context.state.flamethrower_active = false
                      return
                    end

                    do_particle_effect(context, fixed_player_location, "FLAME")
                    do_damage(context, damage_points)
                    timer = timer + 1
                    if timer >= 60 then
                      context.scheduler:cancel_task(task_id)
                      run_phase_three(context, fixed_player_location)
                    end
                  end)
                end

                local function run_phase_one(context, fixed_player_location)
                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(task_id)
                      context.state.flamethrower_active = false
                      return
                    end

                    do_particle_effect(context, fixed_player_location, "SMOKE")
                    counter = counter + 1
                    if counter >= 40 then
                      context.scheduler:cancel_task(task_id)
                      run_phase_two(context, fixed_player_location)
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.player == nil or context.state.flamethrower_active or math.random() > 0.25 then
                      return
                    end
                    if not context.cooldowns.check_local("flamethrower", 400) then
                      return
                    end

                    context.cooldowns.set_global(300)
                    context.state.flamethrower_active = true
                    context.boss:set_ai_enabled(false)
                    run_phase_one(context, clone_location(context.player:get_location()))
                  end
                }
                """);
    }
}
