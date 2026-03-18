package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class PhotonRayLuaConfig extends InlineLuaPowerConfig {
    public PhotonRayLuaConfig() {
        super("photon_ray", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function distance_squared(first, second)
                  local dx = first.x - second.x
                  local dy = first.y - second.y
                  local dz = first.z - second.z
                  return (dx * dx) + (dy * dy) + (dz * dz)
                end

                local function is_valid_target(entity)
                  return entity ~= nil and entity.is_player and entity.game_mode ~= "SPECTATOR"
                end

                local function generate_ray_vector(context, source, target)
                  local vector = context.vectors.get_vector_between_locations(
                    offset_location(source, 0, 1, 0),
                    offset_location(target, 0, 1, 0)
                  )
                  vector = context.vectors.normalize_vector(vector)
                  vector.x = vector.x * 0.5
                  vector.y = vector.y * 0.5
                  vector.z = vector.z * 0.5
                  return vector
                end

                local function drag_target(context, original_vector, source_location, target_location)
                  context.state.photon_ray_player_locations = context.state.photon_ray_player_locations or {}
                  local player_locations = context.state.photon_ray_player_locations
                  local count = #player_locations

                  if count < 5 then
                    player_locations[count + 1] = clone_location(target_location)
                    return original_vector
                  end

                  local old_target = player_locations[1]
                  local new_vector = generate_ray_vector(context, source_location, old_target)

                  for index = 1, count - 1 do
                    player_locations[index] = player_locations[index + 1]
                  end
                  player_locations[count] = clone_location(target_location)

                  return new_vector
                end

                local function do_damage(context, location)
                  local entities = context.entities.get_nearby_entities(60, "living")
                  for index = 1, #entities do
                    local entity = entities[index]
                    local entity_location = entity:get_location()
                    if entity.uuid ~= context.boss.uuid
                      and entity_location.world == location.world
                      and math.abs(entity_location.x - location.x) <= 1.0
                      and math.abs(entity_location.y - location.y) <= 1.0
                      and math.abs(entity_location.z - location.z) <= 1.0 then
                      entity:deal_custom_damage(1)
                    end
                  end
                end

                local function do_warning_particle(context, location)
                  context.world.spawn_particle_at_location(location, {
                    particle = "DUST",
                    amount = 5,
                    x = 0.2,
                    y = 0.2,
                    z = 0.2,
                    speed = 1,
                    red = 0,
                    green = 0,
                    blue = 0
                  })
                end

                local function do_damage_particles(context, location)
                  context.world.spawn_particle_at_location(location, {
                    particle = "DUST",
                    amount = 5,
                    x = 0.2,
                    y = 0.2,
                    z = 0.2,
                    speed = 1,
                    red = math.random(0, 100),
                    green = math.random(0, 100),
                    blue = math.random(100, 255)
                  })

                  local entities = context.entities.get_nearby_entities(60, "living")
                  for index = 1, #entities do
                    local entity = entities[index]
                    local entity_location = entity:get_location()
                    if entity.is_player
                      and entity_location.world == location.world
                      and math.abs(entity_location.x - location.x) <= 1.0
                      and math.abs(entity_location.y - location.y) <= 1.0
                      and math.abs(entity_location.z - location.z) <= 1.0 then
                      do_damage(context, location)
                    end
                  end
                end

                local function do_raytrace_laser(context, laser_vector, source, warning_phase)
                  local clone_location_value = offset_location(source, 0, 1, 0)
                  for _ = 1, 120 do
                    local next_location = offset_location(
                      clone_location_value,
                      laser_vector.x,
                      laser_vector.y,
                      laser_vector.z
                    )
                    if not context.world.is_passable_at_location(next_location) then
                      local block_center = em.create_location(
                        math.floor(next_location.x) + 0.5,
                        math.floor(next_location.y) + 0.5,
                        math.floor(next_location.z) + 0.5,
                        next_location.world,
                        next_location.yaw,
                        next_location.pitch
                      )
                      local tentative_distance = context.vectors.get_vector_between_locations(clone_location_value, block_center)
                      local x = laser_vector.x
                      local y = laser_vector.y
                      local z = laser_vector.z
                      local x_abs = math.abs(tentative_distance.x)
                      local y_abs = math.abs(tentative_distance.y)
                      local z_abs = math.abs(tentative_distance.z)
                      if x_abs > y_abs and x_abs > z_abs then
                        x = x * -1
                      elseif y_abs > x_abs and y_abs > z_abs then
                        y = y * -1
                      elseif z_abs > y_abs and z_abs > x_abs then
                        z = z * -1
                      else
                        context.log.warn("MagmaGuy is bad at math!")
                      end
                      laser_vector = em.create_vector(x, y, z)
                    end

                    clone_location_value = offset_location(
                      clone_location_value,
                      laser_vector.x,
                      laser_vector.y,
                      laser_vector.z
                    )
                    if warning_phase then
                      do_warning_particle(context, clone_location_value)
                    else
                      do_damage_particles(context, clone_location_value)
                    end
                  end
                  return laser_vector
                end

                local function create_ray(context, target, source_location)
                  context.boss:set_ai_enabled(false)
                  local counter = 0
                  local laser_vector = generate_ray_vector(context, source_location, target:get_location())
                  local task_id
                  task_id = context.scheduler:run_every(2, function(context)
                    local current_source = context.boss:get_location()
                    local target_location = target:get_location()
                    if counter > 30
                      or not target:is_alive()
                      or target_location.world ~= source_location.world
                      or distance_squared(target_location, source_location) > (60 * 60)
                      or not context.boss:is_alive() then
                      context.scheduler:cancel_task(task_id)
                      if context.boss:is_alive() then
                        context.boss:set_ai_enabled(true)
                      end
                      return
                    end

                    laser_vector = drag_target(context, laser_vector, current_source, target_location)
                    laser_vector = do_raytrace_laser(context, laser_vector, current_source, counter < (20 / 4.0))
                    counter = counter + 1
                  end)
                end

                local function start_scan_task(context)
                  if context.state.photon_ray_scan_task ~= nil then
                    return
                  end

                  context.state.photon_ray_scan_task = context.scheduler:run_every(1, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(context.state.photon_ray_scan_task)
                      context.state.photon_ray_scan_task = nil
                      return
                    end

                    if not context.cooldowns.local_ready("photon_ray") or not context.cooldowns.global_ready() then
                      return
                    end

                    local entities = context.entities.get_nearby_entities(60, "living")
                    for index = 1, #entities do
                      local entity = entities[index]
                      if is_valid_target(entity) then
                        context.cooldowns.set_local(1200, "photon_ray")
                        context.cooldowns.set_global(400)
                        create_ray(context, entity, context.boss:get_location())
                        break
                      end
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    start_scan_task(context)
                  end,
                  on_exit_combat = function(context)
                    if context.state.photon_ray_scan_task ~= nil then
                      context.scheduler:cancel_task(context.state.photon_ray_scan_task)
                      context.state.photon_ray_scan_task = nil
                    end
                  end
                }
                """);
    }
}
