package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class ZombieBloatLuaConfig extends InlineLuaPowerConfig {
    public ZombieBloatLuaConfig() {
        super("zombie_bloat", Particle.TOTEM_OF_UNDYING.toString(), PowersConfigFields.PowerType.MAJOR_ZOMBIE, """
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

                local function is_same_entity(first, second)
                  return first ~= nil and second ~= nil and first.uuid == second.uuid
                end

                local function living_entity_effect(context, living_entities)
                  if #living_entities == 0 or not context.settings.warning_visual_effects_enabled() then
                    return
                  end

                  local counter = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if counter > 30 then
                      context.scheduler:cancel_task(task_id)
                    end
                    for index = 1, #living_entities do
                      local living_entity = living_entities[index]
                      if living_entity ~= nil and living_entity:is_alive() then
                        local location = living_entity:get_location()
                        context.world.spawn_particle_at_location(
                          offset_location(location, 0, living_entity:get_height() - 1, 0),
                          {
                            particle = "CLOUD",
                            amount = 0,
                            x = 0,
                            y = 0,
                            z = 0,
                            speed = 0
                          }
                        )
                      end
                    end
                    counter = counter + 1
                  end)
                end

                local function bloat_effect(context, event_zombie)
                  local giant = context.world.spawn_entity_at_location("GIANT", event_zombie:get_location())
                  if giant ~= nil then
                    giant:set_ai_enabled(false)
                  end

                  local nearby_entities = context.entities.get_nearby_entities(15, "living")
                  local nearby_valid_living_entities = {}
                  for index = 1, #nearby_entities do
                    local entity = nearby_entities[index]
                    local entity_location = entity:get_location()
                    local zombie_location = event_zombie:get_location()
                    if not is_same_entity(entity, event_zombie)
                      and math.abs(entity_location.x - zombie_location.x) <= 4
                      and math.abs(entity_location.y - zombie_location.y) <= 15
                      and math.abs(entity_location.z - zombie_location.z) <= 4 then
                      nearby_valid_living_entities[#nearby_valid_living_entities + 1] = entity
                    end
                  end

                  local entity_location = event_zombie:get_location()
                  for index = 1, #nearby_valid_living_entities do
                    local living_entity = nearby_valid_living_entities[index]
                    local to_living_entity_vector = context.vectors.get_vector_between_locations(entity_location, living_entity:get_location())
                    local normalized_vector = context.vectors.normalize_vector(to_living_entity_vector)
                    normalized_vector.x = normalized_vector.x * 2
                    normalized_vector.y = 0
                    normalized_vector.z = normalized_vector.z * 2
                    normalized_vector = em.create_vector(normalized_vector.x, normalized_vector.y + 1, normalized_vector.z)
                    living_entity:set_velocity_vector(normalized_vector)
                  end

                  living_entity_effect(context, nearby_valid_living_entities)

                  context.scheduler:run_after(10, function(context)
                    if giant ~= nil and giant:is_valid() then
                      giant:remove()
                    end
                    if event_zombie:is_alive() then
                      event_zombie:set_ai_enabled(true)
                    end
                  end)
                end

                local function start_warning(context, event_zombie)
                  local timer = 0
                  local task_id
                  task_id = context.scheduler:run_every(1, function(context)
                    if timer > 40 then
                      bloat_effect(context, event_zombie)
                      context.scheduler:cancel_task(task_id)
                      return
                    end

                    if timer == 21 then
                      event_zombie:set_ai_enabled(false)
                    end

                    if context.settings.warning_visual_effects_enabled() then
                      local location = event_zombie:get_location()
                      context.world.spawn_particle_at_location(
                        offset_location(location, 0, event_zombie:get_height(), 0),
                        {
                          particle = "TOTEM_OF_UNDYING",
                          amount = 20,
                          x = timer / 24.0,
                          y = timer / 9.0,
                          z = timer / 24.0,
                          speed = 0.1
                        }
                      )
                    end

                    timer = timer + 1
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if math.random() > 0.20
                      or not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(200)
                    start_warning(context, context.boss)
                  end
                }
                """);
    }
}
