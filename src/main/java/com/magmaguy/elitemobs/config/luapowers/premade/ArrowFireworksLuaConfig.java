package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ArrowFireworksLuaConfig extends InlineLuaPowerConfig {
    public ArrowFireworksLuaConfig() {
        super("arrow_fireworks", Material.FIREWORK_ROCKET.toString(), PowersConfigFields.PowerType.OFFENSIVE, """
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

                local function random_integer(minimum, maximum)
                  return math.random(minimum, maximum)
                end

                local function random_direction()
                  return em.create_vector(
                    (math.random() - 0.5) * 2,
                    (math.random() - 0.5) * 2,
                    (math.random() - 0.5) * 2
                  )
                end

                local function spawn_burst_arrow(context, origin)
                  local randomized_direction = random_direction()
                  local arrow = context.boss:summon_projectile(
                    "ARROW",
                    clone_location(origin),
                    offset_location(origin, randomized_direction.x, randomized_direction.y, randomized_direction.z),
                    math.sqrt(
                      (randomized_direction.x * randomized_direction.x) +
                      (randomized_direction.y * randomized_direction.y) +
                      (randomized_direction.z * randomized_direction.z)
                    ),
                    {
                      spawn_at_origin = true,
                      glowing = true
                    }
                  )
                  return arrow
                end

                local function launch_firework_arrow(context, origin)
                  local rocket_arrow = context.boss:summon_projectile(
                    "SPECTRAL_ARROW",
                    clone_location(origin),
                    offset_location(origin, 0, 1, 0),
                    0.5,
                    {
                      spawn_at_origin = true,
                      gravity = false,
                      glowing = true,
                      persistent = false
                    }
                  )

                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if rocket_arrow == nil or not rocket_arrow:is_valid() or not context.boss:is_alive() then
                      context.scheduler:cancel_task(task_id)
                      return
                    end

                    if counter < 30 then
                      context.world.spawn_particle_at_location(rocket_arrow:get_location(), {
                        particle = "CRIT",
                        amount = 1
                      })
                    else
                      local rocket_location = rocket_arrow:get_location()
                      for _ = 1, 30 do
                        spawn_burst_arrow(context, rocket_location)
                      end
                      rocket_arrow:remove()
                      context.scheduler:cancel_task(task_id)
                      return
                    end

                    counter = counter + 1
                  end)
                end

                local function do_arrow_fireworks(context)
                  local centered_location = offset_location(context.boss:get_location(), 0, 3, 0)

                  for _ = 1, context.boss:get_damager_count() do
                    local new_location = clone_location(centered_location)
                    local valid_block_found = false

                    for _ = 1, 5 do
                      local randomized_x = random_integer(-4, 4)
                      local randomized_z = random_integer(-4, 4)
                      new_location = offset_location(centered_location, randomized_x, 0, randomized_z)
                      if context.world.is_passthrough_at_location(new_location) then
                        valid_block_found = true
                        break
                      end
                    end

                    if not valid_block_found then
                      return
                    end

                    launch_firework_arrow(context, new_location)
                  end
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.boss.is_monster or math.random() > 0.15 then
                      return
                    end
                    do_arrow_fireworks(context)
                  end
                }
                """);
    }
}
