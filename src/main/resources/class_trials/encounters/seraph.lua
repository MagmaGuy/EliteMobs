local function chain(c,s)
 local order={}; for i=1,#s.ids do order[i]=s.ids[s.phase==2 and #s.ids-i+1 or i] end
 return C.channel(c,s,'mercy',40,function(c,s)
  local previous=c.trial:position()
  for _,id in ipairs(order) do local actor=C.actor(c,id); if not actor or T.distance(previous,actor:get_location())>5 then break end; T.tether(c,previous,actor:get_location()); previous=actor:get_location() end
 end,function(c,s)
  local previous=c.trial:position()
  for i,id in ipairs(order) do local actor=C.actor(c,id); if not actor or T.distance(previous,actor:get_location())>5 then break end
   C.heal(c,s,id,c.trial.matched_hit); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.7+i*.2); previous=actor:get_location()
  end
 end,{recovery=50})
end
local function ascend(c,s)
 local seq=C.gather(c,s,30,60,0,60); local finish=seq[1].finish
 seq[1].finish=function(c,s) finish(c,s); for _,v in ipairs(C.living(c,s)) do C.shield(c,s,v.id,c.trial.matched_hit,60) end end
 return seq
end
return T.encounter{
 init=function(c,s) C.init(c,s,3) end, passive=C.passive, damaged=C.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local seq=C.flight(c,s,s.ids[#s.ids]); T.append(seq,chain(c,s)); T.lockout(seq,'chain',440); T.start(c,s,'flight',seq,M.cooldown)
  elseif #C.living(c,s)>0 and T.ready(s,'ascend') then T.start(c,s,'ascend',ascend(c,s),480)
  elseif s.healBudget>0 and T.ready(s,'chain') then T.start(c,s,'chain',chain(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
