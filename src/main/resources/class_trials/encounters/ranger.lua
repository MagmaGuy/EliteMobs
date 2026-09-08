local function volley(c,s,angles,group,cap)
  local multiplier=(s.markUntil or 0)>s.tick and 1.1 or 1
  return T.aimShot(c,s,{angles=angles,damage=.45*multiplier,cap=cap or .45*multiplier,group=group,windup=26,lock=12,recovery=30})
end
local function mark(c,s)
 return {T.wait(24,function(c,s) c.trial:pose('cast'); T.sound(c,'ENTITY_PARROT_AMBIENT',1.6) end,
   function(c,s,t) if t%4==0 then T.eye(c,c.trial.player:get_location()) end end,
   function(c,s) s.markUntil=s.tick+120; s.losLost=0; c.trial:say("Hunter's Mark. Leave me an empty lane.") end),T.rest(30)}
end
local function assessedVolley(c,s,angles,group,cap)
 local seq=volley(c,s,angles,group,cap)
 T.append(seq,{T.wait(30,nil,nil,function(c,s)
   if (s.markUntil or 0)>s.tick and c.trial:group_damage(group)==0 then
     s.markUntil=0
     if not s.clearedFeedback then s.clearedFeedback=true; c.trial:say('There. Empty air.') end
     s.nextChoice=math.max(s.nextChoice,s.tick+40)
   end
 end)})
 return seq
end
return T.encounter{
 init=function(c,s) s.markUntil=0; s.losLost=0 end,
 passive=function(c,s)
   if s.markUntil>s.tick then
     if c.trial:line_of_sight() then s.losLost=0 else s.losLost=s.losLost+1 end
     if s.losLost>=20 then s.markUntil=0; s.nextChoice=math.max(s.nextChoice,s.tick+40); if not s.clearedFeedback then s.clearedFeedback=true; c.trial:say('There. Empty air.') end end
     if s.tick%10==0 then T.draw(c,T.circle(c.trial.player:get_location(),.65),T.gold) end
   end
 end,
 choose=function(c,s)
   if s.phasePending and T.ready(s,'windstep') then
     s.phasePending=false; local group='double_fan_'..s.tick; local seq=M.move(c,s)
     T.append(seq,volley(c,s,{-18,-6,10},group,.75)); T.append(seq,assessedVolley(c,s,{-10,6,18},group,.75))
     T.append(seq,{T.wait(1,nil,nil,function(c,s) s.ready.volley=s.tick+200 end)}); T.start(c,s,'windstep',seq,M.cooldown)
   elseif T.distance(c.trial:position(),c.trial.player:get_location())<6 and T.ready(s,'windstep') then T.start(c,s,'windstep',M.move(c,s),M.cooldown)
   elseif T.ready(s,'volley') then T.start(c,s,'volley',assessedVolley(c,s,{-12,0,12},'fan_'..s.tick),200)
   elseif T.ready(s,'mark') then T.start(c,s,'mark',mark(c,s),320)
   else T.basic(c,s,'bow') end
 end
}
