package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class GroundPoundLuaConfig extends LuaPowersConfigFields {
    public GroundPoundLuaConfig() {
        super("ground_pound", Material.DIRT.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
    }

    @Override
    public String getSource() {
        return """
                local function cloud_particle(context, location, amount)
                  context.world:spawn_particle_at_location(location, {
                    particle = "CLOUD",
                    amount = amount,
                    x = 0.01,
                    y = 0.01,
                    z = 0.01,
                    speed = 0.7
                  }, amount)
                end

                local function land_cloud_particle(context, location)
                  context.world:spawn_particle_at_location(location, {
                    particle = "CLOUD",
                    amount = 20,
                    x = 0.1,
                    y = 0.01,
                    z = 0.1,
                    speed = 0.7
                  }, 20)
                end

                local function physical_body(context)
                  local mount = context.boss:get_mount()
                  if mount ~= nil then
                    return mount
                  end
                  return context.boss
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.cooldowns.global_ready() or math.random() > 0.10 then
                      return
                    end

                    context.cooldowns.set_global(200)
                    context.scheduler:run_after(1, function()
                      if not context.boss:is_alive() then
                        return
                      end
                      local body = physical_body(context)
                      body:set_velocity_vector(em.create_vector(0, 1.5, 0))
                      cloud_particle(context, body:get_location(), 10)
                    end)

                    context.scheduler:run_after(20, function()
                    local fall_task
                    local fall_counter = 0
                    fall_task = context.scheduler:run_every(1, function()
                      fall_counter = fall_counter + 1
                      if fall_counter > 100 or not context.boss:is_alive() then
                        context.scheduler:cancel_task(fall_task)
                        return
                      end

                      local body = physical_body(context)
                      local boss_location = body:get_location()
                      local feet_check = em.create_location(
                        boss_location.x,
                        boss_location.y - 0.2,
                        boss_location.z,
                        boss_location.world,
                        boss_location.yaw,
                        boss_location.pitch
                      )

                      if not context.world:is_passthrough_at_location(feet_check) then
                        body:set_velocity_vector(em.create_vector(0, -2, 0))
                        cloud_particle(context, body:get_location(), 10)
                        context.scheduler:cancel_task(fall_task)

                        local land_task
                        local land_counter = 0
                        land_task = context.scheduler:run_every(1, function()
                          land_counter = land_counter + 1
                          if land_counter > 100 or not context.boss:is_alive() then
                            context.scheduler:cancel_task(land_task)
                            return
                          end

                          local landing_body = physical_body(context)
                          local landing_location = landing_body:get_location()
                          if not context.world:is_on_floor_at_location(landing_location) then
                            return
                          end

                          context.scheduler:cancel_task(land_task)
                          land_cloud_particle(context, landing_location)

                          for _, entity in ipairs(context.entities.get_nearby_entities(10, "all")) do
                            if entity.uuid ~= landing_body.uuid then
                              local push = context.vectors.get_vector_between_locations(landing_location, entity:get_location())
                              push = context.vectors.normalize_vector(push)
                              entity:set_velocity_vector({
                                x = push.x * 2,
                                y = 1.5,
                                z = push.z * 2
                              })
                              if entity.is_alive ~= nil and entity:is_alive() then
                                entity:apply_potion_effect("SLOWNESS", 60, 2)
                              end
                            end
                          end
                        end)
                      end
                    end)
                    end)
                  end
                }
                """;
    }
}
