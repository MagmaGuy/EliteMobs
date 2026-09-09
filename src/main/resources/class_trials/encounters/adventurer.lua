local function strike(c,s)
 return T.melee(c,s,{damage=.30,windup=28,radius=3,angle=65,recovery=42,exposure=1.15})
end
local function secondWind(c,s)
 return {
  T.wait(60,function(c,s)
   s.breathing=true; s.breathInterrupted=false
   c.trial:stop(); c.trial:pose('cast'); c.trial:say('Second Wind. A quick hit can stop mine.')
   T.sound(c,'BLOCK_NOTE_BLOCK_CHIME',.8)
  end,function(c,s,t)
   if not s.breathInterrupted and t%5==0 then T.draw(c,T.circle(c.trial:position(),1),{particle='DUST',red=140,green=210,blue=100}) end
  end,function(c,s)
   if not s.breathInterrupted then
    c.boss:restore_health(c.boss:get_maximum_health()*.08)
    T.sound(c,'ENTITY_PLAYER_LEVELUP',1.6)
   end
   s.breathing=false
  end),
  T.rest(50,1.15)
 }
end
return T.encounter{
 init=function(c,s) s.usedSecondWind=false; s.approachSpeed=.16; s.ready.dodge=160 end,
 choose=function(c,s)
  if not s.usedSecondWind and c.boss:get_health()<c.boss:get_maximum_health()*.65 then
   s.usedSecondWind=true; T.start(c,s,'second_wind',secondWind(c,s),1200)
  elseif s.phase==2 and T.ready(s,'dodge') and T.distance(c.trial:position(),c.trial.player:get_location())<5 then
   local steps=M.move(c,s); T.append(steps,strike(c,s)); T.start(c,s,'dodge',steps,M.cooldown)
  elseif T.approach(c,s,2.5) then return
  elseif T.ready(s,'strike') then T.start(c,s,'strike',strike(c,s),80)
  else s.nextChoice=s.tick+5 end
 end,
 damaged=function(c,s)
  if s.breathing and not s.breathInterrupted and c.trial:damaged_actor()=='boss' then
   s.breathInterrupted=true; s.breathing=false
   c.trial:say('Exactly. Do not give me that opening.'); T.sound(c,'BLOCK_WOODEN_BUTTON_CLICK_OFF',1.3)
   T.interrupt(c,s,{T.rest(50,1.15)})
  end
 end
}
