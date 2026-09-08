local function weave(c,s)
 s.rewriteUsed=true; local chosen=C.weakest(c,s,false)[1]; local id=chosen and chosen.id
 return C.channel(c,s,'rewrite',40,function(c,s) local actor=id and C.actor(c,id); if actor then T.tether(c,c.trial:position(),actor:get_location()); T.draw(c,T.circle(actor:get_location(),.4),T.gold) end end,
  function(c,s) if id and C.actor(c,id) then c.trial:arm_survival(id,'gold_knot',120,1); s.knotId=id; s.knotUntil=s.tick+120 end end,{recovery=50})
end
local function prophecy(c,s)
 local chosen=C.weakest(c,s,false)[1]; local id=chosen and chosen.id or 'boss'
 return C.channel(c,s,'prophecy',32,function(c,s) local actor=C.actor(c,id); if actor then T.eye(c,actor:get_location()); T.tether(c,c.trial:position(),actor:get_location()) end end,
  function(c,s) s.prophecyId=id; s.prophecyUntil=s.tick+100 end,{recovery=50})
end
return T.encounter{
 init=function(c,s) C.init(c,s,2); s.knotUntil=0; s.prophecyUntil=0 end,
 passive=function(c,s)
  C.passive(c,s)
  local rescued=c.trial:consume_survival('gold_knot')
  if rescued then C.heal(c,s,rescued,c.trial.matched_hit); s.knotUntil=0; T.sound(c,'ITEM_TOTEM_USE',1.3); c.trial:say('One thread rewritten. The knot is spent.') end
  if s.knotUntil>s.tick then local actor=C.actor(c,s.knotId); if actor and s.tick%5==0 then T.tether(c,c.trial:position(),actor:get_location()); T.draw(c,T.circle(actor:get_location(),.4),T.gold) end end
  if s.prophecyUntil>s.tick then local actor=C.actor(c,s.prophecyId); if actor and s.tick%8==0 then T.eye(c,actor:get_location()) end end
 end,
 damaged=function(c,s)
  C.damage(c,s)
  if s.prophecyUntil>s.tick and c.trial:damaged_actor()==s.prophecyId and c.event.get_damage_amount()>=c.trial.matched_hit then c.event.multiply_damage_amount(.5); s.prophecyUntil=0; T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.7) end
 end,
 choose=function(c,s)
  if not s.rewriteUsed and #C.living(c,s)>0 then T.start(c,s,'rewrite',weave(c,s),0)
  elseif s.phasePending and T.ready(s,'flight') then s.phasePending=false; local id=s.knotId=='attendant_1' and 'attendant_2' or 'attendant_1'; T.start(c,s,'flight',C.flight(c,s,id),M.cooldown)
  elseif T.ready(s,'prophecy') then T.start(c,s,'prophecy',prophecy(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
