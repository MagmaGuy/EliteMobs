-- A short, clearly marked lateral dodge. Follow-up attacks keep their full warning.
M.cooldown=240
function M.move(c,s)
 local destination,valid
 return {
  T.wait(24,function(c,s)
   local p=c.trial:position(); local x,z=T.direction(p,c.trial.player:get_location())
   local side=s.dodgeSide or 1; s.dodgeSide=-side
   destination=T.offset(p,-z*2.5*side,0,x*2.5*side)
   valid=c.trial:safe_destination(destination,true,true)~=nil
   if not valid then destination=T.offset(p,z*2.5*side,0,-x*2.5*side); valid=c.trial:safe_destination(destination,true,true)~=nil end
   T.sound(c,'ENTITY_PLAYER_ATTACK_WEAK',1.2)
  end,function(c,s,t) if valid and t%4==0 then T.draw(c,T.lane(c.trial:position(),destination,.4),T.gold) end end),
  T.wait(10,nil,function(c,s)
   if valid and T.distance(c.trial:position(),destination)>.3 then valid=c.trial:step(destination,.4,true,true) else c.trial:stop() end
  end,function(c,s) if not valid then T.fizzle(c) end end),
  T.rest(24)
 }
end
