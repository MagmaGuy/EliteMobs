local function rescue(c,s)
 return {T.wait(36,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',1.4) end,
  function(c,s,t) local ally=c.trial:actor('attendant_1'); if ally and t%4==0 then T.eye(c,ally:get_location()); T.tether(c,c.trial:position(),ally:get_location()) end end,
  function(c,s) C.shield(c,s,'attendant_1',2*c.trial.matched_hit,100,function(c,s,reason)
   if reason=='broken' then C.heal(c,s,'attendant_1',c.trial.matched_hit); c.trial:say('You chose to break it. The opening is yours.') end
   C.recover(c,s,50)
  end) end),T.wait(100),T.rest(50)}
end
local function foresight(c,s)
 return {T.wait(30,function(c,s) c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',1.2) end,
  function(c,s,t) if t%4==0 then T.eye(c,c.trial:position()) end end,
  function(c,s) s.foresightUntil=s.tick+80 end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) C.init(c,s,1); s.foresightUntil=0 end,
 passive=function(c,s) C.passive(c,s); if s.foresightUntil>s.tick and s.tick%8==0 then T.eye(c,c.trial:position()) end end,
 damaged=function(c,s) C.damage(c,s); if c.trial:damaged_actor()=='boss' and s.foresightUntil>s.tick and c.event.get_damage_amount()>0 then c.event.multiply_damage_amount(.5); s.foresightUntil=0 end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'flight') then s.phasePending=false; T.start(c,s,'flight',C.flight(c,s,'attendant_1'),M.cooldown)
  elseif c.trial:actor('attendant_1') and T.ready(s,'rescue') then T.start(c,s,'rescue',rescue(c,s),440)
  elseif T.ready(s,'foresight') then T.start(c,s,'foresight',foresight(c,s),400)
  else T.basic(c,s,'melee') end
 end
}
