package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FrostConeLuaConfig extends InlineLuaPowerConfig {
    public FrostConeLuaConfig() {
        super("frost_cone", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function get_shot_vector(context, fixed_player_location)
                  local shot = context.vectors.get_vector_between_locations(context.boss:get_location(), fixed_player_location)
                  shot = context.vectors.normalize_vector(shot)
                  shot.x = shot.x + (math.random() - 0.5)
                  shot.y = shot.y + ((math.random() - 0.5) * 0.5)
                  shot.z = shot.z + (math.random() - 0.5)
                  return context.vectors.normalize_vector(shot)
                end

                local function spawn_smoke(context, fixed_player_location)
                  for _ = 1, 100 do
                    local shot = get_shot_vector(context, fixed_player_location)
                    context.world.spawn_particle_at_location(offset_location(context.boss:get_location(), 0, 1, 0), {
                      particle = "SMOKE",
                      amount = 0,
                      x = shot.x,
                      y = shot.y,
                      z = shot.z,
                      speed = 0.75
                    })
                  end
                end

                local function spawn_snowball(context, fixed_player_location)
                  local shot = get_shot_vector(context, fixed_player_location)
                  local origin = offset_location(context.boss:get_location(), shot.x, shot.y + 1, shot.z)
                  local destination = offset_location(origin, shot.x, shot.y, shot.z)
                  local projectile = context.boss:summon_projectile("SNOWBALL", origin, destination, 1.0, {
                    gravity = false,
                    persistent = false,
                    duration = 60,
                    custom_damage = 2
                  })
                  if projectile ~= nil then
                    context.state.frost_cone_projectiles = context.state.frost_cone_projectiles or {}
                    context.state.frost_cone_projectiles[projectile.uuid] = true
                    context.scheduler.run_after(60, function(context)
                      if context.state.frost_cone_projectiles ~= nil then
                        context.state.frost_cone_projectiles[projectile.uuid] = nil
                      end
                    end)
                  end
                end

                local function finish_frost_cone(context)
                  if context.boss:is_alive() then
                    context.boss:set_ai_enabled(true)
                  end
                  context.state.frost_cone_active = false
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.player == nil or context.state.frost_cone_active then
                      return
                    end
                    if not context.cooldowns.check_local("frost_cone", 300) then
                      return
                    end

                    context.cooldowns.set_global(140)
                    context.state.frost_cone_active = true
                    local fixed_player_location = clone_location(context.player:get_location())
                    context.boss:set_ai_enabled(false)

                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      counter = counter + 1
                      if not context.boss:is_alive() or context.boss:get_location().world ~= fixed_player_location.world or counter > 120 then
                        context.scheduler.cancel_task(task_id)
                        finish_frost_cone(context)
                        return
                      end

                      if counter < 60 then
                        spawn_smoke(context, fixed_player_location)
                        return
                      end

                      for _ = 1, 10 do
                        spawn_snowball(context, fixed_player_location)
                      end
                    end)
                  end,

                  on_player_damaged_by_boss = function(context)
                    if context.player == nil or context.event.projectile == nil then
                      return
                    end
                    if context.state.frost_cone_projectiles == nil or not context.state.frost_cone_projectiles[context.event.projectile.uuid] then
                      return
                    end

                    local player_uuid = context.player.uuid
                    context.state.frost_cone_stacks = context.state.frost_cone_stacks or {}
                    local current_amount = (context.state.frost_cone_stacks[player_uuid] or 0) + 1
                    context.state.frost_cone_stacks[player_uuid] = current_amount
                    context.player:apply_potion_effect("SLOWNESS", 140, current_amount)

                    context.scheduler.run_after(100, function(context)
                      if context.state.frost_cone_stacks ~= nil and context.state.frost_cone_stacks[player_uuid] == current_amount then
                        context.state.frost_cone_stacks[player_uuid] = nil
                      end
                    end)
                  end
                }
                """);
    }
}
