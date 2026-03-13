package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class EnderDragonDiscoFireballsLuaConfig extends InlineLuaPowerConfig {
    public EnderDragonDiscoFireballsLuaConfig() {
        super("ender_dragon_disco_fireballs", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function scale_vector(vector, multiplier)
                  return em.create_vector(
                    vector.x * multiplier,
                    vector.y * multiplier,
                    vector.z * multiplier
                  )
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

                local function generate_downwards_vector(context, fireball)
                  local warning_counter = context.state.warning_counter or 0
                  local y_value = (math.cos(warning_counter) / 2) - 0.5
                  local downwards_vector = context.vectors.get_vector_between_locations(
                    context.boss:get_location(),
                    fireball:get_location()
                  )
                  downwards_vector = context.vectors.normalize_vector(downwards_vector)
                  downwards_vector.y = y_value
                  return downwards_vector
                end

                local function generate_visual_particles(context, fireball)
                  local downwards_vector = generate_downwards_vector(context, fireball)
                  local particle_location = clone_location(fireball:get_location())
                  for _ = 1, 200 do
                    particle_location = offset_location(
                      particle_location,
                      downwards_vector.x * 0.3,
                      downwards_vector.y * 0.3,
                      downwards_vector.z * 0.3
                    )
                    if not context.world:is_passable_at_location(particle_location) then
                      break
                    end
                    context.world:spawn_particle_at_location(particle_location, {
                      particle = "SMOKE",
                      amount = 1,
                      x = 1,
                      y = 0,
                      z = 0,
                      speed = 0
                    })
                  end
                end

                local function do_damage_phase(context)
                  local fireballs = context.state.fireballs or {}
                  for index = 1, #fireballs do
                    local fireball = fireballs[index]
                    if fireball:is_valid() then
                      fireball:set_direction_vector(scale_vector(generate_downwards_vector(context, fireball), 0.1))
                    end
                  end
                end

                local function do_warning_phase(context)
                  if context.state.warning_counter == 0 then
                    local fireballs = {}
                    local boss_location = context.boss:get_location()
                    for index = 0, 11 do
                      local angle = (2 * math.pi / 12) * index
                      local spawn_location = offset_location(
                        boss_location,
                        7 * math.cos(angle),
                        0,
                        7 * math.sin(angle)
                      )
                      local fireball = context.boss:summon_projectile(
                        "FIREBALL",
                        spawn_location,
                        spawn_location,
                        1.0,
                        {
                          spawn_at_origin = true,
                          track = true,
                          detonation_power = "ender_dragon_disco_fireballs.yml"
                        }
                      )
                      fireballs[#fireballs + 1] = fireball
                    end
                    context.state.fireballs = fireballs
                  end

                  context.state.warning_counter = (context.state.warning_counter or 0) + 1

                  local fireballs = context.state.fireballs or {}
                  for index = 1, #fireballs do
                    local fireball = fireballs[index]
                    if fireball ~= nil and fireball:is_valid() then
                      if (context.state.warning_counter % 5) == 0 then
                        local relative_location = context.vectors.get_vector_between_locations(
                          context.boss:get_location(),
                          fireball:get_location()
                        )
                        local rotated_location = offset_location(
                          context.boss:get_location(),
                          rotate_around_y(relative_location, 2 * math.pi / 96).x,
                          rotate_around_y(relative_location, 2 * math.pi / 96).y,
                          rotate_around_y(relative_location, 2 * math.pi / 96).z
                        )
                        local velocity = context.vectors.get_vector_between_locations(
                          fireball:get_location(),
                          rotated_location
                        )
                        velocity = scale_vector(velocity, 0.001)
                        fireball:set_direction_vector(velocity)
                        fireball:set_velocity_vector(velocity)
                        fireball:set_yield(5)
                      end

                      generate_visual_particles(context, fireball)
                    end
                  end
                end

                local function stop_power(context)
                  cancel_state_task(context, "disco_fireballs_task")
                  context.state.warning_counter = nil
                end

                local function start_power(context)
                  context.cooldowns:set_local(20 * 120, "ender_dragon_disco_fireballs")
                  context.cooldowns:set_global(20 * 30)
                  context.state.warning_counter = 0
                  context.state.fireballs = {}

                  local counter = 0
                  context.state.disco_fireballs_task = context.scheduler:run_every(1, function(context)
                    if not context.boss:is_alive() then
                      stop_power(context)
                      return
                    end

                    context.boss:set_ender_dragon_phase("SEARCH_FOR_BREATH_ATTACK_TARGET")

                    if not is_landed_phase(context.boss:get_ender_dragon_phase()) then
                      stop_power(context)
                      return
                    end

                    if counter < (20 * 6) then
                      do_warning_phase(context)
                    end

                    if counter > (20 * 6) then
                      do_damage_phase(context)
                      stop_power(context)
                      return
                    end

                    counter = counter + 1
                  end)
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    if context.state.disco_fireballs_scan_task ~= nil then
                      return
                    end

                    context.state.disco_fireballs_scan_task = context.scheduler:run_every(10, function(context)
                      if not context.boss:is_alive() then
                        cancel_state_task(context, "disco_fireballs_scan_task")
                        stop_power(context)
                        return
                      end

                      if context.state.disco_fireballs_task ~= nil
                        or not context.cooldowns:local_ready("ender_dragon_disco_fireballs")
                        or not context.cooldowns:global_ready() then
                        return
                      end

                      if is_landed_phase(context.boss:get_ender_dragon_phase()) then
                        start_power(context)
                      end
                    end)
                  end,
                  on_exit_combat = function(context)
                    cancel_state_task(context, "disco_fireballs_scan_task")
                    stop_power(context)
                  end
                }
                """);
    }
}
