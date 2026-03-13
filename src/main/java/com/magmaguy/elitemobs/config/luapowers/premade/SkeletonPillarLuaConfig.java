package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class SkeletonPillarLuaConfig extends InlineLuaPowerConfig {
    public SkeletonPillarLuaConfig() {
        super("skeleton_pillar", Material.BONE.toString(), PowersConfigFields.PowerType.MAJOR_SKELETON, """
                local excluded_entity_types = {
                  PIG = true,
                  COW = true,
                  CHICKEN = true,
                  WOLF = true,
                  LLAMA = true,
                  OCELOT = true,
                  HORSE = true,
                  SHEEP = true,
                  RABBIT = true,
                  PARROT = true,
                  VILLAGER = true
                }

                local note_pitches = {
                  [1] = 0.793701,
                  [2] = 0.793701,
                  [4] = 1.781797,
                  [7] = 1.189207,
                  [10] = 1.259921,
                  [12] = 1.122462,
                  [14] = 0.943874,
                  [16] = 0.943874,
                  [17] = 0.793701,
                  [18] = 0.943874,
                  [19] = 1.122462,
                  [21] = 0.707107,
                  [22] = 0.707107,
                  [24] = 1.781797,
                  [27] = 1.189207,
                  [30] = 1.259921,
                  [32] = 1.122462,
                  [34] = 0.943874,
                  [36] = 0.943874,
                  [37] = 0.793701,
                  [38] = 0.943874,
                  [39] = 1.122462,
                  [41] = 0.667420,
                  [42] = 0.667420,
                  [44] = 1.781797,
                  [47] = 1.189207,
                  [50] = 1.259921,
                  [52] = 1.122462,
                  [54] = 0.943874,
                  [56] = 0.943874,
                  [57] = 0.793701,
                  [58] = 0.943874,
                  [59] = 1.122462,
                  [61] = 0.629961,
                  [62] = 0.629961,
                  [64] = 1.781797,
                  [67] = 1.189207,
                  [70] = 1.259921,
                  [72] = 1.122462,
                  [74] = 0.943874,
                  [76] = 0.943874,
                  [77] = 0.793701,
                  [78] = 0.943874,
                  [79] = 1.122462
                }

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

                local function location_mover(timer, offset)
                  local theta = ((2 * math.pi) / (20 * 3)) * (timer + 1)
                  return em.create_vector(
                    -offset * math.sin(theta),
                    0,
                    offset * math.cos(theta)
                  )
                end

                local function get_pillar_location(anchor, timer, offset)
                  local movement = location_mover(timer, offset)
                  return offset_location(anchor, movement.x, movement.y, movement.z)
                end

                local function pillar_warning_effect(context, location)
                  context.world:spawn_particle_at_location(location, {
                    particle = "LARGE_SMOKE",
                    amount = 5,
                    x = 0.1,
                    y = 5,
                    z = 0.1,
                    speed = 0.05
                  })
                end

                local function pillar_damage(context, location)
                  local entities = context.entities.get_all_entities("living")
                  for index = 1, #entities do
                    local entity = entities[index]
                    if not excluded_entity_types[entity.entity_type]
                      and entity:overlaps_box_at_location(location, 2, 5, 2) then
                      entity:deal_damage(1)
                    end
                  end
                end

                local function pillar_effect(context, anchor, timer, offset)
                  local location = get_pillar_location(anchor, timer, offset)
                  context.world:spawn_particle_at_location(location, {
                    particle = "EXPLOSION",
                    amount = 15,
                    x = 0.1,
                    y = 5,
                    z = 0.1,
                    speed = 0.05
                  })
                  pillar_damage(context, location)
                end

                local function play_pillar_song(context, sound_location)
                  context.scheduler:run_after(1, function(context)
                    local counter = 0
                    local task_id
                    task_id = context.scheduler:run_every(2, function(context)
                      counter = counter + 1
                      local pitch = note_pitches[counter]
                      if pitch ~= nil then
                        context.world:play_sound_at_location(
                          sound_location,
                          "BLOCK_NOTE_BLOCK_IRON_XYLOPHONE",
                          2.0,
                          pitch
                        )
                      end
                      if counter >= 80 then
                        context.scheduler:cancel_task(task_id)
                      end
                    end)
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.cooldowns.global_ready()
                      or math.random() > 0.20 then
                      return
                    end

                    context.cooldowns.set_global(20 * 27)
                    context.boss:set_ai_enabled(false)

                    local sound_location = clone_location(context.boss:get_location())
                    play_pillar_song(context, sound_location)

                    local initial_location = clone_location(context.boss:get_location())
                    local warning_location_one = get_pillar_location(initial_location, 20, 7)
                    local warning_location_two = get_pillar_location(initial_location, 20, -7)

                    local timer = 1
                    local task_id
                    task_id = context.scheduler:run_every(1, function(context)
                      if timer > (20 * 7) or not context.boss:is_alive() then
                        if context.boss:is_alive() then
                          context.boss:set_ai_enabled(true)
                        end
                        context.scheduler:cancel_task(task_id)
                        return
                      end

                      if timer > 20 and timer < (20 * 7) then
                        local current_location = clone_location(context.boss:get_location())
                        pillar_effect(context, current_location, timer, 7)
                        pillar_effect(context, current_location, timer, -7)
                      else
                        pillar_warning_effect(context, warning_location_one)
                        pillar_warning_effect(context, warning_location_two)
                      end

                      timer = timer + 1
                    end)
                  end
                }
                """);
    }
}
