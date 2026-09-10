local function dust(context,at,count)
  context.world:spawn_dust(at,count,math.random(0,99),math.random(122,254),math.random(0,99))
end
local function land(context)
  local at = context.player.current_location
  for level=1,context.enchantment.level do
    local height, angle = (level+.2-1)/2, 0
    for i=0,7 do
      -- Preserve the original cumulative rotation of each eight-projectile array.
      angle = angle+i*math.pi/4
      local x,z = math.cos(angle),-math.sin(angle)
      context.action:launch_projectile({x=at.x+x,y=at.y+height,z=at.z+z},{x=x,y=height,z=z},.2,12,60,
        function(target)
          if target and target:can_receive_hostile_effect(context.player.uuid) then
            context.action:damage_target(target.uuid,2)
          end
        end,
        function(pos) dust(context,pos,5) end,true)
    end
  end
  context.scheduler:run_later(61,function(ctx) ctx.action:stop() end)
end
return {api_version=1,on_sneak=function(context)
  local stats = context.source.providers.elitemobs
  if not stats.usable_slots.FEET or context.player.is_flying or stats.equipped_levels['elitemobs:earthquake']
      or not context.cooldowns:local_ready('jump') then return end
  if context.cooldowns:local_ready('tap') then context.cooldowns:set_local(10,'tap'); return end
  context.cooldowns:set_local(0,'tap')
  context.cooldowns:set_local(context.parameters.cooldown_ticks,'jump')
  context.player:set_velocity(0,.8,0)
  local ticks,task = 4,nil
  task = context.scheduler:run_repeating(5,1,function(ctx)
    ticks=ticks+1
    if ticks > 200 then ctx.action:stop(); return end
    local at=ctx.player.current_location
    local x,y,z=math.floor(at.x),math.floor(at.y),math.floor(at.z)
    local below,here=ctx.world:block_is_passable(x,y-1,z),ctx.world:block_is_passable(x,y,z)
    if below == nil or here == nil then ctx.action:stop(); return end
    if (not below and at.y-y < .1) or not here then ctx.scheduler:cancel(task); land(ctx); return end
    dust(ctx,at,20)
  end)
end}
