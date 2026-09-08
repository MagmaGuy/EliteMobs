local function gather(c,s)
 local seq=C.gather(c,s,32,60,.25,40); local frame=seq[1].frame
 seq[1].frame=function(c,s,t) frame(c,s,t); if t==12 or t==24 then T.sound(c,'BLOCK_BELL_USE',1+t/60) end end
 return seq
end
local function chorus(c,s)
 local function singers(c,s)
  local result={}; for _,v in ipairs(C.living(c,s)) do if T.distance(c.trial:position(),v.actor:get_location())<=5 then result[#result+1]=v end end
  return result
 end
 return C.channel(c,s,'chorus',40,function(c,s,t)
  local living=singers(c,s); T.draw(c,T.circle(c.trial:position(),5),T.gold)
  for _,v in ipairs(living) do T.tether(c,c.trial:position(),v.actor:get_location()) end
  if t%12==0 and t<36 and living[1+t/12] then T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.8+t/40) end
 end,function(c,s)
  local count=math.min(3,#singers(c,s))
  for _,v in ipairs(C.weakest(c,s,true)) do if T.distance(c.trial:position(),v.actor:get_location())<=5 then C.heal(c,s,v.id,.25*count*c.trial.matched_hit) end end
 end,{recovery=60,interruptRecovery=60})
end
return T.encounter{
 init=function(c,s) C.init(c,s,3); s.gatherUntil=0 end, passive=C.passive,
 damaged=function(c,s) C.damage(c,s); if c.trial:damaged_actor()~='boss' and s.gatherUntil>s.tick then c.event.multiply_damage_amount(.75) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then
   s.phasePending=false; local farthest,distance=nil,-1; for _,v in ipairs(C.living(c,s)) do local d=T.distance(c.trial:position(),v.actor:get_location()); if d>distance then farthest=v.id; distance=d end end
   local seq=C.flight(c,s,farthest); T.append(seq,gather(c,s)); T.lockout(seq,'gather',440); T.start(c,s,'flight',seq,M.cooldown)
  elseif #C.living(c,s)>0 and T.ready(s,'gather') then T.start(c,s,'gather',gather(c,s),440)
  elseif s.healBudget>0 and #C.living(c,s)>0 and T.ready(s,'chorus') then T.start(c,s,'chorus',chorus(c,s),480)
  else T.basic(c,s,'melee') end
 end
}
