return T.encounter{
 init=function(c,s) P.init(c,s,{{'apprentice','IRON_SWORD'}}) end,
 passive=P.passive, damaged=P.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; T.start(c,s,'steed',P.rescue(c,s,{'apprentice'}),M.cooldown)
  elseif c.trial:actor('apprentice') and T.ready(s,'ring') then T.start(c,s,'ring',P.ring(c,s,4,.4,120),400)
  elseif c.trial:actor('apprentice') and T.ready(s,'intercession') then T.start(c,s,'intercession',P.link(c,s,{'apprentice'},{duration=100,fraction=.6,recovery=40}),320)
  elseif T.distance(c.trial:position(),c.trial.player:get_location())>7 and T.ready(s,'steed') then T.start(c,s,'steed',M.move(c,s),M.cooldown)
  else T.basic(c,s,'melee') end
 end
}
