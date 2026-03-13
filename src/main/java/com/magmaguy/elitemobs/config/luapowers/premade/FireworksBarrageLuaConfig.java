package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class FireworksBarrageLuaConfig extends InlineLuaPowerConfig {
    public FireworksBarrageLuaConfig() {
        super("fireworks_barrage", null, PowersConfigFields.PowerType.OFFENSIVE, """
                local function clone_location(location)
                  return em.create_location(location.x, location.y, location.z, location.world, location.yaw, location.pitch)
                end

                local function offset_location(location, x, y, z)
                  return em.create_location(location.x + x, location.y + y, location.z + z, location.world, location.yaw, location.pitch)
                end

                local function distance_squared(first, second)
                  local dx = first.x - second.x
                  local dy = first.y - second.y
                  local dz = first.z - second.z
                  return (dx * dx) + (dy * dy) + (dz * dz)
                end

                local function firework_spec(velocity, shot_at_angle)
                  return {
                    power = 10,
                    effects = {
                      {
                        flicker = true,
                        colors = { "RED", "WHITE", "BLUE" }
                      }
                    },
                    velocity = velocity,
                    shot_at_angle = shot_at_angle
                  }
                end

                local function cube_block_locations(center)
                  local blocks = {}
                  for x = -1, 1 do
                    for y = -1, 1 do
                      for z = -1, 1 do
                        blocks[#blocks + 1] = offset_location(center, x, y, z)
                      end
                    end
                  end
                  return blocks
                end

                local function track_targeted_firework(context, firework, target_location)
                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    counter = counter + 1
                    if firework == nil or not firework:is_valid() or counter > (20 * 5) then
                      context.scheduler:cancel_task(task_id)
                      return
                    end
                    if distance_squared(firework:get_location(), target_location) < (0.01 * 0.01) then
                      firework:detonate()
                      context.world:generate_fake_explosion(cube_block_locations(firework:get_location()), firework:get_location())
                      context.scheduler:cancel_task(task_id)
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.cooldowns:local_ready("fireworks_barrage")
                      or not context.cooldowns:global_ready()
                      or math.random() > 0.25 then
                      return
                    end

                    context.cooldowns:set_local(20 * 30, "fireworks_barrage")
                    context.cooldowns:set_global(20 * 20)

                    context.boss:set_ai_enabled(false)
                    local initial_location = clone_location(context.boss:get_location())
                    local lifted_location = offset_location(initial_location, 0, 10, 0)
                    if context.boss.entity_type ~= "GHAST"
                      and context.world:get_block_type_at_location(lifted_location) == "AIR" then
                      context.boss:teleport_to_location(lifted_location)
                    end

                    local counter = 0
                    local task_id
                    task_id = context.scheduler:run_every(10, function(context)
                      if not context.boss:is_alive() then
                        context.scheduler:cancel_task(task_id)
                        return
                      end

                      local boss_location = context.boss:get_location()
                      for _ = 1, 2 do
                        context.world:spawn_fireworks_at_location(
                          boss_location,
                          firework_spec(
                            em.create_vector(math.random() - 0.5, math.random(), math.random() - 0.5),
                            false
                          )
                        )
                      end

                      local nearby_entities = context.entities.get_nearby_entities(20, "players")
                      for index = 1, #nearby_entities do
                        local player = nearby_entities[index]
                        if player.game_mode == "ADVENTURE" or player.game_mode == "SURVIVAL" then
                          local aim_vector = context.vectors.normalize_vector(
                            context.vectors.get_vector_between_locations(
                              boss_location,
                              player:get_eye_location()
                            )
                          )
                          aim_vector = em.create_vector(aim_vector.x * 0.5, aim_vector.y * 0.5, aim_vector.z * 0.5)
                          local firework = context.world:spawn_fireworks_at_location(
                            boss_location,
                            firework_spec(aim_vector, true)
                          )
                          if firework ~= nil then
                            track_targeted_firework(context, firework, clone_location(player:get_location()))
                          end
                        end
                      end

                      counter = counter + 1
                      if counter > 10 then
                        context.scheduler:cancel_task(task_id)
                        context.boss:set_ai_enabled(true)
                        context.boss:teleport_to_location(initial_location)
                      end
                    end)
                  end
                }
                """);
    }
}
