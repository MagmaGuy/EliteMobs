local function removeFire(c,s,broken)
 s.fire=nil; s.brazierUntil=0; c.trial:remove_actor('brazier')
 if broken then T.sound(c,'BLOCK_GLASS_BREAK',.8); c.trial:say('The source, exactly.'); S.recover(c,s,60,1.2) end
end
local function flashover(c,s)
 local location
 return {T.wait(30,function(c,s) location=T.copy(c.trial.player:get_location()); c.trial:pose('cast'); T.sound(c,'ENTITY_BLAZE_AMBIENT',.8) end,
  function(c,s,t) if t%4==0 then for i=1,3 do local a=i*math.pi*2/3; T.point(c,T.offset(location,math.cos(a),.4,math.sin(a)),S.fire) end end end,
  function(c,s) local prop=S.prop(c,'brazier',location,2,'&6Ember Brazier','CAMPFIRE'); if prop then s.brazierUntil=s.tick+140 end end),T.rest(12)}
end
local function inferno(c,s)
 local shape; local steps={T.wait(36,function(c,s) local prop=c.trial:actor('brazier'); if prop then shape=T.circle(prop:get_location(),4) end; c.trial:pose('cast') end,
  function(c,s,t) if shape and t%4==0 then T.draw(c,shape,S.fire) end end,
  function(c,s) if shape and c.trial:actor('brazier') then s.fire={shape=shape,expires=s.tick+80,nextPulse=s.tick,budget={cap=1,spent=0}}; s.brazierUntil=s.tick+80 end end)}
 if s.phase==2 then T.append(steps,M.move(c,s)) end
 steps[#steps+1]=T.untilDone(80,nil,nil,nil,function(c,s) return s.fire==nil end)
 steps[#steps+1]=T.rest(50); return steps
end
return T.encounter{
 init=function(c,s) S.init(c,s,'STAFF'); s.brazierUntil=0 end,
 passive=function(c,s)
  S.passive(c,s)
  if s.brazierUntil>0 then
   if not c.trial:actor('brazier') then removeFire(c,s,true)
   elseif s.tick>=s.brazierUntil then removeFire(c,s,false)
   elseif s.fire then
    if s.tick%4==0 then T.draw(c,s.fire.shape,S.fire) end
    if s.tick>=s.fire.nextPulse then T.budgetHit(c,s,s.fire.shape,.3,s.fire.budget); s.fire.nextPulse=s.tick+20 end
   end
  end
 end,damaged=S.damage,
 choose=function(c,s)
  if s.phasePending then s.phasePending=false end
  if c.trial:actor('brazier') and not s.fire and T.ready(s,'inferno') then T.start(c,s,'inferno',inferno(c,s),480)
  elseif not c.trial:actor('brazier') and T.ready(s,'flashover') then T.start(c,s,'flashover',flashover(c,s),440)
  else S.basic(c,s) end
 end
}
