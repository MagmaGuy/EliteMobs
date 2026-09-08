local function kneel(c,s)
 local p,hit=false
 return {T.wait(32,function(c,s) p=T.copy(c.trial:position()); c.trial:pose('draw'); T.sound(c,'BLOCK_ANVIL_LAND',.5) end,
  function(c,s,t) if t%4==0 then T.draw(c,T.circle(p,5)) end end),
 T.wait(60,nil,function(c,s,t)
  for wave=0,2 do local age=t-wave*12
   if age>=0 and age<36 then local r=age/36*5; local front=T.circle(p,r,math.max(0,r-.6))
    if t%3==0 then T.draw(c,front) end
    if not hit and T.contains(front,c.trial.player:get_location()) then hit=true; c.trial:push(p,.25); T.weak(c,s,40,.15) end
   end
  end
 end),T.rest(40)}
end
local function escape(c,s)
 local p,exit,arcs; local contact=false
 local seq={T.wait(30,function(c,s)
  p=T.copy(c.trial:position()); exit=T.rotate(p,c.trial.player:get_location(),s.phase==2 and 90 or -90,6); s.lastExit=exit
  arcs={}; local facing=T.rotate(p,exit,180,6)
  for _,r in ipairs({3,4,5,6}) do arcs[#arcs+1]=T.arc(p,facing,r,r-.45,360-math.deg(2*math.asin(math.min(1,1.5/r)))) end
  c.trial:pose('cast'); T.sound(c,'ENTITY_RAVAGER_ROAR',.6)
 end,function(c,s,t)
  if t%4==0 then for _,arc in ipairs(arcs) do T.draw(c,arc) end; T.draw(c,T.lane(p,exit,3),T.gold) end
 end),T.wait(36,nil,function(c,s,t)
  for i,arc in ipairs(arcs) do arc.r=2+i-t/36; arc.inner=arc.r-.45; arc.angle=math.pi-math.asin(math.min(1,1.5/arc.r)); local player=c.trial.player:get_location()
   if t%4==0 then T.draw(c,arc) end
   if not contact and T.contains(arc,player) then contact=true; c.trial:effect('player','SLOWNESS',20,0) end
  end
 end),T.rest(20)}
 T.append(seq,T.melee(c,s,{windup=28,radius=3.5,angle=80,damage=.8,recovery=60}))
 return seq
end
return T.encounter{
 init=function(c,s) P.init(c,s) end,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'steed') then s.phasePending=false; local seq=M.move(c,s,s.lastExit); T.append(seq,escape(c,s)); T.lockout(seq,'escape',360); T.start(c,s,'steed',seq,M.cooldown)
  elseif T.ready(s,'kneel') then T.start(c,s,'kneel',kneel(c,s),400)
  elseif T.ready(s,'escape') then T.start(c,s,'escape',escape(c,s),360)
  else T.basic(c,s,'melee') end
 end
}
