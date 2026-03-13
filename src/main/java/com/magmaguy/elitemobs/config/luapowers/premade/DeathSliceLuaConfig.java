package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class DeathSliceLuaConfig extends InlineLuaPowerConfig {
    public DeathSliceLuaConfig() {
        super("death_slice", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function raytraced_location_list(origin)
                  local locations = {}
                  local safe_side = math.random(0, 15)
                  local step = (2 * math.pi) / 16
                  for i = 0, 14 do
                    for j = 0, 15 do
                      if j ~= safe_side then
                        local theta = step * (j + 1)
                        local x = i * math.cos(theta)
                        local z = i * math.sin(theta)
                        locations[#locations + 1] = offset_location(origin, x, 0, z)
                        locations[#locations + 1] = offset_location(origin, x, 1, z)
                      end
                    end
                  end
                  return locations
                end

                local function distance_squared(first, second)
                  local dx = first.x - second.x
                  local dy = first.y - second.y
                  local dz = first.z - second.z
                  return (dx * dx) + (dy * dy) + (dz * dz)
                end

                local function do_warning_particle(context, location)
                  if math.random() < 0.3 then
                    context.world.spawn_particle_at_location(location, {
                      particle = "LARGE_SMOKE",
                      amount = 1,
                      x = 0.05,
                      y = 0.05,
                      z = 0.05,
                      speed = 0.05
                    })
                  end
                end

                local function do_damage_phase(context, location)
                  if math.random() < 0.3 then
                    context.world.spawn_particle_at_location(location, {
                      particle = "FLAME",
                      amount = 1,
                      x = 0.1,
                      y = 0.1,
                      z = 0.1,
                      speed = 0.05
                    })
                  end

                  local entities = context.entities.get_nearby_entities(20, "living")
                  for index = 1, #entities do
                    local entity = entities[index]
                    if not entity.is_elite then
                      local entity_location = entity:get_location()
                      if entity_location.world == location.world
                        and math.abs(entity_location.x - location.x) <= 0.5
                        and math.abs(entity_location.y - location.y) <= 0.5
                        and math.abs(entity_location.z - location.z) <= 0.5 then
                        entity:deal_custom_damage(1)
                      end
                    end
                  end
                end

                local function finish_death_slice(context)
                  if context.boss:is_alive() then
                    context.boss:set_ai_enabled(true)
                  end
                  context.state.death_slice_active = false
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.state.death_slice_active or math.random() > 0.10 then
                      return
                    end
                    if not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(400)

                    context.state.death_slice_active = true
                    local locations = raytraced_location_list(clone_location(context.boss:get_location()))
                    context.boss:set_ai_enabled(false)

                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(2, function(context)
                      if counter > 100 or not context.boss:is_alive() then
                        context.scheduler.cancel_task(task_id)
                        finish_death_slice(context)
                        return
                      end

                      if counter < 50 then
                        for index = 1, #locations do
                          do_warning_particle(context, locations[index])
                        end
                      else
                        for index = 1, #locations do
                          do_damage_phase(context, locations[index])
                        end
                      end

                      counter = counter + 1
                    end)
                  end
                }
                """);
    }
}
