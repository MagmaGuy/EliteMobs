package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class BulletHellLuaConfig extends InlineLuaPowerConfig {
    public BulletHellLuaConfig() {
        super("bullet_hell", null, PowersConfigFields.PowerType.MAJOR_SKELETON, """
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

                local function is_combat_player(player)
                  return player ~= nil and (player.game_mode == "SURVIVAL" or player.game_mode == "ADVENTURE")
                end

                local function distance_squared(first, second)
                  local dx = first.x - second.x
                  local dy = first.y - second.y
                  local dz = first.z - second.z
                  return (dx * dx) + (dy * dy) + (dz * dz)
                end

                local function arrow_adjustment(context, arrow, player)
                  local target = player:get_eye_location()
                  target = offset_location(target, 0, -0.5, 0)
                  local adjustment = context.vectors.get_vector_between_locations(arrow:get_location(), target)
                  adjustment = context.vectors.normalize_vector(adjustment)
                  adjustment.x = adjustment.x * 0.2
                  adjustment.y = adjustment.y * 0.2
                  adjustment.z = adjustment.z * 0.2
                  return adjustment
                end

                local function track_arrow(context, arrow, player)
                  local counter = 0
                  local task_id
                  task_id = context.scheduler.run_every(1, function(context)
                    local arrow_location = arrow:get_location()
                    local player_location = player:get_location()
                    if player:is_alive()
                      and arrow:is_valid()
                      and arrow_location.world == player_location.world
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
                      arrow:unregister("EFFECT_TIMEOUT")
                      context.scheduler.cancel_task(task_id)
                      return
                    end

                    if counter > 200 then
                      arrow:set_gravity(true)
                      arrow:unregister("EFFECT_TIMEOUT")
                      context.scheduler.cancel_task(task_id)
                      return
                    end

                    counter = counter + 1
                  end)
                end

                local function spawn_tracking_arrow(context, player)
                  local boss_location = context.boss:get_location()
                  local player_location = player:get_location()
                  local arrow = context.boss:summon_projectile("ARROW", boss_location, player_location, 1.0, {
                    gravity = false,
                    persistent = false
                  })
                  if arrow ~= nil then
                    local velocity = arrow:get_velocity()
                    arrow:set_velocity_vector(em.create_vector(
                      velocity.x * 0.5,
                      velocity.y * 0.5,
                      velocity.z * 0.5
                    ))
                    track_arrow(context, arrow, player)
                  end
                end

                local function finish_bullet_hell(context, location)
                  if context.boss:is_alive() then
                    context.boss:set_ai_enabled(true)
                    context.boss:teleport_to_location(location)
                  end
                  context.state.bullet_hell_active = false
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.state.bullet_hell_active or math.random() > 0.25 then
                      return
                    end
                    if not context.cooldowns.check_local("bullet_hell", 400) then
                      return
                    end

                    context.state.bullet_hell_active = true
                    context.boss:set_ai_enabled(false)

                    local hover_location = offset_location(context.boss:get_location(), 0, 10, 0)
                    if context.world.get_block_type_at_location(hover_location) == "AIR" then
                      context.boss:teleport_to_location(hover_location)
                    end

                    local initial_location = clone_location(context.boss:get_location())
                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(10, function(context)
                      if not context.boss:is_alive() then
                        context.scheduler.cancel_task(task_id)
                        context.state.bullet_hell_active = false
                        return
                      end

                      context.boss:spawn_particle_at_self({
                        particle = "DRIPPING_WATER",
                        amount = 10,
                        x = 1,
                        y = 1,
                        z = 1
                      })

                      local players = context.boss:get_nearby_players(20)
                      for index = 1, #players do
                        if is_combat_player(players[index]) then
                          spawn_tracking_arrow(context, players[index])
                        end
                      end

                      counter = counter + 1
                      if counter > 20 then
                        context.scheduler.cancel_task(task_id)
                        finish_bullet_hell(context, initial_location)
                      end
                    end)
                  end
                }
                """);
    }
}
