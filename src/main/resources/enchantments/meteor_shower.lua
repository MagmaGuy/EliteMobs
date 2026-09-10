local function activate(context)
  if not context.item or context.action:has_previous() then return end
  local at=context.player.current_location
  if context.world:location_is_protected(at) or context.world:block_is_passable(math.floor(at.x),math.floor(at.y)+10,math.floor(at.z))==nil then return end
  if not context.item:consume(1) then return end
  context.event:cancel()
  local ticks,task=0,nil
  task=context.scheduler:run_repeating(0,1,function(ctx)
    ticks=ticks+1
    if ticks>200 then ctx.scheduler:cancel(task); return end
    local player=ctx.player.current_location
    local cloud={x=player.x+math.random(-15,14),y=player.y+10+math.random(0,1),z=player.z+math.random(-15,14)}
    if ctx.world:block_is_passable(math.floor(cloud.x),math.floor(cloud.y),math.floor(cloud.z))==nil then return end
    ctx.world:spawn_particle_at_location(cloud,'EXPLOSION',1)
    if ticks<=40 then return end
    local spawn={x=player.x+math.random(-15,14),y=player.y+10+math.random(0,1),z=player.z+math.random(-15,14)}
    if ctx.world:block_is_passable(math.floor(spawn.x),math.floor(spawn.y),math.floor(spawn.z))~=true
        or ctx.world:location_is_protected(spawn) then return end
    local fireball=ctx.world:spawn_entity('FIREBALL',spawn.x,spawn.y,spawn.z)
    if not fireball then return end
    if not ctx.action:own_entity(fireball.uuid) then fireball:remove(); return end
    fireball:set_shooter(ctx.player.uuid)
    fireball:set_direction(math.random()-.5,-1,math.random()-.5)
    if not ctx.action:attribute_projectile(fireball.uuid,function(target,damage)
      if target:can_receive_hostile_effect(ctx.player.uuid)
          and not ctx.world:location_is_protected(target.current_location) then
        ctx.action:damage_target(target.uuid,damage)
      end
    end,true) then fireball:remove(); return end
    ctx.scheduler:run_later(200,function() if fireball.is_valid then fireball:remove() end end)
  end)
end
return {api_version=1,on_right_click=activate,on_shift_right_click=activate}
