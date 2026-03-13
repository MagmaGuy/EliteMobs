package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class EnderDragonEmpoweredLightningLuaConfig extends InlineLuaPowerConfig {
    public EnderDragonEmpoweredLightningLuaConfig() {
        super("ender_dragon_empowered_lightning", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function cancel_state_task(context, key)
                  local task_id = context.state[key]
                  if task_id ~= nil then
                    context.scheduler:cancel_task(task_id)
                    context.state[key] = nil
                  end
                end

                local function is_in_box(candidate, center, x_range, y_range, z_range)
                  local candidate_location = candidate:get_location()
                  return candidate_location.world == center.world
                    and math.abs(candidate_location.x - center.x) <= x_range
                    and math.abs(candidate_location.y - center.y) <= y_range
                    and math.abs(candidate_location.z - center.z) <= z_range
                end

                local function deactivate(context)
                  context.state.is_active = false
                  cancel_state_task(context, "ender_dragon_empowered_lightning_task")
                end

                local function location_randomizer(context, location, counter)
                  if counter > 5 then
                    return nil
                  end

                  local random_location = offset_location(
                    location,
                    math.random(-150, 149),
                    0,
                    math.random(-150, 149)
                  )

                  random_location.y = context.world:get_highest_block_y_at_location(random_location)
                  if random_location.y == -1 then
                    location_randomizer(context, location, counter + 1)
                  end

                  return random_location
                end

                local function fire_lightning(context)
                  context.cooldowns:set_local(20 * 120)
                  context.cooldowns:set_global(20 * 30)

                  local boss_location = context.boss:get_location()
                  local players = context.players:all_players_in_world()
                  for index = 1, #players do
                    if is_in_box(players[index], boss_location, 150, 150, 150) then
                      context.world:run_empowered_lightning_task_at_location(players[index]:get_location())
                    end
                  end

                  for _ = 1, 50 do
                    local random_location = location_randomizer(context, boss_location, 0)
                    if random_location ~= nil then
                      context.scheduler:run_after(math.random(0, (20 * 5) - 1), function(context)
                        context.world:run_empowered_lightning_task_at_location(random_location)
                      end)
                    end
                  end
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    if context.state.is_active then
                      return
                    end

                    context.state.is_active = true
                    cancel_state_task(context, "ender_dragon_empowered_lightning_task")
                    context.state.ender_dragon_empowered_lightning_task = context.scheduler:run_every(20, function(context)
                      if not context.boss.exists then
                        deactivate(context)
                        return
                      end

                      if not context.cooldowns:local_ready() or not context.cooldowns:global_ready() then
                        return
                      end

                      fire_lightning(context)
                    end)
                  end,
                  on_exit_combat = function(context)
                    deactivate(context)
                  end
                }
                """);
    }
}
