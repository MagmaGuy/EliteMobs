-- A platform rim just below the landing cell blocks a straight pull from a lower ledge.
-- Rise over it through a point above the landing cell; both legs stay collision-checked.
local function column_open(context,destination,lift)
  local x,z=math.floor(destination.x),math.floor(destination.z)
  for y=math.floor(destination.y),math.floor(destination.y+lift+1.8) do
    local block=context.world:get_block_at(x,y,z)
    if block~='air' and block~='cave_air' then return false end
  end
  return true
end
local function first_leg(context,destination)
  if context.player:has_clear_movement_path(destination) then return destination end
  for _,lift in ipairs({.5,1,1.5,2,2.5,3}) do
    local above={x=destination.x,y=destination.y+lift,z=destination.z}
    if column_open(context,destination,lift) and context.player:has_clear_movement_path(above) then return above end
  end
end
local function zipline(context,destination)
  local target=first_leg(context,destination)
  if not target then context.action:stop(); return end
  if not context.action:temporary_potion(context.player.uuid,'LEVITATION',200,1) then context.action:stop(); return end
  local ticks,task=0,nil
  local function finish(ctx)
    ctx.scheduler:cancel(task)
    -- Arriving over a rim leaves nothing to stop the player; drop onto the landing cell instead of coasting past it.
    ctx.player:set_velocity(0,0,0)
    ctx.action:restore_potion(ctx.player.uuid,'LEVITATION')
    ctx.action:temporary_potion(ctx.player.uuid,'SLOW_FALLING',60,1)
    ctx.scheduler:run_later(61,function(done) done.action:stop() end)
  end
  task=context.scheduler:run_repeating(1,1,function(ctx)
    local at=ctx.player.current_location
    local x,y,z=target.x-at.x,target.y-at.y,target.z-at.z
    local distance=math.sqrt(x*x+y*y+z*z)
    ticks=ticks+1
    if target~=destination and distance<1 then
      target=destination
      x,y,z=target.x-at.x,target.y-at.y,target.z-at.z
      distance=math.sqrt(x*x+y*y+z*z)
    end
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
    local block,destination=arrow.attachment_block,arrow.attachment_location
    if not block or not destination or block.world~=ctx.player.world
      or ctx.world:get_block_at(math.floor(block.x),math.floor(block.y),math.floor(block.z))~='target' then
      ctx.action:stop(); return
    end
    arrow:set_pickup('DISALLOWED')
    zipline(ctx,destination)
  end)
end}
