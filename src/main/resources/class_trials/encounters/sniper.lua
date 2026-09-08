local function aimed(c,s)
 return R.draw(c,s,{warn=40,lock=14,speed=1.1,damage=function(c,s) return s.markUntil>s.tick and 1.0925 or .95 end,cap=1.1,recovery=56,
  result=function(c,s,hit) if not hit then s.markUntil=0; s.missed=true; c.trial:say('That was the commitment. You moved after it.') end end})
end
return T.encounter{
 init=function(c,s) R.init(c,s,false) end, passive=R.passive,
 choose=function(c,s)
  if s.phase==2 and s.missed and T.ready(s,'step') then s.phasePending=false; s.missed=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif T.ready(s,'expose') then T.start(c,s,'expose',R.mark(c,s,26,120),360)
  elseif T.ready(s,'aimed') then T.start(c,s,'aimed',aimed(c,s),240)
  else R.basic(c,s) end
 end
}
