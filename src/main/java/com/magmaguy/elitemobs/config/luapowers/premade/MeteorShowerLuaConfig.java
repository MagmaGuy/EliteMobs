package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class MeteorShowerLuaConfig extends InlineLuaPowerConfig {
    public MeteorShowerLuaConfig() {
        super("meteor_shower", null, PowersConfigFields.PowerType.UNIQUE, """
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

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if math.random() > 0.25 or not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(400)
                    local initial_location = clone_location(context.boss:get_location())
                    context.boss:set_ai_enabled(false)

                    local counter = 0
                    local task_id
                    task_id = context.scheduler.run_every(1, function(context)
                      if not context.boss:is_alive() then
                        context.scheduler.cancel_task(task_id)
                        return
                      end

                      if counter > 200 then
                        context.scheduler.cancel_task(task_id)
                        context.boss:set_ai_enabled(true)
                        context.boss:teleport_to_location(initial_location)
                        return
                      end

                      local cloud_center = offset_location(context.boss:get_location(), 0, 10, 0)
                      local cloud_location = offset_location(cloud_center, math.random(-15, 14), math.random(0, 1), math.random(-15, 14))
                      context.world.spawn_particle_at_location(cloud_location, {
                        particle = "EXPLOSION",
                        amount = 1
                      })

                      if counter > 40 then
                        local spawn_location = offset_location(cloud_center, math.random(-15, 14), math.random(0, 1), math.random(-15, 14))
                        local direction = em.create_vector(math.random() - 0.5, -0.5, math.random() - 0.5)
                        local destination = offset_location(spawn_location, direction.x, direction.y, direction.z)
                        context.boss:summon_projectile("FIREBALL", spawn_location, destination, 0.5, {
                          duration = 100,
                          spawn_at_origin = true
                        })
                      end

                      counter = counter + 1
                    end)
                  end
                }
                """);
    }
}
