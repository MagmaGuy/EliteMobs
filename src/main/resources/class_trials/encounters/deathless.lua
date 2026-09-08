local function roar(c,s)
 return {T.wait(40,function(c,s) s.inhaling=true; s.pressure=0; c.trial:pose('cast'); T.sound(c,'ENTITY_PLAYER_BREATH',.6) end,
  function(c,s,t) if t%5==0 then T.draw(c,T.circle(c.trial:position(),1.5),B.red) end end,
  function(c,s) s.inhaling=false; s.roars=s.roars+1; s.guardUntil=s.tick+80; B.heal(c,s,'roarHealing',c.boss:get_maximum_health()*.02) end),T.rest(20),T.rest(30)}
end
local function lastBreath(c,s)
 s.inhaling=false; s.guardUntil=0; c.trial:say('One breath left. Watch what I do with it.'); T.sound(c,'ITEM_TOTEM_USE',.6)
 local seq={T.wait(40,function(c,s) c.trial:pose('idle'); c.trial:stop() end,
  function(c,s,t) if t%10==9 then B.heal(c,s,'lastHealing',c.boss:get_maximum_health()*.02); T.draw(c,T.circle(c.trial:position(),1.5),T.gold) end end,
  function(c,s) T.sound(c,'ENTITY_PLAYER_BREATH',1.2) end),T.rest(40)}
 if s.cast then T.interrupt(c,s,seq) else T.start(c,s,'last_breath',seq,0) end
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.roars=0; s.guardUntil=0; s.roarHealing=c.boss:get_maximum_health()*.04; s.lastHealing=c.boss:get_maximum_health()*.08; c.trial:arm_survival('boss','last_breath',0,41) end,
 passive=function(c,s) B.passive(c,s); if c.trial:consume_survival('last_breath') then s.runeSpent=true; lastBreath(c,s) end; if not s.runeSpent and s.tick%8==0 then T.point(c,T.offset(c.trial:position(),0,1.4,-.4),T.gold) end end,
 damaged=function(c,s)
  if s.guardUntil>s.tick then c.event.multiply_damage_amount(.6) end
  if s.inhaling then s.pressure=s.pressure+c.event.damage_amount; if s.pressure>=2*c.trial.matched_hit then s.inhaling=false; T.sound(c,'ENTITY_PLAYER_BREATH',.5); c.trial:say('Breath lost. Opening found.'); T.interrupt(c,s,{T.rest(40)}) end end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; T.start(c,s,'leap',M.move(c,s),M.cooldown)
  elseif s.roars<2 and T.ready(s,'roar') then T.start(c,s,'roar',roar(c,s),480)
  else B.pursue(c,s) end
 end
}
