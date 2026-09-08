return T.encounter{
 init=B.init, passive=function(c,s) B.passive(c,s); s.turnRate=s.controlUntil>s.tick and .85 or 1 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,B.slam(c,s,{warn=36,radius=3.5,damage=.9,launch=true,recovery=56})); T.lockout(seq,'shatter',360); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'unstoppable') then T.start(c,s,'unstoppable',B.iron(c,s,26),440)
  elseif T.ready(s,'shatter') then T.start(c,s,'shatter',B.slam(c,s,{warn=36,radius=3.5,damage=.9,launch=true,recovery=56}),360)
  else T.basic(c,s,'melee') end
 end
}
