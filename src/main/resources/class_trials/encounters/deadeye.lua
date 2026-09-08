local function perfect(c,s)
 return R.draw(c,s,{warn=48,lock=20,speed=1.5,damage=1.3,cap=1.3,recovery=70,missExposure=1.2,
  frame=function(c,s,t) if t%4==0 then local p=c.trial:position(); for i=1,3 do if t>=16*(i-1) then T.point(c,T.offset(p,(i-2)*.5,2,0),T.gold) end end end end,
  result=function(c,s,hit) if hit then T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',1.5) else s.markUntil=0; c.trial:say('A perfect line. An empty one.') end end})
end
return T.encounter{
 init=function(c,s) R.init(c,s,false) end, passive=R.passive,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,R.mark(c,s,30,140)); T.lockout(seq,'mark',440); T.start(c,s,'step',seq,M.cooldown)
  elseif T.ready(s,'mark') then T.start(c,s,'mark',R.mark(c,s,30,140),440)
  elseif T.ready(s,'perfect') then T.start(c,s,'perfect',perfect(c,s),360)
  else R.basic(c,s) end
 end
}
