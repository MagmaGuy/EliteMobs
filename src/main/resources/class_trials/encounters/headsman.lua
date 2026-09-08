local function stroke(c,s)
 local lane
 return {T.wait(40,function(c,s) c.trial:pose('draw'); T.sound(c,'BLOCK_ANVIL_PLACE',.5) end,
  function(c,s,t) if not lane or t<24 then local p=c.trial:position(); local target=T.rotate(p,c.trial.player:get_location(),0,6); lane=T.lane(p,target,2); c.trial:face(target,3) end; if t%4==0 then T.draw(c,lane) end end,
  function(c,s)
   local boosted=s.markUntil>s.tick and c.trial.player:get_health()<c.trial.player:get_maximum_health()*.35
   c.trial:pose('swing'); T.sound(c,'ENTITY_GENERIC_EXPLODE',.5)
   if not T.hit(c,lane,boosted and 1.4 or 1.1) then s.markUntil=0; c.trial:say('Across the blade. Exactly so.') end
  end),T.rest(70,1.25)}
end
return T.encounter{
 init=B.init,
 passive=function(c,s) B.passive(c,s); if s.markUntil>s.tick and s.tick%8==0 then
  local p=T.offset(c.trial.player:get_location(),0,2.4,0); T.tether(c,T.offset(p,-.5,.5,0),p,B.red); T.tether(c,T.offset(p,.5,.5,0),p,B.red)
 end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; T.start(c,s,'leap',M.move(c,s),M.cooldown)
  elseif T.ready(s,'condemn') then T.start(c,s,'condemn',B.mark(c,s,28,120),400)
  elseif T.ready(s,'stroke') then T.start(c,s,'stroke',stroke(c,s),360)
  else B.pursue(c,s) end
 end
}
