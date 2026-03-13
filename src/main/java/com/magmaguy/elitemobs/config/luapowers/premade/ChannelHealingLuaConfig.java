package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class ChannelHealingLuaConfig extends InlineLuaPowerConfig {
    public ChannelHealingLuaConfig() {
        super("channel_healing", null, PowersConfigFields.PowerType.MISCELLANEOUS, """
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

                local function find_healing_target(context)
                  local nearby_entities = context.entities.get_nearby_entities(20, "living")
                  for index = 1, #nearby_entities do
                    local entity = nearby_entities[index]
                    if entity.is_elite and not entity:is_healing() then
                      local maximum_health = entity:get_maximum_health()
                      if maximum_health > 0 and (entity:get_health() / maximum_health) <= 0.8 then
                        return entity
                      end
                    end
                  end
                  return nil
                end

                local function finish_channel(context, target)
                  if target ~= nil and target:is_alive() then
                    target:set_healing(false)
                  end
                  if context.boss:is_alive() then
                    context.boss:set_ai_enabled(true)
                  end
                  context.cooldowns.set_local(20, "channel_healing")
                  context.state.channel_healing_active = false
                  context.state.channel_healing_target = nil
                  context.state.channel_healing_task = nil
                end

                local function run_channel(context, target)
                  context.state.channel_healing_active = true
                  context.state.channel_healing_target = target
                  context.boss:set_ai_enabled(false)
                  target:set_healing(true)

                  local timer = 0
                  local task_id
                  task_id = context.scheduler:run_every(2, function(context)
                    local boss_location = context.boss:get_location()
                    local target_location = target:get_location()
                    local maximum_health = target:get_maximum_health()

                    if not context.boss:is_alive()
                      or not target:is_alive()
                      or maximum_health <= 0
                      or (target:get_health() / maximum_health) > 0.8
                      or boss_location.world ~= target_location.world
                      or distance_squared(boss_location, target_location) > (25 * 25) then
                      context.scheduler:cancel_task(task_id)
                      finish_channel(context, target)
                      return
                    end

                    if timer % 10 == 0 and timer > 0 then
                      local heal_amount = context.boss.level / 2.0
                      target:restore_health(heal_amount)
                      context.world.spawn_particle_at_location(
                        offset_location(target_location, 0, 1, 0),
                        {
                          particle = "TOTEM_OF_UNDYING",
                          amount = 20,
                          x = 0.1,
                          y = 0.1,
                          z = 0.1
                        }
                      )
                    end

                    local healer_origin = offset_location(boss_location, 0, 1, 0)
                    local target_origin = offset_location(target_location, 0, 1, 0)
                    local to_target = context.vectors.get_vector_between_locations(healer_origin, target_origin)
                    to_target = context.vectors.normalize_vector(to_target)
                    to_target.x = to_target.x * 0.5
                    to_target.y = to_target.y * 0.5
                    to_target.z = to_target.z * 0.5

                    local ray_location = offset_location(healer_origin, to_target.x, to_target.y, to_target.z)
                    for _ = 1, 55 do
                      context.world.spawn_particle_at_location(ray_location, {
                        particle = "TOTEM_OF_UNDYING",
                        amount = 1,
                        x = to_target.x,
                        y = to_target.y,
                        z = to_target.z,
                        speed = 0.2
                      })
                      ray_location = offset_location(ray_location, to_target.x, to_target.y, to_target.z)
                      if distance_squared(ray_location, target_location) < 4 then
                        break
                      end
                    end

                    timer = timer + 1
                  end)

                  context.state.channel_healing_task = task_id
                end

                local function start_scan_task(context)
                  if context.state.channel_healing_scan_task ~= nil then
                    return
                  end

                  context.state.channel_healing_scan_task = context.scheduler:run_every(40, function(context)
                    if not context.boss:is_alive() then
                      context.scheduler:cancel_task(context.state.channel_healing_scan_task)
                      context.state.channel_healing_scan_task = nil
                      return
                    end

                    if context.state.channel_healing_active or not context.cooldowns.local_ready("channel_healing") then
                      return
                    end

                    local target = find_healing_target(context)
                    if target ~= nil then
                      run_channel(context, target)
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    start_scan_task(context)
                  end,
                  on_exit_combat = function(context)
                    if context.state.channel_healing_scan_task ~= nil then
                      context.scheduler:cancel_task(context.state.channel_healing_scan_task)
                      context.state.channel_healing_scan_task = nil
                    end
                  end
                }
                """);
    }
}
