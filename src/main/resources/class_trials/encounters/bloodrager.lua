local function frenzy(c,s)
 local damage=function(c,s) return s.phase==2 and not s.dampened and .6 or .45 end
 local seq={T.wait(30,function(c,s) s.exposure=1.2; s.fury=3; c.trial:pose('draw'); T.sound(c,'ENTITY_PLAYER_BREATH',.7) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(c.trial:position(),1.2),B.red) end end)}
 T.append(seq,B.cuts(c,s,{{ticks=30,angle=-20,damage=damage},{ticks=30,angle=20,damage=damage},{ticks=30,angle=-20,damage=damage}},
  {cap=1.2,exposure=1.2,recovery=60,result=function(c,s) s.fury=math.max(0,s.fury-1) end,finished=function(c,s) s.dampened=false end}))
 return seq
end
local function roar(c,s)
 return {T.wait(28,function(c,s) s.roaring=true; s.pressure=0; c.trial:pose('cast'); T.sound(c,'ENTITY_RAVAGER_ROAR',.7) end,
  function(c,s,t) if t%4==0 then for i=1,3 do T.point(c,T.offset(c.trial:position(),i-2,2,0),B.red) end end end,
  function(c,s) s.roaring=false; c.trial:cleanse('boss',true,0) end),T.rest(30)}
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.dampened=false; s.fury=0 end,
 passive=function(c,s) B.passive(c,s); if s.tick%5==0 then for i=1,s.fury do T.point(c,T.offset(c.trial:position(),i-2,2,0),B.red) end end end,
 damaged=function(c,s)
  if s.roaring then s.pressure=s.pressure+c.event.damage_amount
   if s.pressure>=2*c.trial.matched_hit then s.roaring=false; s.dampened=true; c.trial:say('Hah—caught my breath before I could spend it.'); T.sound(c,'ENTITY_PLAYER_BREATH',.6); T.interrupt(c,s,{T.rest(40)}) end
  end
 end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,frenzy(c,s)); T.lockout(seq,'frenzy',360); T.start(c,s,'leap',seq,M.cooldown)
  elseif T.ready(s,'roar') then T.start(c,s,'roar',roar(c,s),400)
  elseif T.ready(s,'frenzy') then T.start(c,s,'frenzy',frenzy(c,s),360)
  else B.pursue(c,s) end
 end
}
