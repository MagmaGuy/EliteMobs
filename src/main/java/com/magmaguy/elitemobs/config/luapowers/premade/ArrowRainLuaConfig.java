package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Particle;

public class ArrowRainLuaConfig extends LuaPowersConfigFields {
    public ArrowRainLuaConfig() {
        super("arrow_rain", Particle.DRIPPING_WATER.toString(), PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                local function random_between(minimum, maximum)
                  return minimum + math.random() * (maximum - minimum)
                end

                local function random_int(minimum, maximum)
                  return math.random(minimum, maximum)
                end

                local function clone_location(location)
                  return em.create_location(location.x, location.y, location.z, location.world, location.yaw, location.pitch)
                end

                local function offset_location(location, x, y, z)
                  local copy = clone_location(location)
                  copy:add(x, y, z)
                  return copy
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.boss:is_ai_enabled() then
                      return
                    end
                    if not context.cooldowns.global_ready() or math.random() > 0.15 then
                      return
                    end

                    context.cooldowns.set_global(300)
                    local initial_location = clone_location(context.boss:get_location())
                    local task_id
                    local counter = 0
                    task_id = context.scheduler:run_every(1, function()
                      counter = counter + 1
                      if counter > 200 or not context.boss:is_alive() then
                        context.scheduler:cancel_task(task_id)
                        if context.boss:is_alive() then
                          context.boss:teleport_to_location(initial_location)
                        end
                        return
                      end

                      local cloud_origin = offset_location(context.boss:get_location(), 0, 10, 0)
                      local cloud_location = offset_location(
                        cloud_origin,
                        random_int(-15, 14),
                        random_int(0, 1),
                        random_int(-15, 14)
                      )
                      context.world:spawn_particle_at_location(cloud_location, "EXPLOSION", 1)

                      if counter > 20 then
                        local arrow_origin = offset_location(
                          cloud_origin,
                          random_int(-15, 14),
                          random_int(0, 1),
                          random_int(-15, 14)
                        )
                        local arrow_target = offset_location(
                          arrow_origin,
                          random_between(-0.5, 0.5),
                          -0.5,
                          random_between(-0.5, 0.5)
                        )
                        context.boss:summon_projectile("ARROW", arrow_origin, arrow_target, 1.0)
                      end
                    end)
                  end
                }
                """;
    }
}
