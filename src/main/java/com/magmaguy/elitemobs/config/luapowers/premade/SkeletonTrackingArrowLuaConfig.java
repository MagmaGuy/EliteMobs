package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class SkeletonTrackingArrowLuaConfig extends InlineLuaPowerConfig {
    public SkeletonTrackingArrowLuaConfig() {
        super("skeleton_tracking_arrow", Material.ARROW.toString(), PowersConfigFields.PowerType.MAJOR_SKELETON, """
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

                local function is_combat_player(player)
                  return player ~= nil and (player.game_mode == "ADVENTURE" or player.game_mode == "SURVIVAL")
                end

                local function arrow_adjustment(context, arrow, player)
                  local target = offset_location(player:get_eye_location(), 0, -0.5, 0)
                  local adjustment = context.vectors.get_vector_between_locations(arrow:get_location(), target)
                  adjustment = context.vectors.normalize_vector(adjustment)
                  adjustment.x = adjustment.x * 0.1
                  adjustment.y = adjustment.y * 0.1
                  adjustment.z = adjustment.z * 0.1
                  return adjustment
                end

                local function tracking_arrow_loop(context, player, arrow)
                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    local player_location = player:get_location()
                    local arrow_location = arrow:get_location()
                    if player:is_alive()
                      and arrow:is_valid()
                      and player_location.world == arrow_location.world
                      and distance_squared(player_location, arrow_location) < 900
                      and not arrow:is_on_ground() then
                      if counter % 10 == 0 then
                        local velocity = arrow:get_velocity()
                        local adjustment = arrow_adjustment(context, arrow, player)
                        arrow:set_velocity_vector(em.create_vector(
                          velocity.x + adjustment.x,
                          velocity.y + adjustment.y,
                          velocity.z + adjustment.z
                        ))
                      end
                      context.world.spawn_particle_at_location(arrow_location, {
                        particle = "FLAME",
                        amount = 10,
                        x = 0.01,
                        y = 0.01,
                        z = 0.01,
                        speed = 0.01
                      })
                    else
                      arrow:set_gravity(true)
                      context.scheduler:cancel_task(task_id)
                      return
                    end

                    if counter > 1200 then
                      arrow:set_gravity(true)
                      context.scheduler:cancel_task(task_id)
                      return
                    end

                    counter = counter + 1
                  end)
                end

                local function spawn_tracking_arrow(context, player)
                  local target_vector = context.vectors.get_vector_between_locations(player:get_location(), player:get_eye_location())
                  target_vector = context.vectors.normalize_vector(target_vector)
                  target_vector.x = target_vector.x * 2
                  target_vector.y = target_vector.y * 2
                  target_vector.z = target_vector.z * 2

                  local boss_location = context.boss:get_location()
                  local origin = offset_location(boss_location, target_vector.x, target_vector.y + 1, target_vector.z)
                  local destination = offset_location(origin, target_vector.x, target_vector.y, target_vector.z)
                  local arrow = context.boss:summon_projectile(
                    "ARROW",
                    origin,
                    destination,
                    2.0,
                    {
                      spawn_at_origin = true,
                      persistent = false
                    }
                  )

                  if arrow == nil then
                    return
                  end

                  local velocity = arrow:get_velocity()
                  arrow:set_velocity_vector(em.create_vector(
                    velocity.x * 0.2,
                    velocity.y * 0.2,
                    velocity.z * 0.2
                  ))
                  arrow:set_gravity(false)
                  tracking_arrow_loop(context, player, arrow)
                end

                local function start_tracking(context)
                  context.state.skeleton_tracking_arrow_task = context.scheduler:run_every(160, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(context.state.skeleton_tracking_arrow_task)
                      context.state.skeleton_tracking_arrow_task = nil
                      context.state.skeleton_tracking_arrow_active = false
                      return
                    end

                    local nearby_players = context.boss:get_nearby_players(20)
                    for index = 1, #nearby_players do
                      local player = nearby_players[index]
                      if is_combat_player(player) then
                        spawn_tracking_arrow(context, player)
                      end
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_target_changed = function(context)
                    if context.state.skeleton_tracking_arrow_active then
                      return
                    end

                    context.state.skeleton_tracking_arrow_active = true
                    start_tracking(context)
                  end
                }
                """);
    }
}
