local function current(c,s)
 return {T.wait(30,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_CONDUIT_AMBIENT_SHORT',1.3) end,
  function(c,s,t) local actor=c.trial:actor('attendant_1'); if actor and t%4==0 then T.tether(c,c.trial:position(),actor:get_location(),C.water) end end,
  function(c,s) s.currentUntil=s.tick+120; s.separated=0 end),T.rest(30)}
end
local function totem(c,s)
 local p
 return {T.wait(36,function(c,s) p=T.offset(c.trial:position(),1.5,0,0); c.trial:pose('cast'); T.sound(c,'BLOCK_WOOD_PLACE',1.2) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,4),C.water) end end,
  function(c,s) c.trial:remove_actor('totem'); if C.focus(c,'totem',p,2,'&eSpirit Totem','JUNGLE_LOG') then s.totem=p; s.totemUntil=s.tick+140; s.totemAlive=true; s.sanctuary=p end end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,1); s.currentUntil=0; s.totemUntil=0; s.nextPulse=20; s.separated=0 end, damaged=C.damage,
 passive=function(c,s)
  C.passive(c,s); if s.tick%5~=0 then return end
  local actor=c.trial:actor('attendant_1'); local linked=actor and T.distance(c.trial:position(),actor:get_location())<=6 and c.trial:actor_los('boss','attendant_1')
  if s.currentUntil>s.tick then
   s.separated=linked and 0 or s.separated+5
   if s.separated>=20 then s.currentUntil=0; c.trial:say('The current cannot reach that far.'); C.recover(c,s,50)
   elseif linked then T.tether(c,c.trial:position(),actor:get_location(),C.water) end
  end
  if s.totemAlive and not c.trial:actor('totem') then s.totemAlive=false; s.totemUntil=0; s.nextPulse=math.max(s.nextPulse,s.tick+20); T.sound(c,'BLOCK_WOOD_BREAK',1) end
  if s.totemUntil>s.tick and s.totemAlive then T.draw(c,T.circle(s.totem,4),C.water)
  elseif s.totem then c.trial:remove_actor('totem'); s.totem=nil; s.totemAlive=false end
  if actor and s.tick>=s.nextPulse then
   local fromCurrent=s.currentUntil>s.tick and linked
   local fromTotem=s.totemAlive and s.totemUntil>s.tick and T.distance(s.totem,actor:get_location())<=4
   if fromCurrent or fromTotem then C.heal(c,s,'attendant_1',.25*c.trial.matched_hit); s.nextPulse=s.tick+(fromCurrent and 20 or 40) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local seq=M.move(c,s,s.totem); T.append(seq,current(c,s)); T.lockout(seq,'current',400); T.start(c,s,'flight',seq,M.cooldown)
  elseif s.healBudget>0 and s.totemUntil<=s.tick and T.ready(s,'totem') then T.start(c,s,'totem',totem(c,s),480)
  elseif c.trial:actor('attendant_1') and s.healBudget>0 and T.ready(s,'current') then T.start(c,s,'current',current(c,s),400)
  else T.basic(c,s,'melee') end
 end
}
