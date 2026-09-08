local function benediction(c,s)
 local shape
 return C.channel(c,s,'benediction',46,function(c,s) T.draw(c,shape,T.gold) end,
  function(c,s) for _,v in ipairs(C.living(c,s)) do if T.contains(shape,v.actor:get_location()) then C.heal(c,s,v.id,c.trial.matched_hit) end end end,
  {begin=function(c,s) shape=T.circle(c.trial:position(),5) end,interruptRecovery=60,interruptExposure=1.2,recovery=60})
end
local function silence(c,s)
 local ring
 return {T.wait(30,function(c,s) ring=T.circle(c.trial:position(),3); T.sound(c,'BLOCK_BELL_USE',.8); c.trial:pose('cast') end,
  function(c,s,t) if t%4==0 then T.draw(c,ring,T.gold) end end,
  function(c,s)
   for _,v in ipairs(C.living(c,s)) do if T.distance(c.trial:position(),v.actor:get_location())<=5 then c.trial:cleanse(v.id,false,0) end end
   if T.contains(ring,c.trial.player:get_location()) then T.weak(c,s,20,.15) end
   s.alliesPausedUntil=s.tick+30
  end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,3) end, passive=C.passive, damaged=C.damage,
 choose=function(c,s)
  local weakest=C.weakest(c,s,false)
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local seq=C.flight(c,s,weakest[1] and weakest[1].id); T.append(seq,benediction(c,s)); T.lockout(seq,'benediction',480); T.start(c,s,'flight',seq,M.cooldown)
  elseif s.healBudget>0 and #weakest>0 and T.ready(s,'benediction') then T.start(c,s,'benediction',benediction(c,s),480)
  elseif T.ready(s,'silence') then T.start(c,s,'silence',silence(c,s),400)
  else T.basic(c,s,'melee') end
 end
}
