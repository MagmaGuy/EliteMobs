local function land(context, level)
  local at = context.player.current_location
  local radius = math.log(level + 1) * 3
  for _, target in ipairs(context.world:get_nearby_entities(at.x, at.y, at.z, radius)) do
    if target:can_receive_hostile_effect(context.player.uuid) then
      local pos = target.current_location
      local x, y, z = pos.x-at.x, pos.y-at.y, pos.z-at.z
      local length = math.sqrt(x*x+y*y+z*z)
      local force = radius / 2
      if target.is_elite then
        local resistance = target.elite.level * target.elite.health_multiplier
        if resistance > 0 then force = force / resistance else force = 0 end
      end
      if length > 0 then
        x, z = x/length*force, z/length*force
        y = .3*force
        if target:has_clear_movement_path({x=pos.x+x,y=pos.y+y,z=pos.z+z}) then target:set_velocity(x,y,z) end
      end
    end
  end
end

return {api_version=1, on_sneak=function(context)
  local levels = context.source.providers.elitemobs.equipped_levels
  if context.player.is_flying or levels['elitemobs:plasma_boots'] or not context.cooldowns:local_ready('jump') then return end
  if context.cooldowns:local_ready('tap') then context.cooldowns:set_local(10,'tap'); return end
  context.cooldowns:set_local(0,'tap')
  local level = levels[context.enchantment.id]
  if not level or level < 1 then return end
  context.cooldowns:set_local(context.parameters.cooldown_ticks,'jump')
  context.player:send_message(context.parameters.activation_message)
  local direction = context.player:get_look_direction()
  local height = math.log(level+1)
  context.player:set_velocity(direction.x*(height+1)/20,height,direction.z*(height+1)/20)
  context.scheduler:run_later(context.parameters.cooldown_ticks,function(ctx)
    ctx.player:send_message(ctx.parameters.available_message)
    ctx.action:stop()
  end)
  local ticks, task = 4, nil
  task = context.scheduler:run_repeating(5,1,function(ctx)
    ticks = ticks+1
    if ticks > 200 then ctx.scheduler:cancel(task); return end
    ctx.player:set_fall_distance(0)
    local at = ctx.player.current_location
    local x,y,z = math.floor(at.x),math.floor(at.y),math.floor(at.z)
    local below, here = ctx.world:block_is_passable(x,y-1,z),ctx.world:block_is_passable(x,y,z)
    if below == nil or here == nil then ctx.action:stop(); return end
    if (not below and at.y-y < .1) or not here then
      ctx.scheduler:cancel(task)
      land(ctx,level)
      return
    end
    ctx.world:spawn_dust(at,20,math.random(80,99),math.random(20,39),math.random(10,19))
  end)
end}
