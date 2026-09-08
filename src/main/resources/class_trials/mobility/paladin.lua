-- Divine Steed: one reusable mounted charge with a committed lane and dismount recovery.
M.cooldown=400
function M.move(c,s,anchor)
  local target,lane,valid,hit
  return {
    T.wait(20,function(c,s) valid=c.trial:mount(); T.sound(c,'ENTITY_HORSE_AMBIENT',.85) end),
    T.wait(30,function(c,s) target=nil; hit=false end,function(c,s,t)
      local origin=c.trial:position()
      if not target or t<18 then local x,z=T.direction(origin,anchor or c.trial.player:get_location()); local distance=anchor and math.min(8,math.max(0,T.distance(origin,anchor)-1.5)) or 8; target=T.offset(origin,x*distance,0,z*distance); c.trial:face(target,4) end
      lane=T.lane(origin,target,2.8); if t%4==0 then T.draw(c,lane) end
    end,function(c,s) valid=valid and c.trial:safe_destination(target,true,false)~=nil end),
    T.wait(20,nil,function(c,s,t)
      if not valid then return end
      if T.distance(c.trial:position(),target)>.5 then valid=c.trial:step(target,.65,true,false) else c.trial:stop() end
      if not hit and T.contains(lane,c.trial.player:get_location()) and T.distance(c.trial:position(),c.trial.player:get_location())<1.4 then hit=c.trial:damage(.75) end
    end,function(c,s) c.trial:stop(); c.trial:dismount(); s.exposure=hit and 1 or 1.2 end),
    T.wait(50,function(c,s) c.trial:pose('idle') end,nil,function(c,s) s.exposure=1 end)
  }
end
