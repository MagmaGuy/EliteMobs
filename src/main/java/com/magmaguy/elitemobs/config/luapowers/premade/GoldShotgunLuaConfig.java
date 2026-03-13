package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public class GoldShotgunLuaConfig extends InlineLuaPowerConfig {
    public GoldShotgunLuaConfig() {
        super("gold_shotgun", null, PowersConfigFields.PowerType.UNIQUE, """
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

                local function vector_length_squared(vector)
                  return (vector.x * vector.x) + (vector.y * vector.y) + (vector.z * vector.z)
                end

                local function normalize(vector)
                  local length = math.sqrt(vector_length_squared(vector))
                  if length <= 0 then
                    return em.create_vector(0, 0, 0)
                  end
                  return em.create_vector(vector.x / length, vector.y / length, vector.z / length)
                end

                local function multiply(vector, amount)
                  return em.create_vector(vector.x * amount, vector.y * amount, vector.z * amount)
                end

                local function add(vector, other)
                  return em.create_vector(vector.x + other.x, vector.y + other.y, vector.z + other.z)
                end

                local function random_unitish_vector()
                  local vector = em.create_vector(
                    math.random() * 0.2 - 0.1,
                    math.random() * 0.2 - 0.1,
                    math.random() * 0.2 - 0.1
                  )
                  return normalize(vector)
                end

                local function get_shot_vector(original_shot_vector)
                  return add(original_shot_vector, multiply(random_unitish_vector(), 0.1))
                end

                local function do_smoke_effect(context, shot_vector)
                  local origin = offset_location(context.boss:get_location(), 0, 0.5, 0)
                  for _ = 1, 200 do
                    local visual_shot_vector = get_shot_vector(shot_vector)
                    context.world:spawn_particle_at_location(origin, {
                      particle = "SMOKE",
                      amount = 0,
                      x = visual_shot_vector.x,
                      y = visual_shot_vector.y,
                      z = visual_shot_vector.z,
                      speed = 0.75
                    })
                  end
                end

                local function generate_visual_projectiles(context, shot_vector)
                  local projectiles = {}
                  local boss_location = context.boss:get_location()
                  for _ = 1, 200 do
                    projectiles[#projectiles + 1] = context.world:spawn_fake_gold_nugget_at_location(
                      boss_location,
                      get_shot_vector(shot_vector),
                      false
                    )
                  end
                  return projectiles
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.player == nil
                      or not context.boss:is_ai_enabled()
                      or not context.cooldowns:local_ready("gold_shotgun")
                      or not context.cooldowns:global_ready() then
                      return
                    end

                    context.cooldowns:set_local(20 * 20, "gold_shotgun")
                    context.cooldowns:set_global(20 * 7)
                    context.boss:set_ai_enabled(false)

                    local target_location = offset_location(context.player:get_location(), 0, 1, 0)
                    local shot_vector = context.vectors.get_vector_between_locations(context.boss:get_location(), target_location)
                    shot_vector = multiply(normalize(shot_vector), 0.5)

                    local counter = 0
                    local task_id
                    task_id = context.scheduler:run_every(1, function(context)
                      if not context.boss:is_alive() then
                        context.scheduler:cancel_task(task_id)
                        return
                      end

                      if counter % 10 == 0 then
                        do_smoke_effect(context, shot_vector)
                      end

                      counter = counter + 1
                      if counter < (20 * 3) then
                        return
                      end

                      context.scheduler:cancel_task(task_id)
                      context.boss:set_ai_enabled(true)
                      context.world:run_fake_gold_nugget_damage(generate_visual_projectiles(context, shot_vector))
                    end)
                  end
                }
                """);
    }
}
