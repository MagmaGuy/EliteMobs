-- Windstep: lateral repositioning; all following shots retain their own draw time.
M.cooldown=280
function M.move(c,s,anchor)
  local target,valid
  return {
    T.wait(16,function(c,s)
      local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location()); local side=s.windSide or 1; s.windSide=-side
      target=anchor or T.offset(p,-z*4*side,0,x*4*side); valid=c.trial:safe_destination(target,true,true)~=nil
      if not valid then target=T.offset(p,z*4*side,0,-x*4*side); valid=c.trial:safe_destination(target,true,true)~=nil end
      T.sound(c,'ENTITY_BREEZE_SLIDE',.9)
    end,function(c,s,t) if t%4==0 then T.draw(c,T.lane(c.trial:position(),target,.5),{particle='DUST',red=100,green=200,blue=100}) end end),
    T.wait(12,nil,function(c,s,t) if valid and T.distance(c.trial:position(),target)>.35 then valid=c.trial:step(target,.65,true,true) else c.trial:stop() end end),
    T.rest(26)
  }
end
