package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ImplosionLuaConfig extends InlineLuaPowerConfig {
    public ImplosionLuaConfig() {
        super("implosion", Material.SLIME_BALL.toString(), PowersConfigFields.PowerType.MISCELLANEOUS, """
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

                return {
                  api_version = 1,
                  on_death = function(context)
                    local death_location = clone_location(context.boss:get_location())
                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      if counter < 20 then
                        for _ = 1, 20 do
                          context.world.spawn_particle_at_location(death_location, {
                            particle = "PORTAL",
                            amount = 1,
                            x = 0.1,
                            y = 0.1,
                            z = 0.1,
                            speed = 1
                          })
                        end
                      end

                      if counter > 60 then
                        local entities = context.entities.get_all_entities("living")
                        for index = 1, #entities do
                          local entity = entities[index]
                          local location = entity:get_location()
                          if location.world == death_location.world then
                            local dx = location.x - death_location.x
                            local dy = location.y - death_location.y
                            local dz = location.z - death_location.z
                            if (dx * dx) + (dy * dy) + (dz * dz) <= 100 and entity.game_mode ~= "SPECTATOR" then
                              local pull = context.vectors.get_vector_between_locations(location, death_location)
                              pull = context.vectors.normalize_vector(pull)
                              entity:set_velocity_vector(pull)
                            end
                          end
                        end

                        context.scheduler.cancel_task(task_id)
                        return
                      end

                      counter = counter + 1
                    end)
                  end
                }
                """);
    }
}
