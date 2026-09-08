local function bond(c,s)
 return {T.wait(32,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_AZALEA_LEAVES_PLACE',1.2) end,
  function(c,s,t) local actor=c.trial:actor('attendant_1'); if actor and t%4==0 then T.tether(c,c.trial:position(),actor:get_location(),C.green) end end,
  function(c,s) s.bondUntil=s.tick+140; s.separated=0; s.nextPulse=s.tick+30 end),T.rest(30)}
end
local function seed(c,s)
 return {T.wait(36,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_CHORUS_FLOWER_GROW',1.2) end,
  function(c,s,t) local actor=c.trial:actor('attendant_1'); if actor and t%4==0 then T.draw(c,T.circle(actor:get_location(),.6),C.green) end end,
  function(c,s) s.seedUntil=s.tick+100; s.seedActive=true end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,1); s.bondUntil=0; s.seedUntil=0; s.separated=0 end,
 passive=function(c,s)
  C.passive(c,s); local actor=c.trial:actor('attendant_1')
  if s.seedPending then
   local pending=s.seedPending; s.seedPending=nil
   if actor and actor:get_health()<pending.health then C.shield(c,s,'attendant_1',c.trial.matched_hit,100); s.seedActive=false; C.recover(c,s,50)
   elseif actor then s.seedUntil=pending.expiry; s.seedActive=true
   else s.seedActive=false; C.recover(c,s,50) end
  end
  if s.seedActive then
   if not actor or s.tick>=s.seedUntil then s.seedActive=false; C.recover(c,s,50)
   elseif s.tick%6==0 then T.draw(c,T.circle(actor:get_location(),.6),C.green) end
  end
  if s.bondUntil>s.tick and s.tick%5==0 then
   local linked=actor and T.distance(c.trial:position(),actor:get_location())<=7
   s.separated=linked and 0 or s.separated+5
   if s.separated>=20 then s.bondUntil=0; c.trial:say('The bond has been stretched beyond its reach.'); C.recover(c,s,50)
   elseif linked then T.tether(c,c.trial:position(),actor:get_location(),C.green); if s.tick>=s.nextPulse then C.heal(c,s,'attendant_1',.25*c.trial.matched_hit); s.nextPulse=s.tick+30 end end
  end
 end,
 damaged=function(c,s)
  C.damage(c,s)
  if c.trial:damaged_actor()=='attendant_1' and s.seedActive and not s.seedPending and s.seedUntil>s.tick and c.event.get_damage_amount()>=c.trial.matched_hit then
   local actor=c.trial:actor('attendant_1'); s.seedPending={health=actor:get_health(),expiry=s.seedUntil}; s.seedActive=false
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; local actor=c.trial:actor('attendant_1'); local target=actor and T.rotate(actor:get_location(),c.trial:position(),180,4) or nil; T.start(c,s,'flight',M.move(c,s,target),M.cooldown)
  elseif c.trial:actor('attendant_1') and s.healBudget>0 and T.ready(s,'bond') then T.start(c,s,'bond',bond(c,s),440)
  elseif c.trial:actor('attendant_1') and T.ready(s,'seed') then T.start(c,s,'seed',seed(c,s),480)
  else T.basic(c,s,'melee') end
 end
}
