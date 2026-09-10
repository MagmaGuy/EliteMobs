local function direction(from, to)
  local x, y, z = to.x - from.x, to.y - from.y, to.z - from.z
  local length = math.sqrt(x*x + y*y + z*z)
  if length < 0.0001 then return nil end
  return {x=x/length, y=y/length, z=z/length}
end

local function activate(context)
  if not context.cooldowns:local_ready("fire") or context.action:has_previous() then return end
  local stats = context.source.providers.elitemobs
  local durability = context.item:get_durability()
  if not stats or stats.weapon_level <= 0 or not durability or durability.current < 4 then return end
  local eye, look = context.player:get_eye_location(), context.player:get_look_direction()
  local ray = context.world:raycast(eye.x, eye.y, eye.z, look.x, look.y, look.z, 30, "NEVER", true)
  local target = ray.hit_block or {x=eye.x+look.x*30, y=eye.y+look.y*30, z=eye.z+look.z*30}
  target = {x=math.floor(target.x)+0.5, y=math.floor(target.y)+1, z=math.floor(target.z)+0.5}
  if not direction(context.player.current_location, target) then return end
  if not context.item:use_durability(4, true) then return end
  context.cooldowns:set_local(context.parameters.cooldown_ticks, "fire")
  context.event:cancel()
  context.action:temporary_potion(context.player.uuid, "SLOWNESS", 100, 20)
  local elapsed, points, task = 0, nil, nil
  task = context.scheduler:run_repeating(0, 1, function()
    local player = context.player
    local origin = player.current_location
    local aim = direction(origin, target)
    if not aim then context.scheduler:cancel(task); return end
    local eyes = player:get_eye_location()
    local particle = elapsed >= 20 and elapsed < 80 and "FLAME" or "SMOKE"
    for i=1,5 do
      context.world:spawn_particle(particle, eyes.x+aim.x, eyes.y-0.5, eyes.z+aim.z,
        0, aim.x+(math.random()-0.5)*0.1, aim.y+(math.random()-0.5)*0.1,
        aim.z+(math.random()-0.5)*0.1, math.random()+0.05)
    end
    if elapsed == 20 then
      -- Preserve the fixed 40 half-block sample points selected after windup.
      points = {}
      for i=1,40 do points[i]={x=origin.x+aim.x*i*0.5, y=origin.y+aim.y*i*0.5, z=origin.z+aim.z*i*0.5} end
    end
    if elapsed >= 20 and elapsed < 80 and elapsed % 20 == 0 then
      local hit = {}
      for _, p in ipairs(points) do
        for _, entity in ipairs(context.world:get_nearby_entities(p.x,p.y,p.z,0.5)) do
          if not hit[entity.uuid] then
            hit[entity.uuid] = true
            if entity.is_alive and entity:can_receive_hostile_effect(player.uuid)
                and not entity:has_potion_effect("FIRE_RESISTANCE") then
              context.action:damage_target(entity.uuid, stats.weapon_level)
            end
          end
        end
      end
    end
    elapsed = elapsed + 1
    if elapsed >= 100 then context.scheduler:cancel(task) end
  end)
end

return {api_version=1, on_right_click=activate, on_shift_right_click=activate}
