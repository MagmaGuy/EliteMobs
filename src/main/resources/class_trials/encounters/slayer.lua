local function rhythm(c,s)
 return B.cuts(c,s,{{ticks=28,angle=-25,damage=.4,weapon='IRON_SWORD'},{ticks=30,angle=25,damage=.4,weapon='IRON_AXE'},
  {ticks=40,angle=0,width=1.3,range=4.5,damage=function(c,s) return c.trial.player:get_health()<c.trial.player:get_maximum_health()*.35 and .9 or .65 end,weapon='IRON_SWORD'}},
  {cap=1.2,recovery=60,result=function(c,s,hit,index) if index==3 and not hit then s.markUntil=0; c.trial:stop(); c.trial:say('No finish there. You kept your feet.') end end})
end
return T.encounter{
 init=B.init, passive=B.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,B.mark(c,s,24,100)); T.lockout(seq,'hunt',400); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'hunt') then T.start(c,s,'hunt',B.mark(c,s,24,100),400)
  elseif T.ready(s,'rhythm') then T.start(c,s,'rhythm',rhythm(c,s),340)
  else B.pursue(c,s) end
 end
}
