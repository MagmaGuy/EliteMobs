package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class PlasmaBlasterLuaConfig extends InlineLuaPowerConfig {
    public PlasmaBlasterLuaConfig() {
        super("plasma_blaster", null, PowersConfigFields.PowerType.MAJOR_ENDERMAN, """
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

                local function is_valid_target(entity)
                  return entity ~= nil and entity.is_player and entity.game_mode ~= "SPECTATOR"
                end

                local function do_damage_fireworks(context, end_location)
                  for _ = 1, 200 do
                    context.world.spawn_particle_at_location(end_location, {
                      particle = "DUST",
                      amount = 1,
                      x = 3,
                      y = 3,
                      z = 3,
                      speed = 1,
                      red = math.random(122, 255),
                      green = math.random(122, 255),
                      blue = math.random(0, 100)
                    })
                  end
                end

                local function do_visual_effect(context, location, counter)
                  local spread = 0.1 * counter / 12.0
                  context.world.spawn_particle_at_location(location, {
                    particle = "DUST",
                    amount = 10,
                    x = spread,
                    y = spread,
                    z = spread,
                    speed = 1,
                    red = math.random(0, 100),
                    green = math.random(122, 255),
                    blue = math.random(0, 100)
                  })
                end

                local function create_projectile(context, shot_vector, source_location, player)
                  local current_location = offset_location(source_location, 0, 1, 0)
                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if counter > 60 then
                      context.scheduler:cancel_task(task_id)
                      return
                    end
                    counter = counter + 1

                    if player:overlaps_box_at_location(current_location, 0.5, 0.5, 0.5) then
                      player:deal_damage_from_boss(1)
                      do_damage_fireworks(context, offset_location(player:get_location(), 0, 1, 0))
                    end
                    if counter % 5 == 0 then
                      do_visual_effect(context, current_location, counter)
                    end

                    current_location = offset_location(
                      current_location,
                      shot_vector.x,
                      shot_vector.y,
                      shot_vector.z
                    )
                    if not context.world.is_passable_at_location(current_location) then
                      context.scheduler:cancel_task(task_id)
                      return
                    end
                  end)
                end

                local function start_scan_task(context)
                  if context.state.plasma_blaster_task ~= nil then
                    return
                  end

                  context.state.plasma_blaster_task = context.scheduler:run_every(80, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(context.state.plasma_blaster_task)
                      context.state.plasma_blaster_task = nil
                      return
                    end

                    local entities = context.entities.get_nearby_entities(30, "living")
                    for index = 1, #entities do
                      local entity = entities[index]
                      if is_valid_target(entity) then
                        local shot_vector = context.vectors.get_vector_between_locations(
                          context.boss:get_location(),
                          entity:get_location()
                        )
                        shot_vector = context.vectors.normalize_vector(shot_vector)
                        shot_vector.x = shot_vector.x * 0.5
                        shot_vector.y = shot_vector.y * 0.5
                        shot_vector.z = shot_vector.z * 0.5
                        create_projectile(context, shot_vector, context.boss:get_location(), entity)
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
                    if context.state.plasma_blaster_task ~= nil then
                      context.scheduler:cancel_task(context.state.plasma_blaster_task)
                      context.state.plasma_blaster_task = nil
                    end
                  end
                }
                """);
    }
}
