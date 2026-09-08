local function lance(c,s)
 local group='lance_'..s.tick; local damage=s.sightUntil>s.tick and .99 or .9
 local steps={S.bolt(c,s,{warn=36,lock=14,damage=damage,group=group,cap=.99,speed=1.2})}
 return T.append(steps,S.flight(c,s,group,60,function(c,s,hit)
  if hit then T.weak(c,s,40,.15); return 1 else s.sightUntil=0; return 1.2 end
 end))
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); s.sightUntil=0 end,
 passive=function(c,s) S.passive(c,s); if s.sightUntil>s.tick and s.tick%5==0 then T.eye(c,c.trial.player:get_location()); if not s.cast or s.cast.key~='lance' then T.draw(c,T.lane(c.trial:position(),c.trial.player:get_location(),1.5),S.violet) end end end,
 damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif T.ready(s,'sight') then T.start(c,s,'sight',{T.wait(30,function(c,s) c.trial:pose('cast') end,function(c,s,t) if t%3==0 then T.eye(c,c.trial:position()) end end,function(c,s) s.sightUntil=s.tick+120 end),T.rest(12)},440)
  elseif T.ready(s,'lance') then T.start(c,s,'lance',lance(c,s),360)
  else S.basic(c,s) end
 end
}
