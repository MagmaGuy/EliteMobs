local colors={fire=S.fire,frost=S.ice,lightning=S.violet}
local function runes(c,s)
 local p=c.trial:position()
 for i,element in ipairs(s.order) do
  local q=T.offset(p,(i-2)*1.5,1.2,0)
  if element=='fire' then T.draw(c,T.circle(q,.3),colors[element])
  elseif element=='frost' then T.draw(c,T.lane(T.offset(q,-.4,0,0),T.offset(q,.4,0,0),.15),colors[element])
  else for j=0,4 do T.point(c,T.offset(q,j%2*.3,j*.18,0),colors[element]) end end
 end
end
local function convergence(c,s)
 local steps={}; local budget={cap=1.1,spent=0}
 for _,element in ipairs(s.order) do local shape,origin,direction,hit
  steps[#steps+1]=T.wait(28,function(c,s)
   origin=c.trial:position(); local target=c.trial.player:get_location(); local x,z=T.direction(origin,target); direction={x=x,z=z}
   if element=='fire' then shape=T.circle(target,3)
   elseif element=='lightning' then shape=T.lane(origin,T.offset(origin,x*12,0,z*12),2)
   else shape=T.lane(T.offset(origin,z*3,0,-x*3),T.offset(origin,-z*3,0,x*3),1.5) end
   c.trial:pose('cast'); T.sound(c,'BLOCK_AMETHYST_BLOCK_CHIME',element=='fire' and .7 or element=='frost' and 1 or 1.5)
  end,function(c,s,t) if t%3==0 then T.draw(c,shape,colors[element]); runes(c,s) end end,
  function(c,s)
   if element~='frost' then local boosted=s.exposureReady and 1.1 or 1; if T.budgetHit(c,s,shape,.45*boosted,budget) then s.exposureReady=false end end
  end)
  if element=='frost' then steps[#steps+1]=T.wait(16,nil,function(c,s,t)
   local p=T.offset(origin,direction.x*t*.5,0,direction.z*t*.5)
   shape=T.lane(T.offset(p,direction.z*3,0,-direction.x*3),T.offset(p,-direction.z*3,0,direction.x*3),1.5)
   if t%2==0 then T.draw(c,shape,S.ice) end
   if not hit and T.budgetHit(c,s,shape,.45*(s.exposureReady and 1.1 or 1),budget) then hit=true; s.exposureReady=false; c.trial:slow_player(.15,10) end
  end) end
  steps[#steps+1]=T.rest(16)
 end
 steps[#steps+1]=T.rest(70); return steps
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); s.order={'fire','frost','lightning'}; s.exposureReady=false end,
 passive=S.passive,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending and T.ready(s,'blink') then s.phasePending=false; s.order={'frost','lightning','fire'}; s.ready.exposure=s.tick; T.start(c,s,'blink',M.move(c,s),M.cooldown)
  elseif T.ready(s,'exposure') then T.start(c,s,'exposure',{T.wait(32,function(c,s) c.trial:pose('cast') end,function(c,s,t) if t%3==0 then runes(c,s) end end,function(c,s) s.exposureReady=true end),T.rest(12)},480)
  elseif T.ready(s,'convergence') then T.start(c,s,'convergence',convergence(c,s),520)
  else S.basic(c,s) end
 end
}
