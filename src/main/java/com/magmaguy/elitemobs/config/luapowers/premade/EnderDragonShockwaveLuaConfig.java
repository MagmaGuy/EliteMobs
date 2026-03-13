package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class EnderDragonShockwaveLuaConfig extends InlineLuaPowerConfig {
    public EnderDragonShockwaveLuaConfig() {
        super("ender_dragon_shockwave", null, PowersConfigFields.PowerType.UNIQUE, """
                local radius = 30

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

                local function cancel_state_task(context, key)
                  local task_id = context.state[key]
                  if task_id ~= nil then
                    context.scheduler:cancel_task(task_id)
                    context.state[key] = nil
                  end
                end

                local function set_affected_blocks(context)
                  local pie_blocks = {}
                  local rotation = math.random() * 2
                  for x = -radius, radius - 1 do
                    for z = -radius, radius - 1 do
                      if not (x > 0 and z > 0) then
                        local distance = math.sqrt((x * x) + (z * z))
                        if distance <= radius then
                          pie_blocks[#pie_blocks + 1] = {
                            distance = math.ceil(distance),
                            vector = rotate_around_y(em.create_vector(x, 0, z), rotation)
                          }
                        end
                      end
                    end
                  end
                  context.state.shockwave_pie_blocks = pie_blocks
                end

                local function generate_real_circle(context)
                  local real_blocks = {}
                  local boss_location = context.boss:get_location()
                  local pie_blocks = context.state.shockwave_pie_blocks or {}
                  for index = 1, #pie_blocks do
                    local pie_block = pie_blocks[index]
                    local raw_location = offset_location(boss_location, pie_block.vector.x, pie_block.vector.y, pie_block.vector.z)
                    for y = 0, -9, -1 do
                      local temp_location = offset_location(raw_location, 0, y, 0)
                      if not context.world:is_passthrough_at_location(temp_location) then
                        real_blocks[#real_blocks + 1] = {
                          distance = pie_block.distance,
                          vector = em.create_vector(pie_block.vector.x, pie_block.vector.y + y, pie_block.vector.z)
                        }
                        break
                      end
                    end
                  end
                  context.state.shockwave_real_blocks = real_blocks
                end

                local function do_warning_phase(context)
                  local boss_location = context.boss:get_location()
                  local warning_phase_counter = context.state.shockwave_warning_phase_counter or 0
                  local real_blocks = context.state.shockwave_real_blocks or {}
                  for index = 1, #real_blocks do
                    local pie_block = real_blocks[index]
                    if pie_block.distance < warning_phase_counter then
                      local raw_location = offset_location(boss_location, pie_block.vector.x, pie_block.vector.y, pie_block.vector.z)
                      if not context.world:is_passthrough_at_location(raw_location) then
                        context.world:spawn_particle_at_location(
                          offset_location(raw_location, 0.5, 1, 0.5),
                          { particle = "SOUL", amount = 1, x = 0.1, y = 0.1, z = 0.1, speed = 0.1 }
                        )
                      end
                    end
                  end
                  context.state.shockwave_warning_phase_counter = warning_phase_counter + 1
                end

                local function do_damage_phase(context)
                  local boss_location = context.boss:get_location()
                  local damage_phase_counter = context.state.shockwave_damage_phase_counter or 0
                  local block_locations = {}
                  local remaining_blocks = {}
                  local real_blocks = context.state.shockwave_real_blocks or {}
                  for index = 1, #real_blocks do
                    local pie_block = real_blocks[index]
                    if pie_block.distance < damage_phase_counter then
                      local raw_location = offset_location(boss_location, pie_block.vector.x, pie_block.vector.y, pie_block.vector.z)
                      if not context.world:is_passthrough_at_location(raw_location) then
                        local hit_box_center = offset_location(raw_location, 0.5, 1.5, 0.5)
                        local entities = context.entities:get_entities_in_box(hit_box_center, 0.5, 1.5, 0.5, "living")
                        for entity_index = 1, #entities do
                          local entity = entities[entity_index]
                          if entity.entity_type ~= "FALLING_BLOCK" then
                            local push = context.vectors.get_vector_between_locations(boss_location, entity:get_location())
                            push.y = 1
                            push = multiply(normalize(push), 3)
                            entity:set_velocity_vector(push)
                            if entity.is_player then
                              entity:deal_custom_damage(20)
                            end
                          end
                        end

                        if context.world:get_blast_resistance_at_location(raw_location) < 7 then
                          block_locations[#block_locations + 1] = raw_location
                        end
                      end
                    else
                      remaining_blocks[#remaining_blocks + 1] = pie_block
                    end
                  end

                  context.state.shockwave_real_blocks = remaining_blocks
                  context.state.shockwave_damage_phase_counter = damage_phase_counter + 1
                  context.world:generate_fake_explosion(block_locations, boss_location)
                end

                local function stop_power(context)
                  cancel_state_task(context, "ender_dragon_shockwave_task")
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    if context.state.ender_dragon_shockwave_scan_task ~= nil then
                      return
                    end

                    context.state.ender_dragon_shockwave_scan_task = context.scheduler:run_every(10, function(context)
                      if not context.boss:is_alive() then
                        cancel_state_task(context, "ender_dragon_shockwave_scan_task")
                        stop_power(context)
                        return
                      end

                      if context.state.ender_dragon_shockwave_task ~= nil
                        or not context.cooldowns:local_ready("ender_dragon_shockwave")
                        or not context.cooldowns:global_ready()
                        or not is_landed_phase(context.boss:get_ender_dragon_phase()) then
                        return
                      end

                      context.cooldowns:set_local(20 * 60, "ender_dragon_shockwave")
                      context.cooldowns:set_global(20 * 30)
                      set_affected_blocks(context)
                      generate_real_circle(context)
                      context.state.shockwave_warning_phase_counter = 0
                      context.state.shockwave_damage_phase_counter = 0

                      local counter = 0
                      context.state.ender_dragon_shockwave_task = context.scheduler:run_every(1, function(context)
                        if not context.boss:is_alive() then
                          stop_power(context)
                          return
                        end

                        context.boss:set_ender_dragon_phase("SEARCH_FOR_BREATH_ATTACK_TARGET")

                        if not is_landed_phase(context.boss:get_ender_dragon_phase()) then
                          stop_power(context)
                          return
                        end

                        if counter < (20 * 3) then
                          do_warning_phase(context)
                        end
                        if counter > (20 * 3) then
                          do_damage_phase(context)
                        end
                        if counter >= (20 * (3 + 6)) then
                          stop_power(context)
                          return
                        end

                        counter = counter + 1
                      end)
                    end)
                  end,
                  on_exit_combat = function(context)
                    cancel_state_task(context, "ender_dragon_shockwave_scan_task")
                    stop_power(context)
                  end
                }
                """);
    }
}
