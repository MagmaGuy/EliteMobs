local offsets={{0,0,0},{0,1,0},{0,-1,0},{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}}
local function zipline(context,destination)
  if not context.player:has_clear_movement_path(destination) then context.action:stop(); return end
  if not context.action:temporary_potion(context.player.uuid,'LEVITATION',200,1) then context.action:stop(); return end
  local ticks,task=0,nil
  local function finish(ctx)
    ctx.scheduler:cancel(task)
    ctx.action:restore_potion(ctx.player.uuid,'LEVITATION')
    ctx.action:temporary_potion(ctx.player.uuid,'SLOW_FALLING',60,1)
    ctx.scheduler:run_later(61,function(done) done.action:stop() end)
  end
  task=context.scheduler:run_repeating(1,1,function(ctx)
    local at=ctx.player.current_location
    local x,y,z=destination.x-at.x,destination.y-at.y,destination.z-at.z
    local distance=math.sqrt(x*x+y*y+z*z)
    ticks=ticks+1
    if distance<1 or ticks>200 then finish(ctx); return end
    x,y,z=x/distance*.5,y/distance*.5,z/distance*.5
    if not ctx.player:has_clear_movement_path({x=at.x+x,y=at.y+y,z=at.z+z}) then ctx.action:stop(); return end
    ctx.player:set_velocity(x,y,z)
  end)
end
return {api_version=1,on_projectile_launch=function(context)
  local arrow=context.world:get_entity(context.source.projectile)
  if not arrow or (arrow.entity_type~='arrow' and arrow.entity_type~='spectral_arrow') then return end
  if not context.action:own_entity(arrow.uuid) then return end
  context.action:replace_previous()
  local ticks,task=0,nil
  task=context.scheduler:run_repeating(1,1,function(ctx)
    ticks=ticks+1
    if not arrow.is_valid or ticks>200 then ctx.action:stop(); return end
    if not arrow.in_block then return end
    ctx.scheduler:cancel(task)
    local at,destination=arrow.current_location,arrow.attachment_location
    if not destination then ctx.action:stop(); return end
    local found=false
    for _,offset in ipairs(offsets) do
      if ctx.world:get_block_at(math.floor(at.x)+offset[1],math.floor(at.y)+offset[2],math.floor(at.z)+offset[3])=='target' then found=true; break end
    end
    if not found then ctx.action:stop(); return end
    arrow:set_pickup('DISALLOWED')
    zipline(ctx,destination)
  end)
end}
