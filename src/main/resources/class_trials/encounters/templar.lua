local function judgment(c,s)
 return P.judgment(c,s,.9,function(c,s) local actor=c.trial:actor('novice'); if actor and s.heals<3 then actor:restore_health(math.min(c.trial.matched_hit,actor:get_maximum_health()-actor:get_health())); s.heals=s.heals+1; T.tether(c,c.trial:position(),actor:get_location()) end end)
end
local function bell(c,s)
 return {T.wait(36,function(c,s) s.channel=true; s.pressure=0; c.trial:pose('cast') end,
  function(c,s,t) if t%12==0 then T.sound(c,'BLOCK_BELL_USE',.8+t/60) end; if t%4==0 then T.draw(c,T.circle(c.trial:position(),5),T.gold) end end,
  function(c,s) s.channel=false; c.trial:cleanse('boss',false,80); local actor=c.trial:actor('novice'); if actor and T.distance(c.trial:position(),actor:get_location())<=5 then c.trial:cleanse('novice',false,80) end end),T.rest(40)}
end
return T.encounter{
 init=function(c,s) P.init(c,s,{{'novice','IRON_SWORD'}}); s.heals=0 end,
 passive=P.passive,
 damaged=function(c,s)
  P.collect(c,s)
  if c.trial:damaged_actor()=='boss' and s.channel then s.pressure=s.pressure+c.event.damage_amount
   if s.pressure>=2*c.trial.matched_hit then s.channel=false; T.sound(c,'BLOCK_GLASS_BREAK',1.5); c.trial:say('You silenced the bell. Use the moment.'); T.interrupt(c,s,{T.rest(40)}) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then
   s.phasePending=false; local seq=P.rescue(c,s,{'novice'}); local actor=c.trial:actor('novice')
   if actor and actor:get_health()<actor:get_maximum_health() then T.append(seq,bell(c,s)); T.lockout(seq,'bell',440) else T.append(seq,judgment(c,s)); T.lockout(seq,'judgment',400) end
   T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'judgment') then T.start(c,s,'judgment',judgment(c,s),400)
  elseif T.ready(s,'bell') then T.start(c,s,'bell',bell(c,s),440)
  else T.basic(c,s,'melee') end
 end
}
