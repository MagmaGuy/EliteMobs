local function breach(c,s)
 return R.draw(c,s,{warn=36,lock=14,speed=1.05,damage=.9,cap=.9,recovery=60,
  result=function(c,s,hit) if hit then s.breachUntil=s.tick+60; T.sound(c,'ITEM_SHIELD_BREAK',1.2) else s.markUntil=0 end end})
end
return T.encounter{
 init=function(c,s) R.init(c,s,true); s.breachUntil=0 end,
 passive=function(c,s) R.passive(c,s); s.outgoing=s.breachUntil>s.tick and 1.15 or 1; if s.breachUntil>s.tick and s.tick%8==0 then T.eye(c,c.trial.player:get_location()) end end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'step') then s.phasePending=false; T.start(c,s,'step',M.move(c,s),M.cooldown)
  elseif T.ready(s,'lock') then T.start(c,s,'lock',R.mark(c,s,30,120),400)
  elseif T.ready(s,'breach') then T.start(c,s,'breach',breach(c,s),300)
  else R.basic(c,s) end
 end
}
