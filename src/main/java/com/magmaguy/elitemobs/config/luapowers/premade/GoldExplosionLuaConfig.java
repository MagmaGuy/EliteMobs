package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class GoldExplosionLuaConfig extends InlineLuaPowerConfig {
    public GoldExplosionLuaConfig() {
        super("gold_explosion", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function generate_visual_projectiles(context)
                  local projectiles = {}
                  local boss_location = context.boss:get_location()
                  for _ = 1, 200 do
                    local velocity = em.create_vector(
                      math.random() - 0.5,
                      math.random() / 1.5,
                      math.random() - 0.5
                    )
                    projectiles[#projectiles + 1] = context.world:spawn_fake_gold_nugget_at_location(
                      offset_location(boss_location, velocity.x, 0.5, velocity.z),
                      velocity,
                      true
                    )
                  end
                  return projectiles
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.boss:is_ai_enabled()
                      or not context.cooldowns:global_ready()
                      or math.random() > 0.25 then
                      return
                    end

                    context.cooldowns:set_global(20 * 20)
                    context.boss:set_ai_enabled(false)

                    local counter = 0
                    local task_id
                    task_id = context.scheduler:run_every(1, function(context)
                      if not context.boss:is_alive() then
                        context.scheduler:cancel_task(task_id)
                        return
                      end

                      counter = counter + 1
                      if context.settings:warning_visual_effects_enabled() then
                        context.world:spawn_particle_at_location(context.boss:get_location(), {
                          particle = "SMOKE",
                          amount = counter,
                          x = 1,
                          y = 1,
                          z = 1,
                          speed = 0
                        })
                      end

                      if counter < (20 * 1.5) then
                        return
                      end

                      context.scheduler:cancel_task(task_id)
                      context.boss:set_ai_enabled(true)
                      context.world:run_fake_gold_nugget_damage(generate_visual_projectiles(context))
                    end)
                  end
                }
                """);
    }
}
