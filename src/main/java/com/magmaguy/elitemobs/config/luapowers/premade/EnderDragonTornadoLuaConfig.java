package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class EnderDragonTornadoLuaConfig extends InlineLuaPowerConfig {
    public EnderDragonTornadoLuaConfig() {
        super("ender_dragon_tornado", null, PowersConfigFields.PowerType.UNIQUE, """
                local function clone_location(location)
                  return em.create_location(location.x, location.y, location.z, location.world, location.yaw, location.pitch)
                end

                local function offset_location(location, x, y, z)
                  return em.create_location(location.x + x, location.y + y, location.z + z, location.world, location.yaw, location.pitch)
                end

                local function vector_length(vector)
                  return math.sqrt((vector.x * vector.x) + (vector.y * vector.y) + (vector.z * vector.z))
                end

                local function normalize(vector)
                  local length = vector_length(vector)
                  if length <= 0 then
                    return em.create_vector(0, 0, 0)
                  end
                  return em.create_vector(vector.x / length, vector.y / length, vector.z / length)
                end

                local function multiply(vector, amount)
                  return em.create_vector(vector.x * amount, vector.y * amount, vector.z * amount)
                end

                local function add_vectors(first, second)
                  return em.create_vector(first.x + second.x, first.y + second.y, first.z + second.z)
                end

                local function subtract_vectors(first, second)
                  return em.create_vector(first.x - second.x, first.y - second.y, first.z - second.z)
                end

                local function rotate_around_y(vector, radians)
                  local cosine = math.cos(radians)
                  local sine = math.sin(radians)
                  return em.create_vector(
                    (vector.x * cosine) + (vector.z * sine),
                    vector.y,
                    (-vector.x * sine) + (vector.z * cosine)
                  )
                end

                local function is_landed_phase(phase)
                  return phase == "BREATH_ATTACK"
                    or phase == "SEARCH_FOR_BREATH_ATTACK_TARGET"
                    or phase == "ROAR_BEFORE_ATTACK"
                end

                local function bxor(a, b)
                  local result = 0
                  local bit = 1
                  while a > 0 or b > 0 do
                    local a_bit = a %% 2
                    local b_bit = b %% 2
                    if a_bit ~= b_bit then
                      result = result + bit
                    end
                    a = math.floor(a / 2)
                    b = math.floor(b / 2)
                    bit = bit * 2
                  end
                  return result
                end

                local function cancel_state_task(context, key)
                  local task_id = context.state[key]
                  if task_id ~= nil then
                    context.scheduler:cancel_task(task_id)
                    context.state[key] = nil
                  end
                end

                local function do_tornado_particles(context)
                  local tornado_eye = context.state.tornado_eye
                  for i = 0, 20 do
                    for _ = 0, (bxor(i, 2) + 1) do
                      local random_particle_vector = rotate_around_y(em.create_vector(i / 2, i, 0), math.random() * (2 * math.pi))
                      local random_particle_location = offset_location(tornado_eye, random_particle_vector.x, random_particle_vector.y, random_particle_vector.z)
                      if math.random() < 0.7 then
                        context.world:spawn_particle_at_location(random_particle_location, {
                          particle = "LARGE_SMOKE", amount = 1, x = 0, y = 0, z = 0, speed = 0.05
                        })
                      else
                        context.world:spawn_particle_at_location(random_particle_location, {
                          particle = "SOUL_FIRE_FLAME", amount = 1, x = 0, y = 0, z = 0, speed = 0.05
                        })
                      end
                    end
                  end
                end

                local function do_terrain_destruction(context)
                  local block_locations = {}
                  for _ = 1, 5 do
                    local location = offset_location(
                      context.state.tornado_eye,
                      math.random(-5, 4),
                      0,
                      math.random(-5, 4)
                    )
                    local highest_y = context.world:get_highest_block_y_at_location(location)
                    local block_location = em.create_location(location.x, highest_y, location.z, location.world, location.yaw, location.pitch)
                    if context.world:get_blast_resistance_at_location(block_location) <= 7 and highest_y ~= -1 then
                      block_locations[#block_locations + 1] = block_location
                    end
                  end
                  context.world:generate_fake_explosion(block_locations, context.state.tornado_eye)
                end

                local function do_entity_displacement(context)
                  local source_entity = context.boss
                  local nearby_entities = context.entities.get_nearby_entities(21, "living")
                  for index = 1, #nearby_entities do
                    local entity = nearby_entities[index]
                    if entity.uuid ~= source_entity.uuid then
                      local entity_location = entity:get_location()
                      if math.abs(entity_location.x - context.state.tornado_eye.x) <= 7
                        and math.abs(entity_location.z - context.state.tornado_eye.z) <= 7 then
                        local relative = context.vectors.get_vector_between_locations(context.state.tornado_eye, entity_location)
                        local rotated_relative = rotate_around_y(relative, 0.5 * math.pi)
                        local vector = subtract_vectors(rotated_relative, relative)
                        vector = multiply(normalize(vector), 0.5)
                        vector.y = 0.5
                        entity:set_velocity_vector(add_vectors(entity:get_velocity(), vector))
                      end
                    end
                  end
                end

                local function stop_power(context)
                  cancel_state_task(context, "ender_dragon_tornado_task")
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    if context.state.ender_dragon_tornado_scan_task ~= nil then
                      return
                    end

                    context.state.ender_dragon_tornado_scan_task = context.scheduler:run_every(10, function(context)
                      if not context.boss:is_alive() then
                        cancel_state_task(context, "ender_dragon_tornado_scan_task")
                        stop_power(context)
                        return
                      end

                      if context.state.ender_dragon_tornado_task ~= nil
                        or not context.cooldowns:local_ready("ender_dragon_tornado")
                        or not context.cooldowns:global_ready()
                        or not is_landed_phase(context.boss:get_ender_dragon_phase()) then
                        return
                      end

                      context.cooldowns:set_local(20 * 60, "ender_dragon_tornado")
                      context.cooldowns:set_global(20 * 30)

                      local boss_location = context.boss:get_location()
                      local offset = rotate_around_y(em.create_vector(6, -6, 0), math.random() * (2 * math.pi))
                      context.state.tornado_eye = em.create_location(
                        boss_location.x + offset.x,
                        boss_location.y + offset.y,
                        boss_location.z + offset.z,
                        boss_location.world,
                        boss_location.yaw,
                        boss_location.pitch
                      )

                      local tornado_speed = context.vectors.get_vector_between_locations(boss_location, context.state.tornado_eye)
                      tornado_speed.y = 0
                      context.state.tornado_speed = multiply(normalize(tornado_speed), 0.2)

                      local counter = 0
                      context.state.ender_dragon_tornado_task = context.scheduler:run_every(1, function(context)
                        if not context.boss:is_alive() then
                          stop_power(context)
                          return
                        end

                        context.boss:set_ender_dragon_phase("SEARCH_FOR_BREATH_ATTACK_TARGET")

                        if not is_landed_phase(context.boss:get_ender_dragon_phase()) then
                          stop_power(context)
                          return
                        end
                        context.state.tornado_eye = offset_location(
                          context.state.tornado_eye,
                          context.state.tornado_speed.x,
                          context.state.tornado_speed.y,
                          context.state.tornado_speed.z
                        )

                        if counter %% 2 == 0 then
                          do_tornado_particles(context)
                          do_terrain_destruction(context)
                          do_entity_displacement(context)
                        end

                        if counter > (20 * 6) then
                          stop_power(context)
                          return
                        end

                        counter = counter + 1
                      end)
                    end)
                  end,
                  on_exit_combat = function(context)
                    cancel_state_task(context, "ender_dragon_tornado_scan_task")
                    stop_power(context)
                  end
                }
                """);
    }
}
