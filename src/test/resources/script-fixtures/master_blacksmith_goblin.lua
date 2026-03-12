local TRIGGER_COOLDOWN = "master_blacksmith_goblin_trigger"
local RESET_COOLDOWN = "master_blacksmith_goblin_reset"

local HAMMER_SLAM_MESSAGE = "&fThe <gradient:#76EFC4:#00C96E:#0A7A44>Master Blacksmith Goblin</gradient> &fhas had enough of your dodging and is preparing a hammer slam attack!!!"
local GROUND_SHATTER_MESSAGE = "&fWatch out! <gradient:#76EFC4:#00C96E:#0A7A44>Master Blacksmith Goblin</gradient> &fis preparing a molten earth shatter!"

local EXIST_PARTICLES = {
  { particle = "ELECTRIC_SPARK" }
}

local HAMMER_WARNING_PARTICLES = {
  { particle = "SMOKE", amount = 1, speed = 0.0 },
  { particle = "CRIT", amount = 1, speed = 0.05 }
}

local HAMMER_IMPACT_PARTICLES = {
  { particle = "CLOUD", amount = 2, speed = 0.1 },
  { particle = "CRIT", amount = 1, speed = 0.2 }
}

local GROUND_WARNING_PARTICLES = {
  { particle = "LARGE_SMOKE", amount = 1, speed = 0.05 },
  { particle = "DRIPPING_LAVA", amount = 1, speed = 0.02 }
}

local GROUND_IMPACT_PARTICLES = {
  { particle = "LAVA", amount = 2, speed = 0.2 },
  { particle = "LARGE_SMOKE", amount = 2, speed = 0.1 }
}

local GROUND_RAY_VARIANTS = {
  { yaw = 0, y_offset = 0, warning = true },
  { yaw = -25, y_offset = 0, warning = true },
  { yaw = 25, y_offset = 0, warning = true },
  { yaw = 0, y_offset = 1 },
  { yaw = 0, y_offset = -1 },
  { yaw = -25, y_offset = 1 },
  { yaw = -25, y_offset = -1 },
  { yaw = 25, y_offset = 1 },
  { yaw = 25, y_offset = -1 }
}

local function each(list, fn)
  for index, value in ipairs(list) do
    fn(value, index)
  end
end

local function run_repeating(context, delay, interval, times, fn)
  context.scheduler:run_after(delay, function()
    local remaining = times
    local task_id
    task_id = context.scheduler:run_every(interval, function()
      if remaining <= 0 then
        context.scheduler:cancel_task(task_id)
        return
      end
      remaining = remaining - 1
      fn()
      if remaining <= 0 then
        context.scheduler:cancel_task(task_id)
      end
    end)
  end)
end

local function make_target(context, spec)
  return context.script:target(spec)
end

local function make_zone(context, spec)
  return context.script:zone(spec)
end

local function boss_target(context)
  return make_target(context, {
    targetType = "SELF"
  })
end

local function nearby_players(context, range)
  return make_target(context, {
    targetType = "NEARBY_PLAYERS",
    range = range
  })
end

local function send_message(handle, message)
  each(handle:entities(), function(entity)
    if entity.send_message ~= nil then
      entity:send_message(message)
    end
  end)
end

local function spawn_particles(context, locations, particles)
  each(locations, function(location)
    each(particles, function(particle)
      context.world:spawn_particle_at_location(location, particle)
    end)
  end)
end

local function play_sound(context, sound, volume, pitch)
  context.boss:play_sound_at_self(sound, volume, pitch)
end

local function run_sounds(context, sound_events)
  each(sound_events, function(sound_event)
    context.scheduler:run_after(sound_event.wait, function()
      play_sound(context, sound_event.sound, sound_event.volume, sound_event.pitch)
    end)
  end)
end

local function repick_after(context, delay)
  context.scheduler:run_after(delay, function()
    if context.boss:has_tag("loop") then
      pick_action(context)
    end
  end)
end

local function set_fire_ticks(handle, ticks)
  each(handle:entities(), function(entity)
    entity:set_fire_ticks(ticks)
  end)
end

