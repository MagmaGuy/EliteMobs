return T.encounter{
 init=function(c,s) P.init(c,s); s.weights=0 end,
 damaged=P.collect,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,P.judgment(c,s,1.2)); T.lockout(seq,'judgment',360); T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'judgment') then T.start(c,s,'judgment',P.judgment(c,s,1.2),360)
  elseif T.ready(s,'censure') then T.start(c,s,'censure',P.censure(c,s),320)
  else T.basic(c,s,'melee') end
 end
}
