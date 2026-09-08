local function trail(c,s)
 local shape
 return {T.wait(30,function(c,s) local p=c.trial:position(); shape=T.lane(p,T.rotate(p,c.trial.player:get_location(),0,6),2); c.trial:pose('draw'); T.sound(c,'BLOCK_GRAVEL_BREAK',.7) end,
  function(c,s,t) if t%4==0 then T.draw(c,shape,B.red) end end,
  function(c,s) s.trail=shape; s.trailUntil=s.tick+120; s.nextHeal=s.tick+20 end),T.rest(30)}
end
local function cyclone(c,s)
 local destination,lane; local budget={spent=0,cap=1.1}
 return {T.wait(36,function(c,s) local p=c.trial:position(); destination=T.rotate(p,c.trial.player:get_location(),0,6); lane=T.lane(p,destination,6); c.trial:pose('draw'); T.sound(c,'ENTITY_PLAYER_ATTACK_SWEEP',.6) end,
  function(c,s,t) if t%4==0 then T.draw(c,lane); T.draw(c,T.circle(c.trial:position(),3)) end end),
 T.wait(60,nil,function(c,s,t)
  c.trial:step(destination,.10,true,true)
  local shape=T.circle(c.trial:position(),3)
  if t%4==0 then T.draw(c,shape); c.trial:pose('swing') end
  if t==16 or t==36 or t==56 then if T.budgetHit(c,s,shape,.45,budget) then B.heal(c,s,'spinHealing',c.boss:get_maximum_health()*.01) end end
 end),T.rest(60,1.2)}
end
return T.encounter{
 init=function(c,s) B.init(c,s); s.trailUntil=0; s.trailHealing=c.boss:get_maximum_health()*.10; s.spinHealing=c.boss:get_maximum_health()*.02 end,
 passive=function(c,s)
  B.passive(c,s)
  if s.trailUntil>s.tick then
   if s.tick%5==0 then T.draw(c,s.trail,B.red) end
   if s.tick>=s.nextHeal then s.nextHeal=s.tick+20; if T.contains(s.trail,c.trial:position()) then B.heal(c,s,'trailHealing',c.boss:get_maximum_health()*.01) end end
  end
 end,
 choose=function(c,s)
  if s.phasePending and s.trailUntil<=s.tick and T.ready(s,'leap') then s.phasePending=false; local seq=M.move(c,s); T.append(seq,trail(c,s)); T.lockout(seq,'trail',440); T.start(c,s,'leap',seq,M.cooldown)
  elseif s.trailUntil<=s.tick and T.ready(s,'trail') then T.start(c,s,'trail',trail(c,s),440)
  elseif T.ready(s,'cyclone') then T.start(c,s,'cyclone',cyclone(c,s),360)
  else B.pursue(c,s) end
 end
}
