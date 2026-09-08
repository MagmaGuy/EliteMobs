local function ground(c,s)
 local p,shape
 return {T.wait(32,function(c,s) p=T.offset(c.trial:position(),1,0,0); shape=T.circle(p,4); c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',1.4) end,
  function(c,s,t) if t%4==0 then T.draw(c,shape,T.gold) end end,
  function(c,s) c.trial:remove_actor('candle'); if C.focus(c,'candle',p,2,'&ePrayer Candle','CANDLE') then s.ground=shape; s.groundUntil=s.tick+120; s.nextPulse=s.tick+20; s.sanctuary=p end end),T.rest(40)}
end
local function miracle(c,s)
 s.miracleSpent=true
 return C.channel(c,s,'miracle',50,function(c,s) local novice=c.trial:actor('attendant_1'); if novice then T.tether(c,c.trial:position(),novice:get_location()) end end,
  function(c,s) C.heal(c,s,'attendant_1',3*c.trial.matched_hit) end,{recovery=60,interruptRecovery=60,feedback='The prayer was precious. You found its only opening.'})
end
return T.encounter{
 init=function(c,s) C.init(c,s,1); s.groundUntil=0 end, damaged=C.damage,
 passive=function(c,s)
  C.passive(c,s)
  if s.groundUntil>s.tick and c.trial:actor('candle') then
   if s.tick%5==0 then T.draw(c,s.ground,T.gold) end
   if s.tick>=s.nextPulse then s.nextPulse=s.tick+20; local novice=c.trial:actor('attendant_1'); local id=novice and 'attendant_1' or 'boss'; local actor=novice or c.boss
    if T.contains(s.ground,actor:get_location()) then C.heal(c,s,id,novice and .25*c.trial.matched_hit or .01*c.boss:get_maximum_health()) end
   end
  elseif s.ground then c.trial:remove_actor('candle'); s.ground=nil; s.groundUntil=0 end
 end,
 choose=function(c,s)
  local novice=c.trial:actor('attendant_1')
  if not s.miracleSpent and novice and novice:get_health()<novice:get_maximum_health()*.25 and s.healBudget>0 then T.start(c,s,'miracle',miracle(c,s),0)
  elseif s.phasePending and T.ready(s,'flight') then s.phasePending=false; T.start(c,s,'flight',C.flight(c,s,'attendant_1'),M.cooldown)
  elseif s.groundUntil<=s.tick and s.healBudget>0 and T.ready(s,'ground') then T.start(c,s,'ground',ground(c,s),480)
  else T.basic(c,s,'melee') end
 end
}