local function schedule_ai_disable(context, wait, duration)
  context.scheduler:run_after(wait, function()
    context.boss:set_ai_enabled(false)
    context.scheduler:run_after(duration, function()
      context.boss:set_ai_enabled(true)
    end)
  end)
end

local function cylinder_zone(context, tracked)
  return make_zone(context, {
    shape = "CYLINDER",
    radius = 6,
    height = 2,
    Target = {
      targetType = "SELF",
      track = tracked or false
    }
  })
end

local function ray_target_spec(y_offset)
  local spec = {
    targetType = "SELF"
  }
  if y_offset ~= 0 then
    spec.offset = "0," .. tostring(y_offset) .. ",0"
  end
  return spec
end

local function make_ground_ray(context, yaw, y_offset)
  return make_zone(context, {
    shape = "ROTATING_RAY",
    animationDuration = 35,
    Target = ray_target_spec(y_offset),
    Target2 = {
      targetType = "NEARBY_PLAYERS",
      range = 20,
      relativeOffset = {
        sourceTarget = {
          targetType = "SELF"
        },
        destinationTarget = {
          targetType = "NEARBY_PLAYERS",
          range = 20
        },
        normalize = true,
        multiplier = 10
      }
    },
    yawPreRotation = yaw,
    pointRadius = 1.0,
    ignoresSolidBlocks = true
  })
end

local function build_ground_rays(context)
  local all_rays = {}
  local warning_rays = {}

  each(GROUND_RAY_VARIANTS, function(variant)
    local ray = make_ground_ray(context, variant.yaw, variant.y_offset)
    table.insert(all_rays, ray)
    if variant.warning then
      table.insert(warning_rays, ray)
    end
  end)

  return warning_rays, all_rays
end

local function trigger_exist_particles(context)
  if context.state.exist_zone == nil then
    return
  end
  spawn_particles(context, context.state.exist_zone:border_locations(0.3), EXIST_PARTICLES)
end

local function hammer_slam(context)
  send_message(nearby_players(context, 30), HAMMER_SLAM_MESSAGE)
  context.boss:play_model_animation("ground_smack")
  context.boss:apply_potion_effect("SLOW", 40, 200)
  schedule_ai_disable(context, 20, 30)
  play_sound(context, "block.anvil.place", 0.1, 0.2)

  local warning_zone = cylinder_zone(context, true)
  run_repeating(context, 0, 5, 8, function()
    spawn_particles(context, warning_zone:border_locations(0.1), HAMMER_WARNING_PARTICLES)
  end)

  context.scheduler:run_after(38, function()
    local impact_zone = cylinder_zone(context, false)
    play_sound(context, "block.anvil.land", 1.5, 0.5)
    play_sound(context, "entity.generic.explode", 0.4, 1.5)
    spawn_particles(context, impact_zone:full_locations(0.4), HAMMER_IMPACT_PARTICLES)

    context.script:push(
      impact_zone:full_target(),
      context.script:relative_vector({
        SourceTarget = {
          targetType = "SELF"
        },
        DestinationTarget = {
          targetType = "NEARBY_PLAYERS"
        },
        normalize = true,
        multiplier = 4.0,
        offset = "0,1.0,0"
      })
    )
  end)

  repick_after(context, 300)
end

local function ground_shatter(context)
  send_message(nearby_players(context, 30), GROUND_SHATTER_MESSAGE)
  context.boss:play_model_animation("ground_shatter")
  context.boss:apply_potion_effect("SLOW", 35, 200)
  schedule_ai_disable(context, 10, 40)
  play_sound(context, "block.stone.step", 1.0, 0.5)

  local warning_rays, all_rays = build_ground_rays(context)

  run_repeating(context, 0, 5, 6, function()
    each(warning_rays, function(ray)
      spawn_particles(context, ray:full_locations(0.3), GROUND_WARNING_PARTICLES)
    end)
  end)

  context.scheduler:run_after(10, function()
    context.script:set_facing(
      boss_target(context),
      context.script:relative_vector({
        SourceTarget = {
          targetType = "SELF"
        },
        DestinationTarget = {
          targetType = "NEARBY_PLAYERS",
          range = 20
        }
      })
    )
  end)

  context.scheduler:run_after(30, function()
    play_sound(context, "block.anvil.land", 1.5, 0.5)
    play_sound(context, "block.lava.pop", 1.0, 0.8)

    each(warning_rays, function(ray)
      spawn_particles(context, ray:full_locations(0.5), GROUND_IMPACT_PARTICLES)
    end)

    each(all_rays, function(ray)
      local target = ray:full_target()
      set_fire_ticks(target, 100)
      context.script:damage(target, 1.0, 1.5)
    end)
  end)

  repick_after(context, 222)
