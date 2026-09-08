local function repulse(c,s)
  local g
  return {
    T.wait(28,function(c,s) g=T.circle(c.trial:position(),3.5); T.sound(c,'BLOCK_BELL_USE',1.1); c.trial:pose('guard') end,
      function(c,s,t) if t==12 then T.sound(c,'BLOCK_BELL_USE',.8) end; if t%4==0 then T.draw(c,g) end end,
      function(c,s) if T.hit(c,g,.45) then local x,z=T.direction(g.p,c.trial.player:get_location()); c.trial.player:set_velocity_vector({x=x*.3,y=.08,z=z*.3}) end; T.draw(c,g,T.gold) end),
    T.rest(30)
  }
end
local function provoke(c,s)
  local sweep
  return {
    T.wait(26,function(c,s) c.trial:pose('guard'); T.sound(c,'ITEM_SHIELD_BLOCK',.7) end,
      function(c,s,t) if t%4==0 then T.draw(c,T.circle(c.trial:position(),4),T.gold) end end,
      function(c,s) s.guarding=true; s.guardPressure=0; s.guardBroken=false end),
    T.wait(60,nil,function(c,s,t) if s.guarding then c.trial:face(c.trial.player:get_location(),1.5); if t%5==0 then T.draw(c,T.circle(c.trial:position(),4),T.gold) end end end,
      function(c,s) s.guarding=false; sweep=T.cone(c.trial:position(),c.trial.player:get_location(),3.5,70) end),
    T.wait(20,nil,function(c,s,t) if not s.guardBroken and s.guardPressure>=2*c.trial.matched_hit and t%4==0 then T.draw(c,sweep) end end,
      function(c,s) if not s.guardBroken and s.guardPressure>=2*c.trial.matched_hit then c.trial:pose('swing'); T.hit(c,sweep,.65); T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP') end end),
    T.rest(40)
  }
end
return T.encounter{
 init=function(c,s) s.guarding=false; s.guardPressure=0 end,
 choose=function(c,s)
   if s.phasePending and T.ready(s,'steed') then
     s.phasePending=false; local seq=repulse(c,s); T.append(seq,{T.rest(40)}); T.append(seq,M.move(c,s));
     T.append(seq,{T.wait(1,nil,nil,function(c,s) s.ready.repulse=s.tick+300 end)}); T.start(c,s,'steed',seq,M.cooldown)
   elseif T.distance(c.trial:position(),c.trial.player:get_location())>6 and T.ready(s,'steed') then T.start(c,s,'steed',M.move(c,s),M.cooldown)
   elseif T.ready(s,'provoke') then T.start(c,s,'provoke',provoke(c,s),240)
   elseif T.ready(s,'repulse') then T.start(c,s,'repulse',repulse(c,s),300)
   else T.basic(c,s,'melee') end
 end,
 damaged=function(c,s)
   if c.trial:damaged_actor()~='boss' or not s.guarding then return end
   local p=c.trial:position(); local a=math.rad(p.yaw or 0); local x,z=T.direction(p,c.trial.player:get_location())
   if x*(-math.sin(a))+z*math.cos(a)>.0 then s.guardPressure=s.guardPressure+c.event.damage_amount; c.event.multiply_damage_amount(.4); T.sound(c,'ITEM_SHIELD_BLOCK',.8)
   else s.guarding=false; s.guardBroken=true; c.trial:pose('idle'); c.trial:say('There. A shield has a back.') end
 end
}
