local function feast(c,s)
 return B.cuts(c,s,{{ticks=32,angle=-25,damage=.55,arc=80},{ticks=24,angle=25,damage=.55,arc=80}},
 {cap=.9,recovery=56,result=function(c,s,hit,i,connected)
  if hit and connected==1 and s.feeds<6 then B.heal(c,s,'healing',c.boss:get_maximum_health()*.02); s.feeds=s.feeds+1 end
 end,finished=function(c,s,connected) if connected==0 then s.markUntil=0; c.trial:say('Empty air. You kept the hunger mine.') end end})
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.feeds=0; s.healing=c.boss:get_maximum_health()*.12 end,
 passive=function(c,s) B.passive(c,s); if s.tick%10==0 then for i=s.feeds+1,6 do T.point(c,T.offset(c.trial:position(),(i-3.5)*.3,2,0),B.red) end end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,B.mark(c,s,26,100)); T.lockout(seq,'scent',360); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'scent') then T.start(c,s,'scent',B.mark(c,s,26,100),360)
  elseif T.ready(s,'feast') then T.start(c,s,'feast',feast(c,s),320)
  else B.pursue(c,s) end
 end
}