end

local function fake_out(context)
  context.boss:play_model_animation("fake_out")
  context.boss:apply_potion_effect("SLOWNESS", 85, 20)
  schedule_ai_disable(context, 20, 65)

  run_repeating(context, 20, 5, 4, function()
    play_sound(context, "minecraft:entity.zombie.step", 0.1, 0.8)
  end)

  run_sounds(context, {
    { wait = 50, sound = "minecraft:entity.generic.big_fall", volume = 0.1, pitch = 0.8 },
    { wait = 50, sound = "elitemobs:em_goblin_events_free_goblin_damaged_3", volume = 0.3, pitch = 1.1 },
    { wait = 75, sound = "minecraft:entity.zombie.step", volume = 0.2, pitch = 0.5 }
  })

  context.scheduler:run_after(80, function()
    local location = boss_target(context):first_location()
    if location ~= nil then
      context.world:spawn_particle_at_location(location, {
        particle = "CLOUD",
        x = 0.3,
        y = 0.1,
        z = 0.3,
        amount = 4,
        speed = 0.05
      })
    end
  end)

  repick_after(context, 222)
end

function pick_action(context)
  context.boss:add_tag("loop")
  context.scheduler:run_after(1, function()
    if not context.boss:has_tag("loop") then
      return
    end

    local roll = math.random(1, 3)
    if roll == 1 then
      hammer_slam(context)
    elseif roll == 2 then
      ground_shatter(context)
    else
      fake_out(context)
    end
  end)
end

local function trigger_pick_action(context)
  if not context.cooldowns:local_ready(TRIGGER_COOLDOWN) or not context.cooldowns:global_ready() then
    return
  end

  context.boss:remove_tag("Idle")
  context.cooldowns:set_local(6300, TRIGGER_COOLDOWN)
  context.cooldowns:set_global(5)

  context.scheduler:run_after(1, function()
    pick_action(context)
  end)

  run_repeating(context, 6000, 1, 150, function()
    context.boss:remove_tag("loop")
  end)
end

return {
  api_version = 1,
  priority = 0,

  on_spawn = function(context)
    context.boss:add_tag("testgoblin")

    context.state.exist_zone = make_zone(context, {
      shape = "SPHERE",
      radius = 20,
      borderRadius = 19,
      Target = {
        targetType = "SELF",
        track = true
      }
    })

    run_repeating(context, 0, 5, 99999, function()
      trigger_exist_particles(context)
    end)

    context.state.trigger_zone = make_zone(context, {
      shape = "DOME",
      radius = 20,
      Target = {
        targetType = "SELF",
        track = true
      }
    })

    context.state.trigger_listener = context.state.trigger_zone:watch({
      on_enter = function()
        trigger_pick_action(context)
      end,
      on_leave = function()
        trigger_pick_action(context)
      end
    })
  end,

  on_boss_damaged_by_player = function(context)
    trigger_pick_action(context)
  end,

  on_exit_combat = function(context)
    if not context.cooldowns:local_ready(RESET_COOLDOWN) or not context.cooldowns:global_ready() then
      return
    end

    context.cooldowns:set_local(60, RESET_COOLDOWN)
    context.cooldowns:set_global(20)
    context.boss:add_tag("Idle")

    context.scheduler:run_after(1, function()
      context.boss:set_ai_enabled(false)
      context.scheduler:run_after(30, function()
        context.boss:set_ai_enabled(true)
      end)
    end)

    run_repeating(context, 0, 1, 20, function()
      context.boss:remove_tag("loop")
    end)
  end
}
